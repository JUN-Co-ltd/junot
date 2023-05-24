package jp.co.jun.edi.component.model.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ヘッダ部のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderPageHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /** 送付先名. */
    @XmlElement(name = "send_to_name")
    private String sendToName;

    /** 仕入先コード. */
    @XmlElement(name = "sire")
    private String sire;

    /** 送付先電話番号. */
    @XmlElement(name = "hphone")
    private String hphone;

    /** ページNo. */
    @XmlElement(name = "page_number")
    private int pageNumber;

    /** 全体ページNo. */
    @XmlElement(name = "total_page_number")
    private int totalPageNumber;

    /** 発注No. */
    @XmlElement(name = "order_number")
    private String orderNumber;

    /** 費目コード. */
    @XmlElement(name = "expense_item_code")
    private String expenseItemCode;

    /** 費目名称. */
    @XmlElement(name = "expense_item_name")
    private String expenseItemName;

    /** 年月日. */
    @XmlElement(name = "product_order_date")
    private String productOrderDate;

    /** 部門コード. */
    @XmlElement(name = "division_code")
    private String divisionCode;

    /** 事業部名. */
    @XmlElement(name = "division_name")
    private String divisionName;

    /** アイテム名. */
    @XmlElement(name = "item_name")
    private String itemName;

    /** 原産国. */
    @XmlElement(name = "country_of_origin")
    private String countryOfOrigin;

    /** 数量. */
    @XmlElement(name = "quantity")
    private BigDecimal quantity;

    /** 単価. */
    @XmlElement(name = "unit_price")
    private BigDecimal unitPrice;

    /** 金額. */
    @XmlElement(name = "price")
    private BigDecimal price;

    /** 製品納期. */
    @XmlElement(name = "product_delivery_date")
    private String productDeliveryDate;

    /** 製品番号. */
    @XmlElement(name = "part_number")
    private String partNumber;

    /** 年度. */
    @XmlElement(name = "year")
    private String year;

    /** 季節. */
    @XmlElement(name = "season")
    private String season;

    /** 上代. */
    @XmlElement(name = "retail_price")
    private BigDecimal retailPrice;

    /** 品名. */
    @XmlElement(name = "product_name")
    private String productName;

    /** 生地発注先　郵便番号. */
    @XmlElement(name = "texture_order_yubin")
    private String textureOrderYubin;

    /** 生地発注先　住所1. */
    @XmlElement(name = "texture_order_address1")
    private String textureOrderAddress1;

    /** 生地発注先　住所2. */
    @XmlElement(name = "texture_order_address2")
    private String textureOrderAddress2;

    /** 生地発注先　住所3. */
    @XmlElement(name = "texture_order_address3")
    private String textureOrderAddress3;

    /** 生地発注先　仕入先コード. */
    @XmlElement(name = "texture_order_code")
    private String textureOrderCode;

    /** 生地発注先　発注先名称. */
    @XmlElement(name = "texture_order_name")
    private String textureOrderName;

    /** 製造担当. */
    @XmlElement(name = "mdf_staff")
    private String mdfStaff;

    /** 企画担当. */
    @XmlElement(name = "planning_staff")
    private String planningStaff;

    /** パタンナー. */
    @XmlElement(name = "pataner")
    private String pataner;

    /** 関連番号. */
    @XmlElement(name = "relation_number")
    private String relationNumber;

    /** 生地番号. */
    @XmlElement(name = "texture_number")
    private String textureNumber;

    /** 反番号. */
    @XmlElement(name = "cloth_number1")
    private String clothNumber;

    /** 品名. */
    @XmlElement(name = "texture_name")
    private String textureName;

    /** 生地納期. */
    @XmlElement(name = "matl_delivery_date")
    private String matlDeliveryDate;

    /** 反数. */
    @XmlElement(name = "total_cloth_count")
    private String totalClothCount;

    /** 用尺(目付). */
    @XmlElement(name = "length_actual")
    private String lengthActual;

    /** 原価欄　生地. */
    @XmlElement(name = "material_cost")
    private String materialCost;

    /** 工賃. */
    @XmlElement(name = "processing_cost")
    private String processingCost;

    /** 附属品. */
    @XmlElement(name = "atacched_cost")
    private String atacchedCost;

    /** その他. */
    @XmlElement(name = "other_cost")
    private String otherCost;

    /** 製造原価. */
    @XmlElement(name = "product_cost")
    private BigDecimal productCost;

    /** 原価率. */
    @XmlElement(name = "cost_rate")
    private BigDecimal costRate;

    /** 規格. */
    @XmlElement(name = "standard")
    private String standard;

    /** 品質表示(共通) パーツ名. */
    @XmlElement(name = "quality_label_head")
    private List<OrderQualityLabelHeadXmlModel> qualityLabelHead;

    /** 適用. */
    @XmlElement(name = "application")
    private String application;

    /** 但し書きラベル. */
    @XmlElement(name = "attention_label")
    private String attentionLabel;

    /** 会社名. */
    @XmlElement(name = "company_name")
    private String companyName;

}
