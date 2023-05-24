package jp.co.jun.edi.api.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.SessionModel;
import jp.co.jun.edi.security.CustomLoginUser;

/**
 * セッションAPI.
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionV1Api {
    /**
     * セッションを取得する.
     *
     * @param loginUser 認証情報
     * @return セッション
     */
    @GetMapping("/me")
    public SessionModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser) {
        return new SessionModel(loginUser);
    }
}
