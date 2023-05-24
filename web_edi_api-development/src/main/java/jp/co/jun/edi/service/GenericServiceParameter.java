package jp.co.jun.edi.service;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jp.co.jun.edi.security.CustomLoginUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * サービス共通パラメーター.
 */
@AllArgsConstructor
@Getter
public class GenericServiceParameter implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ログインユーザ情報. */
    private final CustomLoginUser loginUser;

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
