import { ShopKind } from '../const/shop-kind';
import { Sort } from './sort';

/**
 * 発注生産システムの店舗マスタの検索用Model.
 */
export class JunpcTnpmstSearchCondition {

  /** 事業部コード. */
  divisionCode: string;

  /** 店舗コード. */
  shpcd: string;

  /** 店舗コード前方一致. */
  shpcdAhead: string;

  /** 店舗名. */
  name: string;

  /** 電話番号. */
  telban: string;

  /** 店舗区分. */
  shopkind: ShopKind;

  /** ソート. */
  sort: Sort;

  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;

  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
