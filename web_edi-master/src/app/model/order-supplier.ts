/**
 * 発注先生産メーカーを保持するModel.
 */

export class OrderSupplier {
  /** ID */
  id: number;
  /** 品番ID */
  partNoId: number;
  /** メーカーコード */
  supplierCode: string;
  /** メーカー名 */
  supplierName: string;
  /** 工場コード */
  supplierFactoryCode: string;
  /** 工場名 */
  supplierFactoryName: string;
  /** 委託先工場名 */
  consignmentFactory: string;
}
