package jp.co.jun.edi.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.MCodmstComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.mail.OrderApprovedSendMailComponent;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.mail.OrderApprovedSendModel;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderApprovalType;

/**
 * 発注承認処理.
 */
@Service
public class OrderApproveService extends GenericUpdateService<ApprovalServiceParameter<OrderModel>, ApprovalServiceResponse> {

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private OrderApprovedSendMailComponent orderApprovedSendMailComponent;

    @Autowired
    private MCodmstComponent mCodmstComponent;

    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;

    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<OrderModel> serviceParameter) {
        final OrderModel orderModel = serviceParameter.getItem();

        // 発注存在チェック。取得できない(削除済み)場合は業務エラー
        final TOrderEntity orderEntity = orderRepository.findByOrderId(orderModel.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 品番情報取得。取得できない(削除済み)場合は業務エラー
        final TItemEntity tItemEntity = itemRepository.findById(orderEntity.getPartNoId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 外部連携区分:JUNoT登録以外の場合承認不可
        itemComponent.validateReadOnly(tItemEntity.getExternalLinkingType());

        // 読み取り専用の場合、更新不可
        orderComponent.validateReadOnly(orderEntity.getExpenseItem());

        final CustomLoginUser loginUser = serviceParameter.getLoginUser();

        // ログインユーザの承認権限を取得
        final List<String> orderApprovalAuthorityBlands = mCodmstComponent
                .getOrderApprovalAuthorityBlands(loginUser.getAccountName());

        // ログインユーザによる承認権限チェック。権限がない場合は業務エラー
        if (!(orderComponent.canUserApproved(orderApprovalAuthorityBlands, tItemEntity.getBrandCode()))) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_005));
        }

        // 発注承認ステータスチェック。発注が未確定の場合は業務エラー
        if (!(orderComponent.canOrderApproved(orderEntity))) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_O_009));
        }

        // 発注SKUの更新
        // 裁断数を発注数で更新
        orderSkuRepository.updateproductCutLot(orderModel.getId(), loginUser.getUserId());

        // 発注情報の更新項目セット、更新
        final TOrderEntity confirmOrderEntity = setValueForOrderEntity(loginUser, orderEntity);
        orderRepository.save(confirmOrderEntity);

        // 他テーブルの更新
        orderComponent.updateOtherTable(confirmOrderEntity, tItemEntity, loginUser);

        // メール送信情報用データ取得。
        final ExtendedTOrderEntity extendedTOrderEntity = extendedTOrderRepository.findById(confirmOrderEntity.getId()).orElse(new ExtendedTOrderEntity());
        final ExtendedTItemEntity extendedTItemEntity = extendedTItemRepository.findById(confirmOrderEntity.getPartNoId()).orElse(new ExtendedTItemEntity());

        // 発注承認メール送信
        final OrderApprovedSendModel sendModel = generateSendMailData(extendedTOrderEntity, extendedTItemEntity);
        orderApprovedSendMailComponent.sendMail(sendModel, loginUser.getAccountName());

        // 発注書発行処理(日中と夜間)
        orderComponent.printOrder(orderEntity);

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * 発注情報の項目再セット.
     *
     * @param loginUser ログインユーザ情報
     * @param tOrderEntity 最新の発注情報(DB登録値)
     * @return API側で再セットした発注情報Entity
     */
    private TOrderEntity setValueForOrderEntity(final CustomLoginUser loginUser, final TOrderEntity tOrderEntity) {
        // 承認ステータスをセット
        tOrderEntity.setOrderApproveStatus(OrderApprovalType.APPROVED.getValue());
        // 連携ステータスをセット
        tOrderEntity.setLinkingStatus(LinkingStatusType.LINKING);
        // 発注承認日をセット
        final Date confirmAt = new Date();
        tOrderEntity.setOrderApproveAt(confirmAt);
        // 発注承認者をセット
        tOrderEntity.setOrderApproveUserId(loginUser.getUserId());
        // 連携対象に更新
        orderComponent.setLinkingTarget(tOrderEntity);

        return tOrderEntity;
    }

    /**
     * メール送信用データ作成.
     * @param extendedTOrderEntity DB最新の発注情報
     * @param extendedTItemEntity DB最新の品番情報
     * @return OrderApprovedSendModel
     */
    private OrderApprovedSendModel generateSendMailData(final ExtendedTOrderEntity extendedTOrderEntity, final ExtendedTItemEntity extendedTItemEntity) {
        final OrderApprovedSendModel orderApprovedSendModel = new OrderApprovedSendModel();

        // 品番
        orderApprovedSendModel.setPartNo(extendedTItemEntity.getPartNo());
        // 品名
        orderApprovedSendModel.setProductName(extendedTItemEntity.getProductName());
        // 生産メーカーコード
        orderApprovedSendModel.setMdfMakerCode(extendedTOrderEntity.getMdfMakerCode());
        // 生産メーカー名
        orderApprovedSendModel.setMdfMakerName(extendedTOrderEntity.getMdfMakerName());
        // 発注No
        orderApprovedSendModel.setOrderNumber(extendedTOrderEntity.getOrderNumber());
        // 発注数
        orderApprovedSendModel.setQuantity(extendedTOrderEntity.getQuantity());
        // 納期(修正納期)
        orderApprovedSendModel.setProductDeliveryAt(extendedTOrderEntity.getProductCorrectionDeliveryAt());
        // 発注ID
        orderApprovedSendModel.setOrderId(extendedTOrderEntity.getId());
        // URL
        orderApprovedSendModel.setUrl(propertyComponent.getCommonProperty().getJunotUrl());
        // 件名接頭辞
        orderApprovedSendModel.setSubjectPrefix(propertyComponent.getCommonProperty().getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        // 企画担当
        orderApprovedSendModel.setPlannerCode(extendedTItemEntity.getPlannerCode());
        // 製造担当
        orderApprovedSendModel.setMdfStaffCode(extendedTOrderEntity.getMdfStaffCode());
        // パターンナー
        orderApprovedSendModel.setPlannerCode(extendedTItemEntity.getPlannerCode());
        // 生産メーカー担当：生産メーカー担当宛にはメールを送信しないため、値はセットしない。

        return orderApprovedSendModel;
    }
}
