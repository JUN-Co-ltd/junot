package jp.co.jun.edi.component.model.order;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 *部
 * ページ明細_レコードセクション_合計レコードのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class OrderDetailOfQualityDisplayXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品質表示(共通) パーツ名. */
    @XmlElement(name = "quality_display_parts")
    private String qualityDisplayParts;

    /** 品質表示(共通) 組成名. */
    @XmlElement(name = "quality_display_composition")
    private String qualityDisplayComposition;

    /** 品質表示(共通) 率. */
    @XmlElement(name = "quality_display_rate")
    private Integer qualityDisplayRate;

}
