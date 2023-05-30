import { VDelischeDeliveryRequest } from './v-delische-delivery-request';
import { QualityApprovalStatus, OrderApprovalStatus } from '../const/const';

/**
 * VDeliScheOrderの項目を保持するModel.
 */
export class VDelischeOrder {
  /** 子要素表示中. */
  isOpenChild: boolean;
  /** SKU(孫)表示中の数. */
  openingDeliverySkuNum: number;
  /** デリスケ納品依頼情報リスト */
  delischeDeliveryRequestList: VDelischeDeliveryRequest[] = [];

  /** ID(発注ID). */
  id: number;
  /** 発注No. */
  orderNumber: number;
  /** 製品納期. */
  productDeliveryAt: Date;
  /** 納品月度. */
  productDeliveryAtMonthly: number;
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
  // PRD_0146 #10776 add JFE start
  /** 費目 */
  expenseItem: string;
  /** 関連No */
  relationNumber: number;
  // PRD_0146 #10776 add JFE end
  /** メーカーコード. */
  mdfMakerCode: string;
  /** メーカー名. */
  mdfMakerName: string;
  /** 製造担当者コード. */
  mdfStaffCode: string;
  /** 製造担当者. */
  mdfStaffName: string;
  /** 年度. */
  year: number;
  /** 製品発注日. */
  productOrderAt: Date;
  /** 生産工程区分. */
  productionStatus: string;
  /** 納期遅延件数. */
  lateDeliveryAtCnt: number;
  /** 数量(発注数). */
  quantity: number;
  /** 納品依頼数合計. */
  deliveryLotSum: number;
  /** 入荷数合計. */
  arrivalLotSum: number;
  /** 純売上数. */
  netSalesQuantity: number;
  /** 在庫数. */
  stockQuantity: number;
  /** 上代. */
  retailPrice: number;
  /** 原価. */
  productCost: number;
  /** 発注残. */
  orderRemainingLot: number;
  /** 上代合計. */
  calculateRetailPrice: number;
  /** 原価合計. */
  calculateProductCost: number;
  /** 原価率. */
  costRate: number;
  /** 製品完納区分. */
  productCompleteOrder: string;
  /** 子要素存在フラグ. */
  childExists: boolean;
  /** 発注承認ステータス. */
  orderApproveStatus: OrderApprovalStatus;
  /** 優良誤認承認区分（組成）. */
  qualityCompositionStatus: QualityApprovalStatus;
  /** 優良誤認承認区分（国）. */
  qualityCooStatus: QualityApprovalStatus;
  /** 優良誤認承認区分（有害物質）. */
  qualityHarmfulStatus: QualityApprovalStatus;
}
