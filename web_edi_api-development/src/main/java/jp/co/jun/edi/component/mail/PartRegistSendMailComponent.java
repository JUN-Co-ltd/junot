package jp.co.jun.edi.component.mail;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.PartRegistSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 品番登録時のメール送信コンポーネント.
 */
@Component
public class PartRegistSendMailComponent extends SendMailComponent<PartRegistSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.PART_REGIST;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final PartRegistSendModel sendModel, final String loginAccoutName) {
        // 生産メーカーの担当メールアドレスを取得
        String[] makerMailTo = getMakerMailaddress(sendModel);
        return makerMailTo;
    }
}
