/**
 * SKUのModel.
 */
export class Sku {
  /** ID */
  id: number;
  /** 品番ID */
  partNoId: number;
  /** 品番 */
  partNo: string;
  /** 色 */
  colorCode: string;
  /** 色名称 */
  colorName: string;
  /** サイズ */
  size: string;
  /** JANコード */
  janCode: string;
  /** JANコード名称 */
  janName: string;
  /** 代表JANフラグ */
  representationJanFlg: boolean;
  /** 行インデックス(色) */
  rowIndex: number;
  /** チェックボックス選択中フラグ */
  select: boolean;
}
