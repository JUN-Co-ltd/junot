import { Pipe, PipeTransform } from '@angular/core';
import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import * as Moment_ from 'moment';
import { StringUtils } from '../util/string-utils';
import { NumberUtils } from '../util/number-utils';
import { DateUtils } from '../util/date-utils';

const Moment = Moment_;

@Pipe({
  name: 'dateInput'
})
export class DateInputPipe implements PipeTransform {
  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter
  ) { }

  /**
   * 日付整形処理:
   * 数値以外の場合は、入力値のまま返す。
   * スラッシュありの場合：年、月、日 必須、ただし年は2桁でも整形する
   * スラッシュなしの場合：3、4、6、8桁のみ整形。入力時月日の前0は必須。3桁の場合のみ月の前0省略可
   * @param date 入力値
   * @returns 整形後の日付
   */
  transform(date: string): string {
    if (StringUtils.isEmpty(date)) {
      return '';
    }

    const replaceDate = date.replace(/-/g, '/');
    if (!replaceDate) {
      return date;
    }

    const year = Moment().year().toString();  // 現在年度
    let transformedDate = ''; // 変換後の日付

    if (replaceDate.match(/(\/)/)) {
      // スラッシュありの場合
      transformedDate = this.convertDateWithSlash(replaceDate);
      // 日付が適切かどうか判断する ? 適切 →　'YYYY/MM/DD'にフォーマットして返す ：不適切 → 元の入力値を返す
      return Moment(transformedDate, 'YYYY/MM/DD').isValid() ? Moment(transformedDate, 'YYYY/MM/DD', 'ja').format('YYYY/MM/DD') : date;

    } else {
      // スラッシュなしの場合：年補完またはゼロパディングして8文字にそろえる
      switch (replaceDate.length) {
        case 3: // 月日と認識（例：310）。先頭に年度と0を足す（→ 20190310）
          transformedDate = year + '0' + replaceDate;
          break;
        case 4: // 月日と認識（例：0310）。先頭に年度を足す（→ 20190310）
          transformedDate = year + replaceDate;
          break;
        case 6: // 年月日と認識（例：190310）。先頭に現在年度前2桁を足す（→ 20190310）
          transformedDate = year.slice(0, 2) + replaceDate;
          break;
        case 8: // 年月日と認識（例：20190310）。変換はしないが、変換後の日付にreplaceDateをセット（→ 20190310）
          transformedDate = replaceDate;
          break;
        default: // その他は何もしない
          break;
      }

      // 日付が適切かどうか判断する ? 適切 →　'YYYY/MM/DD'にフォーマットして返す ：不適切 → 元の入力値を返す
      return Moment(transformedDate, 'YYYYMMDD', true).isValid() ? Moment(transformedDate, 'YYYYMMDD', 'ja').format('YYYY/MM/DD') : date;
    }
  }

  /**
   * スラッシュありの場合の日付整形処理
   * string型に変換して返す
   * @param dateWithSlash スラッシュありの日付
   * @returns 整形後の日付
   */
  private convertDateWithSlash(dateWithSlash: string): string {
    // 年月日をスラッシュごとに分割
    const dateParts = dateWithSlash.trim().split('/');
    let ngbDate: NgbDateStruct = { year: null, month: null, day: null };

    if (dateParts.length === 1 && NumberUtils.isNumber(dateParts[0])) {
      // 要素数1つ、スラッシュより前に数値の入力がある（例：2019/） → 年のみと判定
      ngbDate = {
        year: NumberUtils.toInteger(DateUtils.completeYear(dateParts[0])),
        month: null,
        day: null
      };

    } else if (dateParts.length === 2 && NumberUtils.isNumber(dateParts[0]) && NumberUtils.isNumber(dateParts[1])) {
      // 要素数2つ、スラッシュの前後に数値の入力がある（例：3/01） → 月/日と判定
      ngbDate = {
        year: Moment().year(),
        month: NumberUtils.toInteger(DateUtils.padNumber(dateParts[0])),
        day: NumberUtils.toInteger(DateUtils.padNumber(dateParts[1]))
      };

    } else if (dateParts.length === 3 && NumberUtils.isNumber(dateParts[0])
      && NumberUtils.isNumber(dateParts[1]) && NumberUtils.isNumber(dateParts[2])) {
      // 要素数3つ、スラッシュの各要素が数値である（例：2019/03/1） → 年/月/日と判定
      ngbDate = {
        year: NumberUtils.toInteger(DateUtils.completeYear(dateParts[0])),
        month: NumberUtils.toInteger(DateUtils.padNumber(dateParts[1])),
        day: NumberUtils.toInteger(DateUtils.padNumber(dateParts[2]))
      };
    }

    // JSONをstring型に変換
    return this.ngbDateParserFormatter.format(ngbDate);
  }

}
