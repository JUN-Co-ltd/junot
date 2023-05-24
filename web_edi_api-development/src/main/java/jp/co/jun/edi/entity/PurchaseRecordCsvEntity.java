//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigInteger;

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
 * 仕入実績CSVのEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class PurchaseRecordCsvEntity implements Serializable {
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
    private String recordAt;

    /** 伝票No. */
    @Column(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 伝区. */
    @Column(name = "purchase_type")
    private String purchaseType;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 数量. */
    @Column(name = "fix_arrival_count")
    private String fixArrivalCount;

    /** m級. */
    @Column(name = "m_kyu")
    private String mKyu;

    /** 単価. */
    @Column(name = "purchase_unit_price")
    private String purchaseUnitPrice;

    /** 金額. */
    private String unitPriceSum;

    /** 費目. */
    private String expense_item;

    /** 仕入区分. */
    private String sirkbn;

    /** 発注No. */
    private Integer order_no;

    //PRD_0193 #11702 JFE add start
    /** 引取回数.*/
    private String purchase_count;
    //PRD_0193 #11702 JFE add end

}
//PRD_0133 #10181 add JFE end