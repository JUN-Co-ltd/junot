package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

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
 * ExtendedTPurchasePDFEntity.
 * 仕入明細PDFデータ用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTPurchasePDFEntity {

    /** 発注ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝票番号. */
    @Column(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 伝票番号行. */
    @Column(name = "purchase_voucher_line")
    private String purchaseVoucherLine;

    /** サイズ. */
    private String size;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** カラーコード名. */
    @Column(name = "color_code_name")
    private String colorCodeName;

    /** 予定数(固定). */
    @Column(name = "plans_number")
    private int plansNumber;

    /** 確定数. */
    @Column(name = "confirm_number")
    private int confirmNumber;

    /** ソート順. */
    @Column(name = "sort_order")
    private String sortOrder;
	//PRD_0134 #10654 add JEF end
}
