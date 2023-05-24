package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 店別在庫情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopStockModel implements Serializable {

	private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 店舗コード */
    private String shopCode;

    /** 商品コード */
    private String productCode;

    /** 品番 */
    private String partNo;

    /** カラーコード */
    private String colorCode;

    /** サイズ */
    private String size;

    /** 在庫数 */
    private Integer stockLot;

}
