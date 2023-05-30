import { ScreenSettingDeliveryMasterType } from '../const/const';
import { DeliveryStoreInfo } from './delivery-store-info';

/**
 * 納品依頼画面基本データの検索用Model.
 */
export class ScreenSettingDeliverySearchCondition {
  /** マスタタイプリスト. */
  listMasterType: ScreenSettingDeliveryMasterType[];

  // PRD_0031 add SIT start
  /** 品番 */
  partNo: string;
  // PRD_0031 add SIT end

  /** ブランド. */
  brandCode: string;

  /** アイテム. */
  itemCode: string;

  /** シーズン. */
  seasonCode: string;

  /** 納品得意先に登録されている店舗情報リスト. */
  deliveryStoreInfos: DeliveryStoreInfo[];
  //PRD_0123 #7054 JFE add start
  /**ID. */
  id: number;
  //PRD_0123 #7054 JFE add end
}
