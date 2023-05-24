//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 仕入実績一覧検索結果格納用のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class PurchaseRecordCompositeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 仕入先コード. */
    @Column(name = "supplier_code")
    private String supplierCode;

    /** 仕入先名称. */
    @Column(name = "supplier_name")
    private String supplierName;

    /** スポット・工場. */
    @Column(name = "arrival_place")
    private String arrivalPlace;

    /** 物流コード. */
    @Column(name = "logistics_code")
    private String logisticsCode;

    /** 計上日. */
    @Column(name = "record_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date recordAt;

    /** 伝票No. */
    @Column(name = "purchase_voucher_number")
    private Integer purchaseVoucherNumber;

    /** 伝区. */
    @Column(name = "purchase_type")
    private String purchaseType;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 数量. */
    @Column(name = "fix_arrival_count")
    private Integer fixArrivalCount;

    /** m級. */
    //@Column(name = "m_kyu")
    private BigDecimal mkyu;

    /** 単価. */
    @Column(name = "purchase_unit_price")
    private Integer purchaseUnitPrice;

    /** 金額. */
    //@Column(name = "unit_price_sum")
    private Integer unitPriceSum;

    /** 数量合計. */
    @Column(name = "fix_arrival_count_sum")
    private BigInteger fixArrivalCountSum;

    /** m級合計. */
    //@Column(name = "m_kyu_sum")
    private BigDecimal mKyuSum;

    /** 金額(単価*数量). */
    @Column(name = "unit_price_sum_total")
    private BigInteger unitPriceSumTotal;

    //PRD_0193 #11702 JFE add start
    /** 引取回数.*/
    private String purchase_count;
    //PRD_0193 #11702 JFE add end

    /** 費目. */
    private String expense_item;

    /** 仕入区分. */
    private String sirkbn;

    /** 発注No. */
    private Integer order_no;

    // PRD_0166 #10181 jfe mod start
//    // PRD_0162 #10181 jfe add start
//    /** ファイル情報ID. */
//    private Integer file_info_id;
//    // PRD_0162 #10181 jfe add end
    /** ファイル情報ID. */
    @Column(name = "file_info_id")
    private Integer fileInfoId;
    // PRD_0166 #10181 jfe mod end

}
//PRD_0133 #10181 add JFE end