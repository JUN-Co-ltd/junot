package jp.co.jun.edi.component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.mail.MailSenderAttachmentComponent;
import jp.co.jun.edi.component.mail.VelocityMailTemplateComponent;
import jp.co.jun.edi.component.model.MailAttachementFileModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.component.schedule.MaterialOrderLinkingComponent;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderLinkingEntity;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.model.FukukitaruLinkingMailInfoModel;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * フクキタル連携CSVファイルメール送信コンポーネント.
 */
@Component
@Slf4j
public class FukukitaruLinkingMailCsvFileComponent {
    @Autowired
    private MaterialOrderLinkingComponent linkingGetOrderInfoComponent;

    @Autowired
    private MailSenderAttachmentComponent mailSenderAttachmentComponent;

    @Autowired
    private VelocityMailTemplateComponent<FukukitaruLinkingMailInfoModel> velocityMailTemplateComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * CSVをメール送信する.
     * @param linkingOrderInfoEntity フクキタル発注情報
     * @param attachementFile 添付ファイル
     */
    public void mailCsvFile(final ExtendedTFOrderLinkingEntity linkingOrderInfoEntity, final List<File> attachementFile) {

        // メール本文情報取得
        final TItemEntity tItemEntity = linkingGetOrderInfoComponent.getTItemEntity(linkingOrderInfoEntity.getPartNoId());
        final String sire = linkingGetOrderInfoComponent.getSire(linkingOrderInfoEntity.getOrderId());
        final FukukitaruLinkingMailInfoModel sendModel = new FukukitaruLinkingMailInfoModel();
        sendModel.setOrderCode(linkingOrderInfoEntity.getOrderCode());
        sendModel.setPartNo(BusinessUtils.formatPartNo(tItemEntity.getPartNo()));
        sendModel.setSire(sire);
        sendModel.setProductName(tItemEntity.getProductName());

        // テンプレート生成
        final VelocityConvertedMailTemplateModel optionalMailModel = velocityMailTemplateComponent.convert(sendModel, MMailCodeType.ORDER_FUKUKITARU)
                .orElseThrow(() -> new ScheduleException(
                        (LogStringUtil.of("mailCsvFile").message("velocity error.").value("FukukitaruLinkingMailInfoModel", sendModel).build())));

        // メール送信情報
        final String fromMailAddress = propertyComponent.getBatchProperty().getMaterialOrderLinking().getMailFrom();
        final String toMailAddress = propertyComponent.getBatchProperty().getMaterialOrderLinking().getMailTo();
        final String ccMailAddress = propertyComponent.getBatchProperty().getMaterialOrderLinking().getMailCc();
        final String bccMailAddress = propertyComponent.getBatchProperty().getMaterialOrderLinking().getMailBcc();
        final String subject = optionalMailModel.getTitle();
        final String messageBody = optionalMailModel.getBody();
        final List<MailAttachementFileModel> attachementFiles = attachementFile.stream().map(file -> {
            final MailAttachementFileModel model = new MailAttachementFileModel();
            model.setFile(file);
            model.setFileName(file.getName());
            return model;
        }).collect(Collectors.toList());

        // 送信フラグがTRUEの場合、メールを送信する
        if (propertyComponent.getBatchProperty().getMaterialOrderLinking().isMailSend()) {
            final SendMailType sendType = mailSenderAttachmentComponent.sendMail(fromMailAddress, toMailAddress, ccMailAddress, bccMailAddress, subject,
                    messageBody, attachementFiles);
            if (sendType == SendMailType.MAIL_SENDING_ERROR) {
                // メール送信エラー
                throw new ScheduleException((LogStringUtil.of("mailCsvFile").message("mail send error.").value("fromMailAddress", fromMailAddress)
                        .value("toMailAddress", toMailAddress).value("ccMailAddress", ccMailAddress).value("bccMailAddress", bccMailAddress)
                        .value("subject", subject).value("messageBody", messageBody).build()));
            }
            return;
        }

        // メール送信が停止されている警告をログに出力する
        log.warn(LogStringUtil.of("mailCsvFile").message("It is set not to send mail.").value("fromMailAddress", fromMailAddress)
                .value("toMailAddress", toMailAddress).value("ccMailAddress", ccMailAddress).value("bccMailAddress", bccMailAddress).value("subject", subject)
                .value("messageBody", messageBody).build());
    }
}
