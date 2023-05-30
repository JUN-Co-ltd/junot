import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

/**
 * 配分出荷一覧検索のモデル.
 */
export class DistributionShipmentSearchCondition {

  /** 店舗コード(ディスタ選択) */
  shpcd: string;

  /** 課コード */
  divisionCode: string;

  /** 事業部コード */
  departmentCode: string;

  /** 出荷from */
  shippingAtFrom: Date | NgbDateStruct | string;

  /** 出荷to */
  shippingAtTo: Date | NgbDateStruct | string;

  /* PRD_0005 add SIT start */
  /** 入荷日from  **/
  arrivalAtFrom: Date | NgbDateStruct | string;

  /** 入荷日to **/
  arrivalAtTo: Date | NgbDateStruct | string;
  /* PRD_0005 add SIT end */
  /* PRD_0004 add SIT start */
  /** 納品依頼日from **/
  deliveryRequestAtFrom: Date | NgbDateStruct | string;

  /** 納品依頼日to **/
  deliveryRequestAtTo: Date | NgbDateStruct | string;
  /* PRD_0004 add SIT end */

  /** ブランドコード */
  brandCode: string;

  /** アイテムコード */
  itemCode: string;

  /** 次のページのトークン */
  pageToken: string;
}
