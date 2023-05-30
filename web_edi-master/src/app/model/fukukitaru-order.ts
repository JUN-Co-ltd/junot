import { FukukitaruMasterDeliveryType, FukukitaruMasterConfirmStatusType } from '../const/const';
import { FukukitaruOrderSku } from './fukukitaru-order-sku';
import { FukukitaruDestination } from './fukukitaru-destination';
import { FukukitaruItem } from './fukukitaru-item';

/**
 * フクキタル発注情報のModel.
 */
export class FukukitaruOrder {
  /** フクキタル発注:フクキタル発注ID. */
  id: number;

  /** フクキタル発注:フクキタル品番ID. */
  fItemId: number;

  /** フクキタル発注:承認需要フラグ. */
  isApprovalRequired: boolean;

  /** フクキタル発注:請求先ID. */
  billingCompanyId: number;

  /** フクキタル発注:請求先住所. */
  billingDestination: FukukitaruDestination;

  /** フクキタル発注:契約No. */
  contractNumber: string;

  /** フクキタル発注:納入先ID. */
  deliveryCompanyId: number;

  /** フクキタル発注:納入先住所. */
  deliveryDestination: FukukitaruDestination;

  /** フクキタル発注:納入先担当者. */
  deliveryStaff: string;

  /** フクキタル発注:工場No. */
  mdfMakerFactoryCode: string;

  /** フクキタル発注:発注日. */
  orderAt: Date;

  /** フクキタル発注:オーダー識別コード. */
  orderCode: string;

  /** フクキタル発注:発注者コード. */
  orderUserId: number;

  /** フクキタル発注:希望出荷日. */
  preferredShippingAt: Date;

  /** フクキタル発注:リピート数. */
  repeatNumber: number;

  /** フクキタル発注:特記事項. */
  specialReport: string;

  /** フクキタル発注:緊急. */
  urgent: boolean;

  /** フクキタル発注:手配先. */
  deliveryType: FukukitaruMasterDeliveryType;

  /** フクキタル発注:確定ステータス. */
  confirmStatus: FukukitaruMasterConfirmStatusType;

  /** 発注送信日. */
  orderSendAt: Date;

  /** フクキタル発注:洗濯ネーム(1). */
  orderSkuWashName: FukukitaruOrderSku[];

  /** フクキタル発注:アテンションネーム(2). */
  orderSkuAttentionName: FukukitaruOrderSku[];

  /** フクキタル発注:洗濯同封副資材(3). */
  orderSkuWashAuxiliary: FukukitaruOrderSku[];

  /** フクキタル発注:下札(4). */
  orderSkuBottomBill: FukukitaruOrderSku[];

  /** フクキタル発注:アテンションタグ(5). */
  orderSkuAttentionTag: FukukitaruOrderSku[];

  /** フクキタル発注:アテンション下札(6). */
  orderSkuBottomBillAttention: FukukitaruOrderSku[];

  /** フクキタル発注:NERGY用メリット下札(7). */
  orderSkuBottomBillNergyMerit: FukukitaruOrderSku[];

  /** フクキタル発注:下札同封副資材(8). */
  orderSkuBottomBillAuxiliaryMaterial: FukukitaruOrderSku[];

  /** 発注種別. */
  orderType: number;

  /** 責任発注. */
  isResponsibleOrder: boolean;

  /** 合計発注数. */
  totalOrderLot: number;

  /** フクキタル品番情報. */
  fkItem: FukukitaruItem;

  /** 発注情報:発注ID. */
  orderId: number;

  /** 発注情報:発注No. */
  orderNumber: number;

  /** 発注情報:製品修正納期. */
  productCorrectionDeliveryAt: Date;

  /** 品番情報:品番ID. */
  partNoId: number;

  /** 品番情報:品番. */
  partNo: string;

  /** 品番情報:品名. */
  productName: string;

  /** 品番情報:生産メーカー名. */
  mdfMakerName: string;

  /** 備考 */
  remarks: string;

  /** 連携ステータス */
  linkingStatus: number;
}
