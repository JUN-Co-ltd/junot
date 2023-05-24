package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * エラーModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Date timestamp;

    @JsonIgnore
    private final HttpStatus httpStatus;

    private final String message;

    private final String path;

    private List<ErrorDetailModel> errors;

    /**
     * @param timestamp タイムスタンプ
     * @param httpStatus HTTPステータス
     * @param path URLのパス
     */
    public ErrorModel(final Date timestamp, final HttpStatus httpStatus, final String path) {
        this(timestamp, httpStatus, path, httpStatus.getReasonPhrase());
    }

    /**
     * @param timestamp タイムスタンプ
     * @param httpStatus HTTPステータス
     * @param path URLのパス
     * @param message メッセージ
     */
    public ErrorModel(final Date timestamp, final HttpStatus httpStatus, final String path, final String message) {
        this.timestamp = timestamp;
        this.httpStatus = httpStatus;
        this.message = message;
        this.path = path;
    }

    /**
     * HTTPステータスの整数値を返します.
     *
     * @return HTTPステータスの整数値
     */
    public int getStatus() {
        return httpStatus.value();
    }

    /**
     * HTTPステータスのエラー理由を返します.
     *
     * @return HTTPステータスのエラー理由
     */
    public String getError() {
        return httpStatus.getReasonPhrase();
    }
}
