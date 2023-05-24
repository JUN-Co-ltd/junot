package jp.co.jun.edi.component.mail;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.TDeliveryOfficialSendMailEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.mail.DeliveryOfficialSendMailModel;
import jp.co.jun.edi.repository.TDeliveryOfficialSendMailRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailStatusType;

/**
 *
 * 納品依頼正式メール送信用データ作成用コンポーネント.
 *
 */
@Component
public class StackTDeliveryOfficialSendMailComponent {
    @Autowired
    private TDeliveryOfficialSendMailRepository tDeliveryOfficialSendMailRepository;

    @Autowired
    private VelocityMailTemplateComponent<DeliveryOfficialSendMailModel> velocityMailTemplateComponent;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * 納品依頼正式メール送信用データ作成.
     * @param deliveryModel DeliveryModel
     * @param tOrderEntity TOrderEntity
     * @param extendedTItemEntity Optional<ExtendedTItemEntity>
     * @param customLoginUser ログインユーザ情報
     * @return データ作成に成功した場合trueを返す
     */
    public boolean saveDeliveryOfficialSendMailData(
            final DeliveryModel deliveryModel,
            final ExtendedTOrderEntity tOrderEntity,
            final ExtendedTItemEntity extendedTItemEntity,
            final CustomLoginUser customLoginUser) {

        final TDeliveryOfficialSendMailEntity tDeliveryOfficialSendMailEntity = tDeliveryOfficialSendMailRepository.findByDeliveryId(deliveryModel.getId())
                .orElse(new TDeliveryOfficialSendMailEntity());

        // 初期化
        tDeliveryOfficialSendMailEntity.setDeliveryId(deliveryModel.getId());
        tDeliveryOfficialSendMailEntity.setMdfMakerCode(tOrderEntity.getMdfMakerCode());
        tDeliveryOfficialSendMailEntity.setMdfMakerFactoryCode(Optional.ofNullable(tOrderEntity.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY));
        tDeliveryOfficialSendMailEntity.setStatus(SendMailStatusType.UNPROCESSED);

        tDeliveryOfficialSendMailEntity.setCreatedOnlyPdf(null);
        tDeliveryOfficialSendMailEntity.setFromMailAddress(null);
        tDeliveryOfficialSendMailEntity.setToMailAddress(null);
        tDeliveryOfficialSendMailEntity.setCcMailAddress(null);
        tDeliveryOfficialSendMailEntity.setBccMailAddress(null);
        tDeliveryOfficialSendMailEntity.setSubject(null);
        tDeliveryOfficialSendMailEntity.setMessageBody(null);

        // TODO (内部課題No113対応完了までの仮仕様）
        // 「m_kojmst.kojcd」と「t_item.mdf_maker_factory_code」を突合する場合は、
        // 「t_item.mdf_maker_factory_code」がNULLの場合、空文字に変換する
        final String bccMailAddress = mailAddressComponent.getDeliveryMdfMakerFactoryMailaddress(deliveryModel.getId(), tOrderEntity.getMdfMakerCode(),
                Optional.ofNullable(tOrderEntity.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY));
        // BCC宛先が存在しない場合は、PDF生成のみ実行するステータスを設定しキューに入れる
        if (StringUtils.isEmpty(bccMailAddress)) {
            tDeliveryOfficialSendMailEntity.setCreatedOnlyPdf(BooleanType.TRUE);
            tDeliveryOfficialSendMailRepository.save(tDeliveryOfficialSendMailEntity);
            return true;
        }

        // メールテンプレートからメール内容を生成する
        final DeliveryOfficialSendMailModel sendModel = generateSendMailData(deliveryModel, tOrderEntity, extendedTItemEntity);
        final Optional<VelocityConvertedMailTemplateModel> optionalMailModel = velocityMailTemplateComponent.convert(sendModel,
                MMailCodeType.DELIVERY_APPROVED_OFFICIAL);
        if (!optionalMailModel.isPresent()) {
            // メール内容の取得失敗の場合、falseを返す
            return false;
        }

        // メール送信情報をキューに入れる
        final VelocityConvertedMailTemplateModel mailModel = optionalMailModel.get();
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
        tDeliveryOfficialSendMailEntity.setCreatedOnlyPdf(BooleanType.FALSE);
        tDeliveryOfficialSendMailEntity.setFromMailAddress(commonPropertyModel.getSendMailFrom());
        tDeliveryOfficialSendMailEntity.setToMailAddress(commonPropertyModel.getSendMailTo());
        tDeliveryOfficialSendMailEntity.setCcMailAddress(commonPropertyModel.getSendMailCc());
        tDeliveryOfficialSendMailEntity.setBccMailAddress(bccMailAddress);
        tDeliveryOfficialSendMailEntity.setSubject(mailModel.getTitle());
        tDeliveryOfficialSendMailEntity.setMessageBody(mailModel.getBody());

        tDeliveryOfficialSendMailRepository.save(tDeliveryOfficialSendMailEntity);

        return true;
    }

    /**
     * メール送信用データ作成.
     * @param deliveryModel 納品情報
     * @param tOrderEntity 発注情報
     * @param extendedTItemEntity 品番情報
     * @return DeliveryOfficialSendMailModel
     */
    private DeliveryOfficialSendMailModel generateSendMailData(final DeliveryModel deliveryModel, final ExtendedTOrderEntity tOrderEntity,
            final ExtendedTItemEntity extendedTItemEntity) {
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();

        final DeliveryOfficialSendMailModel deliveryOfficialSendMailModel = new DeliveryOfficialSendMailModel();
        //タイトル.接頭語
        deliveryOfficialSendMailModel.setSubjectPrefix(commonPropertyModel.getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        // 署名
        deliveryOfficialSendMailModel.setSignature(commonPropertyModel.getSendMailSignature());

        return deliveryOfficialSendMailModel;
    }
}
