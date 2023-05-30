/**
 * オブジェクトユーティリティ.
 */
export class ObjectUtils {
  constructor(
  ) { }

  /**
   * objがnull、またはundefinedかをチェックする。
   *
   * - ObjectUtils.isNullOrUndefined(null)      = true
   * - ObjectUtils.isNullOrUndefined(undefined) = true
   * - ObjectUtils.isNullOrUndefined('')        = false
   * - ObjectUtils.isNullOrUndefined(' ')       = false
   * - ObjectUtils.isNullOrUndefined('bob')     = false
   * - ObjectUtils.isNullOrUndefined('  bob  ') = false
   *
   * @param obj チェック対象のオブジェクト
   * @returns
   * - true : objがnullまたはundefinedの場合
   */
  static isNullOrUndefined<T>(obj: T | null | undefined): boolean {
    return typeof obj === 'undefined' || obj === null;
  }

  /**
   * objがnullではない、かつundefinedではないかチェックする.
   * @param obj チェック対象のオブジェクト
   * @returns
   * - true : objがnullではない、かつundefinedではない
   */
  static isNotNullAndNotUndefined<T>(obj: T | null | undefined): boolean {
    return !ObjectUtils.isNullOrUndefined(obj);
  }

  /**
   * objがnullかをチェックする。
   *
   * - ObjectUtils.isNull(null)      = true
   * - ObjectUtils.isNull(undefined) = false
   * - ObjectUtils.isNull('')        = false
   * - ObjectUtils.isNull(' ')       = false
   * - ObjectUtils.isNull('bob')     = false
   * - ObjectUtils.isNull('  bob  ') = false
   *
   * @param obj チェック対象のオブジェクト
   * @returns
   * - true : objがnullの場合
   */
  static isNull<T>(obj: T | null | undefined): boolean {
    return obj === null;
  }

  /**
   * objがundefinedかをチェックする。
   *
   * - ObjectUtils.isUndefined(null)      = false
   * - ObjectUtils.isUndefined(undefined) = true
   * - ObjectUtils.isUndefined('')        = false
   * - ObjectUtils.isUndefined(' ')       = false
   * - ObjectUtils.isUndefined('bob')     = false
   * - ObjectUtils.isUndefined('  bob  ') = false
   *
   * @param obj チェック対象のオブジェクト
   * @returns
   * - true : objがundefinedの場合
   */
  static isUndefined<T>(obj: T | null | undefined): boolean {
    return typeof obj === 'undefined';
  }
}
