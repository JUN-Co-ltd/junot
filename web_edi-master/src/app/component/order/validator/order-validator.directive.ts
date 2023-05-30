import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors, AbstractControl } from '@angular/forms';

@Directive({
  selector: '[appOrderValidator]'
})
export class OrderValidatorDirective {
  /**
   * 発注数の合計が0以下の場合エラー
   * @param control
   */
  greaterThanZero(control: AbstractControl): ValidationErrors | null {
    return control.value <= 0 ? { 'greaterThanZero': true } : null;
  }
  //  PRD_0114 単価０NG対応#7820 --add JFE Start//
  equalZero(control: AbstractControl): ValidationErrors | null {
    //--単価が0またはブランクの場合はエラーメッセージ「入力してください」
    if (control.value == 0 || control.value == null || control.value == '') {
     return control.value == 0 ? { 'equalZero': true } : null;
    } else {
     //数値以外の項目が入力されていたら「半角数字で入力してください」
     let chkInt = false;
     const value = control.value;
     chkInt = !/^\d*$/.test(value);
     return chkInt == true ? { 'notInt': true } : null;

    }
  }
  //  PRD_0114 単価０NG対応#7820 --add JFE END--//
}

/**
 * 原価率範囲チェック
 * 原価率が100%を超えている場合、エラー
 * @param control
 */
export const costRateValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const rate = Number(control.get('costRate').value);

  // 原価率が100を超える場合はエラー
  return rate > 100 ? { 'costRateOverHundred': true } : null;
};

// PRD_0023 && No_65 add JFE start
/**
 * 原価合計範囲チェック
 * 原価合計が上代より以上の場合、エラー
 * @param control
 */
export const totalCostValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const otherCost: number = (Number(control.get('otherCost').value)) || 0;
  const matlCost: number = (Number(control.get('matlCost').value)) || 0;
  const processingCost: number = (Number(control.get('processingCost').value)) || 0;
  const accessoriesCost: number = (Number(control.get('accessoriesCost').value)) || 0;

  const total: number = +otherCost + +matlCost + +processingCost + +accessoriesCost;
  const retailPrice: number = (Number(control.get('retailPrice').value)) || 0;

  // 原価率が100を超える場合はエラー
  return total > retailPrice ? { 'totalCostOverRetailPrice': true } : null;
};

// PRD_0023 && No_65 add JFE end
