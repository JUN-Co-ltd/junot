package jp.co.jun.edi.component.model.directDelivery;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ページ明細_レコードセクション_合計レコードのModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・[List]DetailXmlModel
 *      L・HeadXmlModel
 *      L・SectionXmlModel
 *         L・[List]RecordXmlModel
 *      L・<b>TotalRecordXmlModel</b> ←〇
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectDeliveryTotalRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 数量合計. */
    @XmlElement(name = "lot_total")
    private String lotTotal;
}
