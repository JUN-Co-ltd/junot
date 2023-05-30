import { MisleadingRepresentationFile } from './misleading-representation-file';

/**
 * 登録する優良誤認検査ファイル情報を保持するModel.
 */
export class MisleadingRepresentationFileRequest extends MisleadingRepresentationFile {
  /** 登録モード. */
  mode: number;
  /** ファイルの実体(画面のキャッシュ保持で必要。APIRequetでは用いない). */
  fileBlob: Blob;
}
