import { DeliverySku } from './delivery-sku';
import { DeliveryFileInfo } from './delivery-file-info';
import { DeliveryStore } from './delivery-store';

/**
 * 納品依頼明細情報を保持するModel.
 */
export class DeliveryDetail {
  /** ID. */
  id: number;
  /** 納品ID. */
  deliveryId: number;
  /** 納品No. */
  deliveryNumber: string;
  /** 納品依頼日. */
  deliveryRequestAt: Date;
  /** 納品依頼回数. */
  deliveryCount: number;
  /** 課コード. */
  divisionCode: string;
  /** 店舗別登録済フラグ. */
  storeRegisteredFlg: boolean;
  /** 配分区分. */
  allocationType: string;
  /** キャリータイプ2種類あり. */
  hasBothCarryType: boolean;
  /** キャリー区分. */
  carryType: string;
  /** セール対象品区分. */
  saleType: string;
  /** 納品依頼No. */
  deliveryRequestNumber: string;
  /** 物流コード. */
  logisticsCode: string;
  /** 納期. */
  deliveryAt: Date;
  /** 修正納期. */
  correctionAt: Date;
  /** 配分出荷日. */
  allocationCargoAt: Date;
  /** 配分完納フラグ. */
  allocationCompletePaymentFlg: boolean;
  /** 配分確定フラグ. */
  allocationConfirmFlg: boolean;
  /** 入荷フラグ. */
  arrivalFlg: boolean;
  /** 入荷日. */
  arrivalAt: Date;
  /** 入荷管理No. */
  arrivalNumber: string;
  /** 入荷場所. */
  arrivalPlace: string;
  /** ピッキングフラグ. */
  pickingFlg: boolean;
  /** ピッキング日. */
  pickingAt: Date;
  /** 配分完了フラグ. */
  allocationCompleteFlg: boolean;
  /** 配分完了日. */
  allocationCompleteAt: Date;
  /** 配分計上日. */
  allocationRecordAt: Date;
  /** 一括仕入用納品No. */
  bulkDeliveryNumber: string;
  /** 出荷指示済フラグ. */
  shippingInstructionsFlg: boolean;
  /** 出荷指示日. */
  shippingInstructionsAt: Date;
  /** 出荷停止区分. */
  shippingStoped: boolean;
  /** ファックス送信フラグ. */
  faxSend: boolean;
  /** 納品依頼書発行フラグ. */
  deliverySheetOut: boolean;
  /** 配分率ID */
  distributionRatioId: number;
  /** 連携入力者. */
  junpcTanto: string;
  /** 連携ステータス. */
  linkingStatus: string;
  /** 連携日時. */
  linkedAt: Date;
  /** 場所コード. */
  allocationCode: string;
  /** 納品依頼ファイル情報. */
  deliveryFileInfo: DeliveryFileInfo;
  /** 納品SKU情報のリスト. */
  deliverySkus: DeliverySku[];
  /** 納品得意先情報のリスト. */
  deliveryStores: DeliveryStore[];
}
