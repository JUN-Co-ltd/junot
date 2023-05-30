import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl } from '@angular/forms';
import { ValidatorService } from '../service/bo/validator.service';

/**
 * ※このクラスを修正する場合、FromToCheck2Directive、FromToCheck3Directiveも
 * 同じにしてください。
 */
@Directive({
  selector: '[appFromToCheck]',
  providers: [{ provide: NG_VALIDATORS, useExisting: FromToCheckDirective, multi: true }]
})
export class FromToCheckDirective implements Validator {
  // tslint:disable-next-line:no-input-rename
  @Input('appFromToCheck')
  fields: string[] = [];

  constructor(private validatorService: ValidatorService) { }

  validate(control: AbstractControl): { [key: string]: any } | null {
    return this.validatorService.fromOverToValidator(this.fields[0], this.fields[1])(control);
  }
}
