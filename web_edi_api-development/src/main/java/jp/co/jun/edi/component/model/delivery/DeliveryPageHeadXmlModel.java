package jp.co.jun.edi.component.model.delivery;

import java.io.Serializable;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * DeliveryTotalRecordXmlModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryPageHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品日付. */
    @XmlElement(name = "delivery_at")
    private String deliveryAt;

    /** 納品No. */
    @XmlElement(name = "delivery_number")
    private String deliveryNumber;

    /** 発注No. */
    @XmlElement(name = "order_number")
    private BigInteger orderNumber;

    /** 回数. */
    @XmlElement(name = "delivery_count")
    private String deliveryCount;

    /** 取引先コード. */
    @XmlElement(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 取引先名称. */
    @XmlElement(name = "name")
    private String name;

    /** 品番. */
    @XmlElement(name = "part_no")
    private String partNo;

    /** 品名. */
    @XmlElement(name = "product_name")
    private String productName;

    /** 上代. */
    @XmlElement(name = "retail_price")
    private java.math.BigDecimal retailPrice;

    /** 数量. */
    @XmlElement(name = "delivery_lot")
    private java.math.BigDecimal deliveryLot;

    /** 単価. */
    @XmlElement(name = "unit_price")
    private java.math.BigDecimal unitPrice;

    /** 金額. */
    @XmlElement(name = "price")
    private java.math.BigDecimal price;

    /** 住所. */
    @XmlElement(name = "address")
    private String address;

    /** 会社名. */
    @XmlElement(name = "company_name")
    private String companyName;

    /** ブランド名. */
    @XmlElement(name = "brand_name")
    private String brandName;
}
