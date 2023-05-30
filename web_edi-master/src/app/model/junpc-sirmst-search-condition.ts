/**
 * 発注生産システムの仕入先マスタの検索用Model.
 */
export class JunpcSirmstSearchCondition {
  /** 仕入先区分. */
  sirkbn: string;
  /** 検索区分. */
  searchType: string;
  /** 検索文字. */
  searchText: string;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
