package jp.co.jun.edi.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.PurchaseTypeConverter;
import jp.co.jun.edi.type.PurchaseType;
import lombok.Data;
//PRD_0134 #10654 add JEF start
/**
 * 仕入情報(消化委託PDF用)のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class TPurchaseDigestionItemPDFEntity  {
    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝区. */
    @Column(name = "purchase_voucher_type")
    private String purchaseVoucherType;

    /** 入荷日. */
    @Column(name = "arrival_at")
    private Date arrivalAt;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** 仕入伝票No. */
    @Column(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 仕入伝票行. */
    @Column(name = "purchase_voucher_line")
    private String purchaseVoucherLine;

    /** 仕入先. */
    @Column(name = "supplier_code")
    private String supplierCode;

    /** 製品工場. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 入荷確定数. */
    @Column(name = "fix_arrival_count")
    private Integer fixArrivalCount;

    /** 仕入単価. */
    @Column(name = "purchase_unit_price")
    private Integer purchaseUnitPrice;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 入荷数. */
    @Column(name = "arrival_count")
    private Integer arrivalCount;

    /** 仕入区分. */
    @Column(name = "purchase_type")
    @Convert(converter = PurchaseTypeConverter.class)
    private PurchaseType purchaseType;

    /** 入荷場所. */
    @Column(name = "arrival_place")
    private String arrivalPlace;

    //発注情報(t_order)
    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 発注No. */
    @Column(name = "order_number")
    private String orderNumber;

    /**
     * 全済区分.
     */
    @Column(name = "all_completion_type")
    private int allCompletionType;

    /** 単価. */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    //仕入先マスタ(m_sirmst)
    /** 郵便番号. */
    @Column(name = "yubin")
    private String yubin;

    /** 住所1. */
    @Column(name = "add1")
    private String add1;

    /** 住所2. */
    @Column(name = "add2")
    private String add2;

    /** 住所3. */
    @Column(name = "add3")
    private String add3;

    /** 仕入先正式名称. */
    @Column(name = "name")
    private String name;

    //品番情報(t_item)

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 部門コード. */
    @Column(name = "dept_code")
    private String deptCode;

    //コードマスタ
    /** アイテム1 ID:02 */
    @Column(name = "brandName")
    private String brand_name;

    /** アイテム2 ID:02 */
    @Column(name = "company_name")
    private String companyName;

    /** アイテム1 ID:03 */
    @Column(name = "item_name")
    private String itemName;

    /** アイテム2 ID:10 */
    @Column(name = "color_code_name")
    private String colorCodeName;

    //サイズマスタ(m_sizmst)
    /** 順位. */
    @Column(name = "jun")
    private String jun;

    @Column(name = "sirkbn")
    private String sirkbn;

}
//PRD_0134 #10654 add JEF end