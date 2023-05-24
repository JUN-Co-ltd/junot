package jp.co.jun.edi.component.mail;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.ItemRegistSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 商品登録時のメール送信コンポーネント.
 */
@Component
public class ItemRegistSendMailComponent extends SendMailComponent<ItemRegistSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.ITEM_REGIST;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final ItemRegistSendModel sendModel, final String loginAccoutName) {
        // 生産メーカーの担当メールアドレスを取得
        String[] makerMailTo = getMakerMailaddress(sendModel);

        // JUNの担当メールアドレスを取得
        String[] junMailTo = getJunMailaddress(sendModel, loginAccoutName);

        return ArrayUtils.addAll(makerMailTo, junMailTo);
    }
}
