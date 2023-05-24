package jp.co.jun.edi.component.mail;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSendMailEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.model.mail.OrderRequestSendModel;
import jp.co.jun.edi.repository.TOrderSendMailRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.util.CollectionUtils;

/**
 *
 * 納品依頼メール送信用データ作成用コンポーネント.
 *
 */
@Component
public class StackTOrderRequestSendMailComponent {
    @Autowired
    private VelocityMailTemplateComponent<OrderRequestSendModel> velocityMailTemplateComponent;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TOrderSendMailRepository tOrderSendMailRepository;

    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;

    /**
     * 受注確定メール送信用データ作成.
     * @param tOrderEntity TOrderEntity
     * @param extendedTItemEntity Optional<ExtendedTItemEntity>
     * @param customLoginUser ログインユーザ情報
     */
    public void saveOrderSendMailData(final TOrderEntity tOrderEntity,
            final ExtendedTItemEntity extendedTItemEntity, final CustomLoginUser customLoginUser) {

        // 拡張発注情報を取得
        ExtendedTOrderEntity extendedTOrderEntity = extendedTOrderRepository.findById(tOrderEntity.getId()).orElse(new ExtendedTOrderEntity());

        final OrderRequestSendModel sendModel = generateOrderRequestSendMailData(extendedTOrderEntity, extendedTItemEntity);

        final TOrderSendMailEntity tOrderSendMailEntity = new TOrderSendMailEntity();
        tOrderSendMailEntity.setOrderId(tOrderEntity.getId());
        tOrderSendMailEntity.setStatus(SendMailStatusType.UNPROCESSED);

        // 送信先（BCC）を取得
        // JUN：製造担当
        final String[] mdfStaffBccMailAddress = mailAddressComponent.getJunMailaddressArray(tOrderEntity.getMdfStaffCode(),
                customLoginUser.getAccountName());
        // メーカー：メーカーに紐づくすべてのアカウント
        final String[] mdfMakerBccMailAddress = mailAddressComponent.getAllMakerAccountMailaddress(tOrderEntity.getMdfMakerCode());

        final String bccMailAddress = String.join(",", CollectionUtils.concatArrayToList(mdfStaffBccMailAddress, mdfMakerBccMailAddress));

        // BCC宛先が存在しない場合は、PDF生成のみ実行するステータスをキューに入れる
        if (StringUtils.isEmpty(bccMailAddress)) {
            tOrderSendMailEntity.setCreatedOnlyPdf(BooleanType.TRUE);
        } else {
            final Optional<VelocityConvertedMailTemplateModel> optionalMailModel = velocityMailTemplateComponent.convert(sendModel,
                    MMailCodeType.ORDER_CONFIRMED_IMMEDIATE);
            if (optionalMailModel.isPresent()) {
                final VelocityConvertedMailTemplateModel mailModel = optionalMailModel.get();
                tOrderSendMailEntity.setCreatedOnlyPdf(BooleanType.FALSE);
                tOrderSendMailEntity.setFromMailAddress(propertyComponent.getCommonProperty().getSendMailFrom());
                tOrderSendMailEntity.setToMailAddress(propertyComponent.getCommonProperty().getSendMailTo());
                tOrderSendMailEntity.setCcMailAddress(propertyComponent.getCommonProperty().getSendMailCc());
                tOrderSendMailEntity.setBccMailAddress(bccMailAddress);
                tOrderSendMailEntity.setSubject(mailModel.getTitle());
                tOrderSendMailEntity.setMessageBody(mailModel.getBody());
            }
        }
        tOrderSendMailRepository.save(tOrderSendMailEntity);
    }

    /**
     * 受注確定メール送信用データ作成.
     * @param tOrderEntity TOrderEntity
     * @param extendedTItemEntity Optional<ExtendedTItemEntity>
     * @return OrderConfirmedSendModel
     */
    private OrderRequestSendModel generateOrderRequestSendMailData(final ExtendedTOrderEntity tOrderEntity, final ExtendedTItemEntity extendedTItemEntity) {
        final OrderRequestSendModel orderRequestSendModel = new OrderRequestSendModel();
        BeanUtils.copyProperties(extendedTItemEntity, orderRequestSendModel);

        orderRequestSendModel.setMdfMakerCode(tOrderEntity.getMdfMakerCode());
        orderRequestSendModel.setMdfMakerName(tOrderEntity.getMdfMakerName());
        orderRequestSendModel.setMdfStaffCode(tOrderEntity.getMdfStaffCode());

        orderRequestSendModel.setOrderNumber(tOrderEntity.getOrderNumber());
        orderRequestSendModel.setQuantity(tOrderEntity.getQuantity());
        // ※納期は製品修正納期をセット!
        orderRequestSendModel.setProductDeliveryAt(tOrderEntity.getProductCorrectionDeliveryAt());
        orderRequestSendModel.setOrderId(tOrderEntity.getId());
        orderRequestSendModel.setUrl(propertyComponent.getCommonProperty().getJunotUrl());
        orderRequestSendModel.setSubjectPrefix(propertyComponent.getCommonProperty().getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        return orderRequestSendModel;
    }

}
