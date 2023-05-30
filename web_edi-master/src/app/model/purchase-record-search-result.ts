//PRD_0133 #10181 add JFE start
/**
 * 仕入実績一覧検索結果
 */
export class PurchaseRecordSearchResult {

  /** 仕入先コード */
  supplierCode: string;
  /** 仕入先名称 */
  supplierName: string;
  /** スポット・工場 */
  arrivalPlace: string;
  /** 物流コード */
  logisticsCode: string;
  /** 計上日 */
  recordAt: Date;
  /** 伝票No */
  purchaseVoucherNumber: number;
  /** 仕入区分*/
  purchaseType: string;
  /** 品番 */
  partNo: string;
  /** 数量 */
  fixArrivalCount: number;
  /** m数 */
  mkyu: number;
  /** 単価 */
  purchaseUnitPrice: number;
  /** 金額(単価*数量) */
  unitPriceSum: number;

  //◆画面右上の合計欄項目◆

  /** 数量合計 */
  fixArrivalCountSum: number;

  /** m数合計 */
  mkyuSum: number;

  /** 金額の合計 */
  unitPriceSumTotal: number;

  // PRD_0162 #10181 jfe add start
  /** ファイル情報ID. */
  fileInfoId: number;
  // PRD_0162 #10181 jfe add end
}
//PRD_0133 #10181 add JFE end
