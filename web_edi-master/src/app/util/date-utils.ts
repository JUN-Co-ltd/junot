import { NgbDateStruct, NgbDate } from '@ng-bootstrap/ng-bootstrap';
import * as Moment_ from 'moment';
import { NumberUtils } from './number-utils';
import { StringUtils } from './string-utils';
import { ObjectUtils } from './object-utils';

const Moment = Moment_;

/**
 * 日付処理　ユーティリティ.
 */
export class DateUtils {
  constructor(
  ) { }

  /**
   * 日付項目フォーカスアウト時の処理
   * 日付型にpatchValue可能かを判断し、可能であればJSON型の値を返し、不可であればnullを返す
   * @param maskedValue pipe変換済の値
   * @returns ngbDate セットする日付
   */
  static onBlurDate(maskedValue: string): NgbDateStruct {
    // 入力値が日付の場合のみセットする
    if (!Moment(maskedValue, 'YYYY/MM/DD').isValid()) { return null; }

    const ngbDate = this.parse(maskedValue);
    // 年/月/日一つでもnullの場合、処理しない( '2016/2/ ' みたいなのがセットされるのを防ぐ)
    // 年度0の場合はngbDatepickerがformの中身を空にしてしまうので、年度0での入力は不可とする
    if (ngbDate == null || ngbDate.year === 0 || ngbDate.year == null || ngbDate.month == null || ngbDate.day == null) {
      return null;
    }
    return ngbDate;
  }

  /**
   * 日付をstring → JSON型に変換する
   * @param value 変換前の値
   * @returns 変換後の値
   */
  static parse(value: string): NgbDateStruct {
    if (!value) { return null; }

    const dateParts = value.trim().split('/');
    if (dateParts.length === 1 && NumberUtils.isNumber(dateParts[0])) {
      return { year: NumberUtils.toInteger(dateParts[0]), month: null, day: null };
    } else if (dateParts.length === 2 && NumberUtils.isNumber(dateParts[0]) && NumberUtils.isNumber(dateParts[1])) {
      return { year: NumberUtils.toInteger(dateParts[0]), month: NumberUtils.toInteger(dateParts[1]), day: null };
    } else if (dateParts.length === 3 && NumberUtils.isNumber(dateParts[0])
      && NumberUtils.isNumber(dateParts[1]) && NumberUtils.isNumber(dateParts[2])) {
      return {
        year: NumberUtils.toInteger(dateParts[0]),
        month: NumberUtils.toInteger(dateParts[1]),
        day: NumberUtils.toInteger(dateParts[2])
      };
    }
  }

  /**
   * 日付をJSON → string型に変換する
   * @param date 変換前の値
   * @returns 変換後の値
   */
  static format(date: NgbDateStruct): string {
    let stringDate = '';
    if (date) {
      stringDate += date.year + '/';
      stringDate += NumberUtils.isNumber(date.month) ? this.padNumber(date.month) + '/' : '';
      stringDate += NumberUtils.isNumber(date.day) ? this.padNumber(date.day) : '';
    }
    return stringDate;
  }

  /**
   * スラッシュ区切りのstring型日付をNgbDate型に変換する.
   * @param dateStr 'YYYY/MM/DD'
   * @retrun NgbDate
   */
  static convertSlashStringToNgbDate(dateStr: string): NgbDate {
    if (StringUtils.isEmpty(dateStr)) {
      return null;
    }
    const list = dateStr.split('/');
    if (list.length !== 3) {
      return null;
    }
    return {
      year: NumberUtils.toInteger(list[0]),
      month: NumberUtils.toInteger(list[1]),
      day: NumberUtils.toInteger(list[2])
    } as NgbDate;
  }

  /**
   * 年の補完処理
   * 2桁(年度の下2桁と認識)の場合のみ補完処理を行う
   * @param value 入力された年
   * @returns 2桁の場合 → 補完後の値を返却、2桁でない場合 → 入力値を返却
   */
  static completeYear(value: string): string {
    if (value.length === 2) { // 年度が2桁なら、先頭に現在年度の前2桁を足す
      return Moment().year().toString().slice(0, 2) + value;
    }
    return value;
  }

  /**
   * 月日の0補完処理
   * @param value
   * @returns 数値の場合 → 0補完後の値を返却、数値でない場合 → ''を返却
   */
  static padNumber(value: number | string): string {
    if (NumberUtils.isNumber(value)) {
      return `0${ value }`.slice(-2);
    }
    return '';
  }

  /**
   * 基準日が指定日以内かチェックする
   * @param referenceDate 基準日
   * @param apiFrom 指定日from(Api取得データ。時分秒なし想定)
   * @param apiTo 指定日to(Api取得データ。時分秒なし想定)
   * @returns 指定日以内：true、外：false
   */
  static isWithinPeriod(referenceDate: Date, apiFrom: Date, apiTo: Date): boolean {
    // APIから取得したDate型はJSのDate型に認識されない為newする
    const from = new Date(apiFrom);
    const to = new Date(apiTo);

    if (apiTo == null) {
      // toがnullであればfromのみチェック
      return from.getTime() <= referenceDate.getTime();
    }

    to.setDate(to.getDate() + 1); // referenceDateは時分秒を含み、toは時分秒を含まない為、toに+1して「＜」でチェック
    return from.getTime() <= referenceDate.getTime() && referenceDate.getTime() < to.getTime();
  }

  /**
   * 基準日が過去の日付かチェックする
   * 基準日がnullの場合はfalseを返す
   * @param referenceDate 基準日
   * @returns 過去日：true、過去日でないまたは基準日がnull：false
   */
  static isPastDate(referenceDate: Date): boolean {
    if (referenceDate === null) { return false; }
    const now = Moment().format('YYYY/MM/DD'); // 現在の日付
    const date = Moment(referenceDate, 'YYYY/MM/DD', 'ja'); // 基準日
    return date.isBefore(Moment(now, 'YYYY/MM/DD', 'ja'));
  }

  /**
   * 日時を型変換します。
   * - 'YYYY-MM-DDTHH:mm:ss.SSSZZ'形式のstring型の場合、Date型に変換
   * - Date型の場合、'YYYY-MM-DDTHH:mm:ss.SSSZZ'形式のstring型に変換
   * @param value 日時
   * @return 型変換した日時
   */
  static convertDateTime(value: string | Date): string | Date {
    if (value === null || value === undefined) {
      return null;
    }

    if (typeof value === 'string') {
      return new Date(<string> value);
    } else {
      return Moment(<Date> value).format('YYYY-MM-DDTHH:mm:ss.SSSZZ');
    }
  }

  /**
   * Date型をNgbDateStruct型に変換する
   * @param value 変換前の値
   * @returns 変換後の値
   */
  static convertDateToNgbDateStruct(value: string | Date): NgbDateStruct {
    if (value === null || value === undefined) {
      return null;
    }

    const date = <Date> value;

    return {
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate()
    } as NgbDateStruct;
  }

  /**
   * NgbDateStruct型をDate型に変換する
   * @param ngbDate 変換前の日付
   * @param time 変換前の時間（HH:mm:ss形式もしくは、HH:mm形式）（初期値 : '00:00:00'）
   * @returns 変換後の値
   */
  static convertNgbDateStructToDate(ngbDate: NgbDateStruct, time: string = '00:00:00'): Date {
    if (ngbDate === null || ngbDate === undefined) {
      return null;
    }

    let timeValue;

    if (StringUtils.isEmpty(time)) {
      timeValue = '00:00:00';
    } else if (time.length === 5) {
      timeValue = time + ':00';
    } else {
      timeValue = time;
    }

    const times = timeValue.split(':');

    return new Date(ngbDate.year, ngbDate.month - 1, ngbDate.day, <number> times[0], <number> times[1], <number> times[2]);
  }

  /**
   * Date型をHH:mm形式の文字列に変換する
   * @param value 変換前の値
   * @returns 変換後の値
   */
  static convertDateToTime(value: string | Date): String {
    if (value === null || value === undefined) {
      return null;
    }

    const date = <Date> value;

    return Moment(date).format('HH:mm');
  }

  /**
   * 日付を型変換します。
   * - 'YYYY-MM-DDTHH:mm:ss.SSZZ'形式のstring型の場合、Date型に変換
   * - Date型の場合、'YYYY/MM/DD'形式のstring型に変換
   * @param value 日時
   * @return 型変換した日付
   */
  static convertDate(value: string | Date): string | Date {
    if (ObjectUtils.isNullOrUndefined(value)) { return null; }

    if (typeof value === 'string') {
      return new Date(<string> value);
    } else {
      return Moment(<Date> value).format('YYYY/MM/DD');
    }
  }
}
