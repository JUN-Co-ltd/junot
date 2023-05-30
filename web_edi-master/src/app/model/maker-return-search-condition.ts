import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { OnOffType } from '../const/on-off-type';

/**
 * メーカー返品検索条件モデル.
 */
export class MakerReturnSearchCondition {

  /** 店舗コード(ディスタ選択) */
  shpcd: string;

  /** 伝票入力日from */
  voucherNumberInputAtFrom: Date | NgbDateStruct | string;

  /** 伝票入力日to */
  voucherNumberInputAtTo: Date | NgbDateStruct | string;

  /** メーカーコード */
  supplierCode: string;

  /** 伝票日付from */
  voucherNumberAtFrom: Date | NgbDateStruct | string;

  /** 伝票日付to */
  voucherNumberAtTo: Date | NgbDateStruct | string;

  /** 担当者コード */
  mdfStaffCode: string;

  /** 伝票番号from */
  voucherNumberFrom: string;

  /** 伝票番号to */
  voucherNumberTo: string;

  /** 状態 */
  lgSendType: OnOffType;

  /** 製造担当名称. */
  mdfStaffName: string;

  /** 次のページのトークン */
  pageToken: string;
}
