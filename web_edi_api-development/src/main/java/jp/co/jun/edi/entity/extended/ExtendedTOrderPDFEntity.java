package jp.co.jun.edi.entity.extended;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
/**
 *
 * ExtendedTOrderPDFEntity.
 * 受注確定PDFデータ用
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTOrderPDFEntity {
    /** 発注ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;
    /** 品番（製造番号）. */
    private String partNo;
    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;
    /** アイテムコード. */
    @Column(name = "item_code")
    private String itemCode;
    /** 郵便番号. */
    @Column(name = "yubin")
    private String yubin;
    /** 住所1. */
    @Column(name = "address1")
    private String address1;
    /** 住所2. */
    @Column(name = "addreess2")
    private String addreess2;
    /** 住所3. */
    @Column(name = "addreess3")
    private String addreess3;
    /** 送付先名. */
    @Column(name = "send_to_name")
    private String sendToName;
    /** 仕入先コード. */
    @Column(name = "sire")
    private String sire;
    /** 送付先電話番号. */
    @Column(name = "hphone")
    private String hphone;
    /** 発注No. */
    @Column(name = "order_number")
    private String orderNumber;
    /** 費目 費目コード. */
    @Column(name = "expense_item_code")
    private String expenseItemCode;
    /** 費目 費目名称. */
    @Column(name = "expense_item_name")
    private String expenseItemName;
    /** 製品発注日. */
    @Column(name = "product_order_date")
    private Date productOrderDate;
    /** 部門コード. */
    @Column(name = "division_code")
    private String divisionCode;
    /** 事業部名. */
    @Column(name = "division_name")
    private String divisionName;
    /** アイテム名. */
    @Column(name = "item_name")
    private String itemName;
    /** 原産国. */
    @Column(name = "country_of_origin")
    private String countryOfOrigin;
    /** 数量. */
    @Column(name = "quantity")
    private BigDecimal quantity;
    /** 単価. */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    /** 製品納期. */
    @Column(name = "product_delivery_date")
    private Date productDeliveryDate;
    /** 年度. */
    @Column(name = "year")
    private String year;
    /** シーズン. */
    @Column(name = "season")
    private String season;
    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;
    /** 品名. */
    @Column(name = "product_name")
    private String productName;
    /** 製造担当. */
    @Column(name = "mdf_staff")
    private String mdfStaff;
    /** 企画担当. */
    @Column(name = "planning_staff")
    private String planningStaff;
    /** パタンナー. */
    @Column(name = "pataner")
    private String pataner;
    /** 関連No. */
    @Column(name = "relation_number")
    private String relationNumber;
    /** 附属代. */
    @Column(name = "attached_cost")
    private String attachedCost;
    /** その他原価. */
    @Column(name = "other_cost")
    private String otherCost;
    /** 製品原価. */
    @Column(name = "product_cost")
    private BigDecimal productCost;
    /** 適用. */
    @Column(name = "application")
    private String application;
    /** 会社コード. */
    @Column(name = "company_name")
    private String companyName;

}
