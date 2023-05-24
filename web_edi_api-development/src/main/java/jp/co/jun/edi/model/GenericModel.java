package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 共通のModel.
 */
@Data
public abstract class GenericModel implements Serializable {
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

    /**
     * ログ出力するオブジェクトに変換する.
     *
     * @param model オブジェクト
     * @return ログ出力するオブジェクト
     */
    @JsonIgnore
    public static Object toLogObject(final GenericModel model) {
        if (model == null) {
            return null;
        } else {
            return model.getLogObject();
        }
    }
}
