/**
 * 生産ステータスModel
 */
export class ProductionStatus {
  /** ID. */
  id: number;
  /** 発注ID. */
  orderId: number;
  /** 発注No. */
  orderNumber: number;
  /** 生産ステータス. */
  productionStatusType: number;
  /** サンプル上がり予定日. */
  sampleCompletionAt: Date;
  /** サンプル上がり確定予定日. */
  sampleCompletionFixAt: Date;
  /** 仕様確定予定日. */
  specificationAt: Date;
  /** 仕様確定日. */
  specificationFixAt: Date;
  /** 生地入荷予定日. */
  textureArrivalAt: Date;
  /** 生地入荷確定日. */
  textureArrivalFixAt: Date;
  /** 付属入荷予定日. */
  attachmentArrivalAt: Date;
  /** 付属入荷確定日. */
  attachmentArrivalFixAt: Date;
  /** 上がり予定日. */
  completionAt: Date;
  /** 上がり予定確定日. */
  completionFixAt: Date;
  /** 上がり総数. */
  completionCount: number;
  /** 縫製検品到着予定日. */
  sewInspectionAt: Date;
  /** 縫製検品到着確定日. */
  sewInspectionFixAt: Date;
  /** 検品実施予定日. */
  inspectionAt: Date;
  /** 検品実施確定日. */
  inspectionFixAt: Date;
  /** 出港予定日. */
  leavePortAt: Date;
  /** 出港確定日. */
  leavePortFixAt: Date;
  /** 入港予定日. */
  enterPortAt: Date;
  /** 入港確定日. */
  enterPortFixAt: Date;
  /** 通関予定日. */
  customsClearanceAt: Date;
  /** 通関確定日. */
  customsClearanceFixAt: Date;
  /** DISTA入荷予定日. */
  distaArrivalAt: Date;
  /** DISTA入荷確定日. */
  distaArrivalFixAt: Date;
  /** メモ. */
  memo: string;
}
