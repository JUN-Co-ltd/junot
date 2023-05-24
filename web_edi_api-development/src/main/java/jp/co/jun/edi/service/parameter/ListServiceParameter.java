package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;

/**
 * データリスト取得用サービスパラメーター.
 *
 * @param <T>
 */
@Getter
public class ListServiceParameter<T> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** 検索条件. */
    private final T searchCondition;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link T} instance
     */
    @Builder
    public ListServiceParameter(final CustomLoginUser loginUser, final T searchCondition) {
        super(loginUser);
        this.searchCondition = searchCondition;
    }
}
