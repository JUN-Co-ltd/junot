package jp.co.jun.edi.component.mail;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.OrderConfirmedSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 発注確定メール送信コンポーネント.
 */
@Component
public class OrderConfirmedSendMailComponent extends SendMailComponent<OrderConfirmedSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.ORDER_CONFIRMED;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final OrderConfirmedSendModel sendModel, final String loginAccoutName) {

        // 生産メーカーに紐づくメールアドレスを取得
        final String[] makerMailTo = getAllMakerAccountMailaddress(sendModel);

        return makerMailTo;

    }
}
