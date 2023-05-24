package jp.co.jun.edi.component.model.purchaseItem;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

//PRD_0134 #10654 add JEF start
/**
 * データ部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseItemDetailXmlModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** ヘッダ. */
	@XmlElement(name = "head")
	private PurchaeItemHeadXmlModel head;

	/** レコードセクション. */
	@XmlElement(name = "record_section")
	private PurchaeItemSectionXmlModel recordSection;

	/** 表示ページ. */
	@XmlElement(name = "page_number")
	private Integer pageNumber;
}
//PRD_0134 #10654 add JEF end
