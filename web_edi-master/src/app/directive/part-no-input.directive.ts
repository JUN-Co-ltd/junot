import { OnInit, Directive, ElementRef, HostListener } from '@angular/core';
import { AbstractControl, ValidatorFn } from '@angular/forms';
import { PartNoInputPipe } from '../pipe/part-no-input.pipe';

@Directive({
  selector: '[appPartNoInput]'
})
export class PartNoInputDirective implements OnInit {

  private element: HTMLInputElement;

  constructor(
    private elementRef: ElementRef,
    private partNoInputPipe: PartNoInputPipe,
  ) {
    this.element = this.elementRef.nativeElement;
  }

  ngOnInit() {
    this.element.value = this.partNoInputPipe.transform(this.element.value);
  }

  /** focus時にparse関数の結果を返す */
  @HostListener('focus', ['$event.target.value'])
  onFocus(value) {
    this.element.value = this.partNoInputPipe.parse(value);
  }

  /** focus out時にtransform関数の結果を返す */
  @HostListener('blur', ['$event.target.value'])
  onBlur(value) {
    this.element.value = this.partNoInputPipe.transform(value);
  }
}

/**
 * アルファベット3文字-数字5桁のみ許可
 * ※「-」なしも許可
 */
export function partNoFormatValidator(): ValidatorFn {
  const partNoRe = new RegExp(/^[a-zA-Z]{3}[\-]?[0-9]{5}$/);
  return (control: AbstractControl): { [key: string]: any } | null => {
    const noMatch = !(partNoRe.test(control.value));
    return noMatch ? { 'partNoFormat': { value: control.value } } : null;
  };
}
