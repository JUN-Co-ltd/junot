package jp.co.jun.edi.component.model.purchaseItem;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

//PRD_0134 #10654 add JEF start
/**
 * 仕入明細のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "return_item")
public class PurchaseItemXmlModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** ヘッダ部. */
	@XmlElement(name = "page_head")
	private PurchaseItemHeadXmlModel pageHead;

	/** ページ入荷情報部. */
	@XmlElement(name = "stock_info")
	private PurchaseItemStockXmlModel pageStock;

	/** ページ品質表示部. */
	@XmlElement(name = "page_details")
	private  List<PurchaseItemDetailXmlModel> pageDetails;

}
//PRD_0134 #10654 add JEF end
