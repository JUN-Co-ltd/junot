/**
 * 組成のModel.
 */
export class Compositions {
  /** ID */
  id: number;
  /** 品番ID */
  partNoId: number;
  /** 品番 */
  partNo: string;
  /** 連番 */
  serialNumber: number;
  /** 色 */
  colorCode: string;
  /** 色名称 */
  colorName: string;
  /** パーツ */
  partsCode: number;
  /** パーツ名称 */
  partsName: string;
  /** 組成 */
  compositionCode: string;
  /** 組成名称 */
  compositionName: string;
  /** 率 */
  percent: number;
}
