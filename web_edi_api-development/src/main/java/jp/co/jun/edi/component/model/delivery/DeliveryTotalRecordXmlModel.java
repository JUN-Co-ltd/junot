package jp.co.jun.edi.component.model.delivery;


import java.io.Serializable;
import java.util.List;

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

public class DeliveryTotalRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 小計. */
    @XmlElement(name = "whole_subtotal_char")
    private String wholeSubtotalChar;

    /** 合計. */
    @XmlElement(name = "whole_total_amount_char")
    private String wholeTotalAmountChar;

    /** 課別合計. */
    @XmlElement(name = "total_by_section")
    private List<String> totalBySection;

    /** 全体合計. */
    @XmlElement(name = "overall_total")
    private String overallTotal;

    /** 納品場所. */
    @XmlElement(name = "delivery_location")
    private List<String> deliveryLocation;

}
