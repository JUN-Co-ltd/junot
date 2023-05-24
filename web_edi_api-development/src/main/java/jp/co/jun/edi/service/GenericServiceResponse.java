package jp.co.jun.edi.service;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * サービス共通レスポンス.
 */
@Data
public abstract class GenericServiceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ログ出力するオブジェクトを取得する.
     *
     * @return ログ出力するオブジェクト
     */
    @JsonIgnore
    public Object getLogObject() {
        return this;
    }
}
