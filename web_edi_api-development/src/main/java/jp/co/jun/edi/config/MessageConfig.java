package jp.co.jun.edi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * メッセージの定義.
 *
 */
@Configuration
public class MessageConfig implements WebMvcConfigurer {
    @Autowired
    private MessageSource messageSource;

    /**
     * バリデーションのメッセージを、メッセージバンドルから取得する.
     * 本処理がない場合は、ValidationMessages.propertiesから取得される。
     *
     * @return {@link LocalValidatorFactoryBean}
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();

        localValidatorFactoryBean.setValidationMessageSource(messageSource);

        return localValidatorFactoryBean;
    }
}
