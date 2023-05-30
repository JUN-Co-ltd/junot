import { BulkRegistItemResult } from '../model/bulk-regist-item-result';

/**
 * 品番・商品一括登録用のinterface
 */
export interface BulkRegistItem {
  /** 結果リスト. */
  results: BulkRegistItemResult[];

  /** エラーリスト. */
  errors: String[];
}
