import { FukukitaruMasterMaterialType } from '../const/const';

/**
 * フクキタル洗濯ネーム付記用語情報のModel.
 */
export class FukukitaruMaterialAttentionName {
  /** ID. */
  id: number;

  /** 資材種別. */
  materialType: FukukitaruMasterMaterialType;

  /** 資材種別名. */
  materialTypeName: string;

  /** 資材コード. */
  materialCode: string;

  /** 資材コード名. */
  materialCodeName: string;

  /** 出荷単位. */
  moq: number;

  /** 並び順. */
  sortOrder: number;

  /** 種類. */
  type: string;

  /** 品名. */
  productName: string;

  /** タイトル. */
  title: string;

  /** アテンション文. */
  sentence: string;

  /** 旧品番. */
  oldMaterialCode: string;
}
