import { Authority } from 'src/app/enum/authority.enum';

/**
 * マスタメンテ ユーザ検索結果用Model
 */
export class MaintUserSearchResult {
  /** ID */
  id: number;

  /** アカウント名 */
  accountName: string;

  /** 有効/無効 */
  enabled: boolean;

  /** 権限 */
  authorities: Authority[];

  /** 所属会社 */
  company: string;

  /** 氏名 */
  name: string;

  /** メーカー名称. */
  makerName: string;

  /** メールアドレス */
  mailAddress: string;
}
