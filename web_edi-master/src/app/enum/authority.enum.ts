/**
 * 権限の定義.
 */
export enum Authority {
  /** ROLE_USER : ユーザ権限(APIへのアクセス制限のために利用). */
  ROLE_USER = 'ROLE_USER',
  /** ROLE_ADMIN : 管理者権限(APIへのアクセス制限とマスタメンテナンス制限のために利用). */
  ROLE_ADMIN = 'ROLE_ADMIN',
  /** ROLE_JUN : JUN権限. */
  ROLE_JUN = 'ROLE_JUN',
  /** ROLE_QA : QA権限. */
  ROLE_QA = 'ROLE_QA',
  /** ROLE_MAKER : メーカー権限. */
  ROLE_MAKER = 'ROLE_MAKER',
  /** ROLE_DISTA : DISTA権限. */
  ROLE_DISTA = 'ROLE_DISTA',
  /** ROLE_EDI : JUNoTのEDI操作権限. */
  ROLE_EDI = 'ROLE_EDI'
}
