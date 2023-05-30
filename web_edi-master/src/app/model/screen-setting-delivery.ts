import { JunpcStoreHrtmst } from './junpc-store-hrtmst';
import { JunpcTnpmst } from './junpc-tnpmst';
// PRD_0123 #7054 add JFE start
import { MdeliveryLocation } from './m-delivery-location';
// PRD_0123 #7054 add JFE end
// PRD_0033 add SIT start
import { PosOrderDetail } from './pos-order-detail';
// PRD_0033 add SIT end
// PRD_0031 add SIT start
import { ShopStock } from './shop-stock';
// PRD_0031 add SIT end

/**
 * 納品依頼画面基本データのModel.
 */
export class ScreenSettingDelivery {
  /** 閾値. */
  threshold: number;

  /** 店舗別配分率. */
  storeHrtmstList: JunpcStoreHrtmst[];

  /** 店舗マスタ. */
  tnpmstList: JunpcTnpmst[];

  // PRD_0031 add SIT start
  /** 在庫数 */
  shopStockList: ShopStock[];
  // PRD_0031 add SIT end
  // PRD_0033 add SIT start
  /** 売上数 */
  posOrderDetailList: PosOrderDetail[];
  // PRD_0033 add SIT end
  // PRD_0123 #7054 add JFE start
  deliveryLocationList: MdeliveryLocation[];
  // PRD_0123 #7054 add JFE end
}
