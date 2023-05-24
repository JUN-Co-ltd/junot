package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;

/**
 * 認証用サービスパラメーター.
 *
 * @param <T>
 */
@Getter
public class ApprovalServiceParameter<T> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link T} instance
     */
    @Builder
    public ApprovalServiceParameter(final CustomLoginUser loginUser, final T item) {
        super(loginUser);
        this.item = item;
    }
}
