package jp.co.jun.edi.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.ibm.icu.text.Transliterator;

import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.exception.SystemException;
import jp.co.jun.edi.type.MessageCodeType;
import lombok.extern.slf4j.Slf4j;

/**
 * 変換関連のコンポーネント.
 */
@Component
@Slf4j
public class ConvertComponent extends GenericComponent {
    @Autowired
    private ResourceLoader resourceLoader;

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".product-name-kana-dictionary";
    private Tokenizer tokenizer = null;

    /**
     * カナ変換辞書ファイルのpath.
     */
    @Value("${" + PROPERTY_NAME_PREFIX + ".path}")
    private String path;

    /**
     * カナ変換辞書ファイルのファイル名.
     */
    @Value("${" + PROPERTY_NAME_PREFIX + ".file-name}")
    private String fileName;

    /**
     * Beanコンテキスト生成後に呼び出される.
     * Tokenizerにカナ変換辞書ファイル読み込み.
     */
    @PostConstruct
    public void initAfterStartup() {
        // カナ変換辞書の読み込み
        final Resource resource = resourceLoader.getResource("file:" + path + "/" + fileName);
        try (InputStream is = resource.getInputStream();) {
            tokenizer = new Tokenizer.Builder().userDictionary(is).build();
        } catch (IOException e) {
            log.error("IOException.", e);
            throw new SystemException(MessageCodeType.SYSTEM_ERROR, e);
        }
    }

    /**
     * 漢字・ひらがなを全角カタカナに変換する.
     * 英数字記号は変換せずそのまま.
     * @param targetValue 変換文字
     * @return カナ変換後文字列
     */
    public String convertToKana(final String targetValue) {
        final List<Token> tokens = tokenizer.tokenize(targetValue);  // 形態素解析

        final StringBuilder convertValue = new StringBuilder();
        for (final Token token : tokens) {
            if (token.isUser() || token.isKnown()) {
                // ユーザー辞書またはカナ変換
                convertValue.append(token.getReading());
            } else {
                // 英数字記号はそのまま返す
                convertValue.append(token.getSurface());
            }
        }
        return convertValue.toString();
    }

    /**
     * 全角カタカナを半角カタカナに変換する.
     * 英数字記号も半角に変換される.
     * @param targetValue 変換文字
     * @return 半角カタカナ変換後文字列
     */
    public String convertToHalfKana(final String targetValue) {
        final Transliterator fullToHalf = Transliterator.getInstance("Fullwidth-Halfwidth");
        return fullToHalf.transliterate(targetValue);
    }
}
