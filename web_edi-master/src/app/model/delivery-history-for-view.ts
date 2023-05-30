/**
 * 画面用の納品依頼履歴情報を保持するModel.
 */
export class DeliveryHistoryForView {
  /** 納品ID. */
  id: number;
  /** 発注ID. */
  orderId: number;
  /** ファイルID. */
  deliveryFileNoId: number;
  /** 納品依頼日. */
  deliveryRequestAt: Date;
  /** 納期. */
  deliveryAt: Date;
  /** 納品依頼回数. */
  deliveryCount: number;
  /** 承認ステータス. */
  deliveryApproveStatus: string;
  /** 納期ごとの納品依頼数合計. */
  deliveryLotSum: number;
  /** SKUごとの納品依頼数合計. */
  deliveryLotSumListBySku: number[];
}
