package jp.co.jun.edi.component.model.directDelivery;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ヘッダ部のModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・[List]DetailXmlModel
 *      L・<b>HeadXmlModel</b> ←〇
 *      L・SectionXmlModel
 *        L・[List]RecordXmlModel
 *      L・TotalRecordXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectDeliveryHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 年月日. */
    @XmlElement(name = "date")
    private String date;

    /** 課コード. */
    @XmlElement(name = "division_code")
    private String divisionCode;

    /** 伝票番号. */
    @XmlElement(name = "voucher_number")
    private String voucherNumber;

    /** 郵便番号. */
    @XmlElement(name = "yubin")
    private String yubin;

    /** 住所1. */
    @XmlElement(name = "address1")
    private String address1;

    /** 住所2. */
    @XmlElement(name = "address2")
    private String address2;

    /** 住所3. */
    @XmlElement(name = "address3")
    private String address3;

    /** 御得意先様Co. */
    @XmlElement(name = "shop_code")
    private String shopCode;

    /** 御取引先名. */
    @XmlElement(name = "shop_name")
    private String shopName;

    /** 電話番号. */
    @XmlElement(name = "company_tel")
    private String companyTel;

    /** 出荷元. */
    @XmlElement(name = "shipping_origin")
    private String shippingOrigin;
}
