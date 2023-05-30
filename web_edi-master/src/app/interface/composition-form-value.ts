import { CompotisionDetailFormValue } from './composition-detail-form-value';

/** 組成(混率)の入力値の型定義 */
export interface CompotisionFormValue {
  colorCode: string;  // カラーコード
  colorName: string;  // カラー名
  showCompositions: boolean; // チェックボックス表示フラグ
  compositionDetailList: CompotisionDetailFormValue[];  // 組成(混率)詳細FormArray
}
