package jp.co.jun.edi.component.model.delivery;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
/**
 * DeliveryTotalRecordXmlModel.
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryPageDetailsXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ページNo. */
    @XmlElement(name = "page_number")
    private int pageNumber;

    /** ヘッダ. */
    @XmlElement(name = "head")
    private DeliveryHeadXmlModel head;

    /** レコードセクション. */
    @XmlElement(name = "record_section")
    private DeliveryRecordSectionXmlModel recordSection;



}
