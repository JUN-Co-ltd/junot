import { TestBed } from '@angular/core/testing';

import {
  SKU_01_S, SKU_99_M, SKU_01_M_FORM,
  PURCAHSE_2SKUS, PURCAHSE_3SKUS,
  TWO_PURCAHSE_SKUS, THREE_PURCAHSE_SKUS,
  NOT_EXISTS_NO_INSTRUCTION_PURCHASE, EXISTS_NO_INSTRUCTION_PURCHASE,
  NOT_EXISTS_INSTRUCTION_PURCHASE, EXISTS_INSTRUCTION_PURCHASE
} from './mocks/purchase.service.mock';

import { Division } from '../../component/purchase/interface/division';

import { PurchaseService } from './purchase.service';
import { SkuService } from 'src/app/service/bo/sku.service';
import { NumberUtilsService } from 'src/app/service/bo/number-utils.service';
import { DeliverySku } from 'src/app/model/delivery-sku';
import { PurchaseTransactionData } from '../../component/purchase/interface/purchase-transaction-data';
import { DeliveryDetail } from 'src/app/model/delivery-detail';

describe('PurchaseService', () => {
  let purchaseService: PurchaseService;
  let skuServiceSpy: any;
  // let numberUtilsSpy: any;

  beforeEach(() => {
    skuServiceSpy = jasmine.createSpyObj('SkuService', ['isMatchSku', 'isMatchSkuForm']);
    // numberUtilsSpy = jasmine.createSpyObj('NumberUtilsService', ['defaultZero']);

    TestBed.configureTestingModule({
      providers: [
        PurchaseService,
        { provide: SkuService, useValue: skuServiceSpy },
        NumberUtilsService // spyにすべき？
        // { provide: NumberUtilsService, useValue: numberUtilsSpy }
      ]
    });

    purchaseService = TestBed.get(PurchaseService);
  });


  /**
   * calculateSkuTotalLot
   * purchaseに指定したSKUがない場合0を返す.
   */
  it('should calculateSkuTotalLot return 0 if targetSku not find', () => {
    // 準備
    skuServiceSpy.isMatchSku.and.returnValues(false, false);

    // 実行
    const result = purchaseService.calculateSkuTotalLot(PURCAHSE_2SKUS)(SKU_99_M);

    // 確認
    expect(result).toBe(0);
    expect(skuServiceSpy.isMatchSku).toHaveBeenCalledTimes(2);
    // expect(numberUtilsSpy.defaultZero).not.toHaveBeenCalled();
  });

  /**
   * calculateSkuTotalLot
   * purchaseから指定したSKUのpurchaseDivisionsのarrivalCount合計値を返す.
   */
  it('should calculateSkuTotalLot return totalValue', () => {
    // 準備
    // purchase.purchaseSkusの2番目がtrue
    skuServiceSpy.isMatchSku.and.returnValues(false, true);
    // arrivalCountの値
    // const vals = PURCAHSE_3SKUS.purchaseSkus.find(x => x.colorCode === SKU_01_S.colorCode && x.size === SKU_01_S.size)
    //   .purchaseDivisions.map(x => x.arrivalCount);
    // numberUtilsSpy.defaultZero.and.returnValues(...vals);

    // 実行
    const result = purchaseService.calculateSkuTotalLot(PURCAHSE_3SKUS)(SKU_01_S);

    // 確認
    expect(result).toBe(66);
    expect(skuServiceSpy.isMatchSku).toHaveBeenCalledTimes(2);
    // expect(numberUtilsSpy.defaultZero).toHaveBeenCalledTimes(vals.length);
  });

  /**
   * calculateSkuFormTotalLot
   * purchaseSkusから指定したSkuFormGroupのpurchaseDivisionsのarrivalCount合計値を返す.
   */
  it('should calculateSkuFormTotalLot return totalValue', () => {
    // 準備
    skuServiceSpy.isMatchSkuForm.and.returnValues(true);
    // arrivalCountの値
    // const vals = TWO_PURCAHSE_SKUS.find(x => x.colorCode === SKU_01_M_FORM.get('colorCode').value
    //   && x.size === SKU_01_M_FORM.get('size').value)
    //   .purchaseDivisions.map(x => x.arrivalCount);
    // numberUtilsSpy.defaultZero.and.returnValues(...vals);

    // 実行
    const result = purchaseService.calculateSkuFormTotalLot(TWO_PURCAHSE_SKUS, SKU_01_M_FORM);

    // 確認
    expect(result).toBe(23);
    expect(skuServiceSpy.isMatchSkuForm).toHaveBeenCalledTimes(1);
    // expect(numberUtilsSpy.defaultZero).toHaveBeenCalledTimes(vals.length);
  });

  /**
   * calculateDivisionTotalLot
   * purchaseから指定した納品明細の配分課のarrivalCount合計値を返す.
   */
  it('should calculateDivisionTotalLot return totalValue', () => {
    // 準備
    const divisionCode = '01';
    // arrivalCountの値
    // const vals = PURCAHSE_2SKUS.purchaseSkus.map(x => x.purchaseDivisions.find(xx => xx.divisionCode === divisionCode).arrivalCount);
    // console.log('divisionCode:' + divisionCode + ', vals:' + vals);
    // numberUtilsSpy.defaultZero.and.returnValues(...vals);

    // 実行
    const result = purchaseService.calculateDivisionTotalLot(PURCAHSE_2SKUS)(({ divisionCode }) as DeliveryDetail);

    // 確認
    expect(result).toBe(32);
    // expect(numberUtilsSpy.defaultZero).toHaveBeenCalledTimes(vals.length);
  });

  /**
   * calculateDivisionFormTotalLot
   * purchaseSkusから指定した配分課のarrivalCount合計値を返す.
   */
  it('should calculateDivisionFormTotalLot return totalValue', () => {
    // 準備
    const divisionCode = '03';
    // arrivalCountの値
    // const vals = THREE_PURCAHSE_SKUS.map(x => x.purchaseDivisions.find(xx => xx.divisionCode === divisionCode).arrivalCount);
    // console.log('divisionCode:' + divisionCode + ', vals:' + vals);
    // numberUtilsSpy.defaultZero.and.returnValues(...vals);

    // 実行
    const result = purchaseService.calculateDivisionFormTotalLot(THREE_PURCAHSE_SKUS, divisionCode);

    // 確認
    expect(result).toBe(56);
    // expect(numberUtilsSpy.defaultZero).toHaveBeenCalledTimes(vals.length);
  });

  /**
   * calculateTotalLot
   * divisionListからarrivalCount合計値を返す.
   */
  it('should calculateTotalLot return totalValue', () => {
    // 準備
    const divisionList = [
      { totalLot: 10 },
      { totalLot: 11 },
      { totalLot: 12 },
      { totalLot: 30 },
      { totalLot: 0 }
    ] as Division[];

    // 実行
    const result = purchaseService.calculateTotalLot(divisionList);

    // 確認
    expect(result).toBe(63);
  });

  /**
   * getMaxArrivalCountVaue
   * deliverySkuがnullの場合0を返す.
   */
  it('should getMaxArrivalCountVaue return 0 if deliverySku is null', () => {
    // 実行
    const result = purchaseService.getMaxArrivalCountVaue(null);

    // 確認
    expect(result).toBe(0);
  });

  /**
   * getMaxArrivalCountVaue
   * deliverySkuのdeliveryLotを返す.
   */
  it('should getMaxArrivalCountVaue return deliveryLot', () => {
    // 準備
    const deliverySku = { deliveryLot: 100 } as DeliverySku;

    // 実行
    const result = purchaseService.getMaxArrivalCountVaue(deliverySku);

    // 確認
    expect(result).toBe(100);
  });

  /**
   * existsLgNoInstruction
   * LG送信未指示が1つもない場合、falseを返す.
   */
  it('should existsLgNoInstruction return false if not exists LgNoInstruction', () => {
    // 実行
    const result = purchaseService.existsLgNoInstruction(NOT_EXISTS_NO_INSTRUCTION_PURCHASE);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * existsLgNoInstruction
   * LG送信未指示が1つでもある場合、trueを返す.
   */
  it('should existsLgNoInstruction return true if exists LgNoInstruction', () => {
    // 実行
    const result = purchaseService.existsLgNoInstruction(EXISTS_NO_INSTRUCTION_PURCHASE);

    // 確認
    expect(result).toBe(true);
  });

  /**
   * noExistsLgInstruction
   * 仕入情報がnullの場合、trueを返す.
   */
  it('should noExistsLgInstruction return false if exists LgInstruction', () => {
    // 実行
    const result = purchaseService.noExistsLgInstruction({ purchase: null } as PurchaseTransactionData);

    // 確認
    expect(result).toBe(true);
  });

  /**
   * noExistsLgInstruction
   * LG送信指示済が1つでもある場合、falseを返す.
   */
  it('should noExistsLgInstruction return false if exists LgInstruction', () => {
    // 実行
    const result = purchaseService.noExistsLgInstruction({ purchase: EXISTS_INSTRUCTION_PURCHASE } as PurchaseTransactionData);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * noExistsLgInstruction
   * LG送信指示済が1つもない場合、trueを返す.
   */
  it('should noExistsLgInstruction return true if not exists LgInstruction', () => {
    // 実行
    const result = purchaseService.noExistsLgInstruction({ purchase: NOT_EXISTS_INSTRUCTION_PURCHASE } as PurchaseTransactionData);

    // 確認
    expect(result).toBe(true);
  });
});
