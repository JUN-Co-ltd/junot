/**
 * 優良誤認一覧検索結果用Model
 */
export class ItemMisleadingRepresentationSearchResult {

  /** ID */
  id: number;

  /** 品番 */
  partNo: string;

  /** 上代 */
  retailPrice: number;

  /** 品名 */
  productName: string;

  /** 発注No */
  orderNumber: number;

  /** 発注数 */
  quantity: number;

  /** 製造担当 */
  mdfStaffName: string;

  /** 原産国コード */
  cooCode: string;

  /** 原産国名 */
  cooName: string;

  /** 優良誤認承認区分（国） */
  qualityCooStatus: number;

  /** 組成コード */
  compositionCode: string;

  /** 組成 */
  compositionName: string;

  /** 優良誤認承認区分（組成） */
  qualityCompositionStatus: number;

  /** 有害 */
  harmful: string;

  /** 優良誤認承認区分（有害） */
  qualityHarmfulStatus: string;

  /** 承認日 */
  approvalAt: Date | string;
}
