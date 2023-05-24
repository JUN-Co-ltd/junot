//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component.model.PurchaseRecord;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ヘッダ部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseRecordHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 年月日. */
    @XmlElement(name = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private String date;
}
//PRD_0133 #10181 add JFE end