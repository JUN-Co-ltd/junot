package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;

/**
 * データ登録用サービスパラメーター.
 *
 * @param <T>
 */
@Getter
public class CreateServiceParameter<T> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link T} instance
     */
    @Builder
    public CreateServiceParameter(final CustomLoginUser loginUser, final T item) {
        super(loginUser);
        this.item = item;
    }
}
