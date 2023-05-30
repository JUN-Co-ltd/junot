/** アテンション下札の入力値の型定義 */
export interface OrderSkuBottomBillAttention {
  id: number;
  fOrderId: number;
  colorCode: string;
  name: string;
  materialId: number;
  orderLot: number;
}
