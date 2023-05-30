/**
 * ファイル情報のモデル
 */
export class TFile {
  /** ID */
  id: number;
  /** コンテントタイプ */
  contentType: string;
  /** ファイル名 */
  fileName: string;
  /** メモ */
  memo: string;
  /** ファイルデータ */
  fileData: File;
}
