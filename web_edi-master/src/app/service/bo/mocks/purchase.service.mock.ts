import { FormBuilder } from '@angular/forms';

import { ColorSize } from 'src/app/model/color-size';

import { PurchaseDivision } from '../../../component/purchase/interface/purchase-division';
import { PurchaseSku } from '../../../component/purchase/interface/purchase-sku';
import { Purchase } from '../../../component/purchase/interface/purchase';
import { LgSendType } from 'src/app/const/lg-send-type';

// SKU
export const SKU_01_S = { colorCode: '01', size: 'S' } as ColorSize;
export const SKU_01_M = { colorCode: '01', size: 'M' } as ColorSize;
export const SKU_01_L = { colorCode: '01', size: 'L' } as ColorSize;
export const SKU_99_M = { colorCode: '99', size: 'M' } as ColorSize;

// SKUフォーム
export const SKU_01_M_FORM = new FormBuilder().group({
  colorCode: [SKU_01_M.colorCode],
  size: [SKU_01_M.size],
});

// purchaseDivisionリスト ※1SKU内のdivisionCodeはユニークです.
export const PURCAHSE_DIVISION_01 = [
  { divisionCode: '01', arrivalCount: 11 },
  { divisionCode: '02', arrivalCount: 12 },
  { divisionCode: '03', arrivalCount: 0 },
  { divisionCode: '04', arrivalCount: 0 }
] as PurchaseDivision[];

export const PURCAHSE_DIVISION_02 = [
  { divisionCode: '01', arrivalCount: 21 },
  { divisionCode: '02', arrivalCount: 22 },
  { divisionCode: '03', arrivalCount: 23 },
  { divisionCode: '04', arrivalCount: 0 }
] as PurchaseDivision[];

export const PURCAHSE_DIVISION_03 = [
  { divisionCode: '01', arrivalCount: 31 },
  { divisionCode: '02', arrivalCount: 32 },
  { divisionCode: '03', arrivalCount: 33 },
  { divisionCode: '04', arrivalCount: 34 },
] as PurchaseDivision[];

// PurchaseSkuリスト
export const TWO_PURCAHSE_SKUS = [{
  colorCode: SKU_01_M.colorCode,
  size: SKU_01_M.size,
  purchaseDivisions: PURCAHSE_DIVISION_01
},
{
  colorCode: SKU_01_S.colorCode,
  size: SKU_01_S.size,
  purchaseDivisions: PURCAHSE_DIVISION_02
}
] as PurchaseSku[];

export const THREE_PURCAHSE_SKUS = [{
  colorCode: SKU_01_M.colorCode,
  size: SKU_01_M.size,
  purchaseDivisions: PURCAHSE_DIVISION_01
},
{
  colorCode: SKU_01_S.colorCode,
  size: SKU_01_S.size,
  purchaseDivisions: PURCAHSE_DIVISION_02
},
{
  colorCode: SKU_01_L.colorCode,
  size: SKU_01_L.size,
  purchaseDivisions: PURCAHSE_DIVISION_03
}
] as PurchaseSku[];

// purchase
export const PURCAHSE_2SKUS = { purchaseSkus: TWO_PURCAHSE_SKUS } as Purchase;
export const PURCAHSE_3SKUS = { purchaseSkus: THREE_PURCAHSE_SKUS } as Purchase;

// purchaseのLgSendType
const ONLY_INSTRUCTION_PURCAHSE_DIVISIONS1 = [
  { lgSendType: LgSendType.INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.INSTRUCTION as LgSendType }
] as PurchaseDivision[];
const ONLY_INSTRUCTION_PURCAHSE_DIVISIONS2 = [
  { lgSendType: LgSendType.INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.INSTRUCTION as LgSendType }
] as PurchaseDivision[];
const ONLY_INSTRUCTION_PURCAHSE_DIVISIONS3 = [
  { lgSendType: LgSendType.INSTRUCTION as LgSendType }
] as PurchaseDivision[];
const EXISTS_NO_INSTRUCTION_PURCAHSE_DIVISIONS = [
  { lgSendType: LgSendType.INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.INSTRUCTION as LgSendType }
] as PurchaseDivision[];

const ONLY_INSTRUCTION_PURCAHSE_SKUS = [
  { purchaseDivisions: ONLY_INSTRUCTION_PURCAHSE_DIVISIONS1 },
  { purchaseDivisions: ONLY_INSTRUCTION_PURCAHSE_DIVISIONS2 },
  { purchaseDivisions: ONLY_INSTRUCTION_PURCAHSE_DIVISIONS3 }
] as PurchaseSku[];

const EXISTS_NO_INSTRUCTION_PURCAHSE_SKUS = [
  { purchaseDivisions: ONLY_INSTRUCTION_PURCAHSE_DIVISIONS1 },
  { purchaseDivisions: ONLY_INSTRUCTION_PURCAHSE_DIVISIONS2 },
  { purchaseDivisions: EXISTS_NO_INSTRUCTION_PURCAHSE_DIVISIONS },
  { purchaseDivisions: ONLY_INSTRUCTION_PURCAHSE_DIVISIONS3 }
] as PurchaseSku[];

/** LG送信未指示が1つもない仕入情報 */
export const NOT_EXISTS_NO_INSTRUCTION_PURCHASE = { purchaseSkus: ONLY_INSTRUCTION_PURCAHSE_SKUS } as Purchase;

/** LG送信未指示が存在する仕入情報 */
export const EXISTS_NO_INSTRUCTION_PURCHASE = { purchaseSkus: EXISTS_NO_INSTRUCTION_PURCAHSE_SKUS } as Purchase;

const ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS1 = [
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType }
] as PurchaseDivision[];
const ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS2 = [
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType }
] as PurchaseDivision[];
const ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS3 = [
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType }
] as PurchaseDivision[];
const EXISTS_INSTRUCTION_PURCAHSE_DIVISIONS = [
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType },
  { lgSendType: LgSendType.NO_INSTRUCTION as LgSendType }
] as PurchaseDivision[];

const ONLY_NO_INSTRUCTION_PURCAHSE_SKUS = [
  { purchaseDivisions: ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS1 },
  { purchaseDivisions: ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS2 },
  { purchaseDivisions: ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS3 }
] as PurchaseSku[];

const EXISTS_INSTRUCTION_PURCAHSE_SKUS = [
  { purchaseDivisions: ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS1 },
  { purchaseDivisions: ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS2 },
  { purchaseDivisions: EXISTS_INSTRUCTION_PURCAHSE_DIVISIONS },
  { purchaseDivisions: ONLY_NO_INSTRUCTION_PURCAHSE_DIVISIONS3 }
] as PurchaseSku[];

/** LG送信指示済が1つもない仕入情報 */
export const NOT_EXISTS_INSTRUCTION_PURCHASE = { purchaseSkus: ONLY_NO_INSTRUCTION_PURCAHSE_SKUS } as Purchase;

/** LG送信指示済が存在する仕入情報 */
export const EXISTS_INSTRUCTION_PURCHASE = { purchaseSkus: EXISTS_INSTRUCTION_PURCAHSE_SKUS } as Purchase;
