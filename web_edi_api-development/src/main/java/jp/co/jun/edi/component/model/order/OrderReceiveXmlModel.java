package jp.co.jun.edi.component.model.order;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 受注確定書のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "order_receive")
public class OrderReceiveXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダ部. */
    @XmlElement(name = "page_head")
    private OrderPageHeadXmlModel pageHead;

    /** ページ部. */
    @XmlElement(name = "page_details")
    private List<OrderPageDetailsXmlModel> pageDetails;

    /** ページ品質表示部. */
    @XmlElement(name = "page_quality_display")
    private  List<OrderPageQualityDisplayXmlModel> pageQualityDisplay;


}
