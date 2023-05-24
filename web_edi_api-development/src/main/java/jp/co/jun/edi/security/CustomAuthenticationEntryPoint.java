package jp.co.jun.edi.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jp.co.jun.edi.model.ErrorModel;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 認証が必要なリソースに未認証でアクセスしたときの処理.
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException exception) throws IOException, ServletException {
        if (response.isCommitted()) {
            // 応答がすでに出力ストリーム/クライアントに送信されている場合、何もしない
            log.info(LogStringUtil.of("commence").message("Response has already been committed.").build());
            return;
        }

        final ErrorModel error = new ErrorModel(
                new Date(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase());

        log.warn(LogStringUtil.of("commence").exception(exception)
                // "error" の文字列があるエラーレスポンスをログに出力すると、
                // CloudWatch がエラーメールを送信するためエラーレスポンスはログに出力しない。
                // .value("response", error)
                .build());

        // responseにエラーステータスとエラー内容を設定する。
        response.setStatus(error.getStatus());
        response.getOutputStream().write(ObjectMapperUtil.writeValueAsBytes(error));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
