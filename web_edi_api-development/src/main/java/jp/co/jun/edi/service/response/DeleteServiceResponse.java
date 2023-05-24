package jp.co.jun.edi.service.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jp.co.jun.edi.service.GenericServiceResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * データ削除用サービスレスポンス.
 */
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteServiceResponse extends GenericServiceResponse {
    private static final long serialVersionUID = 1L;
}
