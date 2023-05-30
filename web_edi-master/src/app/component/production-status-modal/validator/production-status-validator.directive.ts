import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors } from '@angular/forms';

@Directive({
  selector: '[appProductionStatusValidator]'
})

export class ProductionStatusValidatorDirective { }

/**
 * 生産ステータスがサンプルの入力項目が全て未入力であればエラーとする。
 */
export const sampleRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const sampleCompletionAt = control.get('sampleCompletionAt').value;
  const sampleCompletionFixAt = control.get('sampleCompletionFixAt').value;

  return !memo
    && !sampleCompletionAt && !sampleCompletionFixAt ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスが仕様確定の入力項目が全て未入力であればエラーとする。
 */
export const specificationFixRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const specificationAt = control.get('specificationAt').value;
  const specificationFixAt = control.get('specificationFixAt').value;

  return !memo
    && !specificationAt && !specificationFixAt ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスが生地入荷日の入力項目が全て未入力であればエラーとする。
 */
export const textureArrivalRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const textureArrivalAt = control.get('textureArrivalAt').value;
  const textureArrivalFixAt = control.get('textureArrivalFixAt').value;

  return !memo
    && !textureArrivalAt && !textureArrivalFixAt ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスが付属入荷日の入力項目が全て未入力であればエラーとする。
 */
export const attachmentArrivalRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const attachmentArrivalAt = control.get('attachmentArrivalAt').value;
  const attachmentArrivalFixAt = control.get('attachmentArrivalFixAt').value;

  return !memo
    && !attachmentArrivalAt && !attachmentArrivalFixAt ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスが縫製中の入力項目が全て未入力であればエラーとする。
 */
export const sewingInRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const completionAt = control.get('completionAt').value;
  const completionFixAt = control.get('completionFixAt').value;
  const completionCount = control.get('completionCount').value;

  return !memo
    && !completionAt && !completionFixAt
    && !completionCount ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスが縫製検品の入力項目が全て未入力であればエラーとする。
 */
export const sewingInspectionRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const sewInspectionAt = control.get('sewInspectionAt').value;
  const sewInspectionFixAt = control.get('sewInspectionFixAt').value;

  return !memo
    && !sewInspectionAt && !sewInspectionFixAt ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスが検品の入力項目が全て未入力であればエラーとする。
 */
export const inspectionRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const completionAt = control.get('completionAt').value;
  const completionFixAt = control.get('completionFixAt').value;
  const inspectionAt = control.get('inspectionAt').value;
  const inspectionFixAt = control.get('inspectionFixAt').value;
  const completionCount = control.get('completionCount').value;

  return !memo
    && !completionAt && !completionFixAt
    && !inspectionAt && !inspectionFixAt
    && !completionCount ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスがSHIPの入力項目が全て未入力であればエラーとする。
 */
export const shipRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const leavePortAt = control.get('leavePortAt').value;
  const leavePortFixAt = control.get('leavePortFixAt').value;
  const enterPortAt = control.get('enterPortAt').value;
  const enterPortFixAt = control.get('enterPortFixAt').value;
  const customsClearanceAt = control.get('customsClearanceAt').value;
  const customsClearanceFixAt = control.get('customsClearanceFixAt').value;

  return !memo
    && !leavePortAt && !leavePortFixAt
    && !enterPortAt && !enterPortFixAt
    && !customsClearanceAt && !customsClearanceFixAt ?
    { 'productionStatusRequired': true } : null;
};

/**
 * 生産ステータスがDISTA入荷日の入力項目が全て未入力であればエラーとする。
 */
export const distaArrivalRequiredValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const memo = control.get('memo').value;
  const distaArrivalAt = control.get('distaArrivalAt').value;
  const distaArrivalFixAt = control.get('distaArrivalFixAt').value;

  return !memo
    && !distaArrivalAt
    && !distaArrivalFixAt ?
    { 'productionStatusRequired': true } : null;
};
