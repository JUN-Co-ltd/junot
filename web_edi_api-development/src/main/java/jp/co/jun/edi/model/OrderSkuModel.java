package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注SKU情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderSkuModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 品番. */
    private String partNo;

    /** 色. */
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 製品発注数. */
    private int productOrderLot;

    /** 製品裁断数. */
    private int productCutLot;

    /** 納品依頼数量. */
    private int deliveryLot;

    /** 入荷数量. */
    private int arrivalLot;

    /** 仕入数. */
    private int purchaseLot;

    /** 返品数量. */
    private int returnLot;

    /** 純仕入数. */
    private int netPurchaseLot;

    /** 発注完了日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date orderCompleteAt;

    /** 月末日（当月）. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date monthEndAt;

    /** 月末日（前月）. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date previousMonthEndAt;

    /** 月末日（前々月）. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date monthBeforeEndAt;

    /** 送信区分. */
    private String sendCode;
}
