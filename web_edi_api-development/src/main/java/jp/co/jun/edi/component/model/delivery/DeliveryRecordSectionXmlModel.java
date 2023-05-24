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

public class DeliveryRecordSectionXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 明細. */
    @XmlElement(name = "details")
    private List<DeliveryDetailsXmlModel> details;

    /** 合計レコード. */
    @XmlElement(name = "total_record")
    private DeliveryTotalRecordXmlModel totalRecord;

    /** 送付先. */
    @XmlElement(name = "send_to_address")
    private DeliverySendToAddressXmlModel sendToAddress;



}
