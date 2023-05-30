import { SizeFormValue } from './size-form-value';

/** SKUの入力値の型定義 */
export interface SkuFormValue {
  colorCode: string;  // カラーコード
  colorName: string;  // カラー名
  sizeList: SizeFormValue[];  // サイズFormArray
  rowIndex: number; // SKU行番号
}
