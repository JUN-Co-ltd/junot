/**
 * メーカー返品商品のモデル.
 */
export class MakerReturnProductComposite {
  /** ID. */
  id: number;

  /** 品番ID. */
  partNoId: number;

  /** 品番. */
  partNo: string;

  /** 品名. */
  productName: string;

  /** ブランド. */
  brandCode: string;

  /** アイテム. */
  itemCode: string;

  /** カラーコード. */
  colorCode: string;

  /** サイズ. */
  size: string;

  /** 発注ID. */
  orderId: number;

  /** 発注番号. */
  orderNumber: number;

  /** 発注日. */
  productOrderAt: Date;

  /** 返品数量. */
  returnLot: number;

  /** 上代. */
  retailPrice: number;

  /** 下代(発注の単価). */
  unitPrice: number;

  /** 最新の単価(品番情報のその他原価). */
  otherCost: number;

  /** 在庫数. */
  stockLot: number;

  /** 選択状態 */
  selected = false;
}
