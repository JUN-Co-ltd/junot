import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl } from '@angular/forms';
import { ValidatorService } from '../service/bo/validator.service';

/**
 * 同じDirectiveを1つの要素に複数使用できない為、特例で2を作成します
 * ※このクラスを修正する場合、FromToCheckDirective、FromToCheck3Directiveも
 * 同じにしてください。
 */
@Directive({
  selector: '[appFromToCheck2]',
  providers: [{ provide: NG_VALIDATORS, useExisting: FromToCheck2Directive, multi: true }]
})
export class FromToCheck2Directive implements Validator {
  // tslint:disable-next-line:no-input-rename
  @Input('appFromToCheck2')
  fields: string[] = [];

  constructor(private validatorService: ValidatorService) { }

  validate(control: AbstractControl): { [key: string]: any } | null {
    return this.validatorService.fromOverToValidator(this.fields[0], this.fields[1])(control);
  }
}
