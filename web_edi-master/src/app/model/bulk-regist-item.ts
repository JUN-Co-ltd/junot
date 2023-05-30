import { BulkRegistItemResult } from './bulk-regist-item-result';

/**
 * 品番・商品一括登録の結果用のModel.
 */
export class BulkRegistItem {
  /** 結果リスト */
  results: BulkRegistItemResult[];
  /** エラーリスト */
  errors: string[];
}
