package jp.co.jun.edi.component.model.purchaseItem;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

//PRD_0134 #10654 add JEF start
/**
 * データ部
 * ページ明細_ヘッダ_ヘッダレコード_サイズのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaeItemHeadRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** サイズ. */
    @XmlElement(name = "size")
    private List<String> size;

}
//PRD_0134 #10654 add JEF end