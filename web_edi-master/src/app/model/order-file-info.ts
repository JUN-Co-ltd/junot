/**
 * 発注ファイル情報のモデル
 */
export class OrderFileInfo {
  /** ID */
  id: number;
  /** 発注ID */
  orderId: number;
  /** ファイルID */
  fileNoId: number;
  /** 公開日 */
  publishedAt: Date;
  /** 公開終了日 */
  publishedEndAt: Date;
}
