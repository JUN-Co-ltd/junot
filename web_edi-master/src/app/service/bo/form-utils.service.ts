import { Injectable } from '@angular/core';
import { FormGroup, FormControl, FormArray, AbstractControl } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class FormUtilsService {

  constructor() { }

  /**
   * @param value チェックする値
   * @returns true: valueに値がない場合
   */
  isEmpty(value: any): boolean {
    return value == null || value.length === 0;
  }

  /**
   * @param value チェックする値
   * @returns true: valueに値がある場合
   */
  isNotEmpty(value: any): boolean {
    return !this.isEmpty(value);
  }

  /**
   * 項目をtouched状態にする。
   * @param abstractControl FormGroup | FormArray
   */
  markAsTouchedAllFields(abstractControl: FormGroup | FormArray): void {
    Object.keys(abstractControl.controls).forEach(field => {
      const control = abstractControl.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      } else if (control instanceof FormGroup) {
        this.markAsTouchedAllFields(control);
      } else if (control instanceof FormArray) {
        this.markAsTouchedAllFields(control);
      }
    });
  }

  /**
   * バリデーションエラーのログを出力する.
   * @param abstractControl FormGroup | FormArray
   */
  logValidationErrors(abstractControl: FormGroup | FormArray): void {
    Object.keys(abstractControl.controls).forEach(field => {
      const control = abstractControl.get(field);
      if (control instanceof FormControl) {
        this.logInvalidField(control, field);
        return;
      }
      if (control instanceof FormGroup) {
        this.logValidationErrors(control);
        return;
      }
      if (control instanceof FormArray) {
        this.logValidationErrors(control);
      }
    });
  }

  /**
   * バリデーションエラー項目のログを出力する.
   * @param control AbstractControl
   * @param field フィールド名
   */
  private logInvalidField(control: AbstractControl, field: string): void {
    if (control.invalid) {
      console.debug(
        'invalid:', field,
        'errors', control.errors,
        'value', control.value
      );
    }
  }
}
