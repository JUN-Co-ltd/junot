import { QualityApprovalStatus, MSirmstYugaikbnType } from '../const/const';

import { Sku } from './sku';
import { Compositions } from './compositions';
import { ItemFileInfo } from './item-file-info';
import { MisleadingRepresentationFile } from './misleading-representation-file';
import { MisleadingRepresentation } from './misleading-representation';

/**
 * 品番優良誤認承認情報を保持するModel.
 */
export class ItemMisleadingRepresentation {
  /** 品番ID. */
  id: number;

  /** 品番. */
  partNo: string;

  /** 品名. */
  productName: string;

  /** 最新発注ID. */
  latestOrderId: number;

  /** 最新発注No. */
  latestOrderNumber: number;

  /** 最新発注数. */
  latestQuantity: number;

  /** 生産メーカー. */
  mdfMakerCode: string;

  /** 生産メーカー名称. */
  mdfMakerName: string;

  /** 有害物質対応区分. */
  hazardousSubstanceResponseType: MSirmstYugaikbnType;

  /** 有害物質対応日付. */
  hazardousSubstanceResponseAt: Date;

  /** 原産国コード. */
  cooCode: string;

  /** 原産国名称. */
  cooName: string;

  /** 上代. */
  retailPrice: number;

  /** 生地原価. */
  matlCost: number;

  /** 加工賃. */
  processingCost: number;

  /** 付属品. */
  accessoriesCost: number;

  /** その他原価. */
  otherCost: number;

  /** 投入日. */
  deploymentDate: Date;

  /** 投入週. */
  deploymentWeek: number;

  /** ブランドコード. */
  brandCode: string;

  /** ブランド名. */
  brandName: string;

  /** アイテムコード. */
  itemCode: string;

  /** アイテム名. */
  itemName: string;

  /** 年度. */
  year: number;

  /** シーズンコード. */
  seasonCode: string;

  /** 優良誤認承認区分（組成）. */
  qualityCompositionStatus: QualityApprovalStatus;

  /** 優良誤認承認区分（国）. */
  qualityCooStatus: QualityApprovalStatus;

  /** 優良誤認承認区分（有害物質）. */
  qualityHarmfulStatus: QualityApprovalStatus;

  /** 素材コード. */
  materialCode: string;

  /** 素材名. */
  materialName: string;

  /** 企画担当. */
  plannerCode: string;

  /** 企画担当名称. */
  plannerName: string;

  /** 製造担当. */
  mdfStaffCode: string;

  /** 製造担当名称. */
  mdfStaffName: string;

  /** パターンナー. */
  patanerCode: string;

  /** パターンナー名称. */
  patanerName: string;

  /** メモ. */
  memo: string;

  /** 発注番号. */
  orderNumber: number;

  /** 発注数. */
  quantity: number;

  /** SKU情報のリスト. */
  skus: Sku[];

  /** 組成情報のリスト. */
  compositions: Compositions[];

  /**品番ファイル情報. */
  tanzakuItemFileInfo: ItemFileInfo;

  /** 優良誤認検査ファイル情報のリスト. */
  misleadingRepresentationFiles: MisleadingRepresentationFile[];

  /** 優良誤認承認情報のリスト */
  misleadingRepresentations: MisleadingRepresentation[];

  /** 更新日時 */
  updatedAt: Date;

  /** 読み取り専用 */
  readOnly: boolean;
}
