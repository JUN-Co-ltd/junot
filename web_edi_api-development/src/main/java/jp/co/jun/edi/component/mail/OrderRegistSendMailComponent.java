package jp.co.jun.edi.component.mail;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.OrderRegistSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 受注登録メール送信コンポーネント.
 */
@Component
public class OrderRegistSendMailComponent extends SendMailComponent<OrderRegistSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.ORDER_REGIST;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final OrderRegistSendModel sendModel, final String loginAccoutName) {
        // JUNの担当メールアドレスを取得
        String[] junMailTo = getJunMailaddress(sendModel, loginAccoutName);
        return junMailTo;
    }

}
