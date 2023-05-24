package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 会計仕入確定DATファイル作成用Model.
 */
@Data
public class AccountPurchaseConfirmFileLinkingDatModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** データ種別. */
    private String dataType;
    /** 入荷伝票No. */
    private String sqManageNumber;
    /** 入荷伝票行. */
    private String purchaseVoucherLine;
    /** 部門. */
    private String deptCode;
    /** 場所（自）. */
    private String arrivalPlaceFrom;
    /** 場所（至）. */
    private String arrivalPlaceTo;
    /** 場所（発生）. */
    private String arrivalPlaceOccur;
    /** 配分コード 課. */
    private String divisionCode;
    /** 配分コード グループ. */
    private String allocationCodeGroup;
    /** 配分コード 順位. */
    private String allocationCodeRank;
    /** 仕入先コード. */
    private String supplierCode;
    /** 入荷日. */
    private String arrivalAt;
    /** 伝票日付. */
    private String voucherAt;
    /** 計上日. */
    private String recordAt;
    /** 伝区(伝票区分). */
    private String voucherCategory;
    /** 入出荷区分. */
    private String receiptAndShipmentType;
    /** 伝種(伝票種類). */
    private String voucherVariety;
    /** 訂正区分. */
    private String correctType;
    /** 製品区分. */
    private String productType;
    /** 共通区分１. */
    private String commonType1;
    /** 共通区分２. */
    private String commonType2;
    /** 完納区分. */
    private String completeType;
    /** セット品区分. */
    private String setProductType;
    /** Ｂ級品区分. */
    private String nonConformingProductType;
    /** 仕入区分１. */
    private String purchaseType1;
    /** 仕入区分２. */
    private String purchaseType2;
    /** 区分１. */
    private String type1;
    /** 区分２. */
    private String type2;
    /** 発注No. */
    private String orderNumber;
    /** 引取回数. */
    private String purchaseCount;
    /** 費目. */
    private String expenseItem;
    /** 仕入伝票No. */
    private String purchaseVoucherNumber;
    /** 先方伝票No. */
    private String makerVoucherNumber;
    /** 送り状No. */
    private String invoiceNumber;
    /** 運送会社コード. */
    private String shippingCompanyCode;
    /** 個数口. */
    private String piece;
    /** ハンガー数. */
    private String hangerAmount;
    /** 品番. */
    private String partNo;
    /** 品名. */
    private String productNameKana;
    /** 上代. */
    private String retailPrice;
    /** 単価. */
    private String unitPrice;
    /** 金額. */
    private String productPrice;
    /** 数量. */
    private String fixArrivalCount;
    /** カラー. */
    private String colorCode;
    /** サイズ区分. */
    private String sizeType;
    /** サイズ別数１. */
    private String size1;
    /** サイズ別数２. */
    private String size2;
    /** サイズ別数３. */
    private String size3;
    /** サイズ別数４. */
    private String size4;
    /** サイズ別数５. */
    private String size5;
    /** サイズ別数６. */
    private String size6;
    /** サイズ別数７. */
    private String size7;
    /** サイズ別数８. */
    private String size8;
    /** 年度. */
    private String year;
    /** シーズン. */
    private String seasonCode;
    /** 製造原価 生地. */
    private String matlCost;
    /** 製造原価 工賃. */
    private String processingCost;
    /** 製造原価 附属品. */
    private String accessoriesCost;
    /** 製造原価 その他. */
    private String otherCost;
    /** 製造原価 合計. */
    private String manufacturingCostSum;
    /** 原反 反番号. */
    private String originalFabricClothNumber;
    /** 原反 反数. */
    private String originalFabricClothCount;
    /** 原反 メーター数. */
    private String originalFabricMeter;
    /** 納品依頼No. */
    private String deliveryRequestNumber;
    /** 予備：5桁. */
    private String reserve5Digit;
    /** ファイル識別. */
    private String fileIdentification;
    /** データ固有 日付. */
    private String dataSpecificDate;
    /** データ固有 区分. */
    private String dataSpecificType;
    /** データ固有 伝票No. */
    private String dataSpecificVoucherNumber;
    /** データ固有 行No. */
    private String dataSpecificLineNumber;
    /** 会社 ＩＦ区分. */
    private String ifType;
    /** 会社 コード. */
    private String companyCode;
    /** 予備：13桁. */
    private String reserve13Digit;
    /** 管理情報 メンテ区分. */
    private String manageMntflg;
    /** 管理情報 担当者. */
    private String manageStaff;
    /** 管理情報 WSNo. */
    private String manageWsNumber;
    /** 管理情報 日付. */
    private String manageDate;
    /** 管理情報 時間. */
    private String manageAt;
    /** 管理情報 SEQ. */
    private String manageSeq;
    /** 管理情報 プログラム名. */
    private String manageProgramName;
    /** 管理情報 媒体. */
    private String manageMedium;

    // 採番用項目：
    /** 採番用_仕入データ作成日. */
    private String numSqManageDate;
    /** 採番用_部門(前2桁). */
    private String numDeptCode;
    /** 採番用_入荷場所. */
    private String numArrivalPlace;

}
