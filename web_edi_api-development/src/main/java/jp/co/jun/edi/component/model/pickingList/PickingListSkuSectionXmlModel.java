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
 * ページ明細_SKUセクションのModel.
 *
 * [構成参照]
 * ・XmlModel
 *    L・[List]DetailXmlModel
 *      L・HeadXmlModel
 *      L・<b>SkuSectionXmlModel</b> ←〇
 *        L・[List]ReordColumnXmlModel
 *      L・SectionXmlModel
 *        L・[List]RecordXmlModel
 *          L・<b>SkuSectionXmlModel</b> ←〇
 *            L・[List]ReordColumnXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class PickingListSkuSectionXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** sku情報. */
    @XmlElement(name = "skus")
    private List<PickingListRecordColumnXmlModel> skus;

    /** sku合計情報. */
    @XmlElement(name = "sku_total")
    private PickingListRecordColumnXmlModel skuTotal;

}
