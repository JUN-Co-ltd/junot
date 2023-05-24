package jp.co.jun.edi.component.model.pickingList;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * データ部
 * ページ明細_レコードセクションのModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・[List]DetailXmlModel
 *      L・HeadXmlModel
 *      L・SkuSectionXmlModel
 *        L・[List]ReordColumnXmlModel
 *      L・<b>SectionXmlModel</b> ←〇
 *        L・[List]RecordXmlModel
 *          L・SkuSectionXmlModel
 *            L・[List]ReordColumnXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class PickingListSectionXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 明細. */
    @XmlElement(name = "details")
    private List<PickingListRecordXmlModel> details;
}
