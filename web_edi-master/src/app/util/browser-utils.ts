/**
 * ブラウザユーティリティ.
 */
export class BrowserUtils {
  constructor(
  ) { }

  /**
   * 使用しているブラウザがサポート対象か判定する。
   * サポート対象のブラウザ。
   * - Microsoft Edge
   * - Google Chrome
   *
   * @returns 判定結果
   */
  static isSupport(): boolean {
    const userAgent = window.navigator.userAgent.toLowerCase();

    if (userAgent.indexOf('msie') !== -1 ||
        userAgent.indexOf('trident') !== -1) {
      return false;
    } else if (userAgent.indexOf('opr') !== -1) {
      return false;
    } else if (userAgent.indexOf('edge') !== -1) {
      return true;
    } else if (userAgent.indexOf('chrome') !== -1) {
      return true;
    } else if (userAgent.indexOf('crios') !== -1) {
      return true;
    } else if (userAgent.indexOf('safari') !== -1) {
      return false;
    } else if (userAgent.indexOf('firefox') !== -1) {
      return false;
    }

    return false;
  }
}
