package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.MessageCodeType;
import lombok.Data;

/**
 * エラー詳細Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetailModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * メッセージコード.
     */
    private MessageCodeType code;

    /**
     * メッセージの引数.
     */
    private Object[] args;

    /**
     * メッセージ.
     */
    private String message;

    /**
     * 対象のリソース.
     */
    private String resource;

    /**
     * 対象のフィールド.
     */
    private String field;

    /**
     * 対象の値.
     */
    private Object value;
}
