import { JunpcHrtmstDivision } from './junpc-hrtmst-division';

/**
 * 発注生産システムの配分率マスタのModel.
 */
export class JunpcHrtmst {
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
  /** 課別配分率. */
  hrtmstDivisions: JunpcHrtmstDivision[];
}
