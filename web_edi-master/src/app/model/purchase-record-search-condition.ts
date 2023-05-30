//PRD_0133 #10181 add JFE start
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { PurchaseRecordType } from '../const/purchase-record-type';

/**
 * 仕入実績一覧検索条件.
 */
export class PurchaseRecordSearchCondition {
  /** 対象ディスタ. */
  arrivalShop: string;

  /** 仕入区分. */
  // purchaseType: PurchaseRecordType;
  purchaseType: number;

  /** 計上日from. */
  recordAtFrom: Date | NgbDateStruct | string;

  /** 計上日to. */
  recordAtTo: Date | NgbDateStruct | string;

  /** 品番 */
  partNo: string;

  /** 会社コード */
  comCode: string;

  /** 仕入先コード. */
  sirCodes: string = "";

  /** 仕入先（メーカー）. */
  mdfMakerCode: string;

  /** 仕入先名（メーカー名） */
  mdfMakerName: string;

  /** 事業部コード. */
  divisionCode: string;

  /** 1つの結果ページで返されるリストの最大数 */
  maxResults: number;

  /** 戻す結果ページを指定するトークン. */
  pageToken: string;

  /**費目.製品. */
  expenseProduct: boolean;

  /**費目.生地. */
  expenseMaterial: boolean;

  /**費目.附属. */
  expenseAttached: boolean;

  /**費目.加工. */
  expenseProcessing: boolean;

  /**費目.加工. */
  expenseOther: boolean;
}

//PRD_0133 #10181 add JFE end
