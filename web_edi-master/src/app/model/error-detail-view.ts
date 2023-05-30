import { ErrorDetail } from './error-detail';

/**
 * 画面表示用のエラー詳細のモデル.
 */
export class ErrorDetailView extends ErrorDetail {
  code: string;
  args: any;
  message: string;
  viewErrorMessageCode: string; // 画面表示用エラーメッセージコード
}
