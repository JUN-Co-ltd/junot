import { Authority } from 'src/app/enum/authority.enum';
import { SearchMethod } from 'src/app/enum/search-method.enum';

/**
 * マスタメンテ ユーザ一覧の検索用Model
 */
export class MaintUserSearchCondition {
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;

  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;

  /** アカウント名（ログインID）. */
  accountNames: string[];

  /** 所属会社（会社コード）. */
  companies: string[];

  /** メーカーコード. */
  makerCodes: string[];

  /** メーカー名称. */
  makerNames: string[];

  /** 氏名. */
  names: string[];

  /** メールアドレス. */
  mailAddresses: string[];

  /** 権限. */
  authorities: Authority[];

  /** 有効/無効. */
  enabledList: boolean[];

  /**
   * 検索方法.
   * デフォルトはすべてOR検索。
   * 指定可能な検索方法。
   * - ALL_AND_FULL : すべてAND検索 完全一致
   * - ALL_OR_LIKE : すべてOR検索 部分一致
   */
  searchMethod: SearchMethod = SearchMethod.ALL_OR_LIKE;
}
