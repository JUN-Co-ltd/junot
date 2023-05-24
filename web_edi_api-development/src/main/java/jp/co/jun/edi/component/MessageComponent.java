package jp.co.jun.edi.component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ErrorDetailModel;

/**
 * メッセージ変換用のコンポーネント.
 */
@Component
public class MessageComponent extends GenericComponent {
    private static final Locale LOCALE = Locale.JAPANESE;
    private static final String CODE = "code.";
    private static final String FIELD = "field.";

    @Autowired
    private MessageSource messageSource;

    /**
     * エラー詳細メッセージに変換する.
     *
     * @param resultMessages メッセージのリスト
     * @return エラー詳細メッセージのリスト
     */
    public List<ErrorDetailModel> toErrorDetails(final List<ResultMessage> resultMessages) {
        return resultMessages.stream().map(resultMessage -> toErrorDetail(resultMessage, LOCALE)).collect(Collectors.toList());
    }

    /**
     * エラー詳細メッセージに変換する.
     *
     * @param resultMessage メッセージ
     * @param locale 言語
     * @return エラー詳細メッセージ
     */
    public ErrorDetailModel toErrorDetail(final ResultMessage resultMessage, final Locale locale) {
        final ErrorDetailModel errorDetail = new ErrorDetailModel();

        errorDetail.setCode(resultMessage.getCode());
        errorDetail.setArgs(resultMessage.getArgs());
        errorDetail.setMessage(toMessage(resultMessage, locale));
        errorDetail.setResource(resultMessage.getResource());
        errorDetail.setField(resultMessage.getField());
        errorDetail.setValue(resultMessage.getValue());

        return errorDetail;
    }

    /**
     * メッセージを取得する.
     *
     * @param resultMessage メッセージ
     * @param locale 言語
     * @return メッセージ
     */
    public String toMessage(final ResultMessage resultMessage, final Locale locale) {
        final Object[] args;

        switch (resultMessage.getCode()) {
        case CODE_009:
            args = new String[] {
                    // 項目名を取得
                    messageSource.getMessage(FIELD + resultMessage.getField(), null, locale),
                    // 値を設定
                    toValueString(resultMessage.getValue()),
            };
            break;

        case CODE_008:
        case CODE_010:
            args = new String[] {
                    // 項目名を取得
                    messageSource.getMessage(FIELD + resultMessage.getField(), null, locale) };
            break;

        default:
            args = resultMessage.getArgs();
            break;
        }

        return messageSource.getMessage(CODE + resultMessage.getCode().getValue(), args, locale);
    }

    /**
     * 値を文字列に変換する.
     *
     * @param value 値
     * @return 文字列
     */
    private String toValueString(final Object value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }
}
