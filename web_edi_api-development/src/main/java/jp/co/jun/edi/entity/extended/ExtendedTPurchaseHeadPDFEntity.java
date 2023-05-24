package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
//PRD_0134 #10654 add JEF start
/**
 * ExtendedTPurchaseHeadPDFEntity.
 * 仕入明細PDFデータ用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTPurchaseHeadPDFEntity {
    /** 発注ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 入荷日. */
    @Column(name = "arrival_at")
    private Date arrivalAt;

    /** 課コード(確認中). */
    @Column(name = "division_code")
    private String divisionCode;

    /** 伝票番号. */
    @Column(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 郵便番号. */
    private String yubin;

    /** 住所1. */
    private String address1;

    /** 住所2. */
    private String addreess2;

    /** 住所3. */
    private String addreess3;

    /** 仕入先コード. */
    private String sire;

    /** 送付先名. */
    @Column(name = "send_to_name")
    private String sendToName;

    /** 会社名. */
    @Column(name = "company_name")
    private String companyName;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 仕入区分. */
    @Column(name = "purchase_type")
    private String purchaseType;

    /** 入荷場所. */
    @Column(name = "arrival_place")
    private String arrivalPlace;
//PRD_0134 #10654 add JEF end

    //PRD_0199 JFE add start
    /** 仕入先区分. */
    private String sirkbn;

    /** 発注ID. */
    private Integer order_id;

    /** 費目. */
    private String expense_item;

    /** 引取回数 */
    private String purchase_count;
    //PRD_0199 JFE add end
}
