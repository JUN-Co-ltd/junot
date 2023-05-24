package jp.co.jun.edi.service.response;

import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * データ更新用サービスレスポンス.
 *
 * @param <T>
 */
@Getter
@Builder
public class UpdateServiceResponse<T> extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;
}
