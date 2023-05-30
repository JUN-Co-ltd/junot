/**
 * 納品依頼ファイル情報のモデル
 */
export class DeliveryFileInfo {
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
  /** 公開日 */
  publishedAt: Date;
  /** 公開終了日 */
  publishedEndAt: Date;
}
