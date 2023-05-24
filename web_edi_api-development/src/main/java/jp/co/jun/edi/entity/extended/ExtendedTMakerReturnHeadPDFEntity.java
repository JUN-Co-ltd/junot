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

/**
 * ExtendedTMakerReturnPDFEntity.
 * 返品明細PDFデータ用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTMakerReturnHeadPDFEntity {
    /** 発注ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 返金日. */
    @Column(name = "return_at")
    private Date returnDate;

    /** 課コード(確認中). */
    @Column(name = "division_code")
    private String divisionCode;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

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

}
