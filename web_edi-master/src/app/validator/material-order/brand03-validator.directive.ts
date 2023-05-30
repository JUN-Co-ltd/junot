// フクキタル アダム・エ・ロペ バリデーション
import { Directive } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { StringUtils } from '../../util/string-utils';

@Directive({
  selector: '[appBrand03Validator]'
})
export class Brand03ValidatorDirective { }

// ※※※ ブランドごとのバリデーションは適用しないためコメントアウト。必要があればコメント解除する。 ※※※

/**
 * validation
 * 資材発注洗濯ネームバリデーション
 * テープ巾に25mmを指定した場合、アテンションネームを指定するとエラー
 */
// export const whenTapeWidthSpecifyingAttentionNameValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
//   let isError = false;
//   const checkTapeWidthCode = '25mm'; // 25mm

//   if (StringUtils.isEmpty(control.get('tapeWidthName').value) || StringUtils.isEmpty(control.get('attentionSaveList').value)) {
//     return null;
//   }

//   const tapeWidthName = !StringUtils.isEmpty(control.get('tapeWidthName').value) ? control.get('tapeWidthName').value : null;
//   const attentionSaveList = control.get('attentionSaveList').value;

//   if (tapeWidthName.match(checkTapeWidthCode)) {
//     // テープ巾に25mmを指定した場合
//     attentionSaveList.forEach(val => {
//       if (val.attentionList.length > 0) {
//         // アテンションネームを指定するとエラー
//         isError = true;
//       }
//     });
//   }

//   return isError ? { 'impossibleSpecifyingAttentionName': true } : null;
// };
