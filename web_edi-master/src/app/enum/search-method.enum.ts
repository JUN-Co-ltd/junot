/**
 * 検索方法の定義.
 */
export enum SearchMethod {
  /** ALL_AND_FULL : すべてAND検索 完全一致. */
  ALL_AND_FULL = 'ALL_AND_FULL',
  /** ALL_OR_LIKE : すべてOR検索 部分一致. */
  ALL_OR_LIKE = 'ALL_OR_LIKE'
}
