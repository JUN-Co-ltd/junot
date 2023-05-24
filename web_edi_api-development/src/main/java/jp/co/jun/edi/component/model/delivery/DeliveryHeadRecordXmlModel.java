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

public class DeliveryHeadRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 依頼No. */
    @XmlElement(name = "request_number")
    private String requestNumber;

    /** 課名. */
    @XmlElement(name = "division_name")
    private String divisionName;

    /** 課コード. */
    private String divisionCode;

}
