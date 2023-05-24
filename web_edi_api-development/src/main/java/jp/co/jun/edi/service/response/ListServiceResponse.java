package jp.co.jun.edi.service.response;

import java.util.Date;
import java.util.List;

import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * データリスト取得サービス用レスポンス..
 *
 * @param <T>
 */
@Getter
@Builder
public class ListServiceResponse<T> extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;

    /** データのリスト. */
    private final List<T> items;
    /** 次のページの有無. */
    private final boolean nextPage;
    /** 改訂日時. */
    private Date revisionedAt;
}
