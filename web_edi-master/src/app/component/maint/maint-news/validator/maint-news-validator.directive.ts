import { Directive } from '@angular/core';
import { ValidatorFn, ValidationErrors, AbstractControl } from '@angular/forms';
import { DateUtils } from 'src/app/util/date-utils';

@Directive({
  selector: '[appMaintNewsValidator]'
})
export class MaintNewsValidatorDirective {
  constructor() { }
}

/**
 * validation
 * スペース文字のみの入力になっているかどうか
 */
export const NoWhitespaceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const isOnlyWhitespace = (control.value || '').trim().length === 0;
  return isOnlyWhitespace ? { 'whitespace': true } : null;
};

/**
 * validation
 * 日時が超えていないか
 */
export function DateTimeOverValidator(startDateKey: string, startTimeKey: string, endDateKey: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const f = control.parent;

    if (f === null || f === undefined) {
      return null;
    }

    // 開始日を取得
    const startDate = f.controls[startDateKey].value;
    // 開始時間を取得
    const startTime = f.controls[startTimeKey].value;
    // 終了日を取得
    const endDate = f.controls[endDateKey].value;
    // 終了時間を取得
    const endTime = control.value;

    if (startDate === null || startTime === null || endDate === null) {
      return null;
    }

    if (DateUtils.convertNgbDateStructToDate(endDate, endTime)
      < DateUtils.convertNgbDateStructToDate(startDate, startTime)) {
      return { 'dateTimeOver': true };
    }

    return null;
  };
}
