package jp.co.jun.edi.component.model.purchaseItem;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

//PRD_0134 #10654 add JEF start
/**
 * ヘッダ部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseItemHeadXmlModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 入荷日. */
	@XmlElement(name = "arrival_at")
	private String arrivalAt;

	/** 伝区. */
	@XmlElement(name = "slip_kbn")
	private String slipKbn;

	/** 課コード. */
	@XmlElement(name = "division_code")
	private String divisionCode;

	/** 伝票番号. */
	@XmlElement(name = "purchase_voucher_number")
	//PRD_0152 mod JFE start
//	private String PurchaseVoucherNumber;
	private String purchaseVoucherNumber;
	//PRD_0152 mod JFE end

	/** 郵便番号. */
	@XmlElement(name = "yubin")
	private String yubin;

	/** 住所1. */
	@XmlElement(name = "address1")
	private String address1;

	/** 住所2. */
	@XmlElement(name = "address2")
	private String address2;

	/** 住所3. */
	@XmlElement(name = "address3")
	private String address3;

	/** 御取引先コード. */
	@XmlElement(name = "sire")
	private String sire;

	/** 御取引先名. */
	@XmlElement(name = "send_to_name")
	private String sendToName;

	/** 会社名. */
	@XmlElement(name = "company_name")
	private String companyName;

	/** ブランド名. */
	@XmlElement(name = "brand_name")
	private String brandName;

	/** 会社情報(郵便番号). */
	@XmlElement(name = "company_yubin")
	private String companyYubin;

	/** 会社情報(住所). */
	@XmlElement(name = "company_address")
	private String companyAddress;

	/** 会社情報(電話番号). */
	@XmlElement(name = "company_tel")
	private String companyTel;

	/** 品番. */
	@XmlElement(name = "part_no")
	private String partNo;

	/** 品名. */
	@XmlElement(name = "product_name")
	private String productName;

}
//PRD_0134 #10654 add JEF end
