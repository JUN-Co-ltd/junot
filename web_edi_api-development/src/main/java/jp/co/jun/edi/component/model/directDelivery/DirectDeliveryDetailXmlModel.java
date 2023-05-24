package jp.co.jun.edi.component.model.directDelivery;

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
 *      L・SectionXmlModel
 *        L・[List]RecordXmlModel
 *      L・TotalRecordXmlModel
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectDeliveryDetailXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダ部. */
    @XmlElement(name = "page_head")
    private DirectDeliveryHeadXmlModel pageHead;

    /** レコードセクション. */
    @XmlElement(name = "record_section")
    private DirectDeliverySectionXmlModel recordSection;

    /** 合計レコード. */
    @XmlElement(name = "total_record")
    private DirectDeliveryTotalRecordXmlModel totalRecord;

}
