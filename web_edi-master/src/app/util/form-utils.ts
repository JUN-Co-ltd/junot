import { FormGroup, FormControl, AbstractControl } from '@angular/forms';
import { NumberUtils } from './number-utils';

/**
 * フォームユーティリティ.
 */
export class FormUtils {
  constructor(
  ) { }

  /**
   * 値が空（''）または、null、またはundefinedかをチェックします。
   *
   * @param value チェックする値
   * @returns - true : valueが空またはnullまたはundefinedの場合
   */
  static isEmpty(value: any): boolean {
    return value == null || value === undefined || value.length === 0;
  }

  /**
   * 値が入力されているかをチェックします。
   *
   * @param value チェックする値
   * @returns - true : valueが空、null、undefinedのいずれでもない場合
   */
  static isNotEmpty(value: any): boolean {
    return !FormUtils.isEmpty(value);
  }

  /**
   * エラー表示の有無を返却する。
   * フォーカスアウトしたときの判定
   * @param value AbstractControl
   */
  static isErrorDisplay(value: AbstractControl): boolean {
    return value.invalid && (value.dirty || value.touched);
  }

  /**
   * エラー表示の有無を返却する。
   * フォーカスアウト または SUBMITしたときの判定
   * @param value AbstractControl
   * @param submitted SUBMITフラグ
   */
  static isErrorDisplayWithSubmit(value: AbstractControl, submitted: boolean): boolean {
    return value.invalid && (value.dirty || value.touched || submitted);
  }

  /**
   * 項目をtouched状態にし、エラーメッセージを表示させる。
   * @param formGroup FormGroup
   */
  static markAsTouchedAllFields(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(field => {
      const control = formGroup.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      } else if (control instanceof FormGroup) {
        this.markAsTouchedAllFields(control);
      }
    });
  }

  /**
   * 数値以外の場合は0を返す.
   * @param value any
   * @returns 数値以外の場合は0
   */
  static convertZeroIfNotNumber(value: any): number {
    return NumberUtils.isNumber(value) ? NumberUtils.toInteger(value) : 0;
  }
}
