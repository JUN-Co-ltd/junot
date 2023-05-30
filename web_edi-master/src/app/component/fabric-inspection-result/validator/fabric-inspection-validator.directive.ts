import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors } from '@angular/forms';
import { StringUtils } from '../../../util/string-utils';

@Directive({
  selector: '[appFabricInspectionValidator]'
})
export class FabricInspectionValidatorDirective { }

/**
 * ファイルの添付が0件であればエラーとする。
 */
export const fileRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const fileFormArrayValues = control.get('fileFormArray').value as Array<any>;
  return fileFormArrayValues.length === 0 ? { 'fileRequired': true } : null;
};

/**
 * 取得した品番IDが0件であればエラーとする。
 */
export const itemRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const itemFormArrayValues = control.get('itemFormArray').value as Array<any>;
  const isExistId = itemFormArrayValues.some(itemValue => {
    return StringUtils.isNotEmpty(itemValue['id']);
  });
  return !isExistId ? { 'itemRequired': true } : null;
};

/**
 * t_itemから取得できない品番の入力が1件以上あればエラーとする。
 */
export const itemExistValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const itemFormArrayValues = control.get('itemFormArray').value as Array<any>;
  const isExistErrorPartNo = itemFormArrayValues.some(itemValue => {
    return StringUtils.isNotEmpty(itemValue['partNo'])
      && StringUtils.isEmpty(itemValue['id']);
  });
  return isExistErrorPartNo ? { 'itemExistValidator': true } : null;
};

/**
 * 取得した品番IDに重複があればエラーとする。
 */
export const itemDuplicationValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const itemFormArrayValues = control.get('itemFormArray').value as Array<any>;
  // idの重複を除去したデータを抽出(※nullは重複とみなさない)
  const uniqueList = itemFormArrayValues.filter(
    (value1, idx, array) => (value1.id == null || array.findIndex(value2 => value2.id === value1.id) === idx)
  );
  // 重複除去後、データの件数が減っていれば重複あり
  return itemFormArrayValues.length !== uniqueList.length ? { 'itemDuplicationValidator': true } : null;
};
