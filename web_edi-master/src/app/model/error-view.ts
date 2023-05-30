import { Error } from './error';
import { ErrorDetail } from './error-detail';
import { ErrorDetailView } from './error-detail-view';

/**
 * 画面表示用のエラー情報のモデル.
 */
export class ErrorView extends Error {
  error: string;
  errors: ErrorDetail[];
  message: string;
  path: string;
  status: number;
  timestamp: Date;
  viewErrorMessageCode: string;  // 画面表示用エラーメッセージコード
  viewErrors: ErrorDetailView[]; // 画面表示用エラー詳細リスト
}
