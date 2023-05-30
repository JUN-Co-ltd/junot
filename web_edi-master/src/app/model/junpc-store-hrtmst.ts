import { JunpcStoreHrtmstDivision } from './junpc-store-hrtmst-division';

/**
 * 発注生産システムの店舗別配分率マスタのModel.
 */
export class JunpcStoreHrtmst {
  /** ブランド. */
  brandCode: string;
  /** アイテム. */
  itemCode: string;
  /** シーズン. */
  season: string;
  /** 配分率区分. */
  hrtkbn: string;
  /** 配分率名. */
  rtname: string;
  /** 配分率合計. */
  totalHritu: number;
  /** 店舗別配分率. */
  storeHrtmstDivisions: JunpcStoreHrtmstDivision[];
}
