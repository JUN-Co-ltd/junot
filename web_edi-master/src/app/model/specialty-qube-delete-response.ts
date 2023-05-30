import { SpecialtyQubeDeleteStatusType } from '../const/const';

/**
 * 全店配分削除APIレスポンス情報の項目を保持するModel.
 */
export class SpecialtyQubeDeleteResponse {
  /** 処理時間. */
  processtime: Date;
  /** ステータス. */
  status: SpecialtyQubeDeleteStatusType;
  /** SQロックユーザーID. */
  sqLockUserId: number;
  /** エラーリスト. */
  errorList: String[];
}
