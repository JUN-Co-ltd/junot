// フクキタル 共通 バリデーション
import { Directive } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { CompositionsCommon } from '../../const/const';
import { FukukitaruItemWashPattern } from '../../model/fukukitaru-item-wash-pattern';
import { ObjectUtils } from '../../util/object-utils';
import { StringUtils } from '../../util/string-utils';

@Directive({
  selector: '[appCommonValidator]'
})

export class CommonValidatorDirective { }

/**
 * validation
 * 商品情報画面
 * アテンションタグ付記用語にF-901およびF-902が指定されている場合は絵表示はSF-212が指定されているかを確認する
 */
export const washPatternValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  /** アテンションタグ付記用語(F-901およびF-902) */
  const checkAppendicesTerm = ['F-901', 'F-902'];
  /** 絵表示(SF-212) */
  const checkWashPattern = ['SF-212'];
  /** フクキタルのデータ */
  const fkItem = control.root.get('fkItem').value;
  /** アテンションタグ付記用語(入力値) */
  const userAppendicesTermList = fkItem.washAppendicesTermByColorList;
  /** 絵表示(入力値) */
  const userWashPatternList = fkItem.washPatterns;
  /** エラー有無フラグ */
  let isError = false;

  // アテンションタグ付記用語(入力値)をループ
  userAppendicesTermList.forEach(appendicesTermVal1 => {
    /** F-901またはF-902の場合true */
    let checkSpecified = false;

    // 1. 「洗濯ネーム付記用語」のチェック
    // カラー(00共通以外)の付記用語のリストに何も指定していない場合、裏で00共通のコードが適用されるため、00共通と同じコードでチェックする
    const targetAppendicesTerm = (appendicesTermVal1.appendicesTermList.length === 0)
      ? userAppendicesTermList.find(appendicesTermVal2 => appendicesTermVal2.colorCode === CompositionsCommon.COLOR_CODE) // 00共通
      : appendicesTermVal1; // カラー(00共通以外)

    // 付記用語のリストにF-901またはF-902が指定されているかチェック
    targetAppendicesTerm.appendicesTermList.forEach(val => {
      if (checkAppendicesTerm.includes(val.appendicesTermCode)) {
        // F-901またはF-902の指定有り
        checkSpecified = true;
      }
    });

    // 2. 「絵表示」のチェック
    // F-901またはF-902が指定されている場合、絵表示にSF-212が指定されているかチェック
    if (checkSpecified) {
      // 色ごとにチェックする
      let byColorWashPattern: FukukitaruItemWashPattern = null;
      byColorWashPattern = userWashPatternList.find(washPatternVal1 => washPatternVal1.colorCode === appendicesTermVal1.colorCode);

      if (ObjectUtils.isNotNullAndNotUndefined(byColorWashPattern)) {

        // カラー(00共通以外)の絵表示が未チェックまたは未選択(空白)の場合、裏で00共通のコードが適用されるため、00共通と同じコードでチェックする
        const targetWashPattern = (ObjectUtils.isNullOrUndefined(byColorWashPattern.washPatternId))
          ? userWashPatternList.find(washPatternVal2 => washPatternVal2.colorCode === CompositionsCommon.COLOR_CODE) // 00共通
          : byColorWashPattern; // カラー(00共通以外)

        if (StringUtils.isNotEmpty(targetWashPattern.washPatternCode)
          && !(checkWashPattern.includes(targetWashPattern.washPatternCode))) {
          // 「絵表示」が空以外 AND SF-212以外が指定されている
          isError = true;
        }
      }
    }
  });

  return isError ? { 'washPatternExistence': true } : null;
};

/**
 * validation
 * 商品情報画面
 * テープ種類にSP6600が指定されている場合、テープ巾で32mm以外が指定されているとエラー。
 */
export const tapeCodeAndTapeWidthValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  /** バリデーション有無フラグ */
  let isError = false;
  /** テープ種類(SP6600) */
  const checkTapeName = 'SP6600';
  /** テープ巾(32mm) */
  const checkTapeWidthName = '32mm';
  /** テープ種類入力値 */
  const userTapeName: string = control.root.get('fkItem').value.tapeName;
  /** テープ巾入力値 */
  const userTapeWidthName: string = control.root.get('fkItem').value.tapeWidthName;

  // テープ種類がSP6600の場合チェックする
  if (userTapeName != null && (userTapeName.indexOf(checkTapeName) !== -1)) {
    // テープ巾が32mm以外の場合、エラー
    if (userTapeWidthName == null) {
      isError = true;
    } else {
      isError = (userTapeWidthName.indexOf(checkTapeWidthName) === -1);
    }
  }
  return isError ? { 'tapeCodeExistence': true } : null;
};

/**
 * validation
 * 資材発注下札
 * 靴用品番(アイテムコードA)以外の場合、靴用シールを指定している場合はエラー。
 */
// export const shoeSealValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
//   /** 品番 */
//   const partNo = control.get('partNo').value;
//   /** アイテムコード */
//   const itemCode = partNo.substr(2, 1);
//   /** アイテムコードA */
//   const shoesItemCode = 'A';
//   /** バリデーション有無フラグ */
//   let isError = false;
//   /** 下札類入力値 */
//   const orderSkuBottomBillList = control.get('orderSkuBottomBill').value;

//   if (itemCode !== shoesItemCode) {
//     // 靴用品番(アイテムコードA)以外の場合、靴用シールを指定している場合はエラー
//     orderSkuBottomBillList
//       .filter(val1 => val1.checked)
//       .forEach(val2 => {
//         if (Const.SHOE_SEALS_ARRAY.findIndex(item => item === val2.materialCode) >= 0) {
//           isError = true;
//         }
//       });
//   }

//   return isError ? { 'specifyingShoeSeal': true } : null;
// };
