/**
 * チェック結果の合計数.
 */
export interface TotalCount {
  /** 取込商品・品番数 */
  item: number;
  /** エラー商品・品番数 */
  itemError: number;
  /** 取込SKU数 */
  sku: number;
  /** エラーSKU数 */
  skuError: number;
}
