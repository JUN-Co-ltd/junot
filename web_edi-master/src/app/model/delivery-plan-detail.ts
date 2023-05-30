import { DeliveryPlanSku } from './delivery-plan-sku';

/**
 * DeliveryPlanDetailの項目を保持するModel.
 */
export class DeliveryPlanDetail {
  /** ID. */
  id: number;
  /** 納品予定ID. */
  deliveryPlanId: number;
  /** 納品予定日. */
  deliveryPlanAt: Date;
  /** 納品予定Skuリスト. */
  deliveryPlanSkus: DeliveryPlanSku[];
}
