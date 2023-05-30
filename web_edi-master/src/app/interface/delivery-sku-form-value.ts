/**
 * 納品SKUの入力値の型定義
 */
export interface DeliverySkuFormValue {
  id: number; // 納品SKUID
  deliveryDetailId: number; // 納品明細ID
  deliveryRequestNumber: number;  // 納品依頼No
  divisionCode: string; // 課コード
  allocationCode: string; // 場所コード
  size: string; // サイズ
  colorCode: string;  // カラーコード
  deliveryLot: number;  // 納品数量
  deliveryId: number; // 納品ID
  orderId: number;  // 発注ID
  orderNumber: number;  // 発注No
  partNoId: number; // 品番ID
  partNo: number; // 品番No
  distributionRatioId: number;  // 配分率ID
}
