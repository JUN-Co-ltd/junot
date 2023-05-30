import { Authority } from 'src/app/enum/authority.enum';

/**
 * セッション情報のModel.
 * 権限も合わせて保持する。
 */
export class Session {
  /** ユーザーID. */
  userId: number;
  /** アカウント名. */
  accountName: string;
  /** 会社コード. */
  company: string;
  /** 権限. */
  authorities: Authority[];
  /** 権限タイプ：true : 社内 false : 取引先. */
  affiliation: boolean;
   /** 発注承認可能なブランドリスト */
  orderApprovalAuthorityBlands: string[];
  /** 職種 */
  occupationType: string;
}
