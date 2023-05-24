package jp.co.jun.edi.component.model.order;

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

public class OrderPageDetailsXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダ. */
    @XmlElement(name = "head")
    private OrderHeadXmlModel head;

    /** レコードセクション. */
    @XmlElement(name = "record_section")
    private OrderRecordSectionXmlModel recordSection;

    /** 表示ページ. */
    @XmlElement(name = "page_number")
    private Integer pageNumber;
}
