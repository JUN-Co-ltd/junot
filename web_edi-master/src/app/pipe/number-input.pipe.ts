import { Pipe, PipeTransform } from '@angular/core';
import { DecimalPipe } from '@angular/common';

@Pipe({
  name: 'numberInput'
})
export class NumberInputPipe implements PipeTransform {

  transform(value: any, digits?: string): string {
    if (Number.isFinite(value)) {
      return new DecimalPipe('ja').transform(value, digits);
    } else {
      return value;
    }
  }

  parse(value: string): string {
    return value.replace(/,/g, '');
  }

}
