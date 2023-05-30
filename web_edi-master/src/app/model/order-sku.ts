import { OrderSkuForView } from './order-sku-for-view';
/**
 * 発注SKU情報を保持するModel.
 */
export class OrderSku extends OrderSkuForView {
  /** ID */
  id: number;
  /** 発注No */
  orderNumber: string;
  /** 品番 */
  partNo: string;
  /** 色 */
  colorCode: string;
  /** サイズ */
  size: string;
  /** 製品発注数 */
  productOrderLot: number;
  /** 製品裁断数. */
  productCutLot: number;
  /** 納品依頼数量. */
  deliveryLot: number;
  /** 入荷数量. */
  arrivalLot: number;
  /** 仕入数. */
  purchaseLot: number;
  /** 返品数量. */
  returnLot: number;
  /** 純仕入数. */
  netPurchaseLot: number;
  /** 発注完了日 */
  orderCompleAt: string;
  /** 送信区分 */
  sendCode: string;
}
