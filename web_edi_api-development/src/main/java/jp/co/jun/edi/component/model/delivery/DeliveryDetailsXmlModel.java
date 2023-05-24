package jp.co.jun.edi.component.model.delivery;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
/**
 * DeliveryDetailsXmlModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryDetailsXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** サイズ. */
    @XmlElement(name = "size")
    private String size;

    /** カラーコード. */
    @XmlElement(name = "color_code")
    private String colorCode;

    /** カラーコード名. */
    @XmlElement(name = "color_code_name")
    private String colorCodeName;

    /** 納品依頼数. */
    @XmlElement(name = "delivery_lot")
    private List<String> orderDeliveryLot;

    /** カラー・サイズ小計. */
    @XmlElement(name = "color_size_subtotal")
    private String colorSizeSubtotal;

}
