import { FukukitaruAppendicesTerm } from './fukukitaru-appendices-term';

/** フクキタル連携 色別の付記用語の型定義 */
export interface FukukitaruAppendicesTermByColor {
  /** 色コード. */
  colorCode: string;
  /** 色名. */
  colorName: string;
  /** 付記用語のリスト. */
  appendicesTermList: FukukitaruAppendicesTerm[];
}
