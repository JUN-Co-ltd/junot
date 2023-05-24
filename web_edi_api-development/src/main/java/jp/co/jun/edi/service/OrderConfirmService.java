package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.component.MNumberComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.mail.OrderConfirmedSendMailComponent;
import jp.co.jun.edi.component.mail.StackTOrderRequestSendMailComponent;
import jp.co.jun.edi.entity.MDeliveryCountNumberEntity;
import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.constants.EndAtTypeConstants;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSkuModel;
import jp.co.jun.edi.model.mail.OrderConfirmedSendModel;
import jp.co.jun.edi.repository.MDeliveryCountNumberRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TProductionStatusRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderApprovalType;

/**
 * 発注確定処理.
 */
@Service
public class OrderConfirmService extends GenericUpdateService<ApprovalServiceParameter<OrderModel>, ApprovalServiceResponse> {
    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;

    @Autowired
    private TProductionStatusRepository tProductionStatusRepository;

    @Autowired
    private MDeliveryCountNumberRepository mDeliveryCountNumberRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private MNumberComponent numberComponent;

    @Autowired
    private MKanmstComponent mKanmstComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private OrderConfirmedSendMailComponent orderConfirmedSendMailComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private StackTOrderRequestSendMailComponent stackTOrderRequestSendMailComponent;

    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;

    @Autowired
    private TItemRepository itemRepository;

//  PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ（未作成）. */
	private static final String NOT_CREATED = "0";
//  PRD_0142 #10423 JFE add end

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<OrderModel> serviceParameter) {
        final OrderModel orderModel = serviceParameter.getItem();

        // 品番情報を取得し、データが存在しない場合は例外を投げる
        final TItemEntity itemEntity = itemRepository.findById(orderModel.getPartNoId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 外部連携区分:JUNoT登録以外の場合、確定不可
        itemComponent.validateReadOnly(itemEntity.getExternalLinkingType());

        // 発注存在チェック。取得できない(削除済み)場合は業務エラー
        final TOrderEntity entity = orderRepository.findByOrderId(orderModel.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_O_001)));

        // 読み取り専用の場合、更新不可
        orderComponent.validateReadOnly(entity.getExpenseItem());

        // 発注確定済チェック。発注確定済の場合は業務エラー
        if (orderComponent.isOrderConfirmed(entity.getOrderApproveStatus())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_O_002));
        }

        // 確定用発注情報(項目再セット)
        final TOrderEntity confirmOrderEntity = setValueForOrderEntity(serviceParameter.getLoginUser(), entity);

        // 発注情報の更新
        orderRepository.save(confirmOrderEntity);

        // 発注SKUの更新
        updateOrderSku(orderModel, confirmOrderEntity);

        // 生産ステータスの発注Noを更新
        tProductionStatusRepository.updateOrderNoByOrderId(orderModel.getId(), confirmOrderEntity.getOrderNumber(),
                serviceParameter.getLoginUser().getUserId());

        // 納品依頼回数採番マスタ登録
        insertDeliveryCountNumber(confirmOrderEntity);

        // レスポンスに発注IDを設定
        orderModel.setId(orderModel.getId());

        // 品番情報取得(品番更新、メール用)
        final Optional<ExtendedTItemEntity> extendedTItemEntity = extendedTItemRepository.findById(orderModel.getPartNoId());
        // 発注情報
        final ExtendedTOrderEntity extendedTOrderEntity = extendedTOrderRepository.findById(confirmOrderEntity.getId()).orElse(new ExtendedTOrderEntity());

        // メール送信
        OrderConfirmedSendModel sendModel = generateSendMailData(extendedTOrderEntity, extendedTItemEntity);
        orderConfirmedSendMailComponent.sendMail(sendModel, serviceParameter.getLoginUser().getAccountName());

        // 受注確定情報（即時）を、メール送信キューに格納する
        stackTOrderRequestSendMailComponent.saveOrderSendMailData(confirmOrderEntity, extendedTItemEntity.get(), serviceParameter.getLoginUser());

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * 納品依頼回数採番マスタ登録.
     * @param orderEntity 発注情報Entity
     */
    private void insertDeliveryCountNumber(final TOrderEntity orderEntity) {
        final MDeliveryCountNumberEntity mDeliveryCountNumberEntity = new MDeliveryCountNumberEntity();
        mDeliveryCountNumberEntity.setOrderId(orderEntity.getId());
        mDeliveryCountNumberEntity.setOrderNumber(orderEntity.getOrderNumber());
        mDeliveryCountNumberRepository.save(mDeliveryCountNumberEntity);
    }

    /**
     * 発注SKU情報の更新を行う.
     * @param orderModel 画面から入力された発注情報
     * @param orderEntity 親にあたる発注情報
     */
    private void updateOrderSku(final OrderModel orderModel, final TOrderEntity orderEntity) {
        TOrderSkuEntity confirmOrderSkuEntity;
        for (OrderSkuModel tOrderSkuModel : orderModel.getOrderSkus()) {
            confirmOrderSkuEntity = setValueForOrderSkuEntity(tOrderSkuModel, orderEntity);
            // 発注SKU更新
            orderSkuRepository.save(confirmOrderSkuEntity);
        }
    }

    /**
     * 確定用の発注情報の項目再セット.
     *
     * @param loginUser ログインユーザ情報
     * @param tOrderEntity 最新の発注情報(DB登録値)
     *
     * @return API側で再セットした発注情報Entity
     */
    private TOrderEntity setValueForOrderEntity(final CustomLoginUser loginUser, final TOrderEntity tOrderEntity) {
        // 発注Noをセット
        tOrderEntity.setOrderNumber(this.numberingOrderNumber());
        // PRD_0144 #10776 mod JFE start
        // 関連Noをセット
        if (tOrderEntity.getExpenseItem() != ExpenseItemType.SEWING_ORDER) {
        	// 費目04(縫製発注)以外の場合、発注Noをセット
        tOrderEntity.setRelationNumber(tOrderEntity.getOrderNumber());
        }
        // PRD_0144 #10776 mod JFE end

        // 承認ステータスをセット
        tOrderEntity.setOrderApproveStatus(OrderApprovalType.CONFIRM.getValue());
        // 発注確定日をセット
        final Date confirmAt = new Date();
        tOrderEntity.setOrderConfirmAt(confirmAt);
        // 確定者をセット
        tOrderEntity.setOrderConfirmUserId(loginUser.getUserId());
        // 社内の場合連携入力者をセット
        tOrderEntity.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(loginUser));
//      PRD_0142 #10423 JFE add start
        // TAGDAT作成フラグに未作成を設定
        tOrderEntity.setTagdatCreatedFlg(NOT_CREATED);
//      PRD_0142 #10423 JFE add end

        // 連携対象に更新
        orderComponent.setLinkingTarget(tOrderEntity);

        return tOrderEntity;
    }

    /**
     * 入力された発注SKU情報を登録用の発注Sku情報に詰め替え.
     * 最新のDB登録情報に画面からの入力情報を上書きする。
     *
     * @param orderSkuModel 画面から入力された発注SKU
     * @param orderEntity 親に当たる発注情報(DB登録値)
     *
     * @return 画面の情報とAPI側で算出する情報をセットした発注SKU情報Entity
     */
    private TOrderSkuEntity setValueForOrderSkuEntity(final OrderSkuModel orderSkuModel, final TOrderEntity orderEntity) {
        // DBから最新の発注SKUを取得
        // DBに存在しない場合は空のTOrderSkuEntityを作成
        final TOrderSkuEntity orderSkuEntity = orderSkuRepository.findByIdAndDeletedAtIsNull(orderSkuModel.getId()).orElse(new TOrderSkuEntity());

        // 管理マスタを取得
        final MKanmstEntity mKanmstEntity = mKanmstComponent.getMKanmstEntity();
        // 月末締め日
        Date monthEndAt = mKanmstComponent.getMonthEndAt(mKanmstEntity.getSimymd());
        // 前月締め日
        Date previousMonthEndAt = mKanmstComponent.getPreviousMonthEndAt(mKanmstEntity.getSimymd());
        // 前々月締め日
        Date monthBeforeEndAt = mKanmstComponent.getMonthBeforeEndAt(mKanmstEntity.getSimymd());

        // 発注ID
        orderSkuEntity.setOrderId(orderEntity.getId());
        // 発注No
        orderSkuEntity.setOrderNumber(orderEntity.getOrderNumber());
        // 品番
        orderSkuEntity.setPartNo(orderEntity.getPartNo());

        // 月末日をセットする
        // 月末日（当月）
        orderSkuEntity.setMonthEndAt(monthEndAt);
        // 月末日（前月）
        orderSkuEntity.setPreviousMonthEndAt(previousMonthEndAt);
        // 月末日（前前月）
        orderSkuEntity.setMonthBeforeEndAt(monthBeforeEndAt);

        // 月末発注数量をセットする
        setValueMonthEndOrderLot(orderEntity.getProductOrderAt(), orderSkuEntity);

        return orderSkuEntity;
    }

    /**
     * 発注Noを採番する.
     * @return 発注No採番値
     */
    private BigInteger numberingOrderNumber() {
        // 発注Noの採番
        BigInteger orderNumber = numberComponent.createNumber(
                MNumberTableNameType.T_ORDER,
                MNumberColumnNameType.ORDER_NUMBER);

        // 発注Noの採番値がNULLの場合、エラー返却
        if (Objects.isNull(orderNumber)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_O_003));
        }
        return orderNumber;
    }

    /**
     * 月末発注数量（当月/前月/前々月）の値セット.
     * 該当する月の月末発注数量に製品発注数をセットする
     * 該当する月がない場合は0をセットする
     *
     * @param productOrderAt 生産発注日
     * @param orderSkuEntity 発注SKU情報
     */
    public void setValueMonthEndOrderLot(final Date productOrderAt, final TOrderSkuEntity orderSkuEntity) {
        // 初期値として0をセット
        orderSkuEntity.setMonthEndOrderLot(BigInteger.ZERO.intValue());
        orderSkuEntity.setPreviousMonthEndOrderLot(BigInteger.ZERO.intValue());
        orderSkuEntity.setMonthBeforeEndOrderLot(BigInteger.ZERO.intValue());

        final int endAtType = orderComponent.judgeEndAtTypeByProductOrderAt(productOrderAt, orderSkuEntity);
        // 該当する月とその月以降の月末発注数に発注数量をセットする。
        switch (endAtType) {
        case EndAtTypeConstants.THIS_MONTH: // 月末発注数量（当月）
            orderSkuEntity.setMonthEndOrderLot(orderSkuEntity.getProductOrderLot());
            break;
        case EndAtTypeConstants.PREVIOUS_MONTH: // 月末発注数量（前月）
            orderSkuEntity.setMonthEndOrderLot(orderSkuEntity.getProductOrderLot());            // 月末発注数量（当月）
            orderSkuEntity.setPreviousMonthEndOrderLot(orderSkuEntity.getProductOrderLot());    // 月末発注数量（前月）
            break;
        case EndAtTypeConstants.MONTH_BEFORE: // 月末発注数量（前々月）
        case EndAtTypeConstants.PAST_MONTH:   // 過去月
            orderSkuEntity.setMonthEndOrderLot(orderSkuEntity.getProductOrderLot());            // 月末発注数量（当月）
            orderSkuEntity.setPreviousMonthEndOrderLot(orderSkuEntity.getProductOrderLot());    // 月末発注数量（前月）
            orderSkuEntity.setMonthBeforeEndOrderLot(orderSkuEntity.getProductOrderLot());      // 月末発注数量（前々月）
            break;
        default:
            break;
        }
    }

    /**
     * メール送信用データ作成.
     * @param tOrderEntity ExtendedTOrderEntity
     * @param extendedTItemEntity Optional<ExtendedTItemEntity>
     * @return OrderConfirmedSendModel
     */
    private OrderConfirmedSendModel generateSendMailData(final ExtendedTOrderEntity tOrderEntity, final Optional<ExtendedTItemEntity> extendedTItemEntity) {
        OrderConfirmedSendModel orderConfirmedSendModel = new OrderConfirmedSendModel();
        BeanUtils.copyProperties(extendedTItemEntity.orElse(null), orderConfirmedSendModel);

        orderConfirmedSendModel.setMdfMakerCode(tOrderEntity.getMdfMakerCode());
        orderConfirmedSendModel.setMdfMakerName(tOrderEntity.getMdfMakerName());
        orderConfirmedSendModel.setOrderNumber(tOrderEntity.getOrderNumber());
        orderConfirmedSendModel.setQuantity(tOrderEntity.getQuantity());
        orderConfirmedSendModel.setMdfStaffCode(tOrderEntity.getMdfStaffCode());
        // ※納期は製品修正納期をセット!
        orderConfirmedSendModel.setProductDeliveryAt(tOrderEntity.getProductCorrectionDeliveryAt());
        orderConfirmedSendModel.setOrderId(tOrderEntity.getId());

        return orderConfirmedSendModel;
    }

}
