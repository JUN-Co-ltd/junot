import { Session } from 'src/app/model/session';
import { Authority } from 'src/app/enum/authority.enum';

/** JUN権限 */
const junAuthorities = [Authority.ROLE_JUN];

/** メーカー権限 */
const makerAuthorities = [Authority.ROLE_MAKER];

/** マスタメンテナンス権限 */
const masterMaintenanceAuthorities = [Authority.ROLE_JUN, Authority.ROLE_ADMIN];

/** QA */
const qaAuthorities = [Authority.ROLE_JUN, Authority.ROLE_QA];

/** DISTA権限 */
const distaAuthorities = [Authority.ROLE_DISTA];

/** EDI権限 */
const ediAuthorities = [Authority.ROLE_EDI];

/** メーカー返品権限 */
const makerReturnAuthorities = [Authority.ROLE_ADMIN, Authority.ROLE_DISTA, Authority.ROLE_EDI ];

/**
 * 権限ユーティリティ.
 */
export class AuthUtils {
  constructor(
  ) { }

  /**
   * JUN社員機能の操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isJun(session: Session): boolean {
    return junAuthorities.every((value) => session.authorities.indexOf(value) > -1);
  }

  /**
   * メーカー機能の操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isMaker(session: Session): boolean {
    return makerAuthorities.every((value) => session.authorities.indexOf(value) > -1);
  }

  /**
   * マスタメンテナンスの操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isMasterMaintenance(session: Session): boolean {
    return masterMaintenanceAuthorities.every((value) => session.authorities.indexOf(value) > -1);
  }

  /**
   * QAセンターの操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isQa(session: Session): boolean {
    return qaAuthorities.every((value) => session.authorities.indexOf(value) > -1);
  }

  /**
   * 出荷機能(DISTA)の操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isDista(session: Session): boolean {
    return distaAuthorities.every((value) => session.authorities.indexOf(value) > -1);
  }

  /**
   * JUNoTのEDI操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isEdi(session: Session): boolean {
    return ediAuthorities.every((value) => session.authorities.indexOf(value) > -1);
  }

  /**
   * メーカー返品の操作権限の有無を取得する。
   * @param session セッション
   * @returns 操作権限の有無
   */
  static isMakerReturn(session: Session): boolean {
    return makerReturnAuthorities.some(value => session.authorities.indexOf(value) > -1);
  }
}
