/**
 * 発注生産システムのサイズマスタのModel.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
export class JunpcSizmst {
  /** ID */
  id: number;
  /** 品種(ブランドコード+アイテムコード) */
  hscd: string;
  /** サイズ */
  szkg: string;
  /** 表示順 */
  jun: string;
  // mntflg: string;
  // crtymd: string;
  // updymd: string;
  // tanto: string;
  // souflg: string;
  // souymd: string;
}
