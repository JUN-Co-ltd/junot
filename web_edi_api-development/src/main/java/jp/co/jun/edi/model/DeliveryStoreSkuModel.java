package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品得意先SKU情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStoreSkuModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 納品得意先ID. */
    private BigInteger deliveryStoreId;

    /** サイズ. */
    private String size;

    /** 色. */
    private String colorCode;

    /** 納品数量. */
    private int deliveryLot;

    /** 入荷数量. */
    private int arrivalLot;

    /** 色名称. */
    private String colorName;

    /** 課名称. */
    private String divisionName;
}
