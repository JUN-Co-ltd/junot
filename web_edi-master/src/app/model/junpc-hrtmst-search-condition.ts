/**
 * 発注生産システムの配分率マスタの検索用Model.
 */
export class JunpcHrtmstSearchCondition {
  /** ブランド. */
  brandCode: string;
  /** アイテム. */
  itemCode: string;
  /** シーズン. */
  season: string;
  /** 配分率区分. */
  hrtkbn: string;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
