import { TFile } from './t-file';

/**
 * 優良誤認検査ファイル情報を保持するModel.
 */
export class MisleadingRepresentationFile {
  /** ID. */
  id: number;
  /** 品番ID. */
  partNoId: number;
  /** ファイル情報. */
  file: TFile;
}
