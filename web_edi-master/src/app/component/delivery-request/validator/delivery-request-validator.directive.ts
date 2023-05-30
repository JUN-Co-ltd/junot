import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors, AbstractControl } from '@angular/forms';

import { AllocationCode } from '../../../const/const';
import { StringUtils } from '../../../util/string-utils';

@Directive({
  selector: '[appDeliveryRequestValidator]'
})
export class DeliveryRequestValidatorDirective {

  /**
   * 納期Validator
   * 日曜が選択された場合はエラーとする。
   */
  forbiddenSundayValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) { return null; }
    const deliveryRequestAt = new Date(value.year, (value.month - 1), value.day);
    const dayOfWeek = deliveryRequestAt.getDay();
    return dayOfWeek === 0 ? { 'forbiddenSunday': { value: control.value } } : null;
  }
}

/**
 * 納期・納品数量の条件付き必須Validator
 * 全ての納期及び納品数量が未入力の場合はエラー。
 * また、
 * 納品数量に入力がある場合は対応する納期を必須とする。
 * 納期に入力がある場合は対応する納品数量を必須とする。
 */
export const deliveryAtLotValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const photoDeliveryAtValue = control.get('photoDeliveryAt').value;    // 撮影納期
  const sewingDeliveryAtValue = control.get('sewingDeliveryAt').value;  // 縫検納期
  const deliveryAtValue = control.get('deliveryAt').value;              // 製品納期

  // orderSkuFormArray取得
  const deliveryOrderSkuFormArray = control.get('deliveryOrderSkuFormArray');
  let deliveryOrderSkuFormArrayValue: any[];
  if (deliveryOrderSkuFormArray != null) {
    deliveryOrderSkuFormArrayValue = deliveryOrderSkuFormArray.value as any[];
  }
  const noDeliveryOrderSkuFormArray = control.get('noDeliveryOrderSkuFormArray');
  let noDeliveryOrderSkuFormArrayValue: any[];
  if (noDeliveryOrderSkuFormArray != null) {
    noDeliveryOrderSkuFormArrayValue = noDeliveryOrderSkuFormArray.value as any[];
  }

  let isPhotoDeliveryLotInputted = false;   // 撮影の納品数量が入力されたか
  let isSewingDeliveryLotInputted = false;  // 縫検の納品数量が入力されたか
  let isDeliveryLotInputted = false;        // 製品の納品数量が入力されたか

  // 納品場所以外の納品SKUの納品数量が入力されている場合、対応する課の納品数量入力フラグを設定する
  noDeliveryOrderSkuFormArrayValue.some(orderSku =>
    orderSku.deliverySkus.some(deliverySkus => {
      if (StringUtils.isNotEmpty(deliverySkus.deliveryLot)) {
        switch (deliverySkus.divisionCode) {
          case AllocationCode.PHOTO:
            isPhotoDeliveryLotInputted = true;
            break;
          case AllocationCode.SEWING:
            isSewingDeliveryLotInputted = true;
            break;
          default:
            break;
        }
      }
      if (isPhotoDeliveryLotInputted && isSewingDeliveryLotInputted) { return true; } // loop終了
    })
  );

  // 納品場所の納品SKUの納品数量が入力されている場合、入力されている製品の納品数量入力フラグを設定する
  isDeliveryLotInputted = deliveryOrderSkuFormArrayValue.some(orderSku =>
    orderSku.deliverySkus.some(deliverySkus => StringUtils.isNotEmpty(deliverySkus.deliveryLot)));

  // 納期・納品数量が全て未入力の場合エラー
  if (!photoDeliveryAtValue && !sewingDeliveryAtValue && !deliveryAtValue
    && !isPhotoDeliveryLotInputted && !isSewingDeliveryLotInputted && !isDeliveryLotInputted) {
    return { 'deliveryAtLotRequired': true };
  }

  // 納品数量に入力がある課の納期の必須チェックを行う。納期だけが未入力であればエラー
  const isPhotoDeliveryAtError = isPhotoDeliveryLotInputted && !photoDeliveryAtValue;
  const isSewingDeliveryAtError = isSewingDeliveryLotInputted && !sewingDeliveryAtValue;
  const isDeliveryAtError = isDeliveryLotInputted && !deliveryAtValue;

  // 納期に入力がある課の納品数量の必須チェックを行う。納品数量だけが未入力であればエラー
  const isPhotoDeliveryLotError = photoDeliveryAtValue && !isPhotoDeliveryLotInputted;
  const isSewingDeliveryLotError = sewingDeliveryAtValue && !isSewingDeliveryLotInputted;
  const isDeliveryLotError = deliveryAtValue && !isDeliveryLotInputted;

  // エラーが1つもなければnullを返す
  return (isPhotoDeliveryAtError || isSewingDeliveryAtError || isDeliveryAtError
    || isPhotoDeliveryLotError || isSewingDeliveryLotError || isDeliveryLotError) ?
    {
      'photoDeliveryAtRequired': isPhotoDeliveryAtError,
      'sewingDeliveryAtRequired': isSewingDeliveryAtError,
      'deliveryAtRequired': isDeliveryAtError,
      'photoDeliveryLotRequired': isPhotoDeliveryLotError,
      'sewingDeliveryLotRequired': isSewingDeliveryLotError,
      'deliveryLotRequired': isDeliveryLotError
    }
    : null;
};
