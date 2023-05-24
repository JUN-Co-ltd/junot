package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * メーカー返品確定データ取込用CSVファイルModel.
 */
@Data
public class MakerReturnLinkingImportCsvModel implements Serializable {
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
    /** 入荷区分. */
    private String arrivalType;
    /** 入荷場所. */
    private String arrivalPlace;
    /** 店舗コード. */
    private String storeCode;
    /** 返品日. */
    private String returnAt;
    /** 伝票No. */
    private String voucherNumber;
    /** 伝票行. */
    private String voucherLine;
    /** 店舗伝票No. */
    private String storeVoucherNumber;
    /** 店舗伝票行. */
    private String storeVoucherLine;
    /** 品番. */
    private String partNo;
    /** カラー. */
    private String colorCode;
    /** サイズ(サイズ記号). */
    private String size;
    /** 入荷予定数. */
    private String arrivalCount;
    /** 入荷確定数. */
    private String fixArrivalCount;
    /** 指示番号. */
    private String instructNumber;
    /** 指示番号行. */
    private String instructNumberLine;
}
