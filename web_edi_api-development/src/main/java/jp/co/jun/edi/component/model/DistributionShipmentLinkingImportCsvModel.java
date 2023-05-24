package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 配分出荷指示確定データ取込用CSVファイルModel.
 */
@Data
public class DistributionShipmentLinkingImportCsvModel implements Serializable {
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
    /** 出荷区分. */
    private String shipmentType;
    /** 出荷場所. */
    private String shipmentPlace;
    /** 保留区分. */
    private String suspendType;
    /** 店舗コード. */
    private String tnpCode;
    /** 出荷日. */
    private String shipmentAt;
    /** 出荷伝票No. */
    private String shipmentVoucherNumber;
    /** 出荷伝票行. */
    private String shipmentVoucherLine;
    /** 品番. */
    private String partNo;
    /** カラー. */
    private String colorCode;
    /** サイズ(サイズ記号). */
    private String sizeCode;
    /** 出荷数. */
    private String allocationLot;
    /** 出荷確定数. */
    private String allocationFixLot;
    /** 箱No. */
    private String packageNumber;
    /** 仕入ＫＥＹ　発注番号(発注No). */
    private String orderNumber;
    /** 仕入ＫＥＹ　引取回数(納品依頼回数). */
    private String deliveryCount;
    /** 仕入ＫＥＹ　配分課(課コード). */
    private String divisionCode;
}
