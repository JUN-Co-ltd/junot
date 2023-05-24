package jp.co.jun.edi.security;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.json.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jp.co.jun.edi.component.AvailableTimeComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.filter.AvailableTimeFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 認証設定.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".security-config";

    @Value("${" + PROPERTY_NAME_PREFIX + ".session-management.maximum-sessions}")
    private int sessionManagementMaximumSessions;

    @Value("${" + PROPERTY_NAME_PREFIX + ".session-management.max-sessions-prevents-login}")
    private boolean sessionManagementMaxSessionsPreventsLogin;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.allow-credentials}")
    private boolean corsAllowCredentials;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.allowed-origins}")
    private String[] corsAllowedOrigins;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.allowed-headers}")
    private String[] corsAllowedHeaders;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.exposed-headers}")
    private String[] corsExposedHeaders;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.allowed-methods}")
    private String[] corsAllowedMethods;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.max-age}")
    private long corsMaxAge;

    @Value("${" + PROPERTY_NAME_PREFIX + ".cors.register-cors-configuration}")
    private String corsRegisterCorsConfiguration;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AvailableTimeComponent availableTimeComponent;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void postConstruct() {
        log.info(Json.createObjectBuilder().add("postConstruct", Json.createObjectBuilder()
                .add("sessionManagementMaximumSessions", sessionManagementMaximumSessions)
                .add("sessionManagementMaxSessionsPreventsLogin", sessionManagementMaxSessionsPreventsLogin)
                .add("corsAllowCredentials", corsAllowCredentials)
                .add("corsAllowedOrigins", Json.createArrayBuilder(Arrays.asList(corsAllowedOrigins)))
                .add("corsAllowedHeaders", Json.createArrayBuilder(Arrays.asList(corsAllowedHeaders)))
                .add("corsExposedHeaders", Json.createArrayBuilder(Arrays.asList(corsExposedHeaders)))
                .add("corsAllowedMethods", Json.createArrayBuilder(Arrays.asList(corsAllowedMethods)))
                .add("corsMaxAge", corsMaxAge)
                .add("corsRegisterCorsConfiguration", corsRegisterCorsConfiguration))
                .build().toString());
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        http
            // アクセス制限
            .authorizeRequests()
                .mvcMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                .mvcMatchers("/api/v1/availableTimes")
                    .permitAll()
                .mvcMatchers("/api/v1/**")
                    .hasAnyRole("USER")
                .anyRequest()
                    .authenticated()
            .and()
            // ログイン例外ハンドリング
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            .and()
            // ログイン
            .formLogin()
                .loginProcessingUrl("/api/v1/sessions").permitAll()
                    .usernameParameter("accountName")
                    .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
            .and()
            // ログアウト
            .logout()
                .logoutUrl("/api/v1/sessions/me:delete")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(logoutSuccessHandler())
            .and()
            // CORS（オリジン間リソース共有 (Cross-Origin Resource Sharing) ）
            .cors()
                .configurationSource(corsConfigurationSource())
            .and()
            // CSRF（クロスサイトリクエストフォージェリ（Cross-site Request Forgery））
            .csrf()
                // ログイン時はCSRFトークンを不要にする
                .ignoringAntMatchers("/api/v1/sessions")
                // Javascriptから取得できるようにHttponlyをfalseにする
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            // セッション
            .sessionManagement()
                // 同時ログインユーザー数
                .maximumSessions(sessionManagementMaximumSessions)
                // 同時ログインユーザー数の上限を超えてもログイン可能
                .maxSessionsPreventsLogin(sessionManagementMaxSessionsPreventsLogin)
                // セッション情報のレジストリ登録
                .sessionRegistry(sessionRegistry());
        // @formatter:on

        if (availableTimeComponent.isAvailableTimeEnabled()) {
            // 利用可能時間の指定がある場合は、アカウント認証の前に利用可能時間判定のフィルターを追加
            http.addFilterBefore(new AvailableTimeFilter(availableTimeComponent), UsernamePasswordAuthenticationFilter.class);
        }
    }

    /**
     * @param auth auth
     * @param userDetailsService userDetailsService
     * @param passwordEncoder passwordEncoder
     * @throws Exception Exception
     */
    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth,
            final @Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
            final PasswordEncoder passwordEncoder) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider()).eraseCredentials(true);
    }

    /**
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setHideUserNotFoundExceptions(false);  // ログインエラーの情報を隠蔽しない
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * @return PasswordEncoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * @return sessionRegistry
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * @return HttpSessionEventPublisher
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * @return CorsConfigurationSource
     */
    private CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(corsAllowCredentials);
        config.setAllowedOrigins(Arrays.asList(corsAllowedOrigins));
        config.setAllowedHeaders(Arrays.asList(corsAllowedHeaders));
        config.setExposedHeaders(Arrays.asList(corsExposedHeaders));
        config.setAllowedMethods(Arrays.asList(corsAllowedMethods));
        config.setMaxAge(corsMaxAge);

        source.registerCorsConfiguration(corsRegisterCorsConfiguration, config);

        return source;
    }

    /**
     * 認証が必要なリソースに未認証でアクセスした場合の処理.
     *
     * @return AuthenticationEntryPoint
     */
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    /**
     * アクセスするリソースの認可に失敗した時の処理.
     *
     * @return AccessDeniedHandler
     */
    private AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    /**
     * 認証が成功した時の処理.
     *
     * @return AuthenticationSuccessHandler
     */
    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    /**
     * 認証が失敗した時の処理.
     *
     * @return AuthenticationFailureHandler
     */
    private AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    /**
     * ログアウト時の処理.
     *
     * @return LogoutSuccessHandler
     */
    private LogoutSuccessHandler logoutSuccessHandler() {
        return new HttpStatusReturningLogoutSuccessHandler();
    }

}
