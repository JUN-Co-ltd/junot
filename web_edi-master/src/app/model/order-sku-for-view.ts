/**
 * 画面用の発注sku情報を保持するModel.
 */
export class OrderSkuForView {
  /** 納品可能数 */
  deliverableLot: number;
  /** 納品依頼済数 */
  historyLot: number;
}
