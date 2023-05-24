package jp.co.jun.edi.service.response;

import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * データ登録用サービスレスポンス.
 *
 * @param <T>
 */
@Getter
@Builder
public class CreateServiceResponse<T> extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;
}
