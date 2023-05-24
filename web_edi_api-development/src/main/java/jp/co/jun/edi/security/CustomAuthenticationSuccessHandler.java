package jp.co.jun.edi.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jp.co.jun.edi.model.SessionModel;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 認証が成功した時の処理.
 */
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication auth) throws IOException, ServletException {
        if (response.isCommitted()) {
            // 応答がすでに出力ストリーム/クライアントに送信されている場合、何もしない
            log.info(LogStringUtil.of("onAuthenticationSuccess").message("Response has already been committed.").build());
            return;
        }

        final SessionModel session = new SessionModel((CustomLoginUser) auth.getPrincipal());

        log.info(LogStringUtil.of("onAuthenticationSuccess").value("response", session).build());

        response.setStatus(HttpStatus.OK.value());
        response.getOutputStream().write(ObjectMapperUtil.getObjectMapper().writeValueAsBytes(session));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        clearAuthenticationAttributes(request);
    }

    /**
     * Removes temporary authentication-related data which may have been stored in the
     * session during the authentication process.
     *
     * @param request the request
     */
    private void clearAuthenticationAttributes(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
