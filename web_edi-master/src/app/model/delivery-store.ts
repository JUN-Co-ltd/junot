import { DeliveryStoreSku } from './delivery-store-sku';

/**
 * 納品得意先情報を保持するModel.
 */
export class DeliveryStore {
  /** ID. */
  id: number;

  /** 納品明細ID. */
  deliveryDetailId: number;

  /** 店舗コード. */
  storeCode: string;

  /** 配分区分. */
  allocationType: string;

  /** 店舗別配分率ID. */
  storeDistributionRatioId: number;

  /** 店舗別配分率区分. */
  storeDistributionRatioType: string;

  /** 店舗別配分率. */
  storeDistributionRatio: number;

  /** 配分順. */
  distributionSort: number;

  /** 納品得意先SKU情報のリスト. */
  deliveryStoreSkus: DeliveryStoreSku[];
}
