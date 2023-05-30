/**
 * メーカー返品確定のモデル.
 */
export class MakerReturnConfirm {
  /** チェック(画面input用) */
  check: boolean;

  /** 発注ID */
  orderId: number;

  /** 伝票番号 */
  voucherNumber: string;
}
