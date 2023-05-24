package jp.co.jun.edi.component.model.pickingList;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * データ部のModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・<b>[List]DetailXmlModel</b> ←〇
 *      L・HeadXmlModel
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
public class PickingListDetailXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダ部. */
    @XmlElement(name = "page_head")
    private PickingListHeadXmlModel pageHead;

    /** レコードセクション. */
    @XmlElement(name = "record_section")
    private PickingListSectionXmlModel recordSection;

    /** SKUセクション. */
    @XmlElement(name = "sku_section")
    private PickingListSkuSectionXmlModel skuSection;

    /** ページ数. */
    @XmlElement(name = "page_number")
    private String pageNumber;

}
