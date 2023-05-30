//PRD_0133 #10181 add JFE start
/**
 * 仕入実績CSV検索結果
 */
 export class PurchaseRecordCsv {
  /** 仕入先コード */
  supplierCode: string;
  /** 仕入先名称 */
  supplierName: string;
  /** スポット・工場 */
  arrivalPlace: string;
  /** 物流コード */
  logisticsCode: string;
  /** 計上日 */
  recordAt: string;
  /** 伝票No */
  purchaseVoucherNumber: string;
  /** 仕入区分*/
  purchaseType: string;
  /** 品番 */
  partNo: string;
  /** 数量 */
  fixArrivalCount: string;
  /** m級 */
  mkyu: string;
  /** 単価 */
  purchaseUnitPrice: string;
  /** 金額(単価*数量) */
  unitPriceSum: string;
}
//PRD_0133 #10181 add JFE end
