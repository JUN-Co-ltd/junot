package jp.co.jun.edi.service.parameter;

import java.util.LinkedHashMap;
import java.util.List;

import jp.co.jun.edi.model.GenericModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * 検証用サービスパラメーター.
 *
 * @param <T>
 */
@Getter
public class ValidateServiceParameter<T extends GenericModel> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;

    /** バリデーショングループ. */
    private final List<Object> validationGroups;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link T} instance
     * @param validationGroups {@link T} instance
     */
    @Builder
    public ValidateServiceParameter(final CustomLoginUser loginUser, final T item, @NonNull final List<Object> validationGroups) {
        super(loginUser);
        this.item = item;
        this.validationGroups = validationGroups;
    }

    @Override
    public Object getLogObject() {
        return toLogObject(this);
    }

    /**
     * ログ出力用オブジェクトに変換.
     *
     * @param <T> GenericModel
     * @param model {@link ValidateServiceParameter} instance
     * @return ログ出力用オブジェクト
     */
    private static <T extends GenericModel> LinkedHashMap<String, Object> toLogObject(final ValidateServiceParameter<T> model) {
        return new LinkedHashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("loginUser", model.getLoginUser());
                put("item", GenericModel.toLogObject(model.getItem()));
            }
        };
    }
}
