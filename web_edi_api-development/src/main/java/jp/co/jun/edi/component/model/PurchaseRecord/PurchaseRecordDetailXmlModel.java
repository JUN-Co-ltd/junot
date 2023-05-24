//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component.model.PurchaseRecord;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * データ部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseRecordDetailXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダ部. */
    @XmlElement(name = "page_head")
    private PurchaseRecordHeadXmlModel pageHead;

    /** レコードセクション. */
    @XmlElement(name = "record_section")
    private PurchaseRecordSectionXmlModel recordSection;


    /** ページ数. */
    @XmlElement(name = "page_number")
    private String pageNumber;

}
//PRD_0133 #10181 add JFE start