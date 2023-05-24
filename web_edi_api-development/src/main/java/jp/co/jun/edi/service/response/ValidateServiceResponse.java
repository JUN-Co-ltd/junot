package jp.co.jun.edi.service.response;

import java.util.LinkedHashMap;

import jp.co.jun.edi.model.GenericModel;
import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * 検証用サービスレスポンス.
 *
 * @param <T>
 */
@Getter
@Builder
public class ValidateServiceResponse<T extends GenericModel> extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;

    @Override
    public Object getLogObject() {
        return toLogObject(this);
    }

    /**
     * ログ出力用オブジェクトに変換.
     *
     * @param <T> GenericModel
     * @param model {@link ValidateServiceResponse} instance
     * @return ログ出力用オブジェクト
     */
    private static <T extends GenericModel> LinkedHashMap<String, Object> toLogObject(final ValidateServiceResponse<T> model) {
        return new LinkedHashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("item", GenericModel.toLogObject(model.getItem()));
            }
        };
    }
}
