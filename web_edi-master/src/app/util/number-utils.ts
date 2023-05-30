import { StringUtils } from './string-utils';

/**
 * 数値ユーティリティ.
 */
export class NumberUtils {
  constructor(
  ) { }

  /**
   * 数値かどうかの判断処理
   * @param value 判定する値
   * @returns 数値の場合 → true、数値でない場合 → false
   */
  static isNumber(value: any): boolean {
    return !isNaN(NumberUtils.toInteger(value));
  }

  /**
   * 整数への変換
   * @param value 変換する値
   * @returns 変換後の値
   */
  static toInteger(value: any): number {
    return parseInt(`${ value }`, 10);
  }

  /**
   * 整数へ変換する.
   * 数値でない場合は0を返す.
   * @param value 変換する値
   * @returns 変換後の値
   */
  static defaultZero(value: any): number {
    return this.isNumber(value) ? this.toInteger(value) : 0;
  }

  /**
   * 小数切り捨て.
   * @param value 切りてる値
   * @param digits 小数桁数
   * @returns 切りてる後の値
   */
  static floor(value: number, digits: number): number {
    if (digits == null || digits === 0) {
      return Math.floor(value);
    }

    return Math.floor(value * Math.pow(digits, 1)) / Math.pow(digits, 1);
  }

  /**
   * valueが空（''）、またはnull、またはundefinedの場合、defaultValueの値を返します。
   *
   * - NumberUtils.toNumberDefaultIfEmpty(null, 1)      = 1
   * - NumberUtils.toNumberDefaultIfEmpty('', 1)        = 1
   * - NumberUtils.toNumberDefaultIfEmpty(undefined, 1) = 1
   * - NumberUtils.toNumberDefaultIfEmpty(' ', 1)       = 1
   * - NumberUtils.toNumberDefaultIfEmpty('2', 1)       = 2
   * - NumberUtils.toNumberDefaultIfEmpty('', null)     = null
   *
   * @param value チェックする文字列
   * @param defaultValue valueが空（''）、またはnull、またはundefinedの場合の戻り値
   * @returns str または、defaultValue
   */
  static toNumberDefaultIfEmpty(value: string, defaultValue: number): number {
    if (StringUtils.isEmpty(value)) {
      return defaultValue;
    }
    return Number(value);
  }

  /**
   * 小数の加算処理.
   * @param num1 数値
   * @param num2 数値
   * @return 加算結果
   */
  static addDecimal(num1: number, num2: number): number {
    return Math.round((num1 + num2) * 10) / 10;
  }

  // PRD_0013 add SIT start
  /**
   * 小数点以下切り出し（ex:1.123→0.123）
   * @param num 小数点切り出し対象
   * @return 小数点以下
   */
  static decimalPart(any: any): number {
    const num = parseFloat(any);
    const numStr = num+'';
    const dotIdx = numStr.indexOf(".");
    const result  = "0." + (dotIdx > -1 ? numStr.substring(dotIdx + 1) : "0");
    return  parseFloat( ((num>0)?'+':'-') + result );
  }
  // PRD_0013 add SIT start
}
