import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl } from '@angular/forms';
import { ValidatorService } from '../service/bo/validator.service';

@Directive({
  selector: '[appOverBaseNumber]',
  providers: [{ provide: NG_VALIDATORS, useExisting: OverBaseNumberDirective, multi: true }]
})
export class OverBaseNumberDirective implements Validator {
  // tslint:disable-next-line:no-input-rename
  @Input('appOverBaseNumber')
  fields: string[] = [];

  constructor(private validatorService: ValidatorService) { }

  validate(control: AbstractControl): { [key: string]: any } | null {
    return this.validatorService.overBaseValidator(this.fields[0], this.fields[1])(control);
  }
}
