// フクキタル VIS バリデーション
import { Directive } from '@angular/core';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { Const } from '../../const/const';

@Directive({
  selector: '[appBrand02Validator]'
})
export class Brand02ValidatorDirective { }

// ※※※ ブランドごとのバリデーションは適用しないためコメントアウト。必要があればコメント解除する。 ※※※

/**
 * validation
 * 資材発注下札
 * 靴用品番(アイテムコードA)の場合、SVIS-T102,SVIS-T107を指定している場合はエラー。
 * 逆に靴用品番以外で、SVIS-T111,SVIS-T112を指定している場合はエラー。
 */
// export const shoeNoBottomBillValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
//   /** 品番 */
//   const partNo = control.get('partNo').value;
//   /** アイテムコード */
//   const itemCode = partNo.substr(2, 1);
//   /** アイテムコードA */
//   const shoesItemCode = 'A';
//   /** バリデーション時返却文字列 */
//   let error = '';
//   /** 下札類入力値 */
//   const orderSkuBottomBillList = control.get('orderSkuBottomBill').value;

//   if (itemCode === shoesItemCode) {
//     // 靴用品番(アイテムコードA)の場合、SVIS-T102,SVIS-T107を指定している場合はエラー
//     orderSkuBottomBillList
//       .filter(val1 => val1.checked)
//       .forEach(val2 => {
//         if (Const.NOT_SHOE_SEALS_VIS_ARRAY.findIndex(item => item === val2.materialCode) >= 0) {
//           error = 'shoesNoBottomBill';
//         }
//       });
//   } else {
//     // 靴用品番以外で、SVIS-T111,SVIS-T112を指定している場合はエラー
//     orderSkuBottomBillList
//       .filter(val => val.checked)
//       .forEach(val => {
//         if (Const.SHOE_SEALS_VIS_ARRAY.findIndex(item => item === val.materialCode) >= 0) {
//           error = 'otherShoesNoBottomBill';
//         }
//       });
//   }

//   switch (error) {
//     case 'shoesNoBottomBill':
//       return { 'shoesNoBottomBill': true };
//     case 'otherShoesNoBottomBill':
//       return { 'otherShoesNoBottomBill': true };
//     default:
//       return null;
//   }
// };
