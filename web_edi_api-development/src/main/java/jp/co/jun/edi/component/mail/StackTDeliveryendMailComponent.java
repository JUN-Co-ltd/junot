package jp.co.jun.edi.component.mail;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.TDeliverySendMailEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.mail.DeliverySendMailModel;
import jp.co.jun.edi.repository.TDeliverySendMailRepository;
import jp.co.jun.edi.repository.extended.ExtendedTSkuRepository;
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
public class StackTDeliveryendMailComponent {
    @Autowired
    private TDeliverySendMailRepository tDeliverySendMailRepository;

    @Autowired
    private VelocityMailTemplateComponent<DeliverySendMailModel> velocityMailTemplateComponent;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ExtendedTSkuRepository extendedTSkuRepository;

    /**
     * 納品依頼メール送信用データ作成.
     *
     * @param deliveryModel 納品情報
     * @param tOrderEntity 発注情報
     * @param extendedTItemEntity 品番情報
     * @param customLoginUser ログインユーザ情報
     * @return データ作成に成功した場合trueを返す
     */
    public boolean saveDeliverySendMailData(final DeliveryModel deliveryModel, final ExtendedTOrderEntity tOrderEntity,
            final ExtendedTItemEntity extendedTItemEntity, final CustomLoginUser customLoginUser) {

        final TDeliverySendMailEntity tDeliverySendMailEntity = new TDeliverySendMailEntity();
        tDeliverySendMailEntity.setDeliveryId(deliveryModel.getId());
        tDeliverySendMailEntity.setStatus(SendMailStatusType.UNPROCESSED);

        // 送信先（BCC）を取得
        // JUN：製造担当
        final String[] mdfStaffBccMailAddress = mailAddressComponent.getJunMailaddressArray(tOrderEntity.getMdfStaffCode(),
                customLoginUser.getAccountName());
        // メーカー：メーカーに紐づくすべてのアカウント
        final String[] mdfMakerBccMailAddress = mailAddressComponent.getAllMakerAccountMailaddress(tOrderEntity.getMdfMakerCode());

        final String bccMailAddress = String.join(",", CollectionUtils.concatArrayToList(mdfStaffBccMailAddress, mdfMakerBccMailAddress));

        // BCC宛先が存在しない場合は、PDF生成のみ実行するステータスを設定しキューに入れる
        if (StringUtils.isEmpty(bccMailAddress)) {
            tDeliverySendMailEntity.setCreatedOnlyPdf(BooleanType.TRUE);
            tDeliverySendMailRepository.save(tDeliverySendMailEntity);
            return true;
        }

        // メールテンプレートからメール内容を生成する
        final DeliverySendMailModel sendModel = generateSendMailData(deliveryModel, tOrderEntity, extendedTItemEntity);
        final Optional<VelocityConvertedMailTemplateModel> optionalMailModel = velocityMailTemplateComponent.convert(sendModel,
                MMailCodeType.DELIVERY_APPROVED_IMMEDIATE);
        if (!optionalMailModel.isPresent()) {
            // メール内容の取得失敗の場合、falseを返す
            return false;
        }

        // メール送信情報をキューに入れる
        final VelocityConvertedMailTemplateModel mailModel = optionalMailModel.get();
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
        tDeliverySendMailEntity.setCreatedOnlyPdf(BooleanType.FALSE);
        tDeliverySendMailEntity.setFromMailAddress(commonPropertyModel.getSendMailFrom());
        tDeliverySendMailEntity.setToMailAddress(commonPropertyModel.getSendMailTo());
        tDeliverySendMailEntity.setCcMailAddress(commonPropertyModel.getSendMailCc());
        tDeliverySendMailEntity.setBccMailAddress(bccMailAddress);
        tDeliverySendMailEntity.setSubject(mailModel.getTitle());
        tDeliverySendMailEntity.setMessageBody(mailModel.getBody());

        tDeliverySendMailRepository.save(tDeliverySendMailEntity);

        return true;
    }

    /**
     * メール送信用データ作成.
     * @param deliveryModel 納品情報
     * @param tOrderEntity 発注情報
     * @param extendedTItemEntity 品番情報
     * @return OrderConfirmedSendModel
     */
    private DeliverySendMailModel generateSendMailData(final DeliveryModel deliveryModel, final ExtendedTOrderEntity tOrderEntity,
            final ExtendedTItemEntity extendedTItemEntity) {
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();

        final DeliverySendMailModel deliverySendMailModel = new DeliverySendMailModel();
        //タイトル.接頭語
        deliverySendMailModel.setSubjectPrefix(commonPropertyModel.getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        // 品番
        deliverySendMailModel.setPartNo(extendedTItemEntity.getPartNo());
        // 品名
        deliverySendMailModel.setProductName(extendedTItemEntity.getProductName());
        // 生産メーカーコード
        deliverySendMailModel.setMdfMakerCode(tOrderEntity.getMdfMakerCode());
        // 生産メーカー名
        deliverySendMailModel.setMdfMakerName(tOrderEntity.getMdfMakerName());
        // 発注No
        deliverySendMailModel.setOrderNumber(tOrderEntity.getOrderNumber());
        // 発注数
        deliverySendMailModel.setQuantity(tOrderEntity.getQuantity());
        if (!deliveryModel.getDeliveryDetails().isEmpty()) {
            // 納品日 ※納品詳細.修正納期を設定する
            deliverySendMailModel.setDeliveryAt(deliveryModel.getDeliveryDetails().get(0).getCorrectionAt());
        }
        // 納品数
        deliverySendMailModel.setDeliveryQuantity(extendedTSkuRepository.cntAllDeliveredLot(deliveryModel.getId()));
        // URL
        deliverySendMailModel.setUrl(commonPropertyModel.getJunotUrl());
        // 納品ID
        deliverySendMailModel.setDeliveryId(deliveryModel.getId());

        return deliverySendMailModel;
    }
}
