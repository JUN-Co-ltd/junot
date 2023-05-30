import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

/**
 * 仕入一覧検索条件.
 */
export class PurchaseSearchCondition {
  /** 対象ディスタ. */
  arrivalShop: string;

  /** 納品日from. */
  correctionAtFrom: Date|NgbDateStruct|string;

  /** 納品日to. */
  correctionAtTo: Date|NgbDateStruct|string;

  /** 仕入先（メーカー）. */
  mdfMakerCode: string;

  /** 仕入先名（メーカー名） */
  mdfMakerName: string;

  /** 納品No From. */
  deliveryNumberFrom: string;

  /** 納品No To. */
  deliveryNumberTo: string;

  /** 仕入. */
  arrivalFlg: boolean;

  /** 送信. */
  lgSendFlg: boolean;

  /** 入荷日from. */
  arrivalAtFrom: Date|NgbDateStruct|string;

  /** 入荷日to. */
  arrivalAtTo: Date|NgbDateStruct|string;

  // PRD_0021 add SIT start
  /** 品番 */
  partNo: string;

  /** ブランド */
  brandCode: string;
  // PRD_0021 add SIT end

  /** 1つの結果ページで返されるリストの最大数 */
  maxResults: number;

  /** 戻す結果ページを指定するトークン. */
  pageToken: string;
}
