import { Directive } from '@angular/core';
import { ValidatorFn, FormGroup, ValidationErrors } from '@angular/forms';

import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { AllocationCode } from '../../../const/const';

import { NumberUtils } from '../../../util/number-utils';
import { ListUtils } from '../../../util/list-utils';

import { DeliveryAtAndLotError, DeliveryStoreFormValue } from '../interface/delivery-store-interface';


@Directive({
  selector: '[appDeriveryStoreValidator]'
})
export class DeriveryStoreValidatorDirective {

  constructor() { }

}

/**
 * 納期・納品数量の条件付き必須Validator
 * 全ての納期及び納品数量が未入力の場合はエラー。
 * また、
 * 納品数量に入力がある場合は対応する納期を必須とする。
 * 納期に入力がある場合は対応する納品数量を必須とする。
 */
export const deliveryAtLotValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const val = control.getRawValue();  // (課コードの為に非活性も取得する必要あり)
  const photoDeliveryAt: NgbDateStruct = val.photoDeliveryAt;   // 撮影納期
  const sewingDeliveryAt: NgbDateStruct = val.sewingDeliveryAt; // 縫検納期
  const productDeliveryAt: NgbDateStruct = val.deliveryAt;      // 製品納期
  const deliveryStores: DeliveryStoreFormValue[] = val.deliveryStores;  // 店舗リスト

  if (ListUtils.isEmpty(deliveryStores)) {
    return { 'deliveryAtLotRequired': true };
  }

  // 縫検の納品数量が入力されたか
  const isSewingDeliveryLotInputted = isInputSewing(deliveryStores);

  // 撮影の納品数量が入力されたか
  const isPhotoDeliveryLotInputted = isInputPhoto(deliveryStores);

  // 製品の納品数量が入力されたか
  const isProductDeliveryLotInputted = isInputProduct(deliveryStores);

  // 納期・納品数量が全て未入力の場合エラー
  if (isAllDeliveryAtNoInput(photoDeliveryAt, sewingDeliveryAt, productDeliveryAt)
    && isAllNoInput(isSewingDeliveryLotInputted, isPhotoDeliveryLotInputted, isProductDeliveryLotInputted)) {
    return { 'deliveryAtLotRequired': true };
  }

  const sewing = judgeDeliveryAtOrLotError(isSewingDeliveryLotInputted, sewingDeliveryAt);
  const photo = judgeDeliveryAtOrLotError(isPhotoDeliveryLotInputted, photoDeliveryAt);
  const product = judgeDeliveryAtOrLotError(isProductDeliveryLotInputted, productDeliveryAt);
  const error = {
    'photoDeliveryAtRequired': photo.deliveryAtError,
    'sewingDeliveryAtRequired': sewing.deliveryAtError,
    'deliveryAtRequired': product.deliveryAtError,
    'photoDeliveryLotRequired': photo.lotError,
    'sewingDeliveryLotRequired': sewing.lotError,
    'deliveryLotRequired': product.lotError
  };

  // エラーが1つもなければnullを返す
  return isAnyError(sewing, photo, product) ? error : null;
};

/**
 * 縫検の納品数量 > 0 か.
 * @param deliveryStores 納品得意先フォーム値
 * @returns true:縫検の納品数量 > 0
 */
const isInputSewing = (deliveryStores: DeliveryStoreFormValue[]): boolean =>
  deliveryStores.some(deliveryStore =>
    AllocationCode.SEWING === deliveryStore.divisionCode
    && deliveryStore.deliveryStoreSkus.some(sku => NumberUtils.defaultZero(sku.deliveryLot) > 0));

/**
 * 撮影の納品数量 > 0 か.
 * @param deliveryStores 納品得意先フォーム値
 * @returns true:撮影の納品数量 > 0
 */
const isInputPhoto = (deliveryStores: DeliveryStoreFormValue[]): boolean =>
  deliveryStores.some(deliveryStore =>
    AllocationCode.PHOTO === deliveryStore.divisionCode
    && deliveryStore.deliveryStoreSkus.some(sku => NumberUtils.defaultZero(sku.deliveryLot) > 0));

/**
 * 製品の納品数量 > 0か.
 * @param deliveryStores 納品得意先フォーム値
 * @returns true:製品の納品数量 > 0
 */
const isInputProduct = (deliveryStores: DeliveryStoreFormValue[]): boolean =>
  deliveryStores.some(deliveryStore =>
    AllocationCode.SEWING !== deliveryStore.divisionCode && AllocationCode.PHOTO !== deliveryStore.divisionCode
    && deliveryStore.deliveryStoreSkus.some(sku => NumberUtils.defaultZero(sku.deliveryLot) > 0));

/**
 * 納期が全て未入力か.
 * @param photoDeliveryAt 撮影納期
 * @param sewingDeliveryAt 縫検納期
 * @param productDeliveryAt 製品納期
 * @returns true:納期が全て未入力
 */
const isAllDeliveryAtNoInput = (photoDeliveryAt: NgbDateStruct, sewingDeliveryAt: NgbDateStruct, productDeliveryAt: NgbDateStruct)
  : boolean => photoDeliveryAt == null && sewingDeliveryAt == null && productDeliveryAt == null;

/**
 * 納品数量が全て未入力か.
 * @param isPhotoDeliveryLotInputted 撮影の納品数量入力フラグ
 * @param isSewingDeliveryLotInputted 縫検の納品数量入力フラグ
 * @param isProductDeliveryLotInputted 製品の納品数量入力フラグ
 * @returns true:納品数量が全て未入力
 */
const isAllNoInput = (isPhotoDeliveryLotInputted: boolean, isSewingDeliveryLotInputted: boolean, isProductDeliveryLotInputted: boolean)
  : boolean => !isPhotoDeliveryLotInputted && !isSewingDeliveryLotInputted && !isProductDeliveryLotInputted;

/**
 * 数量・納期エラーチェック.
 * 納品数量に入力がある課の納期の必須チェック.
 * 納期に入力がある課の納品数量の必須チェック.
 * @param isLotInputted 数量入力ありフラグ
 * @param deliveryAt 納期
 * @returns
 *  deliveryAtError:納期だけが未入力であればエラー
 *  lotError:納品数量だけが未入力であればエラー
 */
const judgeDeliveryAtOrLotError = (isLotInputted: boolean, deliveryAt: NgbDateStruct): DeliveryAtAndLotError => {
  return {
    deliveryAtError: isLotInputted && deliveryAt == null,
    lotError: !isLotInputted && deliveryAt != null,
  };
};

/**
 * エラーが1つでもあるか.
 * @param sewing 縫検エラー
 * @param photo 撮影エラー
 * @param product 製品エラー
 * @returns true:エラーあり
 */
const isAnyError = (sewing: DeliveryAtAndLotError, photo: DeliveryAtAndLotError, product: DeliveryAtAndLotError): boolean =>
  sewing.deliveryAtError || photo.deliveryAtError || product.deliveryAtError
  || sewing.lotError || photo.lotError || product.lotError;
