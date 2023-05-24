package jp.co.jun.edi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.model.OrderChangeStateModel;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.constants.DefaultValueConstants;
import jp.co.jun.edi.entity.constants.EndAtTypeConstants;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSkuModel;
//PRD_0112 #7710 JFE add start
import jp.co.jun.edi.repository.MSirmstRepository;
//PRD_0112 #7710 JFE add end
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 発注・発注SKU更新処理.
 */
@Service
public class OrderUpdateService extends GenericUpdateService<UpdateServiceParameter<OrderModel>, UpdateServiceResponse<OrderModel>> {
    @Autowired
    private TOrderRepository orderRepository;
    // PRD_0112 #7710 JFE add start
    @Autowired
    private MSirmstRepository sirmstrepository;
    // PRD_0112 #7710 JFE add end
    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private TDeliveryRepository deliveryRepository;

//  PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ（未作成）. */
	private static final String NOT_CREATED = "0";
//  PRD_0142 #10423 JFE add end

    @Override
    protected UpdateServiceResponse<OrderModel> execute(final UpdateServiceParameter<OrderModel> serviceParameter) {
        final OrderModel orderModel = serviceParameter.getItem();

        // 現在日を取得
        final Date nowDate = DateUtils.truncateDate(DateUtils.createNow());

        // 発注情報を取得
        final Optional<TOrderEntity> orderOptimal = orderRepository.findByOrderId(orderModel.getId());

        final CustomLoginUser loginUser = serviceParameter.getLoginUser();

        // PRD_0112 #7710 JFE add start
        // 更新可否チェック
        //checkCanUpdated(orderOptimal, loginUser);
        checkCanUpdated(orderOptimal, loginUser, orderModel);
        // PRD_0112 #7710 JFE add end

        // 発注情報を取得
        final TOrderEntity tOrder = orderOptimal.get();

        // 納品依頼登録の有無を取得
        final boolean existDelivery = deliveryRepository.countByOrderId(orderModel.getId()) > 0;

        // 発注情報の変更状態取得
        final OrderChangeStateModel orderChangeState = orderComponent.getOrderChangeState(orderModel, tOrder);

        // 発注情報の更新
        setValueForOrderEntity(loginUser, serviceParameter.getItem(), tOrder, orderChangeState);
        orderRepository.save(tOrder);

        // 発注SKUの更新
        updateOrderSku(orderModel, tOrder, loginUser.getUserId(), nowDate, existDelivery);

        // 品番情報取得。取得できない(削除済み)場合は業務エラー
        final TItemEntity tItemEntity = itemRepository.findById(tOrder.getPartNoId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 他テーブル更新
        orderComponent.updateOtherTable(tOrder, tItemEntity, loginUser);

        // 受注確定メール送信キュー、または、発注承認（即時／夜間）メール送信キューへの登録
        orderComponent.printOrder(tOrder);

        // レスポンスに発注IDを設定
        orderModel.setId(orderModel.getId());

        return UpdateServiceResponse.<OrderModel>builder().item(orderModel).build();
    }

    /**
     * 更新可否チェック.
     * ・発注情報存在チェック.
     * ・品番情報存在チェック.
     * ・発注情報 発注ステータスチェック.
     * @param currentOrderOptional 最新のDBの発注情報
     * @param loginUser ログインユーザー
     * @param ordermodel 受注情報
     */
    private void checkCanUpdated(final Optional<TOrderEntity> currentOrderOptional, final CustomLoginUser loginUser,final OrderModel ordermodel) {
        // 発注情報、品番情報の削除・存在チェック
        // DBから最新の発注情報を取得する
        if (!currentOrderOptional.isPresent()) {
            // 存在しない(削除済み)の場合は業務エラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_O_004));
        }

        final TOrderEntity tOrderEntity = currentOrderOptional.get();
        final BigInteger partNoId = tOrderEntity.getPartNoId();

        // DBから最新の品番情報を取得する
        // 存在しない(削除済み)の場合は業務エラー
        itemRepository.findByIdAndDeletedAtIsNull(partNoId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 品番情報を取得し、データが存在しない場合は例外を投げる
        final TItemEntity itemEntity = itemRepository.findById(partNoId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // PRD_0112 #7710 JFE add start
        // 発注先メーカー情報を取得できなければエラーを返す
        final Optional<MSirmstEntity> orderMaker =  sirmstrepository.findByOrderMakerId(ordermodel.getMdfMakerCode());
        if (orderMaker.isPresent() == false){
        	throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_32));
        }
        // PRD_0112 #7710 JFE add end
        // 外部連携区分:JUNoT登録以外の場合、更新不可
        itemComponent.validateReadOnly(itemEntity.getExternalLinkingType());

        // 読み取り専用の場合、更新不可
        orderComponent.validateReadOnly(tOrderEntity.getExpenseItem());
    }
//        // PRD_0112 #7710 JFE add start
//    private void checkCanUpdated(final Optional<TOrderEntity> currentOrderOptional, final CustomLoginUser loginUser) {
//        // 発注情報、品番情報の削除・存在チェック
//        // DBから最新の発注情報を取得する
//        if (!currentOrderOptional.isPresent()) {
//            // 存在しない(削除済み)の場合は業務エラー
//            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_O_004));
//        }
//
//        final TOrderEntity tOrderEntity = currentOrderOptional.get();
//        final BigInteger partNoId = tOrderEntity.getPartNoId();
//
//        // DBから最新の品番情報を取得する
//        // 存在しない(削除済み)の場合は業務エラー
//        itemRepository.findByIdAndDeletedAtIsNull(partNoId).orElseThrow(
//                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
//
//        // 品番情報を取得し、データが存在しない場合は例外を投げる
//        final TItemEntity itemEntity = itemRepository.findById(partNoId).orElseThrow(
//                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
//
//        // 外部連携区分:JUNoT登録以外の場合、更新不可
//        itemComponent.validateReadOnly(itemEntity.getExternalLinkingType());
//
//        // 読み取り専用の場合、更新不可
//        orderComponent.validateReadOnly(tOrderEntity.getExpenseItem());
//    }
//        // PRD_0112 #7710 JFE add end

    /**
     * 入力された発注情報を登録用の発注情報に詰め替え.
     * 最新のDB登録情報に画面からの入力情報を上書きする。
     *
     * @param loginUser ログインユーザ情報
     * @param orderModel 画面から入力された発注情報
     * @param orderEntity 最新の発注情報(DB登録値)、※更新結果が格納される
     * @param orderChangeState 発注情報の変更状態
     */
    private void setValueForOrderEntity(final CustomLoginUser loginUser, final OrderModel orderModel, final TOrderEntity orderEntity,
            final OrderChangeStateModel orderChangeState) {
        /** 画面側で設定される項目 */
        // 製品発注日
        orderEntity.setProductOrderAt(orderModel.getProductOrderAt());
        // 製品修正納期
        orderEntity.setProductCorrectionDeliveryAt(orderModel.getProductCorrectionDeliveryAt());
        // 費目
        orderEntity.setExpenseItem(orderModel.getExpenseItem());
        // 上代
        orderEntity.setRetailPrice(orderModel.getRetailPrice());
        // 単価
        orderEntity.setUnitPrice(orderModel.getUnitPrice());
        // その他原価
        orderEntity.setOtherCost(orderModel.getOtherCost());
        // 生産メーカー
        orderEntity.setMdfMakerCode(orderModel.getMdfMakerCode());
        // 生産メーカー名
        orderEntity.setMdfMakerFactoryName(orderModel.getMdfMakerFactoryName());
        // 生産工場
        orderEntity.setMdfMakerFactoryCode(orderModel.getMdfMakerFactoryCode());
        // 製造担当
        orderEntity.setMdfStaffCode(orderModel.getMdfStaffCode());
        // 生産メーカーコード
        orderEntity.setMdfMakerCode(orderModel.getMdfMakerCode());
        // 生産工場コード
        orderEntity.setMdfMakerFactoryCode(orderModel.getMdfMakerFactoryCode());
        // 生産工場名
        orderEntity.setMdfMakerFactoryName(orderModel.getMdfMakerFactoryName());
        // 委託先工場
        orderEntity.setConsignmentFactory(orderModel.getConsignmentFactory());
        // 原産国
        orderEntity.setCooCode(orderModel.getCooCode());
        // 摘要
        orderEntity.setApplication(orderModel.getApplication());
        // PRD_0144 #10776 add JFE start
        // 関連No
        if (orderEntity.getExpenseItem() == ExpenseItemType.SEWING_ORDER && orderModel.getRelationNumber() == null) {
        	// 費目04(縫製発注)でnullの場合、0をセット
        	orderEntity.setRelationNumber(BigInteger.ZERO);
        } else {
        	orderEntity.setRelationNumber(orderModel.getRelationNumber());
        }
        // PRD_0144 #10776 add JFE end

        /** APIで計算する項目 */
        // 予定用尺
        orderEntity.setPlanLengthActual(orderComponent.setValuePlanLengthActualAndNecessaryLengthActual(orderModel.getExpenseItem()));
        // 実用尺
        orderEntity.setNecessaryLengthActual(
                orderComponent.setValuePlanLengthActualAndNecessaryLengthActual(orderModel.getExpenseItem()));
        // 製品納期(製品修正納期をセット)
        orderEntity.setProductDeliveryAt(orderModel.getProductCorrectionDeliveryAt());
//      PRD_0206 && TEAM_ALBUS-16 add start
        // 製品原価(生地原価+加工原価+附属原価+その他原価をセット)
        // nullの場合は0へ変換
        //orderEntity.setProductCost(orderModel.getOtherCost());
        BigDecimal matlCost = NumberUtils.defaultInt(orderModel.getMatlCost());
        BigDecimal processingCost = NumberUtils.defaultInt(orderModel.getProcessingCost());
        BigDecimal attachedCost = NumberUtils.defaultInt(orderModel.getAttachedCost());
        BigDecimal otherCost = NumberUtils.defaultInt(orderModel.getOtherCost());;
        orderEntity.setProductCost(matlCost.add(processingCost).add(attachedCost).add(otherCost));
//      PRD_0206 && TEAM_ALBUS-16 add end
        // 数量(SKUの合計数から算出)
        orderEntity.setQuantity(BigDecimal.valueOf(orderComponent.sumProductOrderLot(orderModel.getOrderSkus())));
        // 製品金額をセット
        orderEntity.setProductPrice(orderModel.getUnitPrice().multiply(orderEntity.getQuantity()));
        // 社内の場合連携入力者をセット
        orderEntity.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(loginUser));
//      PRD_0142 #10423 JFE add start
        // TAGDAT作成フラグに未作成を設定
        orderEntity.setTagdatCreatedFlg(NOT_CREATED);
//      PRD_0142 #10423 JFE add end


//      PRD_0206 && TEAM_ALBUS-16 add start
        // 生地原価
        orderEntity.setMatlCost(orderModel.getMatlCost());
        // 加工原価
        orderEntity.setProcessingCost(orderModel.getProcessingCost());
        // 附属原価
        orderEntity.setAttachedCost(orderModel.getAttachedCost());
        // 生地単価
        if(orderModel.getMatlUnitPrice() != null) {
            orderEntity.setMatlUnitPrice(orderModel.getMatlUnitPrice());
        }else {
            orderEntity.setMatlUnitPrice(BigDecimal.ZERO);
        }
//      PRD_0206 && TEAM_ALBUS-16 add end

        if (orderChangeState.isUnapprovedTargetChanged()) {
            // MD承認済→MD未承認に戻す場合、承認ステータスを「受注確定済、MD未承認(3)」に更新し、承認済みの情報を初期値で設定する。
            orderComponent.resetApprovedOrderStatus(orderEntity);
        }

        // 連携対象に更新
        orderComponent.setLinkingTarget(orderEntity);
    }

    /**
     * 発注SKU情報の更新を行う.
     * @param orderModel 画面から入力された発注情報
     * @param orderEntity 更新対象のSKUの親に当たる発注情報
     * @param userId 更新ユーザーID
     * @param nowDate 現在日
     * @param existDelivery 納品依頼登録の有無
     */
    private void updateOrderSku(final OrderModel orderModel, final TOrderEntity orderEntity, final BigInteger userId,
            final Date nowDate, final boolean existDelivery) {
        final List<BigInteger> orderSkuIdList = new ArrayList<>();

        // 登録済みの月末日を取得する(新規登録する発注SKUの月末日にセットする)
        final Optional<TOrderSkuEntity> existEndAtOrderSku = orderSkuRepository.findFirstExistEndAtOrderSkuByOrderId(orderModel.getId());

        for (OrderSkuModel tOrderSkuModel : orderModel.getOrderSkus()) {
            final TOrderSkuEntity orderSkuEntity = setValueForOrderSkuEntity(tOrderSkuModel, orderEntity);

            // 確定済の場合は月末発注数量と月末裁断数量の再セット
            if (orderComponent.isOrderConfirmed(orderEntity.getOrderApproveStatus())) {
                // existEndAtOrderSkuがない場合は、データ不整合のためエラーを返却
                if (!existEndAtOrderSku.isPresent()) {
                    throw new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR));
                }
                // 月末日(当月、前月、前々月)いずれかが空の場合は、月末日をセットする
                if (Objects.isNull(orderSkuEntity.getMonthEndAt())
                        || Objects.isNull(orderSkuEntity.getPreviousMonthEndAt())
                        || Objects.isNull(orderSkuEntity.getMonthBeforeEndAt())) {
                    orderSkuEntity.setMonthEndAt(existEndAtOrderSku.get().getMonthEndAt());
                    orderSkuEntity.setPreviousMonthEndAt(existEndAtOrderSku.get().getPreviousMonthEndAt());
                    orderSkuEntity.setMonthBeforeEndAt(existEndAtOrderSku.get().getMonthBeforeEndAt());
                }
                // 月末発注数量と月末裁断数量の再セット
                setValueMonthEndOrderLotAndMonthEndCutLot(orderModel.getProductOrderAt(), orderModel.getProductCorrectionDeliveryAt(),
                        orderEntity.getOrderApproveStatus(), tOrderSkuModel.getProductOrderLot(), orderSkuEntity);
            }

            // 納品依頼数量の再セット
            setValueDeliveryLot(nowDate, existDelivery, orderSkuEntity);

            final TOrderSkuEntity returnEntity = orderSkuRepository.save(orderSkuEntity);
            // 更新・登録したIDはリストに格納
            orderSkuIdList.add(returnEntity.getId());
        }

        // 0件の場合は、存在しないID(0)を設定する
        if (orderSkuIdList.size() == 0) {
            orderSkuIdList.add(BigInteger.ZERO);
        }

        // リストに格納したID以外のレコードに削除日を設定する。
        orderSkuRepository.updateSkuDeletedAtByOrderIdAndExclusionIds(orderModel.getId(), orderSkuIdList, userId);
    }

    /**
     * 入力された発注SKU情報を登録用の発注Sku情報に詰め替え.
     * 最新のDB登録情報に画面からの入力情報を上書きする。
     *
     * @param orderSkuModel 画面から入力された発注SKU
     * @param orderEntity 親に当たる発注情報(DB登録値)
     * @return 画面の情報とAPI側で算出する情報をセットした発注SKU情報Entity
     */
    private TOrderSkuEntity setValueForOrderSkuEntity(final OrderSkuModel orderSkuModel, final TOrderEntity orderEntity) {
        // DBから最新の発注SKUを取得
        // DBに存在しない場合は空のTOrderSkuEntityを作成
        final TOrderSkuEntity orderSkuEntity = orderSkuRepository.findByIdAndDeletedAtIsNull(orderSkuModel.getId()).orElse(new TOrderSkuEntity());

        /** 画面側で設定される項目 */
        // 色
        orderSkuEntity.setColorCode(orderSkuModel.getColorCode());
        // サイズ
        orderSkuEntity.setSize(orderSkuModel.getSize());
        // 製品発注数
        orderSkuEntity.setProductOrderLot(orderSkuModel.getProductOrderLot());

        /** APIで設定する項目 */
        // 発注ID
        orderSkuEntity.setOrderId(orderEntity.getId());
        // 発注No
        orderSkuEntity.setOrderNumber(orderEntity.getOrderNumber());
        // 品番
        orderSkuEntity.setPartNo(orderEntity.getPartNo());
        // 製品裁断数
        orderSkuEntity.setProductCutLot(setValueProductCutLot(orderSkuModel.getProductOrderLot(), orderEntity.getOrderApproveStatus()));
        // 送信区分がない場合は1をセット
        if (Objects.isNull(orderSkuEntity.getSendCode())) {
            orderSkuEntity.setSendCode(DefaultValueConstants.DEFAULT_SEND_CODE);
        }

        return orderSkuEntity;
    }

    /**
     * 製品裁断数の値セット.
     * MD承認済の場合は発注数をセット。MD未承認の場合は0をセットする。
     *
     * @param productOrderLot 製品発注数
     * @param orderApproveStatus 発注承認ステータス
     * @return 製品裁断数の値
     */
    public int setValueProductCutLot(final int productOrderLot, final String orderApproveStatus) {
        if (OrderApprovalType.APPROVED.getValue().equals(orderApproveStatus)) {
            return productOrderLot;
        }
        return BigDecimal.ZERO.intValue();
    }

    /**
     * 納品依頼数量の値セット.
     * 納品依頼登録0件の場合、0をセットする。0件以外の場合は更新しない。
     *
     * <pre>
     * 納品依頼削除後の当日に発注訂正した場合の暫定対応。
     * 納品依頼削除後、発注生産側で納品依頼数量がゼロになるが、
     * JUNoT発生分の発注については、当日の夜間にしかデータがJUNoTに連携されない。
     * そのため、本処理で納品依頼数量をゼロにする。
     * また、納品依頼削除後に発注情報の納品依頼数量も訂正すべきだが、
     * 現時点では、発注生産発生分の発注情報はJUNoTでは更新しないため、ガード処置として本処理にて対処。
     * </pre>
     *
     * @param nowDate 現在日
     * @param existDelivery 納品依頼登録の有無
     * @param orderSkuEntity 発注SKU情報(DB登録値)
     */
    private void setValueDeliveryLot(final Date nowDate, final boolean existDelivery, final TOrderSkuEntity orderSkuEntity) {
        if (existDelivery) {
            // 納品依頼登録がある場合、処理を終了する
            return;
        }

        if (Objects.isNull(orderSkuEntity.getMonthEndAt())
                || Objects.isNull(orderSkuEntity.getPreviousMonthEndAt())
                || Objects.isNull(orderSkuEntity.getMonthBeforeEndAt())) {
            // 月末日(当月、前月、前々月)いずれかが空の場合、処理を終了する
            return;
        }

        // 納品依頼数量
        orderSkuEntity.setDeliveryLot(0);

        // 現在日を基準として、締め日を取得
        final int endAtType = orderComponent.judgeEndAtTypeByProductOrderAt(nowDate, orderSkuEntity);

        switch (endAtType) {
        case EndAtTypeConstants.THIS_MONTH: // 当月
            // 月末納品依頼数（当月）
            orderSkuEntity.setMonthEndDeliveryLot(0);
            break;
        case EndAtTypeConstants.PREVIOUS_MONTH: // 前月
            // 月末納品依頼数（当月）
            orderSkuEntity.setMonthEndDeliveryLot(0);
            // 月末納品依頼数（前月）
            orderSkuEntity.setPreviousMonthEndDeliveryLot(0);
            break;
        case EndAtTypeConstants.MONTH_BEFORE: // 前々月
        case EndAtTypeConstants.PAST_MONTH: // 過去月
            // 月末納品依頼数（当月）
            orderSkuEntity.setMonthEndDeliveryLot(0);
            // 月末納品依頼数（前月）
            orderSkuEntity.setPreviousMonthEndDeliveryLot(0);
            // 月末納品依頼数（前々月）
            orderSkuEntity.setMonthBeforeEndDeliveryLot(0);

            break;
        default:
            break;
        }
    }

    /**
     * 月末発注数量（当月/前月/前々月）と月末裁断数量（当月/前月/前々月）の値セット.
     * 該当する月の月末発注数量に製品発注数と月末裁断数量をセットする
     * 該当する月がない場合は0をセットする
     *
     * ※月末発注数量に製品発注数を設定
     * ※MD承認済の場合のみ月末裁断数量に製品発注数を設定
     *
     * @param productOrderAt (※注)画面から入力された生産発注日
     * @param productCorrectionDeliveryAt (※注)画面から入力された製品修正納期
     * @param orderApproveStatus DBに登録されている親に当たる発注情報の承認ステータス
     * @param productOrderLot (※注)画面から入力された製品発注数
     * @param orderSkuEntity DBに登録されている最新の発注SKU情報
     */
    public void setValueMonthEndOrderLotAndMonthEndCutLot(final Date productOrderAt, final Date productCorrectionDeliveryAt,
            final String orderApproveStatus, final int productOrderLot,
            final TOrderSkuEntity orderSkuEntity) {
        // 初期値として0をセット
        // 月末発注数量
        orderSkuEntity.setMonthEndOrderLot(BigInteger.ZERO.intValue());
        orderSkuEntity.setPreviousMonthEndOrderLot(BigInteger.ZERO.intValue());
        orderSkuEntity.setMonthBeforeEndOrderLot(BigInteger.ZERO.intValue());
        // 月末裁断数量
        orderSkuEntity.setMonthEndCutLot(BigInteger.ZERO.intValue());
        orderSkuEntity.setPreviousMonthEndCutLot(BigInteger.ZERO.intValue());
        orderSkuEntity.setMonthBeforeEndCutLot(BigInteger.ZERO.intValue());

        final int endAtType = orderComponent.judgeEndAtTypeByProductOrderAt(productOrderAt, orderSkuEntity);
        // 該当する月の月末発注数量に製品発注数をセット

        switch (endAtType) {
        case EndAtTypeConstants.THIS_MONTH: // 当月
            orderSkuEntity.setMonthEndOrderLot(productOrderLot);
            break;
        case EndAtTypeConstants.PREVIOUS_MONTH: // 前月
            orderSkuEntity.setMonthEndOrderLot(productOrderLot); // 月末発注数量（当月）
            orderSkuEntity.setPreviousMonthEndOrderLot(productOrderLot); // 月末発注数量（前月）
            break;
        case EndAtTypeConstants.MONTH_BEFORE: // 前々月
        case EndAtTypeConstants.PAST_MONTH: // 過去月
            orderSkuEntity.setMonthEndOrderLot(productOrderLot); // 月末発注数量（当月）
            orderSkuEntity.setPreviousMonthEndOrderLot(productOrderLot); // 月末発注数量（前月）
            orderSkuEntity.setMonthBeforeEndOrderLot(productOrderLot); // 月末発注数量（前々月）

            break;
        default:
            break;
        }

        // MD承認済の場合は月末裁断数量のセットを行う。
        if (OrderApprovalType.APPROVED.getValue().equals(orderApproveStatus)) {
            // 裁断数は生産修正納期を基に算出する。
            final int cutEndAtType = orderComponent.judgeEndAtTypeByProductOrderAt(productCorrectionDeliveryAt, orderSkuEntity);
            // 該当する月の月末裁断数に発注数をセット
            switch (cutEndAtType) {
            case EndAtTypeConstants.THIS_MONTH: // 当月
                orderSkuEntity.setMonthEndCutLot(productOrderLot); // 月末裁断数（当月）
                break;
            case EndAtTypeConstants.PREVIOUS_MONTH: // 前月
                orderSkuEntity.setMonthEndCutLot(productOrderLot); // 月末裁断数（当月）
                orderSkuEntity.setPreviousMonthEndCutLot(productOrderLot); // 月末裁断数（前月）
                break;
            case EndAtTypeConstants.MONTH_BEFORE: // 前々月
            case EndAtTypeConstants.PAST_MONTH: // 過去月
                orderSkuEntity.setMonthEndCutLot(productOrderLot); // 月末裁断数（当月）
                orderSkuEntity.setPreviousMonthEndCutLot(productOrderLot); // 月末裁断数（前月）
                orderSkuEntity.setMonthBeforeEndCutLot(productOrderLot); // 月末裁断数（前々月）
                break;
            default:
                break;
            }
        }
    }

}
