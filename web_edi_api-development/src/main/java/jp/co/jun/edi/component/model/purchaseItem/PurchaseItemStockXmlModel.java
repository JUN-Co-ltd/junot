package jp.co.jun.edi.component.model.purchaseItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

//PRD_0134 #10654 add JEF start
/**
 * ページ入荷情報部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseItemStockXmlModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 入荷先コード. */
	@XmlElement(name = "stock_code")
	private String stockCode;

	/** 入荷先名. */
	@XmlElement(name = "stock_name")
	private String stockName;

	/** 上代. */
	@XmlElement(name = "retail_price")
	private BigDecimal retailPrice;

	/** 依頼No. */
	@XmlElement(name = "request_number")
	private String requestNumber;

	/** 発注No. */
	@XmlElement(name = "order_number")
	private BigInteger orderNumber;

	/** 完納区分. */
	@XmlElement(name = "all_completion_type")
	private int allCompletionType;

	/** 完納区分名. */
	@XmlElement(name = "full_payment_name")
	private String fullPaymentName;

	/** 部門コード. */
	@XmlElement(name = "division_code")
	private String divisionCode;

	/** ブランド名. */
	@XmlElement(name = "brand_name")
	private String brandName;

	/** アイテム名. */
	@XmlElement(name = "item_name")
	private String itemName;

	/** 数量. */
	@XmlElement(name = "quantity")
	private BigDecimal quantity;

	/** 単価. */
	@XmlElement(name = "unit_price")
	private BigDecimal unitPrice;

	/** B級品単価. */
	@XmlElement(name = "non_conforming_product_unit_price")
	private BigDecimal nonConformingProductUnitPrice;

	/** 金額. */
	@XmlElement(name = "price")
	private BigDecimal price;
}
//PRD_0134 #10654 add JEF end
