import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors } from '@angular/forms';

import { StringUtils } from '../../../util/string-utils';

@Directive({
  selector: '[appMisleadingRepresentationValidator]'
})
export class MisleadingRepresentationValidatorDirective {

  constructor() { }

}

/**
 * validation
 * 品種と連番のどちらかが未入力のとき、エラーを返す
 */
export const partNoRequiredValidator: ValidatorFn = (formGroup: FormGroup): ValidationErrors | null => {
  const partNoKind: string = formGroup.get('partNoKind') ? formGroup.get('partNoKind').value : null;
  const partNoSerialNo: string = formGroup.get('partNoSerialNo') ? formGroup.get('partNoSerialNo').value : null;

  if (StringUtils.isEmpty(partNoKind) && StringUtils.isNotEmpty(partNoSerialNo)) {
    return { 'partNoKindEmpty': true };
  } else if (StringUtils.isNotEmpty(partNoKind) && StringUtils.isEmpty(partNoSerialNo)) {
    return { 'partNoSerialNoEmpty': true };
  }

  return null;
};
