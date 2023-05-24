package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.MessageCodeType;
import lombok.Data;

/**
 * エラー内容Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorContentModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageCodeType code;

    private Object value;

    /**
     * コンストラクタ.
     */
    public ErrorContentModel() {

    }

    /**
     * コンストラクタ.
     *
     * @param code エラーコード
     */
    public ErrorContentModel(final MessageCodeType code) {
        this.code = code;
        this.value = null;
    }

    /**
     * コンストラクタ.
     *
     * @param code エラーコード
     * @param value 値
     */
    public ErrorContentModel(final MessageCodeType code, final Object value) {
        this.code = code;
        this.value = value;
    }
}
