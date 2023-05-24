package jp.co.jun.edi.config;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jp.co.jun.edi.security.CustomLoginUser;

/**
 * 監査証跡の設定クラス.
 */
@EnableJpaAuditing
@Configuration
public class AuditingConfig {

    /**
     * @return AuditorAware<String>
     */
    @Bean
    public AuditorAware<BigInteger> createAuditorProvider() {
        return new SecurityAuditor();
    }

    /**
     * @return AuditingEntityListener
     */
    @Bean
    public AuditingEntityListener createAuditingListener() {
        return new AuditingEntityListener();
    }

    /**
     * @author
     *
     */
    public static class SecurityAuditor implements AuditorAware<BigInteger> {

        @Override
        public Optional<BigInteger> getCurrentAuditor() {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                // TODO スケジュール起動されるアプリケーションがある場合、対応を検討する
                return Optional.empty();
            }

            final CustomLoginUser loginUser = (CustomLoginUser) authentication.getPrincipal();

            return Optional.of(loginUser.getUserId());
        }

    }

}
