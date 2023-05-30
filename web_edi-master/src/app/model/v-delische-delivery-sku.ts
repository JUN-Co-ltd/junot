import { QualityApprovalStatus, OrderApprovalStatus } from '../const/const';

/**
 * VDelischeDeliverySkuの項目を保持するModel.
 */
export class VDelischeDeliverySku {
  /** ID(納品SKUID). */
  id: number;
  /** 発注ID. */
  orderId: number;
  /** 納品ID. */
  deliveryId: number;
  /** 納品明細ID. */
  deliveryDetailId: number;
  /** 納期. */
  deliveryAt: Date;
  /** 納品月度. */
  deliveryAtMonthly: number;
  /** ブランド. */
  brandCode: string;
  /** アイテム. */
  itemCode: string;
  /** 品番. */
  partNo: string;
  /** 品名. */
  productName: string;
  /** カラー. */
  colorCode: string;
  /** サイズ. */
  size: string;
  /** シーズン. */
  season: string;
  /** メーカーコード. */
  mdfMakerCode: string;
  /** メーカー名. */
  mdfMakerName: string;
  /** 製品発注日. */
  productOrderAt: Date;
  /** 製品納期. */
  productDeliveryAt: Date;
  /** 製品発注数. */
  productOrderLot: number;
  /** 納期遅延フラグ. */
  lateDeliveryAtFlg: string;
  /** 納品依頼数. */
  deliveryLot: number;
  /** 入荷数. */
  arrivalLot: number;
  /** 上代合計. */
  calculateRetailPrice: number;
  /** 原価合計. */
  calculateProductCost: number;
  /** 発注承認ステータス. */
  orderApproveStatus: OrderApprovalStatus;
  /** 優良誤認承認区分（組成）. */
  qualityCompositionStatus: QualityApprovalStatus;
  /** 優良誤認承認区分（国）. */
  qualityCooStatus: QualityApprovalStatus;
  /** 優良誤認承認区分（有害物質）. */
  qualityHarmfulStatus: QualityApprovalStatus;
}
