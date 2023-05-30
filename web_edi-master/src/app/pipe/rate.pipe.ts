import { Pipe, PipeTransform } from '@angular/core';
import { CalculationUtils } from '../util/calculation-utils';

@Pipe({
  name: 'rate'
})
export class RatePipe implements PipeTransform {

  /**
   * 率計算.
   * @param dividend 被除数
   * @param divisor 除数
   * @returns パーセント値
   */
  transform(dividend: number, divisor: number = 0): number {
    return CalculationUtils.calcRateIsNumber(dividend, divisor);
  }
}
