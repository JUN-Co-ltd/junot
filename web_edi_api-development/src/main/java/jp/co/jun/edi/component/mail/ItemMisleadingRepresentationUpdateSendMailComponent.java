package jp.co.jun.edi.component.mail;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.model.mail.ItemMisleadingRepresentationUpdateSendModel;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * 優良誤認更新メール送信コンポーネント.
 */
@Component
public class ItemMisleadingRepresentationUpdateSendMailComponent extends SendMailComponent<ItemMisleadingRepresentationUpdateSendModel> {
    private static final MMailCodeType MAIL_CODE_TYPE = MMailCodeType.MISLEADING_REPRESENTATION_UPDATE;

    @Override
    MMailCodeType getMailCodeType() {
        return MAIL_CODE_TYPE;
    }

    @Override
    String[] getSendMailaddress(final ItemMisleadingRepresentationUpdateSendModel sendModel, final String loginAccoutName) {
        // 生産メーカーの担当メールアドレスを取得
        final String[] makerMailTo = getMakerMailaddress(sendModel);

        // JUNの担当メールアドレスを取得
        final String[] junMailTo = getJunMailaddress(sendModel, loginAccoutName);

        return ArrayUtils.addAll(makerMailTo, junMailTo);
    }
}
