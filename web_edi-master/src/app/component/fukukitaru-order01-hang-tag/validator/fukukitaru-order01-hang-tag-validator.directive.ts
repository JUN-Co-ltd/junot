import { Directive } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { FormUtils } from '../../../util/form-utils';
import { OrderSkuAttentionTagValue } from 'src/app/interface/order-sku-attention-tag-value';
import { OrderSkuBottomBillAuxiliaryMaterialValue } from 'src/app/interface/order-sku-bottom-bill-auxiliary-material-value';

@Directive({
  selector: '[appFukukitaruOrder01HangTagValidator]'
})
export class FukukitaruOrder01HangTagValidatorDirective { }

/**
 * アテンションタグバリデーション.
 * リストの選択はあるが資材数量がない入力、
 * または資材数量はあるがリストの選択がない入力の場合エラー.
 */
export const attentionTagValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const attentionTagVal = control.value as OrderSkuAttentionTagValue;
  if (!FormUtils.isEmpty(attentionTagVal.materialId) && FormUtils.isEmpty(attentionTagVal.orderLot)
    || FormUtils.isEmpty(attentionTagVal.materialId) && !FormUtils.isEmpty(attentionTagVal.orderLot)) {
    return { 'materialIdAndOrderLotExistence': true };
  }
  return null;
};

/**
 * 同封副資材バリデーション.
 * チェックはあるが資材数量がない入力、
 * または資材数量はあるがチェックがない入力の場合エラー.
 */
export const auxiliaryMaterialValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const auxiliaryMaterialVal = control.value as OrderSkuBottomBillAuxiliaryMaterialValue;
  if (auxiliaryMaterialVal.checked && FormUtils.isEmpty(auxiliaryMaterialVal.orderLot)
    || !auxiliaryMaterialVal.checked && !FormUtils.isEmpty(auxiliaryMaterialVal.orderLot)) {
    return { 'checkAndOrderLotExistence': true };
  }
  return null;
};
