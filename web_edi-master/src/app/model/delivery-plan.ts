import { DeliveryPlanDetail } from './delivery-plan-detail';
import { DeliveryPlanCut } from './delivery-plan-cut';

/**
 * DeliveryPlanの項目を保持するModel.
 */
export class DeliveryPlan {
  /** ID. */
  id: number;
  /** 発注ID. */
  orderId: number;
  /** 品番ID. */
  partNoId: number;
  /** 登録済ステータス. */
  entryStatus: number;
  /** メモ. */
  memo: string;
  /** 納品予定明細情報のリスト. */
  deliveryPlanDetails: DeliveryPlanDetail[];
  /** 納品予定裁断情報のリスト. */
  deliveryPlanCuts: DeliveryPlanCut[];
}
