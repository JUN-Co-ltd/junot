/**
 * データ型変換ユーティリティ.
 */
export class DataMoldChangeUtils {
  constructor(
  ) { }

  /**
   * DBのDate型をJavaScriptのDate型(時分秒のみ)へ変換する。
   * @param dbDate DBのDate型
   * @returns JSのDate型(時分秒のみ)
   */
  static formatDbDateToJsDate(dbDate: Date): Date {
    if (dbDate == null) { return null; }
    const monthList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
    const strDate = dbDate.toString();
    const dateParts = strDate.trim().split('/');
    const year = this.toInteger(dateParts[0]);
    const month = this.toInteger(dateParts[1]);
    const day = this.toInteger(dateParts[2]);
    return new Date(year, monthList.indexOf(month), day);
  }

  /**
   * 値を数値型へ変換する。(10進数)
   * @param value 変換対象のデータ
   * @returns 数値変換したデータ
   */
  static toInteger(value: any): number {
    return parseInt(`${ value }`, 10);
  }
}
