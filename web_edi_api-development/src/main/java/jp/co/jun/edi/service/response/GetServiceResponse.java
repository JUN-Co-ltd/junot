package jp.co.jun.edi.service.response;

import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * データ取得サービス用レスポンス.
 *
 * @param <T>
 */
@Getter
@Builder
public class GetServiceResponse<T> extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;

    /** データ. */
    private final T item;
}
