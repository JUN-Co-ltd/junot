package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * TAGDATメールブランドデータ件数情報用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagdatMailInfoBrandCountModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ブランド. */
    private String brand;

    /** 件数. */
    private int count;
}
