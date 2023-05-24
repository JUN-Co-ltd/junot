package jp.co.jun.edi.component.mail;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.OrderApprovedSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 発注承認メール送信コンポーネント.
 */
@Component
public class OrderApprovedSendMailComponent extends SendMailComponent<OrderApprovedSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.ORDER_APPROVED;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final OrderApprovedSendModel sendModel, final String loginAccoutName) {

        // 生産メーカーに紐づくメールアドレスを取得
        final String[] makerMailTo = getAllMakerAccountMailaddress(sendModel);

        // JUNの担当メールアドレスを取得
        final String[] junMailTo = getJunMailaddress(sendModel, loginAccoutName);

        return ArrayUtils.addAll(makerMailTo, junMailTo);
    }
}
