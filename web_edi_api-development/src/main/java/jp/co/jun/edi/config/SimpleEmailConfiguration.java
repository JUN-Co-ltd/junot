package jp.co.jun.edi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.config.AmazonWebserviceClientFactoryBean;
import org.springframework.cloud.aws.core.region.StaticRegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;

/**
 * AWS SESメールの設定クラス.
 */
@Configuration
public class SimpleEmailConfiguration {

    /**
     * @param region region
     * @param credentialsProvider credentialsProvider
     * @return AmazonWebserviceClientFactoryBean<AmazonSimpleEmailServiceClient>
     */
    @Bean
    public AmazonWebserviceClientFactoryBean<AmazonSimpleEmailServiceClient> amazonSimpleEmailService(
                @Value("${cloud.aws.ses.region}") final String region,
        final AWSCredentialsProvider credentialsProvider) {
        return new AmazonWebserviceClientFactoryBean<>(AmazonSimpleEmailServiceClient.class,
            credentialsProvider, new StaticRegionProvider(region));
    }

}
