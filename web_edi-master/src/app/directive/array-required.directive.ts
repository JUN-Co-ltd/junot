import { Directive, Input } from '@angular/core';
import { FormArray, NG_VALIDATORS, Validator, AbstractControl, ValidationErrors } from '@angular/forms';
import { FormUtilsService } from 'src/app/service/bo/form-utils.service';

@Directive({
  selector: '[appArrayRequired]',
  providers: [{ provide: NG_VALIDATORS, useExisting: ArrayRequiredDirective, multi: true }]
})
export class ArrayRequiredDirective implements Validator {
  // tslint:disable-next-line:no-input-rename
  @Input('appArrayRequired')
  fields: string[] = [];

  constructor(private formUtils: FormUtilsService) { }

  /**
   * FormArrayの入力がない場合エラー.
   */
  validate(control: AbstractControl): ValidationErrors | null {
    // control.get(this.fields[0]).valueとすると稀にエラーになるので存在チェックします
    const array = control.get(this.fields[0]) as FormArray;
    if (array == null) { return; }
    return array.value.every(v => this.formUtils.isEmpty(v[this.fields[1]])) ?
      { [`${ this.fields[0] }Required`]: true } : null;
  }
}
