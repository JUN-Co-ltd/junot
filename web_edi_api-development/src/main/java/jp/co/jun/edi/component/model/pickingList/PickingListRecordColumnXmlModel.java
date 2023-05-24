package jp.co.jun.edi.component.model.pickingList;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 一覧のカラムセクションのModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・[List]DetailXmlModel
 *      L・HeadXmlModel
 *      L・SkuSectionXmlModel
 *        L・<b>[List]ReordColumnXmlModel</b> ←〇
 *      L・SectionXmlModel
 *        L・[List]RecordXmlModel
 *          L・SkuSectionXmlModel
 *            L・<b>[List]ReordColumnXmlModel</b> ←〇
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PickingListRecordColumnXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** カラーコード. */
    @XmlElement(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @XmlElement(name = "size")
    private String size;

    /** 数量. */
    @XmlElement(name = "delivery_lot")
    private String deliveryLot;

    /** 入荷数. */
    @XmlElement(name = "arrival_lot")
    private String arrivalLot;

    /** 小計. */
    @XmlElement(name = "sub_total_lot")
    private String subTotalLot;

}
