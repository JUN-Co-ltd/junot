import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl } from '@angular/forms';
import { ValidatorService } from '../service/bo/validator.service';

/**
 * 同じDirectiveを1つの要素に複数使用できない為、特例で3を作成します
 * ※このクラスを修正する場合、FromToCheckDirective、FromToCheck2Directiveも
 * 同じにしてください。
 */
@Directive({
  selector: '[appFromToCheck3]',
  providers: [{ provide: NG_VALIDATORS, useExisting: FromToCheck3Directive, multi: true }]
})
export class FromToCheck3Directive implements Validator {
  // tslint:disable-next-line:no-input-rename
  @Input('appFromToCheck3')
  fields: string[] = [];

  constructor(private validatorService: ValidatorService) { }

  validate(control: AbstractControl): { [key: string]: any } | null {
    return this.validatorService.fromOverToValidator(this.fields[0], this.fields[1])(control);
  }
}
