import { SearchMethod } from 'src/app/enum/search-method.enum';

/**
 * 優良誤認一覧検索用Model
 */
export class MisleadingRepresentationViewSearchCondition {
  /** 1つの結果ページで返されるリストの最大数 */
  maxResults: number;

  /** 戻す結果ページを指定するトークン.このトークンが指定された場合、ほかのパラメータを無視する */
  pageToken: string;

  /** アカウント名(id) */
  accountName: string;

  /** ブランドコード */
  brandCode: string;

  /** アイテム */
  itemCode: string;

  /** 年度 */
  year: number;

  /** シーズン */
  subSeasonCodeList: string[];

  /** 品種 */
  partNoKind: string;

  /** 連番 */
  partNoSerialNo: string;

  /** 製品納期From */
  productCorrectionDeliveryAtFrom: Date | string;

  /** 製品納期to */
  productCorrectionDeliveryAtTo: Date | string;

  /** 対象 */
  qualityStatusList: number[];

  /** 検査承認日From */
  approvalAtFrom: Date | string;

  /** 検査承認日to */
  approvalAtTo: Date | string;

  /**
   * 検索方法
   * デフォルトは全てOR検索
   * 指定可能な検索方法。
   * - ALL_AND_FULL : すべてAND検索 完全一致
   * - ALL_OR_LIKE : すべてOR検索 部分一致
   */
  searchMethod: SearchMethod = SearchMethod.ALL_AND_FULL;
}
