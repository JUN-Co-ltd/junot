import { Input, AfterViewChecked, Directive, ElementRef } from '@angular/core';

@Directive({
  selector: '[appConstTextAtSpecificValue]'
})
export class ConstTextAtSpecificValueDirective implements AfterViewChecked {
  @Input() private specificValue: string;
  @Input() private constText: string;
  private element: HTMLInputElement;

  constructor(
    private elementRef: ElementRef
  ) {
    this.element = this.elementRef.nativeElement;
  }

  /**
   * データバインド後に呼ばれる処理
   * 特定の値の場合に指定された定数を表示する
   */
  ngAfterViewChecked(): void {
    if (!this.element) { return; }
    if (this.element.innerText === this.specificValue) {
      this.element.innerText = this.constText;
    }
  }
}
