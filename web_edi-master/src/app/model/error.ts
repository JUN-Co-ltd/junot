import { ErrorDetail } from './error-detail';

/**
 * APIレスポンスのエラー情報のモデル.
 */
export class Error {
  /** HTTPステータスのエラー理由. */
  error: string;

  /** エラー詳細. */
  errors: ErrorDetail[];

  /** エラーメッセージ. */
  message: string;

  /** APIのパス. */
  path: string;

  /** HTTPステータス. */
  status: number;

  /** エラーが発生したタイムスタンプ. */
  timestamp: Date;
}
