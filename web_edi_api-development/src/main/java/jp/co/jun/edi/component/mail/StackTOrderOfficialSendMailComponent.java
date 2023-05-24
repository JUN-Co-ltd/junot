package jp.co.jun.edi.component.mail;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.TOrderApprovalOfficialSendMailEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.model.mail.OrderOfficialSendMailModel;
import jp.co.jun.edi.repository.TOrderApprovalOfficialSendMailRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailStatusType;

/**
 *
 * 発注書PDF発行[夜間正式番]・メール送信用データ作成用コンポーネント.
 *
 */
@Component
public class StackTOrderOfficialSendMailComponent {
    @Autowired
    private TOrderApprovalOfficialSendMailRepository tOrderApprovalOfficialSendMailRepository;

    @Autowired
    private VelocityMailTemplateComponent<OrderOfficialSendMailModel> velocityMailTemplateComponent;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * 発注書PDF発行[夜間正式番]・メール送信用データ作成.
     * @param extendedTOrderEntity 拡張発注情報
     * @param extendedTItemEntity 拡張品番情報
     * @param customLoginUser ログインユーザ情報
     * @return データ作成に成功した場合trueを返す
     */
    public boolean saveOrderOfficialSendMailData(
            final ExtendedTOrderEntity extendedTOrderEntity,
            final ExtendedTItemEntity extendedTItemEntity,
            final CustomLoginUser customLoginUser) {

        final TOrderApprovalOfficialSendMailEntity tOrderApprovalOfficialSendMailEntity = tOrderApprovalOfficialSendMailRepository
                .findByOrderId(extendedTOrderEntity.getId()).orElse(new TOrderApprovalOfficialSendMailEntity());

        // 初期化
        tOrderApprovalOfficialSendMailEntity.setOrderId(extendedTOrderEntity.getId());
        tOrderApprovalOfficialSendMailEntity.setMdfMakerCode(extendedTOrderEntity.getMdfMakerCode());
        tOrderApprovalOfficialSendMailEntity
                .setMdfMakerFactoryCode(Optional.ofNullable(extendedTOrderEntity.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY));
        tOrderApprovalOfficialSendMailEntity.setStatus(SendMailStatusType.UNPROCESSED);

        tOrderApprovalOfficialSendMailEntity.setCreatedOnlyPdf(null);
        tOrderApprovalOfficialSendMailEntity.setFromMailAddress(null);
        tOrderApprovalOfficialSendMailEntity.setToMailAddress(null);
        tOrderApprovalOfficialSendMailEntity.setCcMailAddress(null);
        tOrderApprovalOfficialSendMailEntity.setBccMailAddress(null);
        tOrderApprovalOfficialSendMailEntity.setSubject(null);
        tOrderApprovalOfficialSendMailEntity.setMessageBody(null);

        // TODO (内部課題No113対応完了までの仮仕様）
        // 「m_kojmst.kojcd」と「t_item.mdf_maker_factory_code」を突合する場合は、
        // 「t_item.mdf_maker_factory_code」がNULLの場合、空文字に変換する
        final String bccMailAddress = mailAddressComponent.getOrderMdfMakerFactoryMailaddress(extendedTOrderEntity.getMdfMakerCode(),
                Optional.ofNullable(extendedTOrderEntity.getMdfMakerFactoryCode())
                        .orElse(StringUtils.EMPTY));
        // BCC宛先が存在しない場合は、PDF生成のみ実行するステータスを設定しキューに入れる
        if (StringUtils.isEmpty(bccMailAddress)) {
            tOrderApprovalOfficialSendMailEntity.setCreatedOnlyPdf(BooleanType.TRUE);
            tOrderApprovalOfficialSendMailRepository.save(tOrderApprovalOfficialSendMailEntity);
            return true;
        }

        // メールテンプレートからメール内容を生成する
        final OrderOfficialSendMailModel sendModel = generateSendMailData();
        final Optional<VelocityConvertedMailTemplateModel> optionalMailModel = velocityMailTemplateComponent.convert(sendModel,
                MMailCodeType.PURCHASE_ORDER_OFFICIAL);
        if (!optionalMailModel.isPresent()) {
            // メール内容の取得失敗の場合、falseを返す
            return false;
        }

        // メール送信情報をキューに入れる
        final VelocityConvertedMailTemplateModel mailModel = optionalMailModel.get();
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
        tOrderApprovalOfficialSendMailEntity.setCreatedOnlyPdf(BooleanType.FALSE);
        tOrderApprovalOfficialSendMailEntity.setFromMailAddress(commonPropertyModel.getSendMailFrom());
        tOrderApprovalOfficialSendMailEntity.setToMailAddress(commonPropertyModel.getSendMailTo());
        tOrderApprovalOfficialSendMailEntity.setCcMailAddress(commonPropertyModel.getSendMailCc());
        tOrderApprovalOfficialSendMailEntity.setBccMailAddress(bccMailAddress);
        tOrderApprovalOfficialSendMailEntity.setSubject(mailModel.getTitle());
        tOrderApprovalOfficialSendMailEntity.setMessageBody(mailModel.getBody());

        tOrderApprovalOfficialSendMailRepository.save(tOrderApprovalOfficialSendMailEntity);

        return true;
    }

    /**
     * メール送信用データ作成.
     * @return DeliveryOfficialSendMailModel
     */
    private OrderOfficialSendMailModel generateSendMailData() {
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();

        final OrderOfficialSendMailModel orderOfficialSendMailModel = new OrderOfficialSendMailModel();
        //タイトル.接頭語
        orderOfficialSendMailModel.setSubjectPrefix(commonPropertyModel.getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        // 署名
        orderOfficialSendMailModel.setSignature(commonPropertyModel.getSendMailSignature());

        return orderOfficialSendMailModel;
    }
}
