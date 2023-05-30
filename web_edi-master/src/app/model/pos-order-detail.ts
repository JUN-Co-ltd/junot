/**
 * 売上情報明細のモデル
 */
 export class PosOrderDetail {
  /** 店舗コード */
  storeCode: string;
  /** 品番 */
  partNo: string;
  /** カラーコード */
  colorCode: string;
  /** サイズコード */
  sizeCode: string
  /** 売上点数 */
  salesScore: number;
}
