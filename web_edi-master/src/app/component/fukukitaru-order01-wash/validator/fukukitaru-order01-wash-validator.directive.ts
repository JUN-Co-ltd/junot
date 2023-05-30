import { Directive } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { FormUtils } from '../../../util/form-utils';
import { OrderSkuAttentionNameValue } from 'src/app/interface/order-sku-attention-name-value';
import { OrderSkuWashAuxiliaryMaterialValue } from 'src/app/interface/order-sku-wash-auxiliary-material-value';

@Directive({
  selector: '[appFukukitaruOrder01WashValidator]'
})
export class FukukitaruOrder01WashValidatorDirective { }

/**
 * アテンションネームバリデーション.
 * リストの選択はあるが資材数量がない入力、
 * または資材数量はあるがリストの選択がない入力の場合エラー.
 */
export const attentionNameValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const attentionNameVal = control.value as OrderSkuAttentionNameValue;
  if (!FormUtils.isEmpty(attentionNameVal.materialId) && FormUtils.isEmpty(attentionNameVal.orderLot)
    || FormUtils.isEmpty(attentionNameVal.materialId) && !FormUtils.isEmpty(attentionNameVal.orderLot)) {
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

  const auxiliaryMaterialVal = control.value as OrderSkuWashAuxiliaryMaterialValue;

  let isExistOnlyIdNoData = false;   // プルダウンしか入力がないSKUが1件でも存在するか
  let isExistOnlyLotNoData = false;  // 数量しか入力がないSKUが1件でも存在するか

  // 1行ずつチェック
  if (auxiliaryMaterialVal.checked && FormUtils.isEmpty(auxiliaryMaterialVal.orderLot)) {
    // 資材コード名を指定しているが、資材数量が入力なしの場合
    isExistOnlyIdNoData = true;
  } else {
    if (auxiliaryMaterialVal.checked === false || auxiliaryMaterialVal.checked === null) {
      if (!FormUtils.isEmpty(auxiliaryMaterialVal.orderLot)) {
        // 資材コード名は指定してないが、資材数量が入力ありの場合
        isExistOnlyLotNoData = true;
      }
    }
  }
  if (isExistOnlyIdNoData || isExistOnlyLotNoData) { return { 'checkAndOrderLotExistence': true }; }

  return null;
};
