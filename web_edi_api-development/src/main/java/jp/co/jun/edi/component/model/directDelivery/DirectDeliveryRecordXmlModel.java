package jp.co.jun.edi.component.model.directDelivery;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * レコードセクションのModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・[List]DetailXmlModel
 *      L・HeadXmlModel
 *      L・SectionXmlModel
 *        L・<b>[List]RecordXmlModel</b> ←〇
 *      L・TotalRecordXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectDeliveryRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番-{カラーコード}{サイズ}. */
    @XmlElement(name = "part_no_color_size")
    private String partNoColorSize;

    /** 品名. */
    @XmlElement(name = "product_name")
    private String productName;

    /** 上代. */
    @XmlElement(name = "retail_price")
    private String retailPrice;

    /** 納品数量. */
    @XmlElement(name = "delivery_lot")
    private String deliveryLot;

    /** 上代金額. */
    @XmlElement(name = "retail_price_sub_total")
    private String retailPriceSubTotal;

}
