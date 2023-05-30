/**
 * 納品伝票ファイル情報のモデル
 */
export class DeliveryVoucherFileInfo {
  /** ID */
  id: number;
  /** 納品ID */
  deliveryId: number;
  /** 納品依頼回数 */
  deliveryCount: number;
  /** 発注ID */
  orderId: number;
  /** ファイルID */
  fileNoId: number;
  /** 伝票分類 */
  voucherCategory: number;
  /** ステータス */
  status: number;
}
