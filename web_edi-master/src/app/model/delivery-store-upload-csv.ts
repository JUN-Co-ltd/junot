/**
 * 店舗配分アップロードの型定義.
 */
export class DeliveryStoreUploadCsv {
  /** 発注番号 */
  orderNo: string;
  /** 回数 */
  deliveryCount: string;
  /** 品番 */
  partNo: string;
  // PRD_0120#8343 mod JFE start
  // /** 店舗コード */
  // storeCode: string;
  // /** 色 */
  // colorCode: string;
  // /** サイズ */
  // size: string;
  // /** 納品数量 */
  // deliveryLot: string;
  /** 店舗コード */
  stores: DeliveryStoreUploadCsvStore[];
  // PRD_0120#8343 mod JFE end
}

// PRD_0120#8343 add JFE start
/**
 * 店舗配分アップロード店舗の型定義.
 */
export class DeliveryStoreUploadCsvStore {
  /** 店舗コード */
  storeCode: string;
  deliveryStoreSkuFormValues: DeliveryStoreSkuForm[];
}

/**
 * 店舗配分アップロードSKUFormの入力値の型定義.
 */
export class DeliveryStoreSkuForm {
  /** 色 */
  colorCode: string;
  /** サイズ */
  size: string;
  /** 納品数量 */
  deliveryLot: string;
}
// PRD_0120#8343 add JFE end
