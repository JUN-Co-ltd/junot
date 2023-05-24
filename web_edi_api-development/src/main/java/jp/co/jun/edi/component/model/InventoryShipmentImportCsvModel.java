package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;


/**
 * 在庫出荷データ取込用CSVファイルModel.
 */
@Data
public class InventoryShipmentImportCsvModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 管理情報 日付. */
    private String manageDate;

    /** 管理情報 時間. */
    private String manageAt;

    /** 管理情報 管理No. */
    private String manageNumber;

    /** 管理情報 行No. */
    private String sequence;

    /** 出荷区分. */
    private String shippingCategory;

    /** 出荷場所. */
    private String cargoPlace;

    /** 配分順位. */
    private String allocationRank;

    /** 店舗コード. */
    private String shopCode;

    /** 出荷日. */
    private String cargoAt;

    /** 品番. */
    private String partNo;

    /** カラー. */
    private String colorCode;

    /** サイズコード. */
    private String size;

    /** 出荷指示数. */
    private String shippingInstructionLot;

    /** 上代. */
    private String retailPrice;

    /** 掛率. */
    private String rate;

    /** 下代. */
    private String wholesalePrice;

    /** PS区分. */
    private String properSaleType;

    /** セール上代. */
    private String saleRetailPrice;

    /** OFF%. */
    private String offPercent;

    /** 指示管理_担当者. */
    private String instructionManageUserCode;

    /** 指示管理_日付. */
    private String instructionManageUserDate;

    /** 指示管理_時間. */
    private String instructionManageUserAt;

    /** 指示明細__指示番号. */
    private String instructionManageNumber;

    /** 指示明細_店舗. */
    private String instructionManageShopCode;

    /** 指示明細_品番. */
    private String instructionManagePartNo;

    /** 指示明細_課. */
    private String instructionManageDivisionCode;
}
