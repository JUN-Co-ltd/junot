import { FukukitaruMasterMaterialType } from '../const/const';

/**
 * フクキタル発注SKU情報のModel.
 */
export class FukukitaruOrderSku {
  /** フクキタル発注SKU情報ID. */
  id: number;
  /** フクキタル発注ID. */
  fOrderId: number;
  /** カラーコード. */
  colorCode: string;
  /** サイズ. */
  size: string;
  /** 資材ID. */
  materialId: number;
  /** 資材数量. */
  orderLot: number;
  /** 資材種類. */
  materialType: FukukitaruMasterMaterialType;
  /** 資材種類名. */
  materialTypeName: string;
  /** 資材コード. */
  materialCode: string;
  /** 資材コード名. */
  materialCodeName: string;
  /** 並び順. */
  sortOrder: number;
  /** 出荷単位.(画面表示用) */
  moq: number;
}
