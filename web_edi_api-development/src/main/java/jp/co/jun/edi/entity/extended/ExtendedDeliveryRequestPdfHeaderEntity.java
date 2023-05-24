package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;

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
 * 納品依頼書PDFのヘッダEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedDeliveryRequestPdfHeaderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品Id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 回数. */
    @Column(name = "delivery_count")
    private String deliveryCount;

    /** B級品区分. */
    @Column(name = "non_conforming_product_type")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType nonConformingProductType;

    /** B級品単価. */
    @Column(name = "non_conforming_product_unit_price")
    private java.math.BigDecimal nonConformingProductUnitPrice;

    /** 取引先コード. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 取引先名称. */
    @Column(name = "name")
    private String name;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 上代. */
    @Column(name = "retail_price")
    private java.math.BigDecimal retailPrice;

    /** 単価. */
    @Column(name = "unit_price")
    private java.math.BigDecimal unitPrice;

    /** 会社名. */
    @Column(name = "company_name")
    private String companyName;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** アイテムコード. */
    @Column(name = "item_code")
    private String itemCode;

}
