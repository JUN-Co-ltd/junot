package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 他社SKU情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalSkuModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** SKUID. */
    private BigInteger skuId;

    /** 他社品番. */
    private String externalPartNo;

    /** 他社色. */
    private String externalColorCode;

    /** 他社サイズ. */
    private String externalSize;
}
