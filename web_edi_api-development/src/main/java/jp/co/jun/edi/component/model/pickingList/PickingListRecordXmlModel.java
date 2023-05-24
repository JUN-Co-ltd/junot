package jp.co.jun.edi.component.model.pickingList;

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
 *      L・SkuSectionXmlModel
 *        L・[List]ReordColumnXmlModel
 *      L・SectionXmlModel
 *        L・<b>[List]RecordXmlModel</b> ←〇
 *          L・SkuSectionXmlModel
 *            L・[List]ReordColumnXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PickingListRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 店舗コード. */
    @XmlElement(name = "shop_code")
    private String shopCode;

    /** 店舗名. */
    @XmlElement(name = "shop_name")
    private String shopName;

    /** SKUセクション. */
    @XmlElement(name = "sku_section")
    private PickingListSkuSectionXmlModel skuSection;

    /** 数量合計. */
    @XmlElement(name = "lot_total")
    private String lotTotal;

    /** 出荷日. */
    @XmlElement(name = "shipping_at")
    private String shippingAt;

    /** 保留/フリー. */
    @XmlElement(name = "allocation_type")
    private String allocationType;

}
