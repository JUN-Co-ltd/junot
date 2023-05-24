package jp.co.jun.edi.component.model.order;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * データ部
 * ページ明細_レコードセクション_明細のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class OrderDetailsXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** カラーコード. */
    @XmlElement(name = "color_code")
    private String colorCode;

    /** カラー名称. */
    @XmlElement(name = "color_name")
    private String colorName;

    /** 反数. */
    @XmlElement(name = "cloth_count")
    private String clothCount;

    /** メーター数. */
    @XmlElement(name = "quantity_divided_meter")
    private String quantityDividedMeter;

    /** 合計または小計. */
    @XmlElement(name = "total_amount")
    private String totalAmount;

    /** サイズ 数量. */
    @XmlElement(name = "size_quantity")
    private List<String> sizeQuantity;

}
