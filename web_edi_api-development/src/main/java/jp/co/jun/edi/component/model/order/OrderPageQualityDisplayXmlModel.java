package jp.co.jun.edi.component.model.order;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 品質表示部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderPageQualityDisplayXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 表示ページ. */
    @XmlElement(name = "page_number")
    private Integer pageNumber;

    /** 品質表示. */
    @XmlElement(name = "quality_label")
    private List<OrderQualityLabelXmlModel> qualityLabel;

}
