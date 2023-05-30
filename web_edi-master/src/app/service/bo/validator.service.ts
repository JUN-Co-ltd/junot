import { Injectable } from '@angular/core';
import { DateUtilsService } from './date-utils.service';
import { ValidatorFn, ValidationErrors, AbstractControl } from '@angular/forms';
import { CompareResult } from 'src/app/enum/compare-result.enum';
import { NumberUtilsService } from './number-utils.service';
import { FormUtilsService } from './form-utils.service';

@Injectable({
  providedIn: 'root'
})
export class ValidatorService {
  constructor(
    private dateUtils: DateUtilsService,
    private numberUtils: NumberUtilsService,
    private formUtils: FormUtilsService
  ) { }

  /**
   * fromがtoを超えたらエラー.
   * @param fileldNameFrom fromのフィールド名
   * @param fileldNameTo toのフィールド名
   * @param control AbstractControl
   * @returns ValidationErrors | null
   */
  fromOverToValidator = (fileldNameFrom: string, fileldNameTo: string): ValidatorFn =>
    (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      const fromVal = value[fileldNameFrom];
      const toVal = value[fileldNameTo];

      if (fromVal == null || toVal == null) {
        return null;
      }

      let result;
      if (this.dateUtils.isNgbDateStruct(fromVal)) {
        result = this.dateUtils.compare(fromVal, toVal);
      } else {
        result = this.numberUtils.compare(fromVal, toVal);
      }

      const errName = `${ fileldNameFrom }OverTo`;
      return CompareResult.Over === result ? { [errName]: true } : null;
    }

  /**
   * ベースのフィールドに入力がある場合、対象のフィールドに入力がなければエラー.
   * @param baseFileldName ベースのフィールド名
   * @param targetFileldName 対象のフィールド名
   * @param control AbstractControl
   * @returns ValidationErrors | null
   */
  relationRequiredValidator = (baseFileldName: string, targetFileldName: string): ValidatorFn =>
    (control: AbstractControl): ValidationErrors | null => {

      const value = control.value;
      const baseVal = value[baseFileldName];
      const targetVal = value[targetFileldName];

      if (this.formUtils.isEmpty(baseVal)) {
        return null;
      }

      return this.formUtils.isEmpty(targetVal) ? { [`${ targetFileldName }Empty`]: true } : null;
    }

  /**
   * ベースのフィールドの数量を超えればエラー.
   * @param baseFileldName ベースのフィールド名
   * @param targetFileldName 対象のフィールド名
   * @param control AbstractControl
   * @returns ValidationErrors | null
   */
  overBaseValidator = (baseFileldName: string, targetFileldName: string): ValidatorFn =>
    (control: AbstractControl): ValidationErrors | null => {

      const value = control.value;
      const baseVal = value[baseFileldName];
      const targetVal = value[targetFileldName];

      const result = this.numberUtils.compare(targetVal, baseVal);

      return CompareResult.Over === result ? { [`${ targetFileldName }Over`]: true } : null;
    }
}
