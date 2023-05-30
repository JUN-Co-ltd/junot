import { OnInit, Directive, ElementRef, HostListener } from '@angular/core';
import { DateInputPipe } from '../pipe/date-input.pipe';

@Directive({
  selector: '[appDateInput]'
})
export class DateInputDirective implements OnInit {

  private element: HTMLInputElement;

  constructor(
    private elementRef: ElementRef,
    private dateInputPipe: DateInputPipe,
  ) {
    this.element = this.elementRef.nativeElement;
  }

  ngOnInit() {
    this.element.value = this.dateInputPipe.transform(this.element.value);
  }

  @HostListener('blur', ['$event.target.value'])
  onBlur(value) {
    this.element.value = this.dateInputPipe.transform(value);
  }
}
