package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 仕入確定データ取込用CSVファイルModel.
 */
@Data
public class PurchaseLinkingImportCsvModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 管理情報日付. */
    private String manageDate;
    /** 管理情報時間. */
    private String manageAt;
    /** 管理情報管理No. */
    private String manageNumber;
    /** 管理情報行No. */
    private String lineNumber;
    /** データ種別. */
    private String dataType;
    /** 仕入区分. */
    private String purchaseType;
    /** 入荷場所. */
    private String arrivalPlace;
    /** 入荷日. */
    private String arrivalAt;
    /** 仕入伝票No. */
    private String purchaseVoucherNumber;
    /** 仕入伝票行. */
    private String purchaseVoucherLine;
    /** 品番. */
    private String partNo;
    /** カラー. */
    private String colorCode;
    /** サイズ(サイズ記号). */
    private String size;
    /** 入荷数. */
    private String arrivalCount;
    /** 入荷確定数. */
    private String fixArrivalCount;
    /** 良品・不良品区分. */
    private String nonConformingProductType;
    /** 指示番号. */
    private String instructNumber;
    /** 指示番号行. */
    private String instructNumberLine;
}
