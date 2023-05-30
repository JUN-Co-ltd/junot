/** 組成(混率)詳細の入力値の型定義 */
export interface CompotisionDetailFormValue {
  id: number;  // 組成ID
  colorCode: string;  // カラーコード
  partsCode: string;  // パーツコード
  partsName: string;  // パーツ名
  compositionCode: string;  // 組成コード
  compositionName: string;  // 組成名
  percent: string;  // パーセント
}
