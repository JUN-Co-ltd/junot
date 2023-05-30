/**
 * APIレスポンスのエラー詳細のモデル.
 */
export class ErrorDetail {
  /** エラーコード. */
  code: string;

  /** エラーコードの引数. */
  args: any;

  /** メッセージ. */
  message: string;

  /** 対象のリソース. */
  resource: string;

  /** 対象のフィールド. */
  field: string;

  /** 対象の値. */
  value: object;
}
