/**
 * 品番・商品一括登録用のModel.
 */
export class BulkRegistItemResult {
  /** ブランドコード */
  brandCode: string;
  /** 取込商品・品番数 */
  itemCount: number;
  /** エラー商品・品番数 */
  errorItemCount: number;
  /** 取込SKU数 */
  skuCount: number;
  /** エラーSKU数 */
  errorSkuCount: number;
}
