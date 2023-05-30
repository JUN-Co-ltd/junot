/**
 * 品番の内部構造(品種と連番)
 */
export interface PartNo {
  /** 品種 */
  partNoKind: string;
  /** 通番 */
  partNoSerialNo: string;
}
