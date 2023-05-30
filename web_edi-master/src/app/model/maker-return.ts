import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { LgSendType } from '../const/lg-send-type';

import { MakerReturnProductComposite } from './maker-return-product-composite';

/**
 * メーカー返品のモデル.
 */
export class MakerReturn {

  /** 伝票番号. */
  voucherNumber: string;

  /** 伝票番号行. */
  voucherLine: number;

  /** 管理No. */
  manageNumber: string;

  /** 店舗コード. */
  shpcd: string;

  /** 店舗名. */
  shopName: string;

  /** ディスタコード(レスポンスには返ってこない). */
  distaCode: string;

  /** 物流コード. */
  logisticsCode: string;

  /** 仕入先コード. */
  supplierCode: string;

  /** 仕入先名称. */
  supplierName: string;

  /** 返品日. */
  returnAt: Date | string | NgbDateStruct;

  /** 製造担当コード. */
  mdfStaffCode: string;

  /** 製造担当名称. */
  mdfStaffName: string;

  /** 摘要. */
  memo: string;

  /** LG送信区分. */
  lgSendType: LgSendType;

  /** メーカー返品商品リスト. */
  makerReturnProducts: MakerReturnProductComposite[];

  /** メーカー返品ファイルID */
  makerReturnFileNoId: number;
}
