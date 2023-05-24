//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component.model.PurchaseRecord;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * レコードセクションのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 仕入先コード. */
    @XmlElement(name = "supplier_code")
    private String supplierCode;

    /** 仕入先名称. */
    @XmlElement(name = "supplier_name")
    private String supplierName;

    /** スポット・工場. */
    @XmlElement(name = "arrival_place")
    private String arrivalPlace;

    /** 物流コード. */
    @XmlElement(name = "logistics_code")
    private String logisticsCode;

    /** 計上日. */
    @XmlElement(name = "record_at")
    private String recordAt;

    /** 伝票No. */
    @XmlElement(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 伝区. */
    @XmlElement(name = "purchase_type")
    private String purchaseType;

    /** 品番. */
    @XmlElement(name = "part_no")
    private String partNo;

    /** 数量. */
    @XmlElement(name = "fix_arrival_count")
    private String fixArrivalCount;

    /** m級. */
    @XmlElement(name = "m_kyu")
    private String mKyu;

    /** 単価. */
    @XmlElement(name = "purchase_unit_price")
    private String purchaseUnitPrice;

    /** 金額. */
    @XmlElement(name="unit_price_sum")
    private String unitPriceSum;

    /** 費目. */
    private String expense_item;

    /** 仕入区分. */
    private String sirkbn;

    /** 発注No. */
    private Integer order_no;

    //PRD_0193 #11702 JFE add start
    /** 引取回数.*/
    private String purchase_count;
    //PRD_0193 #11702 JFE add end

}
//PRD_0133 #10181 add JFE start