package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import lombok.Data;

/**
 * 入力補助セット詳細のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruInputAssistSetDetailsModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** コード. */
    private String code;

    /** コード名称. */
    private String codeName;

    /** 資材種別. */
    private FukukitaruMasterMaterialType materialType;

}
