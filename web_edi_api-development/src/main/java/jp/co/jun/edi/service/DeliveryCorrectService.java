package jp.co.jun.edi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.deliveryupsert.DeliveryDetailCorrectComponent;
import jp.co.jun.edi.component.mail.StackTDeliveryOfficialSendMailComponent;
import jp.co.jun.edi.component.mail.StackTDeliveryendMailComponent;
import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.DeliverySkuModel;
import jp.co.jun.edi.model.DeliveryStoreSkuModel;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.CorrectServiceParameter;
import jp.co.jun.edi.service.response.CorrectServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 納品依頼関連訂正処理.
 */
@Service
public class DeliveryCorrectService
extends GenericUpdateService<CorrectServiceParameter<DeliveryModel>, CorrectServiceResponse<DeliveryModel>> {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private StackTDeliveryOfficialSendMailComponent stackTDeliveryOfficialSendMailComponent;

    @Autowired
    private StackTDeliveryendMailComponent stackTDeliveryendMailComponent;

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private TOrderRepository orderRepository;

    /**
     * 納品情報の訂正.
     */
    @Autowired
    private DeliveryDetailCorrectComponent deliveryDetailCorrect;

    @Override
    protected CorrectServiceResponse<DeliveryModel> execute(final CorrectServiceParameter<DeliveryModel> serviceParameter) {
        final DeliveryModel requestDeliveryModel = serviceParameter.getItem();
        final CustomLoginUser loginUser = serviceParameter.getLoginUser();
        final BigInteger reqDeliveryId = requestDeliveryModel.getId();

        // 品番情報取得。存在しない場合はエラー
        final ExtendedTItemEntity registeredDbTItemEntity =  itemComponent.getExtendedTItem(requestDeliveryModel.getPartNoId());

        // 発注情報取得。存在しない場合はエラー
        final ExtendedTOrderEntity registeredDbTOrderEntity = orderComponent.getExtendedTOrder(requestDeliveryModel.getOrderId());

        // リクエストパラメータの納品IDに紐づく納品依頼情報テーブルのレコードを取得。存在しない場合はエラー
        final TDeliveryEntity registeredDeliveryEntity = getRegisteredDeliveryEntity(reqDeliveryId);

        // リクエストパラメータの納品IDに紐づく納品明細情報テーブルのレコードリストを取得。存在しない場合はエラー
        final List<TDeliveryDetailEntity> registeredDeliveryDetails = deliveryComponent.getTDeliveryDetailList(reqDeliveryId);

        // 訂正可否チェック
        checkCorrectable(registeredDeliveryEntity, registeredDeliveryDetails,
                requestDeliveryModel, loginUser, registeredDbTItemEntity, registeredDbTOrderEntity);

        // 課別登録の場合は発注SKUの納品依頼数量更新
        if (BooleanType.TRUE != requestDeliveryModel.getFromStoreScreenFlg()) {
            updateOrderSku(requestDeliveryModel, loginUser.getUserId());
        }

        // INSERTする納品明細の 納品依頼No を設定するために、採番した納品依頼Noをリクエストパラメータの納品依頼明細リストにセット
        generateDeliveryNumberForRequestDeliveryDetails(requestDeliveryModel);

        // 更新用レコードを用意して納品依頼情報の更新 ※INSERTになるケースありえない
        generateDeliveryForUpdate(requestDeliveryModel, registeredDeliveryEntity);
        deliveryRepository.save(registeredDeliveryEntity);

        // 納品明細とその子孫テーブルの登録・更新 ※INSERTになるケースあり
        // ※registeredDeliveryDetails.get(0)・・・INSERTする納品明細の 納品No、納品依頼日、納品依頼回数、納期 を設定するために、納品依頼明細の先頭値のみ渡す
        // (課別の訂正画面では製品で配分先が同一の新規の課を追加登録できる)
        upsertDeliveryDetailAndChildRelationship(requestDeliveryModel, loginUser, registeredDeliveryEntity,
                registeredDeliveryDetails.get(0), registeredDbTItemEntity);

        // B級品単価に入力がある場合は発注情報更新
        final BigDecimal nonConformingProductUnitPrice = requestDeliveryModel.getNonConformingProductUnitPrice();
        if (nonConformingProductUnitPrice != null
                && registeredDbTOrderEntity.getNonConformingProductUnitPrice() == null) {
            orderRepository.updateNonConformingProductUnitPrice(registeredDbTOrderEntity.getId(), nonConformingProductUnitPrice, loginUser.getUserId());
        }

        // 納品依頼メール送信管理と納品依頼正式メール送信管理にメール送信情報を登録する.
        // PRD_0044 mod SIT start
        //saveDeliveryMail(requestDeliveryModel, loginUser);
        if (BooleanType.TRUE != requestDeliveryModel.getStoreScreenSaveCorrectFlg()) {
            saveDeliveryMail(requestDeliveryModel, loginUser);
        }
        // PRD_0044 mod SIT end

        // レスポンス用のModelを作成
        final DeliveryModel responseModel = new DeliveryModel();
        responseModel.setId(reqDeliveryId);

        return CorrectServiceResponse.<DeliveryModel>builder().item(responseModel).build();
    }

    /**
     * 発注SKUを更新する.
     * @param requestDeliveryModel リクエストの納品情報
     * @param userId ユーザーID
     */
    private void updateOrderSku(
            final DeliveryModel requestDeliveryModel,
            final BigInteger userId) {

        final BigInteger orderId = requestDeliveryModel.getOrderId();

        final Page<TOrderSkuEntity> orderSkuPage = orderSkuRepository.findByOrderId(orderId, PageRequest.of(0, Integer.MAX_VALUE));
        if (!orderSkuPage.hasContent()) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }

        final List<TDeliverySkuEntity> groupedDbDeliverySkus = deliverySkuRepository.sumDeliveryLotGroupBySku(requestDeliveryModel.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final Map<String, Integer> reqDeliveryLotMap = sumRequestDeliveryLotGroupBySku(requestDeliveryModel.getDeliveryDetails());

        orderSkuPage.getContent().forEach(os -> updateOrderSkuDeliveryLot(os, groupedDbDeliverySkus, reqDeliveryLotMap, userId));
    }

    /**
     * @param os TOrderSkuEntity
     * @param groupedDbDeliverySkus DB登録済のSKUごとの納品数量(または入荷数量)
     * @param reqDeliveryLotMap リクエストのSKUごとの納品数量
     * @param userId ユーザーID
     */
    private void updateOrderSkuDeliveryLot(
            final TOrderSkuEntity os,
            final List<TDeliverySkuEntity> groupedDbDeliverySkus,
            final Map<String, Integer> reqDeliveryLotMap,
            final BigInteger userId) {
        final String colorCode = os.getColorCode();
        final String size = os.getSize();
        final String key = deliveryComponent.generateSkuKey(colorCode, size);
        final Integer dbLot = findDeliveryLotBySku(groupedDbDeliverySkus, colorCode, size);
        final Integer reqLot = reqDeliveryLotMap.get(key);

        if (Objects.equals(reqLot, dbLot)) {
            return;
        }

        final int deliveryLot = NumberUtils.defaultInt(os.getDeliveryLot()) - NumberUtils.defaultInt(dbLot) + NumberUtils.defaultInt(reqLot);
        orderSkuRepository.updateDeliveryLot(deliveryLot, os.getId(), userId);
    }

    /**
     * @param groupedDbDeliverySkus DBに登録されている数量集計済の納品SKUリスト
     * @param colorCode カラーコード
     * @param size サイズ
     * @return 引数で指定したSKUの納品依頼数
     */
    private Integer findDeliveryLotBySku(
            final List<TDeliverySkuEntity> groupedDbDeliverySkus,
            final String colorCode,
            final String size) {
        return groupedDbDeliverySkus.stream()
                .filter(ds -> ds.getColorCode().equals(colorCode) && ds.getSize().equals(size))
                .map(ds -> ds.getDeliveryLot()).findFirst()
                .orElse(null);
    }

    /**
     * @param deliveryDetails リクエストの納品情報
     * @return SKUごとの納品数量合計
     */
    private Map<String, Integer> sumRequestDeliveryLotGroupBySku(final List<DeliveryDetailModel> deliveryDetails) {
        return deliveryDetails.stream()
                .flatMap(dd -> dd.getDeliverySkus().stream())
                .collect(Collectors.groupingBy(ds -> deliveryComponent.generateSkuKey(ds.getColorCode(), ds.getSize()),
                        Collectors.summingInt(DeliverySkuModel::getDeliveryLot)));
    }

    /**
     * 納品明細とその子孫テーブルの登録・更新を行う.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param loginUser ログインユーザ情報
     * @param registeredDeliveryEntity 現時点でDBに登録されている納品依頼
     * @param registeredFirstDeliveryDetailEntity 現時点でDBに登録されている納品依頼明細リストの先頭値
     * @param registeredDbTItemEntity 品番情報
     */
    private void upsertDeliveryDetailAndChildRelationship(final DeliveryModel requestDeliveryModel, final CustomLoginUser loginUser,
            final TDeliveryEntity registeredDeliveryEntity, final TDeliveryDetailEntity registeredFirstDeliveryDetailEntity,
            final ExtendedTItemEntity registeredDbTItemEntity) {
        // 課別画面からの訂正の場合、得意先・得意先SKUを削除して再設定する
        deliveryComponent.prepareUpdateFromDivisionScreen(requestDeliveryModel, loginUser.getUserId(), registeredDbTItemEntity);

        // 納品明細とその子孫テーブルの訂正
        final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> model = new DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity>();
        model.setModelForUpdateList(requestDeliveryModel.getDeliveryDetails());
        model.setLoginUser(loginUser);
        model.setParentEntity(registeredDeliveryEntity);
        model.setRegisteredFirstDeliveryDetailEntity(registeredFirstDeliveryDetailEntity);
        model.setFromStoreScreen(BooleanType.TRUE.equals(requestDeliveryModel.getFromStoreScreenFlg()));
        // PRD_0044 add SIT start
        model.setStoreScreenSaveCorrect(BooleanType.TRUE.equals(requestDeliveryModel.getStoreScreenSaveCorrectFlg()));
        // PRD_0044 add SIT end
        deliveryDetailCorrect.upsert(model);
    }

    /**
     * 更新用の納品依頼情報に項目を設定.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param registeredDeliveryEntity 現時点でDBに登録されている納品依頼情報
     */
    private void generateDeliveryForUpdate(final DeliveryModel requestDeliveryModel, final TDeliveryEntity registeredDeliveryEntity) {
        /** 画面側で設定される項目 */
        // 配分率区分
        registeredDeliveryEntity.setDistributionRatioType(requestDeliveryModel.getDistributionRatioType());
        // メモ
        registeredDeliveryEntity.setMemo(requestDeliveryModel.getMemo());
        // 納期変更理由ID
        registeredDeliveryEntity.setDeliveryDateChangeReasonId(requestDeliveryModel.getDeliveryDateChangeReasonId());
        // 納期品行理由詳細
        registeredDeliveryEntity.setDeliveryDateChangeReasonDetail(requestDeliveryModel.getDeliveryDateChangeReasonDetail());
        // B級品区分
        registeredDeliveryEntity.setNonConformingProductType(requestDeliveryModel.isNonConformingProductType());
        // B級品単価
        registeredDeliveryEntity.setNonConformingProductUnitPrice(requestDeliveryModel.getNonConformingProductUnitPrice());
        // 最終納品ステータス
        registeredDeliveryEntity.setLastDeliveryStatus(requestDeliveryModel.getLastDeliveryStatus());

        /** API側で設定される項目 */
        // SQロックフラグ
        registeredDeliveryEntity.setSqLockFlg(BooleanType.FALSE);
        // SQロックユーザーID
        registeredDeliveryEntity.setSqLockUserId(null);
    }

    /**
     * 訂正時のバリデーションチェック.
     *
     * @param registeredDeliveryEntity 現時点でDBに登録されている納品依頼情報
     * @param registeredDeliveryDetails 登録されている納品依頼明細
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param loginUser ログインユーザー
     * @param registerdDbTItemEntity リクエストパラメータの納品IDに紐づく品番情報
     * @param registerdDbTOrderEntity リクエストパラメータの納品IDに紐づく発注情報
     */
    private void checkCorrectable(
            final TDeliveryEntity registeredDeliveryEntity,
            final List<TDeliveryDetailEntity> registeredDeliveryDetails,
            final DeliveryModel requestDeliveryModel,
            final CustomLoginUser loginUser,
            final ExtendedTItemEntity registerdDbTItemEntity,
            final ExtendedTOrderEntity registerdDbTOrderEntity) {
        // 承認済チェック。未承認だったら業務エラー
        if (!deliveryComponent.isDeliveryApproved(registeredDeliveryEntity.getDeliveryApproveStatus())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_016));
        }

        // ログイン権限チェック。メーカー権限の場合は業務エラー
        if (!loginUser.isAffiliation()) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_017));
        }

        //PRD_0123 #7054 del JFE start
        // 追加不可チェック。※訂正時の納品依頼の追加は不可
        //checkDeliveryInsert(requestDeliveryModel, registeredDeliveryDetails.get(0));
        //PRD_0123 #7054 del JFE end
        final ResultMessages rsltMsg = deliveryComponent.checkCommonValidate(requestDeliveryModel, registerdDbTItemEntity, registerdDbTOrderEntity);
        final Page<TPurchaseEntity> purcahsePage = purchaseRepository.findByDeliveryId(requestDeliveryModel.getId(), PageRequest.of(0, Integer.MAX_VALUE));

        if (BooleanType.TRUE == requestDeliveryModel.getFromStoreScreenFlg()) {
            // 店舗配分訂正時
            validateAtFromStore(requestDeliveryModel, registeredDeliveryDetails, purcahsePage, rsltMsg);
        } else {
            // 課別訂正時
            validateAtFromDivision(requestDeliveryModel, registeredDeliveryDetails, purcahsePage, rsltMsg);
        }

        // 直送状態変更不可チェック。※仕入情報が既に登録されている場合は直送状態の変更不可
        if (registeredDeliveryDetails.get(0).getCarryType() != requestDeliveryModel.getDeliveryDetails().get(0).getCarryType()) {
            if (purcahsePage.hasContent()) {
                rsltMsg.add(MessageCodeType.CODE_D_030);
            }
        }

        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
    }

    /**
     * 店舗配分訂正時のバリデーションチェック.
     * @param requestDeliveryModel リクエストの納品情報
     * @param registeredDeliveryDetails DB登録済の納品明細リスト
     * @param purcahsePage 仕入情報
     * @param rsltMsg エラーメッセージ
     */
    private void validateAtFromStore(
            final DeliveryModel requestDeliveryModel,
            final List<TDeliveryDetailEntity> registeredDeliveryDetails,
            final Page<TPurchaseEntity> purcahsePage,
            final ResultMessages rsltMsg) {
        // 配分出荷指示済チェック
        deliveryComponent.checkShippingInstructed(registeredDeliveryDetails, rsltMsg);

        if (registeredDeliveryDetails.stream().noneMatch(dd -> BooleanType.TRUE == dd.getArrivalFlg())) {
            // 未入荷の場合、課別との数量不一致時エラー
            deliveryComponent.checkNotMatchToDivisionLot(requestDeliveryModel, rsltMsg);
            return;
        }

        // 入荷済の場合、ゼロ確時エラー
        if (deliveryComponent.isZeroFix(requestDeliveryModel.getId())) {
            rsltMsg.add(MessageCodeType.CODE_D_029);
            return;
        }

        // 入荷済の場合、仕入確定数超過時エラー
        if (purcahsePage.hasContent()) {
            checkOverPurchaseAtFromStore(requestDeliveryModel, purcahsePage.getContent(), rsltMsg);
        }
    }

    /**
     * 店舗配分訂正時の仕入確定数超過チェック.
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param purcahses 仕入リスト
     * @param rsltMsg ResultMessages
     */
    private void checkOverPurchaseAtFromStore(
            final DeliveryModel requestDeliveryModel,
            final List<TPurchaseEntity> purcahses,
            final ResultMessages rsltMsg) {

        requestDeliveryModel.getDeliveryDetails()
        .forEach(dd -> {
            final String divisionCode = dd.getDivisionCode();
            dd.getDeliveryStores().forEach(store -> {
                store.getDeliveryStoreSkus().forEach(sku -> {
                    final Optional<TPurchaseEntity> opt = extractPurchase(purcahses, divisionCode, sku);
                    if (isOverFixArrivalCount(opt, sku)) {
                        rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_025,
                                getMessage("code.400_D_25", divisionCode, sku.getColorCode(), sku.getSize())));
                    }
                });
            });
        });
    }

    /**
     * 課別訂正時のバリデーションチェック.
     * @param requestDeliveryModel リクエストの納品情報
     * @param registeredDeliveryDetails DB登録済の納品明細リスト
     * @param purcahsePage 仕入情報
     * @param rsltMsg エラーのメッセージ
     */
    private void validateAtFromDivision(
            final DeliveryModel requestDeliveryModel,
            final List<TDeliveryDetailEntity> registeredDeliveryDetails,
            final Page<TPurchaseEntity> purcahsePage,
            final ResultMessages rsltMsg) {
        // 仕入済チェック
        deliveryComponent.checkArrived(registeredDeliveryDetails, purcahsePage.getContent(), rsltMsg);

        // 仕入数超過チェック
        if (purcahsePage.hasContent()) {
            checkOverPurchaseAtFromDivision(requestDeliveryModel, purcahsePage.getContent(), rsltMsg);
        }
    }

    /**
     * 課別訂正時の仕入数超過チェック.
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param purcahses 仕入リスト
     * @param rsltMsg ResultMessages
     */
    private void checkOverPurchaseAtFromDivision(
            final DeliveryModel requestDeliveryModel,
            final List<TPurchaseEntity> purcahses,
            final ResultMessages rsltMsg) {

        requestDeliveryModel.getDeliveryDetails()
        .forEach(dd -> dd.getDeliverySkus()
                .forEach(sku -> {
                    final String divisionCode = dd.getDivisionCode();
                    final Optional<TPurchaseEntity> opt = extractPurchase(purcahses, divisionCode, sku);
                    if (isOverArrivalCount(opt, sku)) {
                        rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_D_025,
                                getMessage("code.400_D_25", divisionCode, sku.getColorCode(), sku.getSize())));
                    }
                }));
    }

    /**
     * @param purcahses 仕入情報リスト
     * @param divisionCode 課コード
     * @param sku 納品SKU
     * @return 仕入情報
     */
    private Optional<TPurchaseEntity> extractPurchase(
            final List<TPurchaseEntity> purcahses,
            final String divisionCode,
            final DeliverySkuModel sku) {
        return purcahses.stream()
                .filter(p -> p.getDivisionCode().equals(divisionCode)
                        && p.getColorCode().equals(sku.getColorCode())
                        && p.getSize().equals(sku.getSize()))
                .findFirst();
    }

    /**
     * @param purcahses 仕入情報リスト
     * @param divisionCode 課コード
     * @param sku 納品得意先SKU
     * @return 仕入情報
     */
    private Optional<TPurchaseEntity> extractPurchase(
            final List<TPurchaseEntity> purcahses,
            final String divisionCode,
            final DeliveryStoreSkuModel sku) {
        return purcahses.stream()
                .filter(p -> p.getDivisionCode().equals(divisionCode)
                        && p.getColorCode().equals(sku.getColorCode())
                        && p.getSize().equals(sku.getSize()))
                .findFirst();
    }
    /**
     * @param opt 仕入情報
     * @param sku 納品SKU
     * @return true:仕入数を超過
     */
    private boolean isOverArrivalCount(final Optional<TPurchaseEntity> opt, final DeliverySkuModel sku) {
        return opt.isPresent()
                && sku.getDeliveryLot() > opt.get().getArrivalCount();
    }

    /**
     * @param opt 仕入情報
     * @param sku 納品得意先SKU
     * @return true:仕入確定数を超過
     */
    private boolean isOverFixArrivalCount(final Optional<TPurchaseEntity> opt, final DeliveryStoreSkuModel sku) {
        return opt.isPresent()
                && sku.getDeliveryLot() > opt.get().getFixArrivalCount();
    }

    /**
     * @param code コード
     * @param args 引数
     * @return メッセージ
     */
    private String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.JAPANESE);
    }

    //PRD_0123 #7054 del JFE start
//    /**
//     * 納品依頼情報追加チェック.
//     *
//     * 更新時に納品依頼の追加は不可。
//     * 場所コードが登録されているものと異なる場合は納品依頼追加となるので、エラーにする
//     *
//     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
//     * @param registeredTopDeliveryDetail 登録されている納品依頼明細リストの先頭値
//     */
//    private void checkDeliveryInsert(final DeliveryModel requestDeliveryModel, final TDeliveryDetailEntity registeredTopDeliveryDetail) {
//        // 登録されている場所コードを取得する(※場所コード=物流コードの先頭1ケタ)
//        final String registeredAllocationCode = registeredTopDeliveryDetail.getLogisticsCode().substring(0, 1);
//
//        // 入力された納品依頼明細リストの物流コードが全てcurrentAllocationCodeと同じかチェックする
//        // 1つでも異なるものがある場合は納品依頼追加不可エラーを返す
//        if (requestDeliveryModel.getDeliveryDetails().stream()
//                .anyMatch(deliveryDetailModel -> !Objects.equals(deliveryDetailModel.getAllocationCode(), registeredAllocationCode))) {
//            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_012));
//        }
//    }
    //PRD_0123 #7054 del JFE end
    /**
     * 採番した納品依頼Noをリクエストパラメータの納品依頼明細リストにセット.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     */
    private void generateDeliveryNumberForRequestDeliveryDetails(final DeliveryModel requestDeliveryModel) {
        // 入力された納品依頼明細からIDのないものを抽出
        final List<DeliveryDetailModel> deliveryDetalsIdNotExist = requestDeliveryModel.getDeliveryDetails().stream()
                .filter(deliveryDetail -> Objects.isNull(deliveryDetail.getId()))
                .collect(Collectors.toList());

        // 納品依頼Noを採番し、納品依頼明細Modelにセット
        deliveryDetalsIdNotExist.stream().forEach(deliveryDetailModel -> {
            // 納品依頼Noの採番
            final String deliveryRequestNumber = String.format("%06d", deliveryComponent.numberingDeliveryRequestNumber());
            deliveryDetailModel.setDeliveryRequestNumber(deliveryRequestNumber);
        });
    }

    /**
     * 現時点でDBに登録されている納品依頼情報を取得する.
     * 取得できない場合はエラー
     *
     * @param deliveryId 納品依頼ID
     * @return 現時点でDBに登録されている納品依頼情報
     */
    public TDeliveryEntity getRegisteredDeliveryEntity(final BigInteger deliveryId) {
        return deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_015)));
    }

    /**
     * 納品依頼メール送信管理と納品依頼正式メール送信管理にメール送信情報を登録する.
     * @param deliveryModel 納品情報
     * @param loginUser ユーザ情報
     */
    private void saveDeliveryMail(final DeliveryModel deliveryModel, final CustomLoginUser loginUser) {

        // 品番情報取得。存在しない場合はエラー
        final ExtendedTItemEntity extendedTItem = itemComponent.getExtendedTItem(deliveryModel.getPartNoId());

        // 発注情報取得。存在しない場合はエラー
        final ExtendedTOrderEntity extendedTOrder = orderComponent.getExtendedTOrder(deliveryModel.getOrderId());

        // 納品依頼メール送信管理に情報を登録
        stackTDeliveryendMailComponent.saveDeliverySendMailData(deliveryModel, extendedTOrder, extendedTItem, loginUser);
        // 納品依頼正式メール送信管理に情報を登録
        stackTDeliveryOfficialSendMailComponent.saveDeliveryOfficialSendMailData(deliveryModel, extendedTOrder, extendedTItem, loginUser);
    }
}
