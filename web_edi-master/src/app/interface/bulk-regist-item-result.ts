/**
 * 品番・商品一括登録の結果用のinterface
 */
export interface BulkRegistItemResult {
  /** ブランドコード. */
  brandCode: string;

  /** 取込商品・品番数. */
  itemCount: number;

  /** エラー商品・品番数. */
  errorItemCount: number;

  /** 取込SKU数. */
  skuCount: number;

  /** エラーSKU数. */
  errorSkuCount: number;
}
