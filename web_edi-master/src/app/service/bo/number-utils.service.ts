import { Injectable } from '@angular/core';
import { CompareResult } from 'src/app/enum/compare-result.enum';

@Injectable({
  providedIn: 'root'
})
export class NumberUtilsService {

  constructor() { }

  /**
   * ※null、undefined、またはArrayの場合はnullを返します.
   * ※isNaNでtrueと判定された場合もnullを返します.
   * ※isNaNの仕様についてはドキュメントを確認してください.
   * @param value 変換する値
   * @returns 10進数の数値型へ変換した値
   */
  toInteger(value: any): number | null {
    if (value == null || Array.isArray(value)) {
      return null;
    }

    const parse = parseInt(`${ value }`, 10);

    return isNaN(parse) ? null : parse;
  }

  /**
   * 数値判断処理.
   * toInteger関数実行結果がnullの場合はfalseを返します.
   * @param value 判定する値
   * @returns true：数値
   */
  isNumber(value: any): boolean {
    return this.toInteger(value) == null ? false : true;
  }

  /**
   * 数値変換します.
   * 有限数ではない値、またはnull、undefined、またはArrayはnullで返します.
   * ※isFiniteの仕様についてはドキュメントを確認してください.
   * @param value 数値変換する値
   * @returns 数値変換した値。数値でない場合はnull
   */
  defaultNull(value: any): number | null {
    if (value == null || Array.isArray(value) || !isFinite(value)) {
      return null;
    }

    return Number(value);
  }

  /**
   * 数値変換します.
   * 有限数ではない値、またはnull、undefined、またはArrayは0にします.
   * ※isFiniteの仕様についてはドキュメントを確認してください.
   * @param value 数値変換する値
   * @returns 数値変換した値。数値でない場合は0
   */
  defaultZero(value: any): number {
    if (value == null || Array.isArray(value) || !isFinite(value)) {
      return 0;
    }

    return Number(value);
  }

  /**
   * ※数値変換できない場合はnullを返します.
   * @param val1 比較する値
   * @param val2 比較する値
   * @return Less:valの方が小さい、Over:val1の方が大きい、Equal：等しい
   */
  compare(val1: any, val2: any): CompareResult | null {
    const v1 = this.toInteger(val1);
    const v2 = this.toInteger(val2);

    if (v1 == null || v2 == null) {
      return null;
    }

    if (v1 < v2) {
      return CompareResult.Less;
    }

    if (v1 > v2) {
      return CompareResult.Over;
    }

    return CompareResult.Equal;
  }
}
