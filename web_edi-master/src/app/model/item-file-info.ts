/**
 * 品番ファイル情報のモデル
 */
export class ItemFileInfo {
  /** ID */
  id: number;
  /** ファイルID */
  fileNoId: number;
  /** ファイル名 */
  fileName: string;
  /** 品番ID */
  partNoId: number;
  /** ファイル分類(短冊か見積もりか) */
  fileCategory: number;
}
