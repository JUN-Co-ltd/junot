import { FukukitaruMasterDeliveryType, FukukitaruMasterType } from '../const/const';

/**
 * フクキタル連携 画面用マスタの検索用Model.
 */
export class ScreenSettingFukukitaruSearchCondition {
  /** マスタタイプリスト */
  listMasterType: FukukitaruMasterType[];
  /** 品番ID. */
  partNoId: number;
  /** 発注ID. */
  orderId: number;
  /** デリバリ */
  deliveryType: FukukitaruMasterDeliveryType;
  /** 検索会社名 */
  searchCompanyName: string;
  /** 品種. */
  partNoKind: string;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
