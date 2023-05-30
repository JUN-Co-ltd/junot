import { LgSendType } from '../const/lg-send-type';
import { CarryType } from '../const/const';

/**
 * 仕入検索結果
 */
export class PurchaseSearchResult {

  /** 納品ID */
  deliveryId: number;
  /** 発注No */
  orderNumber: number;
  /** 納品No */
  deliveryNumber: string;
  /** 回数（納品回数） */
  deliveryCount: number;
  /** 課コード */
  divisionCode: string;
  /** キャリー区分. */
  carryType: CarryType;
  /** 仕入状態(仕入ステータス) */
  arrivalFlg: boolean;
  /** 納品日 */
  correctionAt: Date;
  /** 仕入先コード */
  mdfMakerCode: string;
  /** 仕入先名 */
  mdfMakerName: string;
  /** 品番 */
  partNo: string;
  /** 品名 */
  productName: string;
  /** 配分数 */
  deliveryLot: number;
  /** 仕入(入荷)数合計(納品明細基準) */
  arrivalCountSum: number;
  /** 仕入(入荷)確定数合計(納品明細基準) */
  fixArrivalCountSum: number;
  /** 仕入登録済数 */
  purchaseRegisteredCount: number;
  /** 仕入指示送信数 */
  purchaseConfirmedCount: number;
  /** LG送信区分. */
  lgSendType: LgSendType;
}
