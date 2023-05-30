import { Pipe, PipeTransform } from '@angular/core';
import { NgbDateParserFormatter, NgbDate } from '@ng-bootstrap/ng-bootstrap';

import { CalculationUtils } from '../util/calculation-utils';
import { StringUtils } from '../util/string-utils';

@Pipe({
  name: 'mdWeek'
})
export class MdWeekPipe implements PipeTransform {
  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter
  ) { }

  transform(value: Date): number {
    const dateStr = StringUtils.toStringSafe(value);
    if (StringUtils.isEmpty(dateStr)) {
      return null;
    }

    const dateSt = this.ngbDateParserFormatter.parse(dateStr);
    return CalculationUtils.calcWeek({ year: dateSt.year, month: dateSt.month, day: dateSt.day } as NgbDate);
  }
}
