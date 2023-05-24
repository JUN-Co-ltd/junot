package jp.co.jun.edi.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.config.PropertyName;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".filter.logging-filter.enabled", matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LoggingFilter implements Filter {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".filter.logging-filter";

    private static final String REQUEST_ID = "requestId";

    @Value("${" + PROPERTY_NAME_PREFIX + ".output-http-headers}")
    private String[] outputHttpHeaders;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void postConstruct() {
        log.info(Json.createObjectBuilder().add("postConstruct", Json.createObjectBuilder()
                .add("outputHttpHeaders", Json.createArrayBuilder(Arrays.asList(outputHttpHeaders))))
                .build().toString());
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("non-HTTP request");
        }
        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("non-HTTP response");
        }
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    /**
     * @param request The request to process
     * @param response The response associated with the request
     * @param chain Provides access to the next filter in the chain for this filter to pass the request and response to for further processing
     * @throws IOException if an I/O error occurs during this filter's processing of the request
     * @throws ServletException if the processing fails for any other reason
     */
    private void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final long start = System.currentTimeMillis();

        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        try {
            // ログ出力用にリクエストIDを採番
            MDC.put(REQUEST_ID, UUID.randomUUID().toString());

            log.info("{\"doFilter\":{\"start\":{\"uri\":\"{}\",\"method\":\"{}\"{}}}}",
                    uri,
                    method,
                    getHeaderString(request));

            outputHeaderAndParameterToLog(request);

            chain.doFilter(request, response);
        } finally {
            log.info("{\"doFilter\":{\"end\":{\"time\":{},\"uri\":\"{}\",\"method\":\"{}\",\"status\":{}}}}",
                    System.currentTimeMillis() - start,
                    uri,
                    method,
                    response.getStatus());

            MDC.remove(REQUEST_ID);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * @param request The request to process
     * @return HeaderString
     */
    private String getHeaderString(final HttpServletRequest request) {
        if (outputHttpHeaders.length == 0) {
            return "";
        }

        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        for (int i = 0; i < outputHttpHeaders.length; i++) {
            toHeaderString(jsonObjectBuilder, request, outputHttpHeaders[i]);
        }

        return ",\"header\":" + jsonObjectBuilder.build().toString();
    }

    /**
     * @param jsonObjectBuilder {@link JsonObjectBuilder} instance
     * @param request {@link HttpServletRequest} instance
     * @param name ヘッダー名
     */
    private void toHeaderString(final JsonObjectBuilder jsonObjectBuilder, final HttpServletRequest request, final String name) {
        final Enumeration<String> values = request.getHeaders(name);

        if (values.hasMoreElements()) {
            final String value = values.nextElement();

            if (values.hasMoreElements()) {
                final JsonArrayBuilder jsonArrayValueBuilder = Json.createArrayBuilder();
                jsonArrayValueBuilder.add(value);

                while (values.hasMoreElements()) {
                    jsonArrayValueBuilder.add(values.nextElement());
                }

                jsonObjectBuilder.add(name, jsonArrayValueBuilder);
            } else {
                jsonObjectBuilder.add(name, value);
            }
        } else {
            jsonObjectBuilder.addNull(name);
        }
    }

    /**
     * パラメーターをログに出力する.
     *
     * @param request The request to process
     */
    private void outputHeaderAndParameterToLog(final HttpServletRequest request) {
        if (!log.isDebugEnabled()) {
            return;
        }

        final StringBuilder sb = new StringBuilder();

        final String method = request.getMethod();

        sb.append("\n--> ").append(method).append(" ").append(request.getRequestURI()).append(" ").append(request.getProtocol());

        final Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            toHeaderStringForDebug(sb, request, headerNames.nextElement());
        }

        sb.append("\n");

        // 全リクエストパラメーター名を取得
        final Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            toParameterStringForDebug(sb, request, parameterNames.nextElement());
        }

        sb.append("\n--> END ").append(method);

        log.debug(sb.toString());
    }

    /**
     * @param sb {@link StringBuilder} instance
     * @param request {@link HttpServletRequest} instance
     * @param name ヘッダー名
     */
    private void toHeaderStringForDebug(final StringBuilder sb, final HttpServletRequest request, final String name) {
        final Enumeration<String> values = request.getHeaders(name);

        if (values.hasMoreElements()) {
            while (values.hasMoreElements()) {
                sb.append("\n").append(name).append("=").append(values.nextElement());
            }
        } else {
            sb.append("\n").append(name).append("=");
        }
    }

    /**
     * @param sb {@link StringBuilder} instance
     * @param request {@link HttpServletRequest} instance
     * @param name パラメーター名
     */
    private void toParameterStringForDebug(final StringBuilder sb, final HttpServletRequest request, final String name) {
        // パラメーター値を取得
        final String[] values = request.getParameterValues(name);

        if (values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                sb.append("\n").append(name).append("=").append(values[0]);
            }
        } else {
            sb.append("\n").append(name).append("=");
        }
    }
}
