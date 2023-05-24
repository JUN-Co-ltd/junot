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
 * ページ明細_レコードセクション_明細のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseItemDetailsXmlModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** カラーコード. */
	@XmlElement(name = "color_code")
	private String colorCode;

	/** カラー名称. */
	@XmlElement(name = "color_name")
	private String colorName;

	/** サイズ 予定数. */
	@XmlElement(name = "plans_number")
	private List<String> plansNumber;

	/** サイズ 確定数. */
	@XmlElement(name = "confirm_number")
	private List<String> confirmNumber;

	/** 予定数合計または小計. */
	@XmlElement(name = "color_plans_subtotal")
	private String plansSubtotal;

	/** 確定数合計または小計. */
	@XmlElement(name = "color_confirm_subtotal")
	private String confirmSubtotal;

}
//PRD_0134 #10654 add JEF end
