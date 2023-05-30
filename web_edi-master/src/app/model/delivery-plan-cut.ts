/**
 * DeliveryPlanCutの項目を保持するModel.
 */
export class DeliveryPlanCut {
  /** ID. */
  id: number;
  /** 納品予定ID. */
  deliveryPlanId: number;
  /** サイズ. */
  size: string;
  /** 色. */
  colorCode: string;
  /** 裁断数. */
  deliveryPlanCutLot: number;
}
