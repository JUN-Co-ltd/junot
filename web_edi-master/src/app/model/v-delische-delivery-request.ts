import { OrderApprovalStatus, QualityApprovalStatus } from '../const/const';
import { VDelischeDeliverySku } from './v-delische-delivery-sku';

/**
 * VDelischeDeliveryRequestの項目を保持するModel.
 */
export class VDelischeDeliveryRequest {
  /** 子要素表示中. */
  isOpenChild: boolean;
  /** デリスケ納品SKU情報リスト */
  delischeDeliverySkuList: VDelischeDeliverySku[];

  /** 発注ID. */
  orderId: number;
  /** 納品ID. */
  deliveryId: number;
  /** 納品依頼回数. */
  deliveryCount: number;
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
  /** 納期遅延フラグ. */
  lateDeliveryAtFlg: string;
  /** 発注数合計. */
  productOrderLotSum: number;
  /** 納品依頼数合計. */
  deliveryLotSum: number;
  /** 入荷数合計. */
  arrivalLotSum: number;
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
