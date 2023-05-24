package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;

/**
 * データ訂正用サービスパラメーター.
 * 納品依頼訂正時に使用
 *
 * @param <T>
 */
@Getter
public class CorrectServiceParameter<T> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link T} instance
     */
    @Builder
    public CorrectServiceParameter(final CustomLoginUser loginUser, final T item) {
        super(loginUser);
        this.item = item;
    }
}
