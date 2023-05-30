import { Authority } from 'src/app/enum/authority.enum';

/**
 * マスタメンテ ユーザ用Model
 */
export class MaintUser {
  /** ID */
  id: number;

  /** アカウント名 */
  accountName: string;

  /** パスワード */
  password: string;

  /** 有効/無効 */
  enabled: boolean;

  /** 権限 */
  authorities: Authority[];

  /** 所属会社 */
  company: string;

  /** 氏名 */
  name: string;

  /** メールアドレス */
  mailAddress: string;

  /** メーカー名称. */
  makerName: string;
}
