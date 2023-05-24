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

/**
 * ExtendedTMakerReturnPDFEntity.
 * 返品明細PDFデータ用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTMakerReturnPDFEntity {

    /** 発注ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

    /** 伝票番号行. */
    @Column(name = "voucher_line")
    private String voucherLine;

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
}
