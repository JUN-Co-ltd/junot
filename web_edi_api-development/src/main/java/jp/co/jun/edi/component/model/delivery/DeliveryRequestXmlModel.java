package jp.co.jun.edi.component.model.delivery;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品依頼書PDF作成情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "delivery_request")
public class DeliveryRequestXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ページヘッダ部. */
    @XmlElement(name = "page_head")
    private DeliveryPageHeadXmlModel pageHead;

    /** ページ明細. */
    @XmlElement(name = "page_details")
    private List<DeliveryPageDetailsXmlModel> pageDetails;

}
