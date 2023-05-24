package jp.co.jun.edi.component.model.delivery;


import java.io.Serializable;

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

public class DeliverySendToAddressXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** FAXNo. */
    @XmlElement(name = "nfax")
    private String nFax;

    /** 住所ラベル. */
    @XmlElement(name = "address_label")
    private String addressLabel;

    /** 郵便番号. */
    @XmlElement(name = "npost_label")
    private String nPostLabel;

    /** 住所. */
    @XmlElement(name = "naddress")
    private String nAddress;

    /** TEL. */
    @XmlElement(name = "nphone_number")
    private String nPhoneNumber;

    /** 撮影用注意ラベル. */
    @XmlElement(name = "shooting_caution_label")
    private String shootingCautionLabel;

    /** 出荷日. */
    @XmlElement(name = "cargo_at")
    private String cargoAt;

    /** 運送会社. */
    @XmlElement(name = "shipping_company")
    private String shippingCompany;

    /** 送り状. */
    @XmlElement(name = "invoice")
    private String invoice;

    /** 備考. */
    @XmlElement(name = "remarks")
    private String remarks;
}
