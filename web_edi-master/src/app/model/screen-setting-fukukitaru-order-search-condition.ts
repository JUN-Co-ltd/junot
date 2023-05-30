import { FukukitaruMasterDeliveryType, FukukitaruMasterType } from '../const/const';
/**
 *フクキタル連携 画面用マスタの検索用Model.
 */
export class ScreenSettingFukukitaruOrderSearchCondition {
  /** 発注ID */
  orderId: number;
  /** 品番ID */
  partNoId: number;
  /** マスタタイプリスト */
  listMasterType: FukukitaruMasterType[];
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
