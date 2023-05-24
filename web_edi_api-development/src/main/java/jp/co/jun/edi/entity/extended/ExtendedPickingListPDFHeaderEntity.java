package jp.co.jun.edi.entity.extended;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * ピッキングリストPDFフォーマットのヘッダ用Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedPickingListPDFHeaderEntity {

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** 課(配分課). */
    @Column(name = "division_code")
    private String divisionCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** B級品区分. */
    @Column(name = "non_conforming_product_type")
    private boolean nonConformingProductType;

    /** B級品単価. */
    @Column(name = "non_conforming_product_unit_price")
    private BigDecimal nonConformingProductUnitPrice;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 修正納期. */
    @Column(name = "correction_at")
    private Date correctionAt;

    /** 配分完納フラグ. */
    @Column(name = "allocation_complete_payment_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType allocationCompletePaymentFlg;

    /** 入荷日. */
    @Column(name = "arrival_at")
    private Date arrivalAt;

    /** 仕入れ先コード(m_sirmst). */
    @Column(name = "sire")
    private String sire;

    /** 仕入れ先正式名称(m_sirmst). */
    @Column(name = "name")
    private String name;
}
