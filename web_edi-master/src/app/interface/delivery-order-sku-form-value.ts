import { DeliverySkuFormValue } from './delivery-sku-form-value';

/**
 * 納品依頼画面の発注SKUの入力値の型定義
 */
export interface DeliveryOrderSkuFormValue {
  colorCode: string;  // カラーコード
  size: string; // サイズ
  deliverySkus: DeliverySkuFormValue[];  // 納品SKUFormArray
}
