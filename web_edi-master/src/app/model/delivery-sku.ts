/**
 * 納品SKU情報を保持するModel.
 */
export class DeliverySku {
  /** ID. */
  id: number;
  /** 納品明細Id. */
  deliveryDetailId: number;
  /** 納品依頼No. */
  deliveryRequestNumber: string;
  /** 課コード. */
  divisionCode: string;
  /** サイズ. */
  size: string;
  /** 色. */
  colorCode: string;
  /** 納品数量. */
  deliveryLot: number;
  /** 入荷数量. */
  arrivalLot: number;
  /** 色名称. */
  colorName: string;
  /** 課名称.*/
  divisionName: string;
}
