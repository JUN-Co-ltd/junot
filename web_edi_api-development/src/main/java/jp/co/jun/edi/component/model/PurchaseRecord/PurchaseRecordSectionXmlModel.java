//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component.model.PurchaseRecord;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class PurchaseRecordSectionXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 明細. */
    @XmlElement(name = "details")
    private List<PurchaseRecordXmlModel> details;

}
//PRD_0133 #10181 add JFE end