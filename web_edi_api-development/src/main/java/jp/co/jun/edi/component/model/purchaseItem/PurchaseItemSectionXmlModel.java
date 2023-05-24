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
 * ページ明細_レコードセクションのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class PurchaseItemSectionXmlModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 明細. */
	@XmlElement(name = "details")
	private List<PurchaseItemDetailsXmlModel> details;

	/** 合計レコード. */
	@XmlElement(name = "total_record")
	private PurchaseItemTotalRecordXmlModel totalRecord;

}
//PRD_0134 #10654 add JEF end
