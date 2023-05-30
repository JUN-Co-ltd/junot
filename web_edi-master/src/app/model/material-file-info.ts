import { FukukitaruMasterType } from '../const/const';

/**
 * 資材発注ファイル情報のモデル
 */
export class MaterialFileInfo {
  /** ファイルID */
  fileNoId: number;
  /** 資材種別 */
  masterType: FukukitaruMasterType;
}
