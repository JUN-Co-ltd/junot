package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * SKU情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkuModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 色. */
    private String colorCode;

    /** 色名称. */
    private String colorName;

    /** サイズ. */
    private String size;

    /** JANコード. */
    private String janCode;

    /** 代表JANフラグ. */
    private BooleanType representationJanFlg;

    /** JANコード名称. */
    private String janName;

    /** 他社SKU. */
    private ExternalSkuModel externalSku;
}
