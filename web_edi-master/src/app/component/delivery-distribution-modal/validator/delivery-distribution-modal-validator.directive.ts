import { Directive } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { DeliveryDistributionSpecificationType } from '../../../const/const';

@Directive({
  selector: '[appDeliveryDistributionModalValidator]'
})
export class DeliveryDistributionModalValidatorDirective {}

/**
 * validation
 * 配分指定のバリデーション
 */
export const DistributionValueValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  let isError = false;

  const totalDistributionValue: number
    = control.root.get('totalDistributionValue') ? control.root.get('totalDistributionValue').value : null;
  const distributionType: number = control.root.get('distributionType') ? control.root.get('distributionType').value : null;

  if (distributionType === DeliveryDistributionSpecificationType.DISTRIBUTION_RATE) {
    if (100 < control.value) {
      // 配分率の場合、100％を超えたらエラー
      isError = true;
    }
  }

  if (distributionType === DeliveryDistributionSpecificationType.DISTRIBUTION_NUMBER) {
    if (totalDistributionValue < control.value) {
      // 配分数の場合、指定可能な配分数を超えたらエラー
      isError = true;
    }
  }

  return isError ? { 'overSpecifiable' : true } : null;
};
