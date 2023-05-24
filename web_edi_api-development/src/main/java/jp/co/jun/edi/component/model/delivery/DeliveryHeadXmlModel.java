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
public class DeliveryHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダレコード. */
    @XmlElement(name = "head_record")
    private List<DeliveryHeadRecordXmlModel> headRecord;



}
