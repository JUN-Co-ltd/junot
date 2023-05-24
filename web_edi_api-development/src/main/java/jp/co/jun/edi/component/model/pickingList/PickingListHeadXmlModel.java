package jp.co.jun.edi.component.model.pickingList;

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
 *      L・SkuSectionXmlModel
 *        L・[List]ReordColumnXmlModel
 *      L・SectionXmlModel
 *        L・[List]RecordXmlModel
 *          L・SkuSectionXmlModel
 *            L・[List]ReordColumnXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PickingListHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 年月日. */
    @XmlElement(name = "date")
    private String date;

    /** ページ総数. */
    @XmlElement(name = "page_number_total")
    private String pageNumberTotal;

    /** ブランドコード. */
    @XmlElement(name = "brand_code")
    private String brandCode;

    /** ブランド名. */
    @XmlElement(name = "brand_name")
    private String brandName;

    /** 課コード. */
    @XmlElement(name = "division_code")
    private String divisionCode;

    /** 課名. */
    @XmlElement(name = "division_name")
    private String divisionName;

    /** 品番. */
    @XmlElement(name = "part_no")
    private String partNo;

    /** 品名. */
    @XmlElement(name = "product_name")
    private String productName;

    /** 発注No. */
    @XmlElement(name = "order_number")
    private String orderNumber;

    /** 回数. */
    @XmlElement(name = "delivery_count")
    private String deliveryCount;

    /** 上代. */
    @XmlElement(name = "retail_price")
    private String retailPrice;

    /** 数量合計. */
    @XmlElement(name = "lot_total")
    private String lotTotal;

    /** 配分予定日. */
    @XmlElement(name = "correction_at")
    private String correctionAt;

    /** 区分. */
    @XmlElement(name = "allocation_complete_payment_flg")
    private String allocationCompletePaymentFlg;

    /** 取引先コード. */
    @XmlElement(name = "supplier_code")
    private String supplierCode;

    /** 取引先名. */
    @XmlElement(name = "supplier_name")
    private String supplierName;

    /** 入荷日. */
    @XmlElement(name = "arrival_at")
    private String arrivalAt;

    /** ストアコード. */
    @XmlElement(name = "store_code")
    private String storeCode;
}
