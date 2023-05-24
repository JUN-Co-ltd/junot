//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入実績CSV一覧画面用の検索結果のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseRecordCsvModel implements Serializable {
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private String recordAt;

    /** 伝票No. */
    private String purchaseVoucherNumber;

    /** 伝区. */
    private String purchaseType;

    /** 品番. */
    private String partNo;

    /** 数量. */
    private String fixArrivalCount;

    /** m級. */
    private String mKyu;

    /** 単価. */
    private String purchaseUnitPrice;

    /** 金額. */
    private String unitPriceSum;


}
//PRD_0133 #10181 add JFE end