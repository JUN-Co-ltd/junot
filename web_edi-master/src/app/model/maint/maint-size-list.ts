//PRD_0137 #10669 add start
/**
 * マスタメンテ コード管理テーブル一覧結果用Model
 */
export class MaintSizeList {
  /** ID */
  id: string;

  /** 表示順. */
  jun: string;

  /** サイズ. */
  szkg: string;
}

/**
 * マスタメンテ 検索用Model
 */
export class MaintSizeSearch {
  pageToken: string;
  page: number;
  maxResults: number;
  conditions: any;
}
//PRD_0137 #10669 add end
