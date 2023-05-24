package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * データ更新用サービスパラメーター.
 *
 * @param <T>
 */
@Getter
public class UpdateServiceParameter<T> extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;

    /** 更新前データ. */
    private final T preItem;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link T} instance
     * @param preItem {@link T} instance
     */
    @Builder
    public UpdateServiceParameter(@NonNull final CustomLoginUser loginUser, @NonNull final T item, final T preItem) {
        super(loginUser);
        this.item = item;
        this.preItem = preItem;
    }
}
