import { Injectable } from '@angular/core';
import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DateUtils } from '../util/date-utils';

@Injectable()
export class NgbDateFRParserFormatter extends NgbDateParserFormatter {
  /**
   * 日付をstring → JSON型に変換する
   * @param value 変換前の値
   * @returns 変換後の値
   */
  parse(value: string): NgbDateStruct {
    return DateUtils.parse(value);
  }

  /**
   * 日付をJSON → string型に変換する
   * @param date 変換前の値
   * @returns 変換後の値
   */
  format(date: NgbDateStruct): string {
    return DateUtils.format(date);
  }
}
