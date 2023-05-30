import { FukukitaruMasterMaterialType } from '../const/const';

/**
 * 入力補助セットのModel.
 */
export class FukukitaruInputAssistSet {
  /** ID. */
  id: number;

  /** セット名. */
  setName: string;

  /** 入力補助セット詳細. */
  listInputAssistSetDetails: FukukitaruInputAssistSetDetails[];
}

/**
 * 入力補助セット詳細のModel.
 */
export class FukukitaruInputAssistSetDetails {
  /** ID. */
  id: number;

  /** コード. */
  code: string;

  /** コード名称. */
  codeName: string;

  /** 資材種別. */
  materialType: FukukitaruMasterMaterialType;
}
