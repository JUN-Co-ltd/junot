package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * フクキタル発注SKU情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruOrderSkuModel implements Serializable {
    private static final long serialVersionUID = 1L;


    /** フクキタル発注SKU情報ID. */
    private BigInteger id;

    /** フクキタル発注ID. */
    private BigInteger fOrderId;

    /** カラーコード. */
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 資材ID. */
    private BigInteger materialId;

    /** 資材数量. */
    private Integer orderLot;

    /** 資材種類. */
    private Integer materialType;

    /** 資材種類名. */
    private String materialTypeName;

    /** 資材コード. */
    private String materialCode;

    /** 資材コード名. */
    private String materialCodeName;

    /** 並び順. */
    private Integer sortOrder;

}
