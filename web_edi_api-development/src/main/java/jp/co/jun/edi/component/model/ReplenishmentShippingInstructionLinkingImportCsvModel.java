package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 補充出荷指示データ取込用CSVファイルModel.
 */
@Data
public class ReplenishmentShippingInstructionLinkingImportCsvModel implements Serializable {
    private static final long serialVersionUID = 1L;
  /** 管理情報日付. */
  private String manageDate;
  /** 管理情報時間. */
  private String manageAt;
  /** 管理情報 SEQ. */
  private String sequence;
  /** 出荷先店舗コード（移動先店舗）. */
  private String shipmentShopCode;
  /** 保留先店舗コード. */
  private String holdShopCode;
  /** 出荷日. */
  private String cargoAt;
  /** 出荷場所. */
  private String cargoPlace;
  /** 品番. */
  private String partNo;
  /** カラー. */
  private String colorCode;
  /** サイズ. */
  private String size;
  /** 出荷指示数. */
  private String shippingInstructionsLot;
  /** データ種別. */
  private String dataType;
  /** 登録日. */
  private String createdAt;
  /** 修正日. */
  private String updatedAt;
  /** 入力者. */
  private String tanto;
}
