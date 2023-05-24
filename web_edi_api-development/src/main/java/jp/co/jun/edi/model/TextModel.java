package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * テキストのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** テキスト. */
    private String text;
}
