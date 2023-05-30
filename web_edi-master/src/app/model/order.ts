import { OrderSku } from './order-sku';
import { OrderFileInfo } from './order-file-info';
/**
 * 発注一覧のを保持するModel.
 */
export class Order {
  /** ID */
  id: number;
  /** 発注No */
  orderNumber: number;
  // PRD_0144 #10776 add JFE start
  /** 関連No */
  relationNumber: number;
  // PRD_0144 #10776 add JFE end
  /** 品番ID */
  partNoId: number;
  /** 品番 */
  partNo: string;
  /** 費目 */
  expenseItem: string;
  /** 裁断自動区分 */
  cutAutoType: string;
  /** 生地メーカーコード */
  matlMakerCode: string;
  /** 生地品番 */
  matlPartNo: string;
  /** 生地品名 */
  matlProductName: string;
  /** 生地納期 */
  matlDeliveryAt: string;
  /** 生地m数 */
  matlMeter: number;
  /** 生地単価 */
  matlUnitPrice: number;
  /** 反番 */
  clothNumber: string;
  /** 実用尺 */
  necessaryLengthActual: number;
  /** 用尺単位 */
  necessaryLengthUnit: number;
  /** 生地原価 */
  matlCost: number;
  /** 発注先メーカーID(製品). */
  productOrderSupplierId: number;
  /** 製品メーカーコード */
  mdfMakerCode: string;
  /** 製品メーカー名 */
  mdfMakerName: string;
  /** 生産工場 */
  mdfMakerFactoryCode: string;
  /** 生産工場名 */
  mdfMakerFactoryName: string;
  /** 委託先工場名 */
  consignmentFactory: string;
  /** 原産国 */
  cooCode: string;
  /** 原産国名称 */
  cooName: string;
  /** 製品発注日 */
  productOrderAt: Date;
  /** 製品納期 */
  productDeliveryAt: Date;
  /** 製品修正納期 */
  productCorrectionDeliveryAt: Date;
  /** 発注完了日 */
  porderCompleteAt: Date;
  /** 製造担当コード */
  mdfStaffCode: string;
  /** 製造担当名 */
  mdfStaffName: string;
  /** 製品完納区分 */
  productCompleteOrder: string;
  /** 製品済区分 */
  productCompleteType: string;
  /** 全済区分 */
  allCompletionType: string;
  /** 数量 */
  quantity: number;
  /** 単価 */
  unitPrice: number;
  /** 上代 */
  retailPrice: number;
  /** 製品原価 */
  productCost: number;
  /** 加工賃 */
  processingCost: number;
  /** 附属代 */
  attachedCost: number;
  /** その他原価 */
  otherCost: number;
  /** B級品単価. */
  nonConformingProductUnitPrice: number;
  /** 輸入区分 */
  importCode: string;
  /** 摘要 */
  application: string;
  /** 送信区分 */
  sendCode: string;
  /** 納品依頼回数. */
  deliveryCount: number ;
  /** 発注承認ステータス */
  orderApproveStatus: string;
  /** 発注確定日 */
  orderConfirmAt: Date;
  /** 発注確定者 */
  orderConfirmUserId: number;
  /** 発注承認日 */
  orderApproveAt: Date;
  /** 発注書出力フラグ */
  orderSheetOut: number;
  /** 連携登録者. */
  junpcTanto: string;
  /** 連携ステータス. */
  linkingStatus: string;
  /** 連携日時. */
  linkedAt: Date;
  /** 発注SKU情報 */
  orderSkus: OrderSku[];
  /** 発注ファイル情報. */
  orderFileInfo: OrderFileInfo;
  /** 読み取り専用 */
  readOnly: boolean;
}
