package jp.co.jun.edi.component.mail;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * メール送信コンポーネント.
 */
@Component
@Slf4j
public class MailSenderComponent {
    private static final String COMMA = ",";
    @Autowired
    private MailSender mailSender;

    /**
     * メール送信を行う.
     *@param fromMailAddress    送信元メールアドレス
     *@param toMailAddress      送信先メールアドレス
     *@param ccMailAddress      送信先CCメールアドレス
     *@param bccMailAddress     送信先BCCメールアドレス
     *@param subject            件名
     *@param messageBody        本文
     *@return SendMailType メール送信結果
     */
    public SendMailType sendMail(final String fromMailAddress,
            final String toMailAddress,
            final String ccMailAddress,
            final String bccMailAddress,
            final String subject,
            final String messageBody) {
        final String[] arrayToMailAddress = StringUtils.split(toMailAddress, COMMA);
        final String[] arrayCcMailAddress = StringUtils.split(ccMailAddress, COMMA);
        final String[] arrayBccMailAddress = StringUtils.split(bccMailAddress, COMMA);

        return sendMail(fromMailAddress, arrayToMailAddress, arrayCcMailAddress, arrayBccMailAddress, subject, messageBody);

    }

    /**
     * メール送信を行う.
     *@param fromMailAddress    送信元メールアドレス
     *@param toMailAddress      送信先メールアドレス
     *@param ccMailAddress      送信先CCメールアドレス
     *@param bccMailAddress     送信先BCCメールアドレス
     *@param subject            件名
     *@param messageBody        本文
     *@return SendMailType メール送信結果
     */
    public SendMailType sendMail(final String fromMailAddress,
            final String[] toMailAddress,
            final String[] ccMailAddress,
            final String[] bccMailAddress,
            final String subject,
            final String messageBody) {

        try {
            final SimpleMailMessage message = new SimpleMailMessage();
            //送信元をセット
            message.setFrom(fromMailAddress);

            // 送信先をセット
            final String[] toAddress = toMailAddress;
            log.debug("送信対象メールアドレス：" + Arrays.toString(toAddress));
            if (Objects.isNull(toMailAddress) || toMailAddress.length == 0) {
                log.warn("宛先のメールアドレスを取得できませんでした。");
                return SendMailType.MAIL_SENDING_ERROR;
            }

            message.setTo(toMailAddress);

            // CC送信先をセット
            final String[] ccAddress = ccMailAddress;
            log.debug("送信対象CCメールアドレス：" + Arrays.toString(ccAddress));
            if (Objects.isNull(ccMailAddress) || ccMailAddress.length == 0) {
                log.warn("CC宛のメールアドレスを取得できませんでした。");
            }
            message.setCc(ccAddress);

            // BCC送信先をセット
            final String[] bccAddress = bccMailAddress;
            log.debug("送信対象BCCメールアドレス：" + Arrays.toString(bccAddress));
            if (Objects.isNull(bccMailAddress) || bccMailAddress.length == 0) {
                log.warn("BCC宛のメールアドレスを取得できませんでした。");
            }
            message.setBcc(bccAddress);

            //件名をセット
            message.setSubject(subject);

            //本文をセット
            message.setText(messageBody);

            mailSender.send(message);

            return SendMailType.MAIL_SENT;

        } catch (Exception e) {
            log.error(LogStringUtil.of("sendMail").exception(e).build());
            return SendMailType.MAIL_SENDING_ERROR;
        }

    }
}
