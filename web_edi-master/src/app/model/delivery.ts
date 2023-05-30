import { DeliveryDetail } from './delivery-detail';
import { DeliveryVoucherFileInfo } from './delivery-voucher-file-info';

/**
 * 納品依頼情報を保持するModel.
 */
export class Delivery {
  /** ID. */
  id: number;
  /** 発注ID. */
  orderId: number;
  /** 発注No. */
  orderNumber: number;
  /** 品番ID. */
  partNoId: number;
  /** 品番. */
  partNo: string;
  /** 納品依頼回数. */
  deliveryCount: number;
  /** 最終納品ステータス. */
  lastDeliveryStatus: string;
  /** 承認ステータス. */
  deliveryApproveStatus: string;
  /** 承認日. */
  deliveryApproveAt: Date;
  /** 配分率区分 */
  distributionRatioType: string;
  /** メモ */
  memo: string;
  /** 納期変更理由Id */
  deliveryDateChangeReasonId: number;
  /** 納期変更理由詳細 */
  deliveryDateChangeReasonDetail: string;
  /** B級品区分. */
  nonConformingProductType: boolean;
  /** B級品単価. */
  nonConformingProductUnitPrice: number;
  /** 納品明細情報のリスト. */
  deliveryDetails: DeliveryDetail[];
  /** SQロックユーザーID. */
  sqLockUserId: number;
  /** SQロックユーザーアカウント名. */
  sqLockUserAccountName: string;
  /** 店舗別登録フラグ.  */
  fromStoreScreenFlg: boolean;
  /** 納品伝票ファイル情報のリスト. */
  deliveryVoucherFileInfos: DeliveryVoucherFileInfo[];
}
