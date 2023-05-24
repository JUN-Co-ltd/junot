package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ブランドコードのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandCodeModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ブランドコード. */
    private String brandCode;

    /** ブランド名. */
    private String brandName;
}
