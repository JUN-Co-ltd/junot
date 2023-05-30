import { Injectable } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { NumberUtilsService } from './number-utils.service';
import * as Moment_ from 'moment';
import { CompareResult } from 'src/app/enum/compare-result.enum';

const Moment = Moment_;

@Injectable({
  providedIn: 'root'
})
export class DateUtilsService {

  constructor(
    private numberUtils: NumberUtilsService
  ) { }

  /**
   * @param value 変換する値
   * @returns NgbDateStruct型変換後の値
   */
  parse(value: string | Date): NgbDateStruct | null {
    if (value == null) {
      return null;
    }

    const dateParts = this.splitDate(value);

    return {
      year: this.numberUtils.toInteger(dateParts[0]),
      month: this.numberUtils.toInteger(dateParts[1]),
      day: this.numberUtils.toInteger(dateParts[2])
    };
  }

  /**
   * ※区切り文字なしの場合、ゼロパディングなしは未考慮
   * @param value 変換する値
   * @returns 年月日で分割した日付
   */
  private splitDate(value: string | Date): string[] {
    if (typeof value !== 'string') {
      return (Moment(value).format('YYYY/MM/DD')).trim().split('/');
    } else if (value.includes('-')) {
      return value.trim().split('-');
    } else if (value.includes('/')) {
      return value.trim().split('/');
    }

    return (Moment(value, 'YYYYMMDD').format('YYYY/MM/DD')).trim().split('/');
  }

  /**
   * @param date 変換する値
   * @returns string型に変換した値
   */
  toString(date: NgbDateStruct): string {
    if (date == null) {
      return null;
    }

    return date.year + '/'
      + (this.numberUtils.isNumber(date.month) ? this.padding0ToYYYMM(date.month) + '/' : '')
      + (this.numberUtils.isNumber(date.day) ? this.padding0ToYYYMM(date.day) : '');
  }

  /**
   * 月日の0補完処理
   * @param value
   * @returns 数値の場合 → 0補完後の値を返却、数値でない場合 → ''を返却
   */
  padding0ToYYYMM(value: number | string): string {
    if (this.numberUtils.isNumber(value)) {
      return `0${ value }`.slice(-2);
    }

    return '';
  }

  /**
   * 日付項目フォーカスアウト時の処理
   * 日付型にpatchValue可能かを判断し、可能であればJSON型の値を返し、不可であればnullを返す
   * @param maskedValue pipe変換済の値
   * @returns セットする日付
   */
  onBlurDate(maskedValue: string): NgbDateStruct {
    // 入力値が日付の場合のみセットする
    if (!Moment(maskedValue, 'YYYY/MM/DD').isValid()) {
      return null;
    }

    const ngbDate = this.parse(maskedValue);

    // 年/月/日一つでもnullの場合、処理しない( '2016/2/ ' の様な値がセットされるのを防ぐ)
    // 年度0の場合はngbDatepickerがformの中身を空にしてしまうので、年度0での入力は不可とする
    if (ngbDate == null || ngbDate.year === 0 || ngbDate.year == null || ngbDate.month == null || ngbDate.day == null) {
      return null;
    }

    return ngbDate;
  }

  /**
   * @returns NgbDateStruct型の現在日
   */
  generateCurrentDate(): NgbDateStruct {
    const current = new Date();
    const moment = Moment(current).format('YYYY/MM/DD');
    return this.parse(moment);
  }

  /**
   * @param days 追加する日数
   * @returns 現在日に引数の日数を追加した NgbDateStruct型
   */
  generateCurrentAddDayDate(days: number) {
    const current = new Date();
    const moment = Moment(current).add(days, 'day').format('YYYY/MM/DD');
    return this.parse(moment);
  }

  /**
   * @param days 減らす日数
   * @returns 現在日に引数の日数を引いた NgbDateStruct型
   */
  generateCurrentSubstractDayDate(days: number) {
    const current = new Date();
    const moment = Moment(current).subtract(days, 'day').format('YYYY/MM/DD');
    return this.parse(moment);
  }

  /**
   * @param date1 比較する日付
   * @param date2 比較する日付
   * @return Less:date1の方が古い、Over:date1の方が未来、Equal：等しい
   */
  compare(date1: NgbDateStruct, date2: NgbDateStruct): CompareResult {
    const d1 = new Date(date1.year, date1.month - 1, date1.day, 0, 0, 0);
    const d2 = new Date(date2.year, date2.month - 1, date2.day, 0, 0, 0);

    if (d1 < d2) {
      return CompareResult.Less;
    }

    if (d1 > d2) {
      return CompareResult.Over;
    }

    return CompareResult.Equal;
  }

  /**
   * ※year,month,dayに値があるかのみ判定します.
   * @param value 検査対象
   * @return true:NgbDateStruct
   */
  isNgbDateStruct(value: any): boolean {
    if (value == null
      || value.year == null
      || value.month == null
      || value.day == null) {
      return false;
    }

    return true;
  }
}
