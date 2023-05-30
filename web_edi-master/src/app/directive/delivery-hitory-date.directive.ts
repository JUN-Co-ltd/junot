import { AfterViewChecked, Directive, ElementRef } from '@angular/core';
import * as Moment_ from 'moment';

const Moment = Moment_;

@Directive({
  selector: '[appDeliveryHitoryDate]'
})
export class DeliveryHitoryDateDirective implements AfterViewChecked {
  private element: HTMLInputElement;

  constructor(
    private elementRef: ElementRef
  ) {
    this.element = this.elementRef.nativeElement;
  }

  /**
   * データバインド後に呼ばれる処理
   * MM/DD(aaa)に整形する
   */
  ngAfterViewChecked(): void {
    if (!this.element) { return; }

    const innerText = this.element.innerText;
    if (innerText.length === 8 && innerText.slice(-1) === ')') { return; }  // 変換済の場合は処理しない
    const deliveryDate = new Date(innerText);
    const dayOfWeek = deliveryDate.getDay();
    const dayOfWeekStr = ['日', '月', '火', '水', '木', '金', '土'][dayOfWeek];
    this.element.innerText = Moment(deliveryDate).format('MM/DD').toString() + '(' + dayOfWeekStr + ')';
  }
}
