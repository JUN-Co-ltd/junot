/**
 * 発注生産システムのサイズマスタの検索用Model.
 */
export class JunpcSizmstSearchCondition {
  /** 品種. */
  hscd: string;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
