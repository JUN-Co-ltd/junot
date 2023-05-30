// PRD_0087 del SIT start
// 未使用のため削除
//import { DeliveryDetail } from './delivery-detail';
// PRD_0087 del SIT end

/**
 * 配分一覧
 * 納品依頼検索結果を保持するModel.
 */
export class DeliverySearchResult {
  /** 納品ID. */
  deliveryId: number;
  /** 発注ID. */
  orderId: number;
  /** 発注No. */
  orderNumber: number;
  /** 品番ID. */
  partNoId: number;
  /** 品番. */
  partNo: string;
  /** 品名. */
  productName: string;
  /** 承認ステータス. */
  deliveryApproveStatus: string;
  /** 納品依頼回数. */
  deliveryCount: number;
  /** 納品日(修正納期). */
  correctionAt: Date;
　/** 店舗別登録済フラグ. */
  storeRegisteredFlg: boolean;
  // PRD_0087 mod SIT start
  ///** 配分確定フラグ. */
  //allocationConfirmFlg: boolean;
  /** 配分完了日. */
  allocationCompleteAt: Date;
  /** 配分計上日. */
  allocationRecordAt: Date;
  // PRD_0087 mod SIT end
  /** 発注数量. */
  quantity: number;
  /**
   * 完納フラグ.
   * false：未完(0)、true:完納(1)
   */
  orderCompleteFlg: boolean;
  /**
   * 仕入フラグ
   * false：未仕入(0)、true:仕入済(1)
   */
  purchasesFlg: boolean;
  /**
   * 承認フラグ
   * false：未承認(0)、true:承認済(1)
   */
  approvaldFlg: boolean;
  /**
   * 出荷フラグ
   * false：未出荷(0)、true:出荷済(1)
   */
  shipmentFlg: boolean;
  /** 要再配分フラグ */
  reAllocationFlg: boolean;
  /** 引取数(納品SKUの納品数量の合計). */
  transactionLot: number;
  /** 配分数(納品得意先SKUの納品数量の合計). */
  // PRD_0127 #9837 add JFE start
  /** 納品先 */
  companyName: string;
  // PRD_0127 #9837 add JFE end
  allocationLot: number;
  /** 配分状態. */
  allocationStatus: string;
  /** 仕入確定数 */
  fixArrivalCount: number;
  /** 入荷フラグ.
   * false：未入荷(0)、true:入荷済(1)
   */
  arrivalFlg: boolean;
  /** 出荷指示フラグ.
   * false：未出荷指示(0)、true:出荷指示済(1)
   */
  shippingInstructionsFlg: boolean;
}
