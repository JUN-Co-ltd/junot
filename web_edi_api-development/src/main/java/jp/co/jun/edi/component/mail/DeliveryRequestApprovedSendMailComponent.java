package jp.co.jun.edi.component.mail;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.DeliveryRequestApprovedSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 納品依頼承認メール送信コンポーネント.
 */
@Component
public class DeliveryRequestApprovedSendMailComponent extends SendMailComponent<DeliveryRequestApprovedSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.DELIVERY_APPROVED;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final DeliveryRequestApprovedSendModel sendModel, final String loginAccoutName) {

        // 生産メーカーに紐づくメールアドレスを取得
        final String[] makerMailTo = getAllMakerAccountMailaddress(sendModel);

        return makerMailTo;
    }
}
