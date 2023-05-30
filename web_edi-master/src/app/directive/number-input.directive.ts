import { OnInit, Directive, ElementRef, HostListener } from '@angular/core';
import { NumberInputPipe } from '../pipe/number-input.pipe';

@Directive({
  selector: '[appNumberInput]'
})
export class NumberInputDirective implements OnInit {

  private element: HTMLInputElement;
  private digits = '1.0-0';

  constructor(
    private elementRef: ElementRef,
    private numberInputPipe: NumberInputPipe,
  ) {
    this.element = this.elementRef.nativeElement;
  }

  ngOnInit() {
    this.element.value = this.numberInputPipe.transform(this.element.value, this.digits);
  }

  @HostListener('focus', ['$event.target.value'])
  onFocus(value) {
    this.element.value = this.numberInputPipe.parse(value);
  }

  @HostListener('blur', ['$event.target.value'])
  onBlur(value) {
    this.element.value = this.numberInputPipe.transform(value, this.digits);
  }
}
