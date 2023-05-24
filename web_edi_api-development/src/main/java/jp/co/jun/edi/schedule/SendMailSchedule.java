package jp.co.jun.edi.schedule;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.SendMailScheduleComponent;
import jp.co.jun.edi.component.mail.MailSenderComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TSendMailEntity;
import jp.co.jun.edi.type.SendMailType;

/**
 * メール送信スケジュール.
 */
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.send-mail-schedule.enabled", matchIfMissing = true)
public class SendMailSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.send-mail-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private SendMailScheduleComponent sendMailScheduleComponent;
    @Autowired
    private MailSenderComponent mailSenderComponent;

    private static final String SEPARATED_STR = ",";

    /**
     * メール送信を実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {

        // メール情報取得
        final List<TSendMailEntity> mailDataList = sendMailScheduleComponent.getMailInfo();

        for (final TSendMailEntity tSendMailEntity : mailDataList) {

            // メール情報の格納
            final String[] toMailAddress = StringUtils.split(tSendMailEntity.getToMailAddress(), SEPARATED_STR);
            final String[] ccMailAddress = StringUtils.split(tSendMailEntity.getCcMailAddress(), SEPARATED_STR);
            final String[] bccMailAddress = StringUtils.split(tSendMailEntity.getBccMailAddress(), SEPARATED_STR);
            final String fromMailAddress = tSendMailEntity.getFromMailAddess();
            final String subject = tSendMailEntity.getSubject();
            final String messageBody = tSendMailEntity.getMessageBody();

            // メール送信
            final SendMailType state = mailSenderComponent.sendMail(fromMailAddress, toMailAddress, ccMailAddress, bccMailAddress, subject, messageBody);

            // 送信状態の更新
            sendMailScheduleComponent.updateSendStatus(state, tSendMailEntity.getId());
        }
    }
}
