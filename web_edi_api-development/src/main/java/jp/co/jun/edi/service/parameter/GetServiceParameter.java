package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;

/**
 * データ取得サービス用パラメーター.
 *
 * @param <T>
 */
@Getter
public class GetServiceParameter<T> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** ID(主キー). */
    private final T id;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param id {@link T} instance
     */
    @Builder
    public GetServiceParameter(final CustomLoginUser loginUser, final T id) {
        super(loginUser);
        this.id = id;
    }
}
