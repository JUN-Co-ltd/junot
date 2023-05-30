/**
 * 真偽型ユーティリティ.
 */
export class BooleanUtils {
  constructor(
  ) { }

  /**
   * string型のboolean値をboolean型へ変換します。
   * 'true'でも'false'でもない場合はnullを返します。
   *
   * @param str 文字列型の真偽値
   * @returns boolean型に変換した値
   */
  static parseStrToBoolean(str: string): boolean {
    switch (str) {
      case 'true':
        return true;
      case 'false':
        return false;
      default:
        return null;
    }
  }
}
