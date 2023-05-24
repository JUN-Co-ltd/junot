package jp.co.jun.edi.component.mail;

import java.nio.file.Files;
import java.util.List;

import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.MailAttachementFileModel;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 添付ファイルありメール送信コンポーネント.
 */
@Component
@Slf4j
public class MailSenderAttachmentComponent {
    private static final String COMMA = ",";
    private static final String ATTACHEMENT_FILE_MIME_TYPE = "application/octet-stream";
    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * メール送信を行う.
     * @param fromMailAddress 送信元メールアドレス
     * @param toMailAddress 送信先メールアドレス
     * @param ccMailAddress 送信先CCメールアドレス
     * @param bccMailAddress 送信先BCCメールアドレス
     * @param subject 件名
     * @param messageBody 本文
     * @param attachementFiles 添付ファイル
     * @return SendMailType メール送信結果
     */
    public SendMailType sendMail(final String fromMailAddress,
            final String toMailAddress,
            final String ccMailAddress,
            final String bccMailAddress,
            final String subject,
            final String messageBody,
            final List<MailAttachementFileModel> attachementFiles) {
        final String[] arrayToMailAddress = StringUtils.split(toMailAddress, COMMA);
        final String[] arrayCcMailAddress = StringUtils.split(ccMailAddress, COMMA);
        final String[] arrayBccMailAddress = StringUtils.split(bccMailAddress, COMMA);

        return sendMail(fromMailAddress, arrayToMailAddress, arrayCcMailAddress, arrayBccMailAddress, subject, messageBody, attachementFiles);

    }
    //PRD_0187 JFE add start
    /**
     * メール送信を行う.
     * @param fromMailAddress 送信元メールアドレス
     * @param toMailAddress 送信先メールアドレス
     * @param ccMailAddress 送信先CCメールアドレス
     * @param bccMailAddress 送信先BCCメールアドレス
     * @param subject 件名
     * @param messageBody 本文
     * @param attachementFiles 添付ファイル
     * @return SendMailType メール送信結果
     */
    public SendMailType sendPurchaseVoucherMail(final String fromMailAddress,
            final String toMailAddress,
            final String ccMailAddress,
            final String bccMailAddress,
            final String subject,
            final String messageBody,
            final String signature,
            final List<MailAttachementFileModel> attachementFiles) {
        final String[] arrayToMailAddress = StringUtils.split(toMailAddress, COMMA);
        final String[] arrayCcMailAddress = StringUtils.split(ccMailAddress, COMMA);
        final String[] arrayBccMailAddress = StringUtils.split(bccMailAddress, COMMA);

        return sendPurchaseVoucherMail(fromMailAddress, arrayToMailAddress, arrayCcMailAddress, arrayBccMailAddress, subject, messageBody,signature, attachementFiles);

    }
    //PRD_0187 JFE add end

    /**
     * メール送信を行う.
     * @param fromMailAddress 送信元メールアドレス
     * @param toMailAddress 送信先メールアドレス
     * @param ccMailAddress 送信先CCメールアドレス
     * @param bccMailAddress 送信先BCCメールアドレス
     * @param subject 件名
     * @param messageBody 本文
     * @param attachementFiles 添付ファイル
     * @return SendMailType メール送信結果
     */
    public SendMailType sendMail(final String fromMailAddress,
            final String[] toMailAddress,
            final String[] ccMailAddress,
            final String[] bccMailAddress,
            final String subject,
            final String messageBody,
            final List<MailAttachementFileModel> attachementFiles) {

        try {
            javaMailSender.send(mimeMessage -> {
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(fromMailAddress);
                helper.setTo(toMailAddress);
                helper.setCc(ccMailAddress);
                helper.setBcc(bccMailAddress);
                helper.setSubject(subject);
                helper.setText(messageBody);

                for (final MailAttachementFileModel file : attachementFiles) {
                    String fileName = file.getFileName();
                    // ファイル名がNULLまたは空文字の場合は、Fileパスのファイル名を設定する
                    if (StringUtils.isEmpty(file.getFileName())) {
                        fileName = file.getFile().getName();
                    }

                    helper.addAttachment(fileName, new ByteArrayDataSource(Files.readAllBytes(file.getFile().toPath()), ATTACHEMENT_FILE_MIME_TYPE));
                }
            });

            return SendMailType.MAIL_SENT;

        } catch (Exception e) {
            log.error(LogStringUtil.of("sendMail").exception(e).build(), e);
            return SendMailType.MAIL_SENDING_ERROR;
        }

    }
    //PRD_0187 JFE add start
    /**
     * メール送信を行う.
     * @param fromMailAddress 送信元メールアドレス
     * @param toMailAddress 送信先メールアドレス
     * @param ccMailAddress 送信先CCメールアドレス
     * @param bccMailAddress 送信先BCCメールアドレス
     * @param subject 件名
     * @param messageBody 本文
     * @param attachementFiles 添付ファイル
     * @return SendMailType メール送信結果
     */
    public SendMailType sendPurchaseVoucherMail(final String fromMailAddress,
            final String[] toMailAddress,
            final String[] ccMailAddress,
            final String[] bccMailAddress,
            final String subject,
            final String messageBody,
            final String signature,
            final List<MailAttachementFileModel> attachementFiles) {

        try {
            javaMailSender.send(mimeMessage -> {
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                String TextAddSignature = messageBody.replace("$!{model.signature}", signature);

                helper.setFrom(fromMailAddress);
                helper.setTo(toMailAddress);
                helper.setCc(ccMailAddress);
                helper.setBcc(bccMailAddress);
                helper.setSubject(subject);
                helper.setText(TextAddSignature);

                for (final MailAttachementFileModel file : attachementFiles) {
                    String fileName = file.getFileName();
                    // ファイル名がNULLまたは空文字の場合は、Fileパスのファイル名を設定する
                    if (StringUtils.isEmpty(file.getFileName())) {
                        fileName = file.getFile().getName();
                    }

                    helper.addAttachment(fileName, new ByteArrayDataSource(Files.readAllBytes(file.getFile().toPath()), ATTACHEMENT_FILE_MIME_TYPE));
                }
            });

            return SendMailType.MAIL_SENT;

        } catch (Exception e) {
            log.error(LogStringUtil.of("sendMail").exception(e).build(), e);
            return SendMailType.MAIL_SENDING_ERROR;
        }

    }
    //PRD_0187 JFE add end

}
