package jp.co.jun.edi.component.mail;

import java.io.StringWriter;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.MMailTemplateEntity;
import jp.co.jun.edi.repository.MMailTemplateRepository;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.util.MailFormatUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * メールテンプレート用Velocity置換コンポーネント.
 * @param <T>
 */
@Component
@Slf4j
public class VelocityMailTemplateComponent<T> extends GenericComponent {
    private VelocityEngine velocityEngine;

    @Autowired
    private MMailTemplateRepository mMailTemplateRepository;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void initAfterStartup() {
        this.velocityEngine = new VelocityEngine();
    }

    /**
     * メールテンプレートを読み込んで、Velocityで置換したタイトルと本文を返す.
     * @param sendModel 置換データモデル
     * @param mMailCodeType MMailCodeType
     * @return Optional<ConvertedMailVelocityModel>
     */
    public Optional<VelocityConvertedMailTemplateModel> convert(final T sendModel, final MMailCodeType mMailCodeType) {

        try {
            final Optional<MMailTemplateEntity> optional = mMailTemplateRepository.findByMailCode(mMailCodeType);
            if (!optional.isPresent()) {
                log.error("メールのテンプレートが取得できませんでした。メールコード：" + mMailCodeType.getValue());
                return Optional.ofNullable(null);
            }

            final VelocityContext velocityContext = getVelocityContext(sendModel);

            final MMailTemplateEntity mMailTemplateEntity = optional.get();

            final VelocityConvertedMailTemplateModel model = new VelocityConvertedMailTemplateModel();
            model.setTitle(merge(mMailTemplateEntity.getTitle(), velocityContext));
            model.setBody(merge(mMailTemplateEntity.getContent(), velocityContext));

            return Optional.ofNullable(model);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.ofNullable(null);
        }
    }

    /**
     * VelocityContextを設定して返す.
     * @param sendModel 置換データモデル
     * @return VelocityContext
     */
    private VelocityContext getVelocityContext(final T sendModel) {
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("customTool", new MailFormatUtil());
        velocityContext.put("dateTool", new DateTool());
        velocityContext.put("numberTool", new NumberTool());
        velocityContext.put("model", sendModel);
        return velocityContext;
    };

    /**
     * テンプレートテキストに置換データをバインドする.
     * @param templateText テンプレートテキスト
     * @param velocityContext VelocityContext
     * @return バインドされた文字列
     * @throws Exception Exception
     */
    private String merge(final String templateText, final VelocityContext velocityContext) throws Exception {
        final StringWriter writer = new StringWriter();
        velocityEngine.init();
        velocityEngine.evaluate(velocityContext, writer, "push notification.", templateText);
        final String bindedValue = writer.toString();
        writer.close();
        return bindedValue;
    }
}
