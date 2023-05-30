/**
 * 発注生産システムのコードマスタの検索用Model.
 */
export class JunpcCodmstSearchCondition {
  /** ブランドコード */
  brand: string;

  /** 種類 */
  kind: string;

  /** アイテムコード */
  item: string;

  /** コード1 */
  code1: string;

  /** コード1リスト */
  code1s: string[];

  /** 配分課コード */
  divisionCode: string;

  /** 社員タイプ */
  staffType: string;

  /** 検索タイプ */
  searchType: string;

  /** 検索文字列 */
  searchText: string;

  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;

  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
