package jp.co.jun.edi.entity.schedule;

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
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.PsTypeConverter;
import jp.co.jun.edi.entity.converter.ShippingCategoryTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.PsType;
import jp.co.jun.edi.type.ShippingCategoryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 在庫出荷ファイル作成用Entity.
 */
@Entity
@Table(name = "t_inventory_shipment")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedInventoryShipmentScheduleEntity extends LinkingCreateCsvFileCommonEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 倉庫連携ファイルID. */
    @Column(name = "wms_linking_file_id")
    private BigInteger wmsLinkingFileId;

    /** 管理情報 日付. */
    @Column(name = "manage_date")
    private Date manageDate;

    /** 管理情報 時間. */
    @Column(name = "manage_at")
    private Date manageAt;

    /** 管理情報 管No. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 管理情報 行No. */
    @Column(name = "sequence")
    private String sequence;

    /** 出荷区分. */
    @Column(name = "shipping_category")
    @Convert(converter = ShippingCategoryTypeConverter.class)
    private ShippingCategoryType shippingCategory;

    /** 出荷場所. */
    @Column(name = "cargo_place")
    private String cargoPlace;

    /** B級品区分. */
    @Column(name = "non_conforming_product_type")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType nonConformingProductType;

    /** 配分順位. */
    @Column(name = "allocation_rank")
    private Integer allocationRank;

    /** 店舗コード. */
    @Column(name = "shop_code")
    private String shopCode;

    /** 出荷日. */
    @Column(name = "cargo_at")
    private Date cargoAt;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** カラー. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 出荷指示数. */
    @Column(name = "shipping_instruction_lot")
    private Integer shippingInstructionLot;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 掛率. */
    @Column(name = "rate")
    private BigDecimal rate;

    /** 下代. */
    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;

    /** PS区分. */
    @Column(name = "proper_sale_type")
    @Convert(converter = PsTypeConverter.class)
    private PsType psType;

    /** セール上代. */
    @Column(name = "sale_retail_price")
    private BigDecimal saleRetailPrice;

    /** OFF%. */
    @Column(name = "off_percent")
    private BigDecimal offPercent;

    /** 指示管理_担当者. */
    @Column(name = "instruction_manage_user_code")
    private String instructionManageUserCode;

    /** 指示管理_日付. */
    @Column(name = "instruction_manage_user_date")
    private Date instructionManageUserDate;

    /** 指示管理_時間. */
    @Column(name = "instruction_manage_user_at")
    private Date instructionManageUserAt;

    /** 指示明細_指示番号. */
    @Column(name = "instruction_manage_number")
    private String instructionManageNumber;

    /** 指示明細_指示番号行. */
    @Column(name = "instruction_manage_number_line")
    private Integer instructionManageNumberLine;

    /** 指示明細_店舗. */
    @Column(name = "instruction_manage_shop_code")
    private String instructionManageShopCode;

    /** 指示明細_品番. */
    @Column(name = "instruction_manage_part_no")
    private String instructionManagePartNo;

    /** 指示明細_課. */
    @Column(name = "instruction_manage_division_code")
    private String instructionManageDivisionCode;
}
