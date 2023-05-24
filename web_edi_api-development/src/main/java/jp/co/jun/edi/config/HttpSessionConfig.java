package jp.co.jun.edi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;

/**
 * HTTP Sessionの設定クラス.
 */
@Configuration
@ConditionalOnProperty(value = "spring.session.store-type", havingValue = "redis", matchIfMissing = false)
public class HttpSessionConfig extends RedisHttpSessionConfiguration {

    @Value("${server.servlet.session.timeout}")
    private int maxInactiveIntervalInSeconds;

    /**
     * Elasticache用. Spring SessionがAWS上のRedisのCONFIGを実行しないための設定.
     *
     * @return ConfigureRedisAction
     */
    @Bean
    public ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    /**
     * @return RedisOperationsSessionRepository
     */
    @Bean
    public RedisOperationsSessionRepository sessionRepository() {
        this.setMaxInactiveIntervalInSeconds(maxInactiveIntervalInSeconds);

        return super.sessionRepository();
    }
}
