import { Order } from './order';
import { Delivery } from './delivery';
import { OrderFileInfo } from './order-file-info';
/**
 * VOrderの項目を保持するModel.
 */
export class VOrder extends Order {
  /** 品名. */
  productName: string;
  /** 年度. */
  year: number;
  /** サブシーズン. */
  subSeasonCode: string;
  /** ブランド. */
  brandCode: string;
  /** ブランドソートコード. */
  brandSortCode: string;
  /** 部門コード. */
  deptCode: string;
  /** 希望納品日. */
  preferredDeliveryDate: string;
  /** プランナーコード. */
  plannerCode: string;
  /** パタンナーコード. */
  patanerCode: string;
  /** 優良誤認区分. */
  misleadingRepresentation: boolean;
  /** 優良誤認承認区分（組成）. */
  qualityCompositionStatus: number;
  /** 優良誤認承認区分（国）. */
  qualityCooStatus: number;
  /** 優良誤認承認区分（有害物質）. */
  qualityHarmfulStatus: number;
  /** 納品依頼情報. */
  deliverys: Delivery[];
  /** 生産メーカー名. */
  mdfMakerName: string;
  /** 納品予定Id. */
  deliveryPlanId: number;
  /** 納品予定明細数. */
  deliveryPlanDetailsCnt: number;
  /** 発注ファイル情報. */
  orderFileInfo: OrderFileInfo;
  /** 生産ステータス数. */
  productionStatusCnt: number;
  /** フクキタル資材発注利用可能フラグ(true:利用可能、false:利用不可能). */
  materialOrderAvailable: boolean;
  /** フクキタル資材発注確定1件以上存在する(true:存在する、false:存在しない). */
  existsMaterialOrderConfirm: boolean;
  /** 登録ステータス */
  registStatus: number;
}
