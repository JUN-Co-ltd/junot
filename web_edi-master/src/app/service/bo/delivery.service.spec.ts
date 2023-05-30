import { TestBed } from '@angular/core/testing';

import { SKUS, onlyLotDeliveryDetailMock, deliveryDetailsMock } from './mocks/delivery.service.mock';

import { DeliveryDetail } from 'src/app/model/delivery-detail';
import { DeliverySku } from 'src/app/model/delivery-sku';

import { DeliveryService } from './delivery.service';
import { SkuService } from './sku.service';
import { ColorSize } from 'src/app/model/color-size';

describe('DeliveryService', () => {
  let deliveryService: DeliveryService;
  // let skuServiceSpy: any;

  beforeEach(() => {
    // skuServiceSpy = jasmine.createSpyObj('SkuService', ['isMatchSku']);

    TestBed.configureTestingModule({
      providers: [
        DeliveryService,
        SkuService // spyにすべき？
        // {provide: SkuService, useValue: skuServiceSpy}
      ]
    });

    deliveryService = TestBed.get(DeliveryService);
  });

  /**
   * distinctColorSize
   * 重複除去したカラーサイズリストを返す.
   */
  it('should distinctColorSize return distinctColorSizeList', () => {
    // 準備
    const deliverySkus = [
      { colorCode: '01', size: 'S' },
      { colorCode: '01', size: 'M' },
      { colorCode: '03', size: 'M' },
      { colorCode: '04', size: 'LL' }
    ] as DeliverySku[];
    const deliverySkus2 = [
      { colorCode: '02', size: 'S' },
      { colorCode: '02', size: 'M' },
      { colorCode: '04', size: 'LL' }
    ] as DeliverySku[];
    const deliverySkus3 = [
      { colorCode: '01', size: 'S' },
      { colorCode: '02', size: 'M' },
      { colorCode: '04', size: 'L' },
    ] as DeliverySku[];
    const deliveryDetails = [
      { deliverySkus: deliverySkus },
      { deliverySkus: deliverySkus2 },
      { deliverySkus: deliverySkus3 }
    ] as DeliveryDetail[];

    /*
      // spyにすると期待値定義難
      skuServiceSpy.isMatchSku.and.returnValues(
      false, false, false, false,
      false, true, false, true,
      false, true, false, true,
      );
    */

    // 実行
    const result = deliveryService.distinctColorSize(deliveryDetails);

    // 確認
    expect(result.length).toBe(7, 'total length is unexpected');
    expect(result.filter(x => x.colorCode === '01' && x.size === 'S').length).toBe(1, '01S length is unexpected');
    expect(result.filter(x => x.colorCode === '01' && x.size === 'M').length).toBe(1, '01M length is unexpected');
    expect(result.filter(x => x.colorCode === '02' && x.size === 'S').length).toBe(1, '02S length is unexpected');
    expect(result.filter(x => x.colorCode === '02' && x.size === 'M').length).toBe(1, '02M length is unexpected');
    expect(result.filter(x => x.colorCode === '03' && x.size === 'M').length).toBe(1, '03M length is unexpected');
    expect(result.filter(x => x.colorCode === '04' && x.size === 'L').length).toBe(1, '04L length is unexpected');
    expect(result.filter(x => x.colorCode === '04' && x.size === 'LL').length).toBe(1, '04LL length is unexpected');
    // expect(skuServiceSpy.isMatchSku).toHaveBeenCalledTimes(15);
  });

  /**
   * sortByAsc
   * colorCode昇順のサイズリストを返す.
   */
  it('should sortByAsc return colorCode sorted list', () => {
    // 実行
    const result = deliveryService.sortByAsc(SKUS);

    // 確認
    expect(result[0].colorCode).toBe('00', 'result[0].colorCode is unexpected');
    expect(result[1].colorCode).toBe('00', 'result[1].colorCode is unexpected');
    expect(result[2].colorCode).toBe('01', 'result[2].colorCode is unexpected');
    expect(result[3].colorCode).toBe('02', 'result[3].colorCode is unexpected');
    expect(result[4].colorCode).toBe('02', 'result[4].colorCode is unexpected');
    expect(result[5].colorCode).toBe('06', 'result[5].colorCode is unexpected');
    expect(result[6].colorCode).toBe('22', 'result[6].colorCode is unexpected');
    expect(result[7].colorCode).toBe('22', 'result[7].colorCode is unexpected');
  });

  /**
   * sortByAsc
   * size昇順のサイズリストを返す.
   */
  it('should sortByAsc return size sorted list', () => {
    // 実行
    const result = deliveryService.sortByAsc(SKUS, 'size');

    // 確認
    expect(result[0].size).toBe('23.0', 'result[0].size is unexpected');
    expect(result[1].size).toBe('23.5', 'result[1].size is unexpected');
    expect(result[2].size).toBe('23.5', 'result[2].size is unexpected');
    expect(result[3].size).toBe('24', 'result[3].size is unexpected');
    expect(result[4].size).toBe('L', 'result[4].size is unexpected');
    expect(result[5].size).toBe('LL', 'result[5].size is unexpected');
    expect(result[6].size).toBe('S', 'result[6].size is unexpected');
    expect(result[7].size).toBe('S', 'result[7].size is unexpected');
  });

  /**
   * calculateDivisionTotalLot
   * 納品数量合計を返す.
   */
  it('should calculateDivisionTotalLot return total deliveryLot', () => {
    // 実行
    const result = deliveryService.calculateDivisionTotalLot(onlyLotDeliveryDetailMock);

    // 確認
    expect(result).toBe(55);
  });

  /**
   * calculateSkuTotalLot
   * 納品数量合計を返す.
   */
  it('should calculateSkuTotalLot return total deliveryLot', () => {
    // 実行
    const result1 = deliveryService.calculateSkuTotalLot(deliveryDetailsMock)({ colorCode: '01', size: 'S' } as ColorSize);
    const result2 = deliveryService.calculateSkuTotalLot(deliveryDetailsMock)({ colorCode: '02', size: 'S' } as ColorSize);
    const result3 = deliveryService.calculateSkuTotalLot(deliveryDetailsMock)({ colorCode: '02', size: '23.5' } as ColorSize);

    // 確認
    expect(result1).toBe(6, 'result1 is unexpected');
    expect(result2).toBe(6666, 'result2 is unexpected');
    expect(result3).toBe(40, 'result3 is unexpected');
  });
});
