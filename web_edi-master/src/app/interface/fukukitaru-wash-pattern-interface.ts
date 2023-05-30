/** フクキタル連携 絵表示(洗濯マーク)の型定義 */
export interface FukukitaruWashPatternInterface {
  /** フクキタル洗濯ネーム付記用語情報ID. */
  id: number;
  /** 洗濯マークID. */
  washPatternId: number;
  /** カラーコード. */
  colorCode: string;
  /** カラー名. */
  colorName: string;
  /** 洗濯マークコード. */
  washPatternCode: string;
  /** 洗濯マーク名称. */
  washPatternName: string;
  /** プルダウン表示切り替え. */
  showWashPattern: boolean;
}
