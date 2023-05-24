package jp.co.jun.edi.component.mail;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.DeliveryPlanSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 納品予定更新メール送信コンポーネント.
 */
@Component
public class DeliveryPlanUpdateSendMailComponent extends SendMailComponent<DeliveryPlanSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.DELIVERY_PLAN_UPDATE;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final DeliveryPlanSendModel sendModel, final String loginAccoutName) {
        // JUNの担当メールアドレスを取得
        String[] junMailTo = getJunMailaddress(sendModel, loginAccoutName);
        return junMailTo;
    }
}
