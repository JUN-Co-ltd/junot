import { SearchMethod } from 'src/app/enum/search-method.enum';

/**
 * マスタメンテ 取引先一覧の検索用Model
 */
export class MaintSireSearchCondition {
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;

  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;

  /** ブランドコード. */
  brandCode: string;

  /** 仕入先コード. */
  sireCode: string;

  /** 区分. */
  // PRD_0161 mod JFE start
  //reckbns: string[];
  reckbns: string[] = [];
  // PRD_0161 mod JFE end

  /** 未使用コード. */
  unusedCodeFlg: boolean;

  /**
   * 検索方法.
   * デフォルトはすべてOR検索。
   * 指定可能な検索方法。
   * - ALL_AND_FULL : すべてAND検索 完全一致
   * - ALL_OR_LIKE : すべてOR検索 部分一致
   */
  searchMethod: SearchMethod = SearchMethod.ALL_AND_FULL;
}
