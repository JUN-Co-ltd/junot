import { LgSendType } from '../const/lg-send-type';

/**
 * メーカー返品検索結果モデル.
 */
export class MakerReturnSearchResult {

  /** 伝票番号 */
  voucherNumber: string;

  /** LG送信区分 */
  lgSendType: LgSendType;

  /** 伝票日付 */
  returnAt: Date;

  /** 仕入先コード */
  supplierCode: string;

  /** 仕入先名称 */
  supplierName: string;

  /** 数量 */
  returnLot: number;

  /** 金額 */
  unitPrice: number;

  /** 発注ID */
  orderId: number;

  /** 発注番号 */
  orderNumber: number;

  /** 伝票入力日 */
  createdAt: Date;
}
