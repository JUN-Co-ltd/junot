import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'monthly'
})
export class MonthlyPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    const date = new Date(value);
    let monthly = date.getMonth() + 1; // getMonth()の返り値は0～11
    if (date.getDate() > 20) { // 21日以上は翌月
      monthly++;
      if (monthly > 12) {
        monthly = 1;
      }
    }
    return monthly;
  }
}
