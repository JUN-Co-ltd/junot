package jp.co.jun.edi.component.mail;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.entity.MMailTemplateEntity;
import jp.co.jun.edi.model.mail.GetMailAdressCommonModel;
import jp.co.jun.edi.repository.MMailTemplateRepository;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.util.MailFormatUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * メール送信コンポーネント.
 * @param <T>
 */
@Component
@Slf4j
public abstract class SendMailComponent<T extends GetMailAdressCommonModel> extends GenericComponent {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private MMailTemplateRepository mMailTemplateRepository;

    @Autowired
    private MUserRepository mUserRepository;

    /**
     * JUN企業コード.
     */
    @Value("${junot.mail.company}")
    private String company;

    /**
     * ログインユーザー送信可否フラグ.
     */
    @Value("${junot.mail.login-user-send}")
    private boolean isLoginUserSendOk;

    /**
     * 送信可否フラグ.
     */
    @Value("${junot.mail.send}")
    private boolean isSendOk;

    /**
     * fromAddress.
     */
    @Value("${junot.mail.from}")
    private String fromAddress;

    /**
     * to.
     * JUN代表メールアドレス
     */
    @Value("${junot.mail.to}")
    private String toAddress;

    /**
     * url.
     * サイトのURL
     */
    @Value("${junot.mail.url}")
    private String url;

    private VelocityEngine velocityEngine;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void initAfterStartup() {
        this.velocityEngine = new VelocityEngine();
    }

    /**
     * メール送信を行う.
     * @param sendModel 置換データモデル
     * @param loginAccoutName ログインアカウント名
     */
    public void sendMail(final T sendModel, final String loginAccoutName) {
        try {
            final Optional<MMailTemplateEntity> optional = mMailTemplateRepository.findByMailCode(getMailCodeType());
            if (!optional.isPresent()) {
                log.error("メールのテンプレートが取得できませんでした。メールコード：" + getMailCodeType().getValue());
                return;
            }
            final SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromAddress);
            message.setTo(toAddress);

            // 送信先をセット
            final String[] mailAdress = getSendMailaddress(sendModel, loginAccoutName);
            log.debug("送信対象メールアドレス：" + Arrays.toString(mailAdress));
            if (mailAdress.length == 0) {
                log.warn("BCC宛のメールアドレスを取得できませんでした。メールコード：" + getMailCodeType().getValue());
            }
            message.setBcc(mailAdress);

            final VelocityContext velocityContext = getVelocityContext(sendModel);

            final MMailTemplateEntity mMailTemplateEntity = optional.get();
            final String title = merge(mMailTemplateEntity.getTitle(), velocityContext);
            final String body = merge(mMailTemplateEntity.getContent(), velocityContext);
            message.setSubject(title);
            message.setText(body);

            if (isSendOk) {
                mailSender.send(message);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * VelocityContextを設定して返す.
     * @param sendModel 置換データモデル
     * @return VelocityContext
     */
    private VelocityContext getVelocityContext(final T sendModel) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("customTool", new MailFormatUtil());
        velocityContext.put("dateTool", new DateTool());
        velocityContext.put("numberTool", new NumberTool());
        velocityContext.put("model", sendModel);
        velocityContext.put("url", StringUtils.defaultString(url));
        return velocityContext;
    };

    /**
     * メールコードタイプを返す.
     * @return MMailCodeType
     */
    abstract MMailCodeType getMailCodeType();

    /**
     * 送信先メールアドレスを取得する.
     * @param sendModel 置換データモデル
     * @param loginAccoutName ログインアカウント名
     * @return mailTo
     */
    abstract String[] getSendMailaddress(T sendModel, String loginAccoutName);

    /**
     * 生産メーカー担当のメールアドレスを取得する.
     * @param sendModel 置換データモデル
     * @return メールアドレスの配列
     */
    protected String[] getMakerMailaddress(final T sendModel) {
        // 生産メーカー担当ID(ユーザーID)でアドレス取得
        final String mailAddressStr = mUserRepository.findMailAddressById(sendModel.getMdfMakerStaffId());
        final String[] mailAddress = splitMailAddressList(Arrays.asList(mailAddressStr));

        return mailAddress;
    };

    /**
     * メーカーコードに紐づく全アカウントのメールアドレスを取得する.
     * @param sendModel 置換データモデル
     * @return メールアドレスの配列
     */
    protected String[] getAllMakerAccountMailaddress(final T sendModel) {
        // メーカーコード
        final List<String> mailAddresssList = mUserRepository.findMailAddressByCompany(sendModel.getMdfMakerCode());
        return splitMailAddressList(mailAddresssList);
    };

    /**
     * JUNの製造担当、企画担当、パタンナーのメールアドレスを取得する.
     * @param sendModel 置換データモデル
     * @param loginAccoutName ログインアカウント名
     * @return メールアドレスの配列
     */
    protected String[] getJunMailaddress(final T sendModel, final String loginAccoutName) {
        final Set<String> staffCodesSet = Stream.of(
                // 製造担当を設定
                sendModel.getMdfStaffCode())
                // メール送信対象のアカウントか判定する
                .filter(staffCode -> isSendMailAccount(staffCode, loginAccoutName))
                .collect(Collectors.toCollection(HashSet::new));

        if (staffCodesSet.isEmpty()) {
            return new String[0];
        }

        // アカウント名リストでアドレス取得
        final List<String> mailAddressList = mUserRepository
                .findMailAddressByAccountNameAndCompany(staffCodesSet, company);
        final String[] mailAddress = splitMailAddressList(mailAddressList);

        return mailAddress;
    };

    /**
     * メール送信対象のアカウントか判定する.
     * @param staffCode 担当者コード
     * @param loginAccoutName ログインアカウント名
     * @return 判定結果
     */
    private boolean isSendMailAccount(final String staffCode, final String loginAccoutName) {
        if (StringUtils.isBlank(staffCode)) {
            // NULL、空文字、空白の場合は、送信対象外
            return false;
        }

        if (!isLoginUserSendOk) {
            // ログインユーザーへ送信しない場合
            if (StringUtils.equals(staffCode, loginAccoutName)) {
                // ログインアカウント名と一致する場合、送信対象外
                return false;
            }
        }

        return true;
    }

    /**
     * メールアドレスをカンマで分割し、重複除去後配列につめる.
     * @param mailAddress 取得したメールアドレスのリスト
     * @return 分割後のメールアドレスの配列
     */
    private String[] splitMailAddressList(final List<String> mailAddress) {

        String[] emails = {};

        if (mailAddress.isEmpty()) {
            return emails;
        }

        for (String address: mailAddress) {    // ;ではなくて:なので注意
            String[] email = StringUtils.split(address, ",");
            emails = ArrayUtils.addAll(emails, email);
       }

        // 重複したメールアドレスを除去
        final List<String> list = Arrays.asList(emails);
        final String[] splittedMailAddress = (String[]) new LinkedHashSet<>(list).toArray(new String[0]);

        return splittedMailAddress;
    }

    /**
     * テンプレートテキストに置換データをバインドする.
     * @param templateText テンプレートテキスト
     * @param velocityContext VelocityContext
     * @return バインドされた文字列
     * @throws Exception Exception
     */
    private String merge(final String templateText, final VelocityContext velocityContext) throws Exception {
        String bindedValue = "";
        StringWriter writer = new StringWriter();
        velocityEngine.init();
        velocityEngine.evaluate(velocityContext, writer, "push notification.", templateText);
        bindedValue = writer.toString();
        writer.close();
        return bindedValue;
    }
}
