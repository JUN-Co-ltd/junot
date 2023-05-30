/** サイズの入力値の型定義 */
export interface SizeFormValue {
  id: number;         // サイズID
  colorCode: string;  // カラーコード
  size: string;       // サイズ
  janCode: string;    // JANコード
  select: boolean;    // チェックボックス選択中フラグ
  brandCode: string;  // ブランドコード
  deptCode: string;   // 部門コード
}
