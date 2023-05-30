import { MisleadingRepresentationType } from '../const/const';

/**
 * 優良誤認承認情報を保持するModel.
 */
export class MisleadingRepresentation {
  /** ID. */
  id: number;

  /** 品番ID. */
  partNoId: number;

  /** 優良誤認検査対象区分. */
  misleadingRepresentationType: MisleadingRepresentationType;

  /** 色コード. */
  colorCode: string;

  /** 原産国コード. */
  cooCode: string;

  /** 生産メーカーコード. */
  mdfMakerCode: string;

  /** 検査承認日. */
  approvalAt: Date;

  /** 承認者アカウント名. */
  approvalUserAccountName: string;

  /** 承認者アカウント名称. */
  approvalUserName: string;

  /** メモ. */
  memo: string;

  /** 更新日時. */
  updatedAt: Date;
}
