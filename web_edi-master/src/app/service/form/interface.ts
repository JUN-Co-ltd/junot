import { MisleadingRepresentationType } from 'src/app/const/const';

/**
 * 優良誤認検査フォーム項目.
 */
export interface MisleadingRepresentationInspectionForm {
  /** 優良誤認承認情報ID. */
  id: number;
  /** 優良誤認検査対象区分. */
  misleadingRepresentationType: MisleadingRepresentationType;
  /** 原産国コード. */
  cooCode: string;
  /** カラーコード. */
  colorCode: string;
  /** カラー名称. */
  colorName: string;
  /** 生産メーカーコード. */
  mdfMakerCode: string;
  /** チェック. */
  check: boolean;
  /** 承認者アカウント(コード). */
  approvalUserAccountName: string;
  /** 承認者名称. */
  approvalUserName: string;
  /** 承認日. */
  approvalAt: Date;
  /** メモ. */
  memo: string;
  /** 更新日時. */
  updatedAt;
}
