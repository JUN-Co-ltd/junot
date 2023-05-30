/** フクキタル連携 付記用語の型定義 */
export interface FukukitaruAppendicesTerm {
  /** ID. */
  id: number;
  /** フクキタル用付記用語マスタID. */
  appendicesTermId: number;
  /** 付記用語コード. */
  appendicesTermCode: string;
  /** 付記用語コード名. */
  appendicesTermCodeName: string;
  /** 付記用語文章. */
  appendicesTermSentence: string;
  /** 特徴. */
  characteristic: string;
}
