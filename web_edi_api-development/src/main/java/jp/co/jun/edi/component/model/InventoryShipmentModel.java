package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.InstructorSystemType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.ShippingCategoryType;
import lombok.Data;

/**
 * 取込用在庫出荷情報のModel.
 */
@Data
public class InventoryShipmentModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 日付. */
    private Date manageDate;

    /** 時間. */
    private Date manageAt;

    /** 管理No. */
    private String manageNumber;

    /** 行No. */
    private Integer sequence;

    /** 指示元システム. */
    private InstructorSystemType instructorSystem;

    /** 移動元店舗コード. */
    private Integer originShopCode;

    /** 出荷区分. */
    private ShippingCategoryType shippingCategory;

    /** 出荷場所. */
    private String cargoPlace;

    /** B級品区分. */
    private BooleanType nonConformingProductType;

    /** 配分順位. */
    private Integer allocationRank;

    /** 店舗コード. */
    private String shopCode;

    /** 出荷日. */
    private Date cargoAt;

    /** 品番. */
    private String partNo;

    /** 色. */
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 出荷指示数. */
    private Integer shippingInstructionLot;

    /** 出荷確定数. */
    private Integer fixShippingInstructionLot;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 掛率. */
    private BigDecimal rate;

    /** 下代. */
    private BigDecimal wholesalePrice;

    /** PS区分. */
    private String properSaleType;

    /** セール上代. */
    private BigDecimal saleRetailPrice;

    /** OFF%. */
    private BigDecimal offPercent;

    /** 指示管理_担当者. */
    private String instructionManageUserCode;

    /** 指示管理_日付. */
    private Date instructionManageUserDate;

    /** 指示管理_時間. */
    private Date instructionManageUserAt;

    /** 指示明細_指示番号. */
    private Integer instructionManageNumber;

    /** 指示明細_店舗. */
    private String instructionManageShopCode;

    /** 指示明細_品番. */
    private String instructionManagePartNo;

    /** 指示明細_課. */
    private String instructionManageDivisionCode;

    /** 出荷伝票No. */
    private String shipmentVoucherNumber;

    /** 出荷伝票行. */
    private Integer shipmentVoucherLine;

    /** LG送信区分. */
    private LgSendType lgSendType;

}
