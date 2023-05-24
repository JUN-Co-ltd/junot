package jp.co.jun.edi.component.mail;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.DeliveryRequestRegistSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 納品依頼登録メール送信コンポーネント.
 */
@Component
public class DeliveryRequestRegistSendMailComponent extends SendMailComponent<DeliveryRequestRegistSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.DELIVERY_REGIST;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final DeliveryRequestRegistSendModel sendModel, final String loginAccoutName) {
        // JUNの担当メールアドレスを取得
        String[] junMailTo = getJunMailaddress(sendModel, loginAccoutName);
        return junMailTo;
    }
}
