/**
 * マスタメンテ コード管理テーブル一覧結果用Model
 */
export class MaintCodeResult {
  /** テーブルID. */
  tableId: string;

  /** マスタ名称. */
  name: string;
}

/**
 * マスタメンテ テーブルSettings用Model
 */
export class MaintTableInfoResult {
  /** テーブルID. */
  id: string;

  /** テーブルID. */
  tableId: string;

  fields: MaintTableInfofields[];

  createdUserId: string;

  createdAt: string | Date;

  updatedUserId: string;

  updatedAt: string | Date;

  deletedAt: string;
}

/**
 * マスタメンテ テーブルSettings_fields結果用Model
 */
export class MaintTableInfofields {
  key: string;
  name: string;
  type: string;
  validators: string[];
}

/**
 * マスタメンテ 検索用Model
 */
export class MaintCodeSearch {
  pageToken: string;
  page: number;
  maxResults: number;
  conditions: any;
}

/**
 * マスタメンテ 更新用Model
 */
export class UpdateCode {
  revisionedAt: string | Date;
  items: any[];
}
