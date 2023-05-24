package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.deliveryupsert.DeliveryDetailUpsertComponent;
import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TDeliveryStoreRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.DivisionCodeType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品依頼関連テーブル更新処理.
 */
@Service
public class DeliveryUpdateService
extends GenericUpdateService<UpdateServiceParameter<DeliveryModel>, UpdateServiceResponse<DeliveryModel>> {

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private TDeliveryStoreRepository deliveryStoreRepository;

    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private DeliveryDetailUpsertComponent deliveryDetailUpsert;

    @Override
    protected UpdateServiceResponse<DeliveryModel> execute(final UpdateServiceParameter<DeliveryModel> serviceParameter) {
        final DeliveryModel requestDeliveryModel = serviceParameter.getItem();
        // 発注生産システム側で管理しているログインユーザのIDを取得
        final CustomLoginUser loginUser = serviceParameter.getLoginUser();

        // 品番情報取得。存在しない場合はエラー
        final ExtendedTItemEntity registeredDbTItemEntity = itemComponent.getExtendedTItem(requestDeliveryModel.getPartNoId());

        // 発注情報取得。存在しない場合はエラー
        final ExtendedTOrderEntity registeredDbTOrderEntity = orderComponent.getExtendedTOrder(requestDeliveryModel.getOrderId());

        // リクエストパラメータの納品IDに紐づく納品依頼情報テーブルのレコードを取得。存在しない場合はエラー
        final TDeliveryEntity registerdDbTDeliveryEntity = deliveryRepository.findByIdAndDeletedAtIsNull(requestDeliveryModel.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_009)));

        // リクエストパラメータの納品IDに紐づく納品明細情報テーブルのレコードリストを取得。存在しない場合はエラー
        final List<TDeliveryDetailEntity> registerdDbTDeliveryDetails = deliveryComponent.getTDeliveryDetailList(requestDeliveryModel.getId());

        // 更新可否チェック
        checkUpdatable(requestDeliveryModel, loginUser, registerdDbTDeliveryEntity, registeredDbTItemEntity, registeredDbTOrderEntity);

        // 課別画面からの更新の場合、得意先・得意先SKUを削除して再設定する
        deliveryComponent.prepareUpdateFromDivisionScreen(requestDeliveryModel, loginUser.getUserId(), registeredDbTItemEntity);

        // 納品依頼UPDATE ※子孫テーブルはINSERTになるケースあり
        updateDeliveryAndChildRelationship(requestDeliveryModel, loginUser, registerdDbTDeliveryEntity,
                registerdDbTDeliveryDetails, registeredDbTItemEntity, registeredDbTOrderEntity);

        // 今回登録した(これから登録)納品依頼レコードのリスト
        final List<TDeliveryEntity> resultDeliveries = new ArrayList<>();

        // 納品依頼INSERT ※子孫テーブルもINSERT
        insertIntoDeliveryAndChildRelationship(requestDeliveryModel, loginUser, registerdDbTDeliveryDetails,
                registeredDbTItemEntity, registeredDbTOrderEntity, resultDeliveries);

        // レスポンス用のModelを作成
        final DeliveryModel responseModel = generateResponseModel(serviceParameter.getItem(), resultDeliveries);

        return UpdateServiceResponse.<DeliveryModel>builder().item(responseModel).build();
    }

    /**
     * 納品依頼情報の更新、及びその子孫テーブルの登録・更新処理を行う.
     * @param requestDeliveryModel 画面から入力された納品依頼明細情報
     * @param loginUser ログインユーザ情報
     * @param registerdDbTDeliveryEntity リクエストパラメータの納品IDに紐づく納品依頼情報
     * @param registerdDbTDeliveryDetails リクエストパラメータの納品IDに紐づく納品明細情報テーブルのレコードリスト
     * @param registeredDbTItemEntity 現時点でDBに登録されている品番情報
     * @param registerdDbTOrderEntity 現時点でDBに登録されている発注情報
     */
    private void updateDeliveryAndChildRelationship(final DeliveryModel requestDeliveryModel, final CustomLoginUser loginUser,
            final TDeliveryEntity registerdDbTDeliveryEntity, final List<TDeliveryDetailEntity> registerdDbTDeliveryDetails,
            final ExtendedTItemEntity registeredDbTItemEntity, final ExtendedTOrderEntity registerdDbTOrderEntity) {

        // リクエストパラメータの中で配分場所がDB登録済のレコードのみ抽出
        final List<DeliveryDetailModel> deliveryDetailListForUpdate = requestDeliveryModel.getDeliveryDetails().stream()
                .filter(requestDeliveryDetail -> isUpdateRecord(requestDeliveryDetail, registerdDbTDeliveryDetails))
                .collect(Collectors.toList());

        // 課別画面からの更新でUPDATE用の納品依頼明細リストがない場合は納品依頼情報及びその子孫のテーブルも論理削除
        // →登録済の納品明細レコードが画面で全て削除されたケース
        final boolean isFromStoreScreen = BooleanType.TRUE.equals(requestDeliveryModel.getFromStoreScreenFlg());
        if (!deliveryComponent.isExistDeliveryDetails(deliveryDetailListForUpdate)) {
            deleteDeliveryAndChildRelationship(registerdDbTDeliveryEntity, registerdDbTDeliveryDetails, loginUser);
            return;
        }

        // 更新レコードの納期は共通の為、先頭を取得(更新時に他の配分場所を追加した場合は新規登録の方で処理する)
        final DeliveryDetailModel firstDeliveryDetailForUpdate = deliveryDetailListForUpdate.get(0);

        // 更新用データ設定
        generateDeliveryEntityForUpdate(requestDeliveryModel, registerdDbTDeliveryEntity, firstDeliveryDetailForUpdate, registerdDbTOrderEntity);
        deliveryRepository.save(registerdDbTDeliveryEntity);

        // 店舗配分時、入力がない課でも共通の納品明細レコードの項目は更新する
        if (isFromStoreScreen) {
            updateDetailCommonItemsAtStoreScreen(requestDeliveryModel, loginUser);
        }

        // 納品明細情報とその子孫テーブルUpsert
        final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> model = new DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity>();
        model.setModelForUpdateList(deliveryDetailListForUpdate);
        model.setLoginUser(loginUser);
        model.setParentEntity(registerdDbTDeliveryEntity);
        model.setFromStoreScreen(isFromStoreScreen);
        deliveryDetailUpsert.upsert(model);
    }

    /**
     * 得意先別画面からの入力で、数量の入力がない課でも納期等の共通項目は更新する.
     * @param requestDeliveryModel リクエストパラメータ
     * @param loginUser ログインユーザー情報
     */
    private void updateDetailCommonItemsAtStoreScreen(final DeliveryModel requestDeliveryModel, final CustomLoginUser loginUser) {
        final DeliveryDetailModel firstDeliveryDetailModel = requestDeliveryModel.getDeliveryDetails().get(0);
        // 通知メール送信、納期を更新
        deliveryDetailRepository.updateDetailCommonItemsByDeliveryId(
                firstDeliveryDetailModel.getFaxSend().getValue(),
                firstDeliveryDetailModel.getCorrectionAt(),
                requestDeliveryModel.getId(), loginUser.getUserId());
    }

    /**
     * 更新用の納品依頼情報Entityを作成する.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param registerdDbTDeliveryEntity 現時点でDBに登録されている納品依頼情報
     * @param firstDeliveryDetailForUpdate 画面から入力された納品明細情報
     * @param registerdDbTOrderEntity 現時点でDBに登録されている発注情報
     */
    private void generateDeliveryEntityForUpdate(final DeliveryModel requestDeliveryModel,
            final TDeliveryEntity registerdDbTDeliveryEntity, final DeliveryDetailModel firstDeliveryDetailForUpdate,
            final ExtendedTOrderEntity registerdDbTOrderEntity) {
        /* 画面側で設定される項目 */
        // 配分率区分
        registerdDbTDeliveryEntity.setDistributionRatioType(requestDeliveryModel.getDistributionRatioType());
        // メモ
        registerdDbTDeliveryEntity.setMemo(requestDeliveryModel.getMemo());
        // B級品区分
        registerdDbTDeliveryEntity.setNonConformingProductType(requestDeliveryModel.isNonConformingProductType());
        // B級品単価
        registerdDbTDeliveryEntity.setNonConformingProductUnitPrice(requestDeliveryModel.getNonConformingProductUnitPrice());
        // 最終納品ステータス
        registerdDbTDeliveryEntity.setLastDeliveryStatus(requestDeliveryModel.getLastDeliveryStatus());

        // 納期変更理由IDと詳細を設定
        deliveryComponent.generateDeliveryDateChangeReasonIdAndDetail(registerdDbTDeliveryEntity, registerdDbTOrderEntity,
                requestDeliveryModel, firstDeliveryDetailForUpdate);
    }

    /**
     * 納品依頼とその子孫テーブルの登録処理を行う.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param loginUser ログインユーザ情報
     * @param registerdDbTDeliveryDetails リクエストパラメータの納品IDに紐づく納品明細テーブルのレコードリスト
     * @param registerdDbTItemEntity 現時点でDBに登録されている品番情報
     * @param registerdDbTOrderEntity 現時点でDBに登録されている発注情報
     * @param resultDeliveries 今回登録した(このメソッドで登録する)納品依頼のリスト
     */
    private void insertIntoDeliveryAndChildRelationship(final DeliveryModel requestDeliveryModel, final CustomLoginUser loginUser,
            final List<TDeliveryDetailEntity> registerdDbTDeliveryDetails, final ExtendedTItemEntity registerdDbTItemEntity,
            final ExtendedTOrderEntity registerdDbTOrderEntity, final List<TDeliveryEntity> resultDeliveries) {

        // INSERT用の納品依頼明細リスト
        final List<DeliveryDetailModel> deliveryDetailsForInsert = requestDeliveryModel.getDeliveryDetails().stream()
                .filter(requestDeliveryDetail -> !isUpdateRecord(requestDeliveryDetail, registerdDbTDeliveryDetails))
                .collect(Collectors.toList());

        final DeliveryModel deliveryForInsert = generateDeliveryForInsert(requestDeliveryModel, deliveryDetailsForInsert);

        // 納品依頼とその子孫テーブルの登録
        if (Objects.nonNull(deliveryDetailsForInsert) && !deliveryDetailsForInsert.isEmpty()) {
            deliveryComponent.insertIntoDeliveryAndChildRelationship(registerdDbTItemEntity, registerdDbTOrderEntity,
                    loginUser, deliveryForInsert, resultDeliveries);
        }

        // メール送信
        deliveryComponent.sendDeliveryRequestRegistMails(registerdDbTItemEntity, registerdDbTOrderEntity, loginUser, resultDeliveries);
    }

    /**
     * 登録用の納品依頼情報Model作成する.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param deliveryDetailsForInsert 登録用の納品明細リスト
     * @return 登録用の納品依頼情報Model
     */
    private DeliveryModel generateDeliveryForInsert(final DeliveryModel requestDeliveryModel,
            final List<DeliveryDetailModel> deliveryDetailsForInsert) {
        final DeliveryModel deliveryForInsert = new DeliveryModel();
        BeanUtils.copyProperties(requestDeliveryModel, deliveryForInsert);
        deliveryForInsert.setDeliveryDetails(deliveryDetailsForInsert);
        return deliveryForInsert;
    }

    /**
     * 更新時のバリデーションチェック.
     * 不備があれば業務エラーをスロー.
     *
     * @param requestDeliveryModel リクエストパラメータの納品依頼情報
     * @param loginUser ログインユーザー
     * @param registerdDbTDeliveryEntity リクエストパラメータの納品IDに紐づく納品依頼情報
     * @param registerdDbTItemEntity リクエストパラメータの納品IDに紐づく品番情報
     * @param registerdDbTOrderEntity リクエストパラメータの納品IDに紐づく発注情報
     */
    private void checkUpdatable(final DeliveryModel requestDeliveryModel, final CustomLoginUser loginUser, final TDeliveryEntity registerdDbTDeliveryEntity,
            final ExtendedTItemEntity registerdDbTItemEntity, final ExtendedTOrderEntity registerdDbTOrderEntity) {
        // 承認済だったら業務エラー
        if (deliveryComponent.isDeliveryApproved(registerdDbTDeliveryEntity.getDeliveryApproveStatus())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_008));
        }

        // バリデーションチェック
        final ResultMessages rsltMsg = deliveryComponent.checkCommonValidate(requestDeliveryModel, registerdDbTItemEntity, registerdDbTOrderEntity);

        // 店舗配分時、課別と数量不一致の場合エラー
        if (BooleanType.TRUE == requestDeliveryModel.getFromStoreScreenFlg()) {
            deliveryComponent.checkNotMatchToDivisionLot(requestDeliveryModel, rsltMsg);
        }

        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
    }

    /**
     * 更新対象の納品明細レコードか判定する.
     * @param requestDeliveryDetail リクエストパラメータの納品明細リスト
     * @param registerdDbDeliveryDetails リクエストパラメータの納品IDに紐づく納品明細テーブルのレコードリスト
     * @return true:更新レコード false:新規登録レコード
     */
    private boolean isUpdateRecord(final DeliveryDetailModel requestDeliveryDetail,
            final List<TDeliveryDetailEntity> registerdDbDeliveryDetails) {

        final String requestDbDivisionCode = requestDeliveryDetail.getDivisionCode();
        final String registerdDbDivisionCode = registerdDbDeliveryDetails.get(0).getDivisionCode();

        // リクエストパラメータの部署コードが縫製検品・本社撮影の場合、リクエストパラメータと登録済DBの部署コードが同じであれば同一納品依頼
        if (requestDbDivisionCode.equals(DivisionCodeType.SEWING.getValue())
                || requestDbDivisionCode.equals(DivisionCodeType.PHOTO.getValue())) {
            return Objects.equals(requestDbDivisionCode, registerdDbDivisionCode);
        }

        // リクエストパラメータの部署コードが縫製検品・本社撮影以外の場合、
        // DB登録済の部署コードが縫製検品・本社撮影以外で場所コードが同じであれば同一納品依頼
        return !registerdDbDivisionCode.equals(DivisionCodeType.SEWING.getValue())
                && !registerdDbDivisionCode.equals(DivisionCodeType.PHOTO.getValue())
                && Objects.equals(requestDeliveryDetail.getAllocationCode(), registerdDbDeliveryDetails.get(0).getLogisticsCode().substring(0, 1));
    }

    /**
     * レスポンス用の納品依頼Modelを作成する.
     * ・UPDATEの納品依頼がある場合はUPDATEした納品依頼IDをセット
     * ・UPDATEの納品依頼がなく、INSERTの納品依頼がある場合は、INSERTした納品依頼IDをセット
     *
     * @param deliveryModel 画面から入力された納品依頼情報
     * @param registedDeliveries 登録した納品依頼リスト
     * @return 納品IDをセットしたレスポンスModel
     */
    private DeliveryModel generateResponseModel(final DeliveryModel deliveryModel, final List<TDeliveryEntity> registedDeliveries) {
        // レスポンス用のModel
        final DeliveryModel responseModel = new DeliveryModel();
        responseModel.setId(deliveryModel.getId());

        // 更新した納品依頼を取得：
        final Optional<TDeliveryEntity> updatedDelivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryModel.getId());

        if (!updatedDelivery.isPresent()) {
            // 更新した納品依頼が存在しない場合は登録した一番小さい納品依頼IDをセット
            final BigInteger smallestRegistedDeliveryId = deliveryComponent.getMinimumRegistedDeliveryId(registedDeliveries);
            responseModel.setId(smallestRegistedDeliveryId);
        }

        return responseModel;
    }

    /**
     * 納品依頼及びその子孫テーブルを論理削除する.
     *
     * @param registerdDbTDeliveryEntity 現時点で登録されている納品依頼情報
     * @param registerdDbTDeliveryDetails 現時点で登録されている納品依頼IDに紐づく納品明細リスト
     * @param loginUser ログインユーザ情報
     */
    private void deleteDeliveryAndChildRelationship(final TDeliveryEntity registerdDbTDeliveryEntity,
            final List<TDeliveryDetailEntity> registerdDbTDeliveryDetails, final CustomLoginUser loginUser) {
        // 削除日付
        final Date deleteAt = new Date();

        // 納品依頼が承認済みかチェック。済みだったら業務エラー
        if (deliveryComponent.isDeliveryApproved(registerdDbTDeliveryEntity.getDeliveryApproveStatus())) {
           throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_005));
        }

        // 納品情報の削除日更新
        registerdDbTDeliveryEntity.setDeletedAt(deleteAt);
        deliveryRepository.save(registerdDbTDeliveryEntity);

        // 納品明細情報の削除日更新
        deliveryDetailRepository.updateDetailDeletedAtByDeliveryId(registerdDbTDeliveryEntity.getId(), loginUser.getUserId());

        final List<BigInteger> deliveryDetailIds = registerdDbTDeliveryDetails.stream()
                .map(deliveryDetail -> deliveryDetail.getId()).collect(Collectors.toList());
        if (!deliveryDetailIds.isEmpty()) {
            // 納品SKU情報の削除日更新
            deliverySkuRepository.updateSkuDeletedAtByDeliveryDetailIds(deliveryDetailIds, loginUser.getUserId());
            // 納品得意先情報の削除日更新
            deliveryStoreRepository.updateDeletedAtByDeliveryDetailIds(deliveryDetailIds, loginUser.getUserId());
            // 納品得意先SKU情報の削除日更新
            deliveryStoreSkuRepository.updateDeletedAtByDeliveryDetailIds(deliveryDetailIds, loginUser.getUserId());
        }
    }
}
