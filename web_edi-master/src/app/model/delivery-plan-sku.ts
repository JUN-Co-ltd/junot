/**
 * DeliveryPlanSkuの項目を保持するModel.
 */
export class DeliveryPlanSku {
  /** ID. */
  id: number;
  /** 納品予定ID. */
  deliveryPlanId: number;
  /** 納品予定明細ID. */
  deliveryPlanDetailId: number;
  /** サイズ. */
  size: string;
  /** 色. */
  colorCode: string;
  /** 納品予定数. */
  deliveryPlanLot: number;
}
