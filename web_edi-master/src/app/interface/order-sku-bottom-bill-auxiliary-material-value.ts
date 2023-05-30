/** 下札同封副資材の入力値の型定義 */
export interface OrderSkuBottomBillAuxiliaryMaterialValue {
  id: number;
  fOrderId: number;
  checked: boolean;
  name: string;
  materialId: number;
  orderLot: number;
  moq: number;
}
