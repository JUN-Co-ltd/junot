package jp.co.jun.edi.entity.extended;

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

import jp.co.jun.edi.entity.GenericEntity;
import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張メーカー返品情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTMakerReturnEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

    /** 伝票番号行. */
    @Column(name = "voucher_line")
    private Integer voucherLine;

    /** 倉庫連携ファイルID. */
    @Column(name = "wms_linking_file_id")
    private BigInteger wmsLinkingFileId;

    /** 管理No. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 店舗コード. */
    @Column(name = "shpcd")
    private String shpcd;

    /** 物流コード. */
    @Column(name = "logistics_code")
    private String logisticsCode;

    /** 仕入先コード. */
    @Column(name = "supplier_code")
    private String supplierCode;

    /** 返品日. */
    @Column(name = "return_at")
    private Date returnAt;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注番号. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 返品数量. */
    @Column(name = "return_lot")
    private Integer returnLot;

    /** 摘要. */
    private String memo;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;

    /** 店舗名. */
    @Column(name = "shop_name")
    private String shopName;

    /** 仕入先名称. */
    @Column(name = "supplier_name")
    private String supplierName;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** アイテムコード. */
    @Column(name = "item_code")
    private String itemCode;

    /** 発注日. */
    @Column(name = "product_order_at")
    private Date productOrderAt;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 下代(発注の単価). */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** 最新の単価(品番情報のその他原価). */
    @Column(name = "other_cost")
    private BigDecimal otherCost;

    /** メーカー返品ファイルID. */
    @Column(name = "maker_return_file_no_id")
    private BigInteger makerReturnFileNoId;

    /** 製造担当コード. */
    @Column(name = "mdf_staff_code")
    private String mdfStaffCode;

    /** 製造担当名称. */
    @Column(name = "mdf_staff_name")
    private String mdfStaffName;

    /** 在庫数. */
    @Column(name = "stock_lot")
    private Integer stockLot;
}
