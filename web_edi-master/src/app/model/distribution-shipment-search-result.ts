
import { CarryType } from '../const/const';
import { LgSendType } from '../const/lg-send-type';

/**
 * 配分出荷指示一覧検索結果モデル.
 */
export class DistributionShipmentSearchResult {

  /** 納品明細ID */
  id: number;

  /** 出荷日 */
  shippingInstructionsAt: Date;

  /** 入荷フラグ */
  arrivalFlg: boolean;

  /** 入荷日 */
  arrivalAt: Date;

  /** 納品依頼日 */
  deliveryRequestAt: Date;

  /** 発注No */
  orderNumber: number;

  /** 納品依頼No */
  deliveryNumber: string;

  /** 納品依頼回数 */
  deliveryCount: number;

  /** 課コード */
  divisionCode: string;

  /** キャリー区分. */
  carryType: CarryType;

  /** 品番 */
  partNo: string;

  /** 品名 */
  productName: string;

  /** 数量(納品明細に紐づく納品得意先情報の得意先SKUの納品数量合計) */
  deliveryLotSum: number;

  /** 仕入確定数 */
  fixArrivalLotSum: number;

  /** 上代金額 */
  retailPriceSum: number;

  /** 送信状態 */
  sendStatus: LgSendType;
}
