package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
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
 * 納品依頼書PDFの明細Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedDeliveryRequestPdfDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** カラーコード名. */
    @Column(name = "color_code_name")
    private String colorCodeName;

    /** 課コード11数量. */
    @Column(name = "division_code11")
    private int divisionCode11;

    /** 課コード12数量. */
    @Column(name = "division_code12")
    private int divisionCode12;

    /** 課コード13数量. */
    @Column(name = "division_code13")
    private int divisionCode13;

    /** 課コード14数量. */
    @Column(name = "division_code14")
    private int divisionCode14;

    /** 課コード15数量. */
    @Column(name = "division_code15")
    private int divisionCode15;

    /** 課コード16数量. */
    @Column(name = "division_code16")
    private int divisionCode16;

    /** 課コード17数量. */
    @Column(name = "division_code17")
    private int divisionCode17;

    /** 課コード21数量. */
    @Column(name = "division_code21")
    private int divisionCode21;

    /** 課コード22数量. */
    @Column(name = "division_code22")
    private int divisionCode22;

    /** 課コード18数量. */
    @Column(name = "division_code18")
    private int divisionCode18;

}
