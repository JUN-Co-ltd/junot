//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入実績一覧画面用の検索結果のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseRecordSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 仕入先コード. */
    private String supplierCode;

    /** 仕入先名称. */
    private String supplierName;

    /** スポット・工場. */
    private String arrivalPlace;

    /** 物流コード. */
    private String logisticsCode;

    /** 計上日. */
    private Date recordAt;

    /** 伝票No. */
    private Integer purchaseVoucherNumber;

    /** 伝区. */
    private String purchaseType;

    /** 品番. */
    private String partNo;

    /** 数量. */
    private Integer fixArrivalCount;

    /** m級. */
    private BigDecimal mkyu;

    /** 単価. */
    private Integer purchaseUnitPrice;

    /** 金額. */
    private Integer unitPriceSum;

    /** 数量合計. */
    private BigInteger fixArrivalCountSum;

    /** m級合計. */
    private BigDecimal mKyuSum;

    /** 金額(単価*数量). */
    private BigInteger unitPriceSumTotal;

    // PRD_0162 #10181 jfe add start
    /** ファイル情報ID. */
    private Integer fileInfoId;
    // PRD_0162 #10181 jfe add end

}
//PRD_0133 #10181 add JFE end