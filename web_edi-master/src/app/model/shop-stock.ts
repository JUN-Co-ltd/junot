/**
 * 店別在庫情報のモデル
 */
export class ShopStock {
  /** id */
  id: number;
  /** 店舗コード */
  shopCode: string;
  /** 商品コード */
  productCode: string;
  /** 品番 */
  partNo: string;
  /** カラーコード */
  colorCode: string;
  /** サイズ */
  size: string
  /** 在庫数 */
  stockLot: number;
}
