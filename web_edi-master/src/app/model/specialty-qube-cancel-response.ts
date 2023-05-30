import { SpecialtyQubeCancelStatusType } from '../const/const';

/**
 * 店別配分キャンセルAPIレスポンス情報の項目を保持するModel.
 */
export class SpecialtyQubeCancelResponse {
  /** 処理時間. */
  processtime: Date;
  /** ステータス. */
  status: SpecialtyQubeCancelStatusType;
  /** SQロックユーザーID. */
  sqLockUserId: number;
  /** エラーリスト. */
  errorList: String[];
}
