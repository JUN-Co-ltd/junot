package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * フクキタル用資材情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruMasterModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** ID. */
    private BigInteger id;

    /** コード. */
    private String code;

    /** コード名称. */
    private String codeName;

    /** 出荷単位. */
    private Integer moq;
}
