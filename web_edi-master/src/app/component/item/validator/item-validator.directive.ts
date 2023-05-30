import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors, AbstractControl } from '@angular/forms';

import { ViewMode } from '../../../const/const';
import { StringUtils } from '../../../util/string-utils';
import { CalculationUtils } from '../../../util/calculation-utils';
import { ObjectUtils } from 'src/app/util/object-utils';

@Directive({
  selector: '[appItemValidator]'
})
export class ItemValidatorDirective { }

/**
 * validation
 * 投入日 <= P終了日でなければエラーとする。
 */
export const deploymentWeekLessOrEqualsPendWeekValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  // control.get('formViewMode').valueとすると稀にエラーになるので存在チェックします
  const formViewModeCtrl = control.get('formViewMode');
  if (ObjectUtils.isNullOrUndefined(formViewModeCtrl)) {
    return;
  }

  const formViewMode = formViewModeCtrl.value;
  // 画面表示モードが商品登録または商品更新ではチェックしない
  if (formViewMode === ViewMode.ITEM_NEW || formViewMode === ViewMode.ITEM_EDIT) { return null; }

  const deploymentDate = control.get('deploymentDate').value;
  const pendDate = control.get('pendDate').value;
  if (!deploymentDate || !pendDate) { return null; }

  const dDate = new Date(deploymentDate.year, deploymentDate.month - 1, deploymentDate.day);
  const pDate = new Date(pendDate.year, pendDate.month - 1, pendDate.day);

  return dDate > pDate ? { 'deploymentWeekLessOrEqualsPendWeek': true } : null;
};

/**
 * validation
 * 生産メーカーコードと生産メーカー名が取得出来ているかを確認する
 * 生産メーカーコードが入力されている時に、生産メーカーが画面上セットされていない場合は、エラーとする。
 */
export const mdfMakerExistenceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const isError = control.get('orderSuppliers').value.some(value => existsCodeOnly(value.supplierCode, value.supplierName));
  return isError ? { 'makerExistence': true } : null;
};

/**
 * @param code コード
 * @param name 名称
 * @returns true:codeは存在するがnameがない
 */
const existsCodeOnly = (code: string, name: string): boolean => StringUtils.isNotEmpty(code) && StringUtils.isEmpty(name);

/**
 * validation
 * 生産工場コードと生産工場名が取得出来ているか
 * 生産工場コードが入力されている時に、生産工場メーカーが画面上セットされていない場合は、エラーとする。
 */
export const mdfMakerFactoryExistenceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const isError = control.get('orderSuppliers').value.some(value => existsCodeOnly(value.supplierFactoryCode, value.supplierFactoryName));
  return isError ? { 'mdfMakerFactoryExistence': true } : null;
};

/**
 * validation
 * 企画担当コードと企画担当名が取得出来ているか
 * 企画担当コードが入力されている時に、企画担当メーカーが画面上セットされていない場合は、エラーとする。
 */
export const plannerExistenceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const patanerCode = control.get('plannerCode').value;
  const patanerName = control.get('plannerName').value;
  return StringUtils.isNotEmpty(patanerCode) && StringUtils.isEmpty(patanerName) ? { 'plannerExistence': true } : null;
};

/**
 * validation
 * 製造担当コードと製造担当名が取得出来ているか
 * 製造担当コードが入力されている時に、製造担当名メーカーが画面上セットされていない場合は、エラーとする。
 */
export const mdfStaffExistenceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const mdfStaffCode = control.get('mdfStaffCode').value;
  const mdfStaffName = control.get('mdfStaffName').value;
  return StringUtils.isNotEmpty(mdfStaffCode) && StringUtils.isEmpty(mdfStaffName) ? { 'mdfStaffExistence': true } : null;
};

/**
 * validation
 * パターンナーコードとパターンナー名が取得出来ているか
 * パターンナーコードが入力されている時に、パターンナーメーカーが画面上セットされていない場合は、エラーとする。
 */
export const patanerExistenceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const patanerCode = control.get('patanerCode').value;
  const patanerName = control.get('patanerName').value;
  return StringUtils.isNotEmpty(patanerCode) && StringUtils.isEmpty(patanerName) ? { 'patanerExistence': true } : null;
};

/**
 * validation
 * 投入週番号の範囲が正しいか
 */
export const deploymentWeekNumberCorrectValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  // 画面表示モードが商品登録または商品更新ではチェックしない
  const formViewMode = control.get('formViewMode').value;
  if (formViewMode === ViewMode.ITEM_NEW || formViewMode.value === ViewMode.ITEM_EDIT) { return null; }

  const deploymentWeek = control.get('deploymentWeek').value;
  if (!deploymentWeek) { return null; }

  const deploymentDate = control.get('deploymentDate').value;
  const correctWeekNum = CalculationUtils.calcWeek(deploymentDate);
  return Number(deploymentWeek) !== correctWeekNum ? { 'deploymentWeekNumberCorrect': true } : null;
};

/**
 * validation
 * p終了週番号の範囲が正しいか
 */
export const pendWeekNumberCorrectValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  // 画面表示モードが商品登録または商品更新ではチェックしない
  const formViewMode = control.get('formViewMode').value;
  if (formViewMode === ViewMode.ITEM_NEW || formViewMode.value === ViewMode.ITEM_EDIT) { return null; }

  const pendWeek = control.get('pendWeek').value;
  if (!pendWeek) { return null; }

  const pendDate = control.get('pendDate').value;
  const correctWeekNum = CalculationUtils.calcWeek(pendDate);
  return Number(pendWeek) !== correctWeekNum ? { 'pendWeekNumberCorrect': true } : null;
};

/**
 * validation
 * スペース文字のみの入力になっているかどうか
 */
export const NoWhitespaceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const isOnlyWhitespace = (control.value || '').trim().length === 0;
  return isOnlyWhitespace ? { 'whitespace': true } : null;
};

/**
 * validation
 * フクキタル用 カテゴリコード必須チェック
 */
export const CategoryCodeRequiredValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const fkAvailable: boolean = control.root.get('fkAvailable') ? control.root.get('fkAvailable').value : false;

  if (!fkAvailable) {
    // フクキタル以外は、処理を終了
    return null;
  }

  const partNoKind: string = control.root.get('partNoKind') ? control.root.get('partNoKind').value : null;

  if (StringUtils.isEmpty(partNoKind)) {
    // 品種が空の場合は、処理を終了
    return null;
  }

  // ブランドコードを取得
  const categoryCode: string = control.value;

  let isEmptyCategoryCode = false;

  if (StringUtils.isEmpty(categoryCode)) {
    // カテゴリコード未入力の場合、エラー
    isEmptyCategoryCode = true;
  }

  return isEmptyCategoryCode ? { 'categoryCode': true } : null;
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
  const retailPrice = (Number(control.get('retailPrice').value)) || 0;

  // 原価合計が上代より以上の場合、エラー
  return total > retailPrice ? { 'totalCostOverRetailPrice': true } : null;
};
// PRD_0023 && No_65 add JFE end
