import { Delivery } from './delivery';
/**
 * VDeliveryの項目を保持するModel.
 */
export class VDelivery extends Delivery {
  /** 納品日(修正納期). */
  correctionAt: Date;
  /** 納品依頼回数. */
  deliveryCount: number;
  /** 納品数量合計. */
  sumDeliveryLot: number;
}
