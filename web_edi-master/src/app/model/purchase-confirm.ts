/**
 * LG送信(仕入確定)用Model
 */
export class PurchaseConfirm {
  /** チェック(画面input用) */
  check: boolean;

  /** 納品ID */
  deliveryId: number;

  /** 課 */
  divisionCode: string;

  /** 引取回数(納品明細の納品回数) */
  purchaseCount: number;
}
