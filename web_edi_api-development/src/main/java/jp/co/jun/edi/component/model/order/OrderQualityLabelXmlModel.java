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

public class OrderQualityLabelXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** カラーコード(四角一個). */
    @XmlElement(name = "color_code")
    private String colorCode;

    /** 品質表示詳細. */
    @XmlElement(name = "detail_of_quality_display")
    private List<OrderDetailOfQualityDisplayXmlModel> detailOfQualityDisplay;

}
