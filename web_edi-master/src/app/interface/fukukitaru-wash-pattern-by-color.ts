/** フクキタル連携 色別の絵表示(洗濯マーク)の型定義 */
export interface FukukitaruWashPatternByColor {
  /** 色コード. */
  colorCode: string;
  /** 色名. */
  colorName: string;
  /** 洗濯マークID. */
  washPatternId: number;
  /** 洗濯マーク名称. */
  washPatternName: string;
}
