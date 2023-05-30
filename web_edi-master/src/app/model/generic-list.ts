/**
 * リストのModel
 */
export class GenericList<T> {
  /** 結果リスト. */
  items: T[];
  /** この結果の次のページにアクセスするためのトークン. */
  nextPageToken: string;
}
