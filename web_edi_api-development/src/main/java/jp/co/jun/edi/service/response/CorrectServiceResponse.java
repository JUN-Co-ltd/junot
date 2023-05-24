package jp.co.jun.edi.service.response;

import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * データ訂正用サービスレスポンス.
 * 納品依頼訂正時に使用
 *
 * @param <T>
 */
@Getter
@Builder
public class CorrectServiceResponse<T> extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;
}
