import { PurchaseDivision } from './purchase-division';

export interface PurchaseSku {
  /** カラーコード. */
  colorCode: string;

  /** カラー名. */
  colorName: string;

  /** サイズ. */
  size: string;

  /** 仕入配分課リスト. */
  purchaseDivisions: PurchaseDivision[];

  /** 合計数. */
  totalLot: number;

  /** 並び順が最初の色. */
  isFirstColor: boolean;
}
