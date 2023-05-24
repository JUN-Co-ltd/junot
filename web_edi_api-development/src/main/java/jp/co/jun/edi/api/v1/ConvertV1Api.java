package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.ConvertComponent;
import jp.co.jun.edi.model.TextModel;
import jp.co.jun.edi.security.CustomLoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * 変換API.
 */
@RestController
@RequestMapping("/api/v1/convert")
@Slf4j
public class ConvertV1Api {
    @Autowired
    private ConvertComponent convertComponent;

    /**
     * カナ変換します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param textModel 文字列
     * @return {@link HttpEntity} instance
     */
    @PostMapping("/kana")
    public TextModel convertKana(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final TextModel textModel) {
        log.info("call() execution param. {\"textModel\":\"" + textModel + "\"}");
        final String kanaStr = convertComponent.convertToKana(textModel.getText());

        final TextModel resText = new TextModel();
        resText.setText(kanaStr);
        log.info("call() return param. {omitted}");
        return resText;
    }

}
