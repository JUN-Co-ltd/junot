import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors } from '@angular/forms';
import { StringUtils } from '../../../util/string-utils';

@Directive({
  selector: '[appDeliveryPlanValidator]'
})
export class DeliveryPlanValidatorDirective { }

/**
 * 生産率閾値チェック
 * @param control
 */
export const deliveryPlanCutRateValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const rate = Number(control.get('deliveryPlanCutRate').value);
  const thresholdRate = Number(control.get('threshold').value);
  // PRD_0191 add JFE start
  const expenseItem = control.get('expenseItem').value;
  // PRD_0191 add JFE end

  // 生産率が閾値の範囲外の時はエラー
  // PRD_0191 mod JFE start
  // 費目04の場合、下限値はチェックしない
  //return Math.abs(rate) > thresholdRate ? { 'deliveryPlanCutRateThresholdOver': true } : null;
  if (expenseItem === '04') {
    return rate > thresholdRate ? { 'deliveryPlanCutRateThresholdOver': true } : null;
  } else {
  return Math.abs(rate) > thresholdRate ? { 'deliveryPlanCutRateThresholdOver': true } : null;
  }
  // PRD_0191 mod JFE end
};

/**
 * 増減産率閾値チェック
 * @param control
 */
export const increaseOrDecreaseLotRateValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const rate = Number(control.get('increaseOrDecreaseLotRate').value);
  const thresholdRate = Number(control.get('threshold').value);

  // 増減産率が上限値を超えている時はエラー。下限値はチェックしない。
  return rate > thresholdRate ? { 'increaseOrDecreaseLotRateThresholdOver': true } : null;
};

/**
 * 納品予定数の入力が0件であればエラーとする。
 * @param control
 */
export const allDeliveryPlanLotRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const deliveryPlanDetailValues = control.getRawValue().deliveryPlanDetails; // disable項目の値も取得する
  if (deliveryPlanDetailValues == null) { return null; }
  const isIputExists = deliveryPlanDetailValues.some(deliveryPlanDetailValue => {
    return deliveryPlanDetailValue.deliveryPlanSkus.some(deliveryPlanSku => {
      return StringUtils.isNotEmpty(deliveryPlanSku.deliveryPlanLot);
    });
  });
  return !isIputExists ? { 'allDeliveryPlanLotRequired': true } : null;
};

/**
 * 納品予定日の入力がある納品予定明細の納品予定数の入力が0件であればエラーとする。
 * @param control
 */
export const deliveryPlanLotRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const deliveryPlanDetailValues = control.get('deliveryPlanDetails').value;
  if (deliveryPlanDetailValues == null) { return null; }
  const isErrorExists = deliveryPlanDetailValues.some(deliveryPlanDetailValue => {
    return StringUtils.isNotEmpty(deliveryPlanDetailValue.deliveryPlanAt)
      && !deliveryPlanDetailValue.deliveryPlanSkus.some(deliveryPlanSku => {
        return StringUtils.isNotEmpty(deliveryPlanSku.deliveryPlanLot);
      });
  });
  return isErrorExists ? { 'deliveryPlanLotRequired': true } : null;
};
