import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl } from '@angular/forms';
import { ValidatorService } from '../service/bo/validator.service';

/**
 * ※このクラスを修正する場合、RequiredRelation2Directiveも
 * 同じにしてください。
 */
@Directive({
  selector: '[appRequiredRelation]',
  providers: [{ provide: NG_VALIDATORS, useExisting: RequiredRelationDirective, multi: true }]
})
export class RequiredRelationDirective implements Validator {

  // tslint:disable-next-line:no-input-rename
  @Input('appRequiredRelation')
  fields: string[] = [];

  constructor(private validatorService: ValidatorService) { }

  validate(control: AbstractControl): { [key: string]: any } | null {
    return this.validatorService.relationRequiredValidator(this.fields[0], this.fields[1])(control);
  }
}
