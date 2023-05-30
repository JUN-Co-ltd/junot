/**
 *フクキタル連携 付記用語マスタ情報のModel(洗濯ネーム付記用語、アテンションタグ付記用語).
 */
export class FukukitaruMasterAppendicesTerm {
  /** ID */
  id: number;
  /** 付記用語コード. */
  appendicesTermCode: string;
  /** 付記用語コード名. */
  appendicesTermCodeName: string;
  /** 付記用語文章. */
  appendicesTermSentence: string;
  /** 特徴. */
  characteristic: string;
  /** 並び順. */
  sortOrder: number;
}
