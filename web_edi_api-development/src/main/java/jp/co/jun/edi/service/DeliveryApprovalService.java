package jp.co.jun.edi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.MDeliveryCountNumberComponent;
import jp.co.jun.edi.component.MNumberComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.mail.DeliveryRequestApprovedSendMailComponent;
import jp.co.jun.edi.component.mail.StackTDeliveryOfficialSendMailComponent;
import jp.co.jun.edi.component.mail.StackTDeliveryendMailComponent;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.extended.ExtendedSendMailDeliveryAtEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.mail.DeliveryRequestApprovedSendModel;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedSendMailDeliveryAtRepository;
import jp.co.jun.edi.repository.extended.ExtendedTSkuRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.DivisionCodeType;
import jp.co.jun.edi.type.MNumberColumnNameType;
import jp.co.jun.edi.type.MNumberTableNameType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 納品承認処理.
 */
@Service
public class DeliveryApprovalService
        extends GenericUpdateService<ApprovalServiceParameter<DeliveryModel>, ApprovalServiceResponse> {

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private DeliveryRequestApprovedSendMailComponent deliveryRequestApprovedSendMailComponent;

    @Autowired
    private MNumberComponent numberComponent;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private MDeliveryCountNumberComponent deliveryCountNumberComponent;

    @Autowired
    private ExtendedTSkuRepository extendedTSkuRepository;

    @Autowired
    private ExtendedSendMailDeliveryAtRepository extendedSendMailDeliveryAtRepository;

    @Autowired
    private StackTDeliveryOfficialSendMailComponent stackTDeliveryOfficialSendMailComponent;

    @Autowired
    private StackTDeliveryendMailComponent stackTDeliveryendMailComponent;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<DeliveryModel> serviceParameter) {
        final DeliveryModel deliveryModel = serviceParameter.getItem();
        final BigInteger deliveryId = deliveryModel.getId();
        final BigInteger userId = serviceParameter.getLoginUser().getUserId();

        // DBの最新の納品依頼情報を取得。存在しない場合はエラー
        final TDeliveryEntity currentDeliveryEntity = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_010)));

        // バリデーションチェック。NGの場合は業務エラー
        checkApprovable(currentDeliveryEntity);

        // 品番情報取得。存在しない場合はエラー
        final ExtendedTItemEntity extendedTItem = itemComponent.getExtendedTItem(deliveryModel.getPartNoId());

        // 発注情報取得。存在しない場合はエラー
        final ExtendedTOrderEntity extendedTOrder = orderComponent.getExtendedTOrder(deliveryModel.getOrderId());

        // 納品依頼と納品明細情報の更新
        final Integer currentDeliveryCount = updateDeliveryAndDeliveryDetail(currentDeliveryEntity, userId, serviceParameter.getLoginUser());

        // 発注情報の納品依頼回数更新
        orderRepository.updateDeliveryCount(currentDeliveryCount, userId, deliveryModel.getOrderId());

        // B級品単価に入力がある場合は発注情報更新
        final BigDecimal nonConformingProductUnitPrice = currentDeliveryEntity.getNonConformingProductUnitPrice();
        if (nonConformingProductUnitPrice != null
                && extendedTOrder.getNonConformingProductUnitPrice() == null) {
            orderRepository.updateNonConformingProductUnitPrice(extendedTOrder.getId(), nonConformingProductUnitPrice, userId);
        }

        // 発注SKUの納品数量加算
        orderSkuRepository.addDeliveryLotByDeliveryApprove(deliveryId, userId);

        // メール送信
        final DeliveryRequestApprovedSendModel sendModel = generateSendMailData(extendedTItem, extendedTOrder, currentDeliveryEntity);
        deliveryRequestApprovedSendMailComponent.sendMail(sendModel, serviceParameter.getLoginUser().getAccountName());

        // 納品依頼メール送信管理に情報を登録
        stackTDeliveryendMailComponent.saveDeliverySendMailData(deliveryModel, extendedTOrder, extendedTItem, serviceParameter.getLoginUser());
        // 納品依頼正式メール送信管理に情報を登録
        stackTDeliveryOfficialSendMailComponent.saveDeliveryOfficialSendMailData(deliveryModel, extendedTOrder, extendedTItem,
                serviceParameter.getLoginUser());

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * 納品依頼情報と納品明細情報を更新する.
     * @param currentDeliveryEntity 最新の発注情報(DB登録値)
     * @param userId ユーザID
     * @param junpcTanto 発注生産システム側で管理しているログインユーザのID
     * @return 納品依頼回数
     */
    private Integer updateDeliveryAndDeliveryDetail(
            final TDeliveryEntity currentDeliveryEntity,
            final BigInteger userId,
            final CustomLoginUser junpcTanto) {

        // DBに登録されている納品依頼及び子テーブルのデータ取得
        // (リクエストにない納品明細データも承認する為)
        final DeliveryModel dbDeliveryModel = deliveryComponent.findDeliveryById(currentDeliveryEntity.getId());

        // 明細情報を、縫製検品、本社撮影先頭、その他は場所コード昇順でソート
        final List<DeliveryDetailModel> deliveryDetails = deliveryComponent.sortDeliveryDetailForUpsert(dbDeliveryModel);

        final BigInteger orderId = dbDeliveryModel.getOrderId();

        // 納品依頼回数を採番
        final Integer currentDeliveryCount = deliveryCountNumberComponent.numberDeliveryCount(orderId);

        // 承認処理
        final TDeliveryEntity approveDeliveryEntity = generateDeliveryForApproval(currentDeliveryEntity, currentDeliveryCount);
        deliveryRepository.save(approveDeliveryEntity);

        // 納品Noの採番
        final BigInteger deliveryNumberCount = numberComponent.createNumber(
                MNumberTableNameType.T_DELIVERY_DETAIL,
                MNumberColumnNameType.DELIVERY_NUMBER);

        final Date today = DateUtils.createNow();

        for (final DeliveryDetailModel deliveryDetail : deliveryDetails) {
            // 納品依頼Noの採番(課ごとに採番＝納品明細1レコードごと)
            final String deliveryRequestNumber = String.format("%06d", deliveryComponent.numberingDeliveryRequestNumber());

            // 納品明細情報の採番系データ更新
            deliveryDetailRepository.updateNumberingById(
                    String.format("%06d", deliveryNumberCount), // 納品No
                    today, // 納品依頼日
                    currentDeliveryCount, // 納品依頼回数
                    deliveryRequestNumber, // 納品依頼No
                    loginUserComponent.getAccountNameWithAffiliation(junpcTanto), // 連携入力者
                    userId, deliveryDetail.getId());

            // 納品SKUの納品依頼No更新
            deliverySkuRepository.updateDeliveryRequestNumberByDeliveryDetailId(deliveryRequestNumber, userId, deliveryDetail.getId());
        }

        return currentDeliveryCount;
    }

    /**
     * 承認用の納品依頼情報を作成する.
     *
     * @param deliveryEntity 最新の納品依頼情報(DB登録値)
     * @param deliveryCount 採番された納品依頼回数
     * @return APIで再セットされた納品依頼情報Entity
     */
    private TDeliveryEntity generateDeliveryForApproval(final TDeliveryEntity deliveryEntity, final int deliveryCount) {
        // 納品依頼回数をセット
        deliveryEntity.setDeliveryCount(deliveryCount);
        // 承認ステータスをセット
        deliveryEntity.setDeliveryApproveStatus(ApprovalType.APPROVAL.getValue());
        // 承認日をセット
        deliveryEntity.setDeliveryApproveAt(new Date());

        return deliveryEntity;
    }

    /**
     * メール送信用データ作成.
     * @param extendedTItem DB最新の品番情報
     * @param extendedTOrder DB最新の発注情報
     * @param tDeliveryEntity TDeliveryEntity
     * @return DeliveryRequestApprovedSendModel
     */
    private DeliveryRequestApprovedSendModel generateSendMailData(final ExtendedTItemEntity extendedTItem, final ExtendedTOrderEntity extendedTOrder,
            final TDeliveryEntity tDeliveryEntity) {
        final DeliveryRequestApprovedSendModel deliveryRequestApprovedModel = new DeliveryRequestApprovedSendModel();
        // 品番情報をセット
        BeanUtils.copyProperties(extendedTItem, deliveryRequestApprovedModel);

        // 発注情報をセット
        deliveryRequestApprovedModel.setMdfMakerCode(extendedTOrder.getMdfMakerCode());
        deliveryRequestApprovedModel.setMdfMakerName(extendedTOrder.getMdfMakerName());
        deliveryRequestApprovedModel.setMdfStaffCode(extendedTOrder.getMdfStaffCode());
        deliveryRequestApprovedModel.setOrderNumber(extendedTOrder.getOrderNumber());
        deliveryRequestApprovedModel.setQuantity(extendedTOrder.getQuantity());
        deliveryRequestApprovedModel.setProductDeliveryAt(extendedTOrder.getProductCorrectionDeliveryAt()); // 製品修正納期を使用

        // 納期3種を取得
        final ExtendedSendMailDeliveryAtEntity sendDeliveryAtEntity = extendedSendMailDeliveryAtRepository.findDeliveryAt(tDeliveryEntity.getId(),
                DivisionCodeType.PHOTO.getValue(), DivisionCodeType.SEWING.getValue()).orElse(null);
        deliveryRequestApprovedModel.setPhotoDeliveryAt(sendDeliveryAtEntity.getPhotoDeliveryAt());
        deliveryRequestApprovedModel.setSewingDeliveryAt(sendDeliveryAtEntity.getSewingDeliveryAt());
        deliveryRequestApprovedModel.setDeliveryAt(sendDeliveryAtEntity.getDeliveryAt());

        // 納品数量を取得
        final BigDecimal allDeliveredLot = extendedTSkuRepository.cntAllDeliveredLot(tDeliveryEntity.getId());
        deliveryRequestApprovedModel.setAllDeliveredLot(allDeliveredLot);
        return deliveryRequestApprovedModel;
    }

    /**
     * 承認時のバリデーションチェック.
     * 既に承認済の場合業務エラーをスロー.
     * @param tDelivery 最新のDBの納品依頼情報
     */
    private void checkApprovable(final TDeliveryEntity tDelivery) {
        if (deliveryComponent.isDeliveryApproved(tDelivery.getDeliveryApproveStatus())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_004));
        }
    }

}
