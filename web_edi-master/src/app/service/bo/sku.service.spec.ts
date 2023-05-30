import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { NO_01_COLORS, SKUS } from './mocks/sku.service.mock';
import { SkuService } from './sku.service';
import { ColorSize } from 'src/app/model/color-size';

describe('SkuService', () => {
  let skuService: SkuService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SkuService]
    });

    skuService = TestBed.get(SkuService);
  });

  /**
   * isMatchSkuForm
   * 2つの引数のcolorCodeとsizeがともに同一であればtrueを返す.
   */
  it('should isMatchSkuForm return true if both match', () => {
    // 準備
    const colorSize = { colorCode: '01', size: 'S' } as ColorSize;
    const form = new FormBuilder().group({
      colorCode: ['01'],
      size: ['S']
    });

    // 実行
    const result = skuService.isMatchSkuForm(colorSize, form);

    // 確認
    expect(result).toBe(true);
  });

  /**
   * isMatchSkuForm
   * 2つの引数のcolorCodeが不一致であればfalseを返す.
   */
  it('should isMatchSkuForm return false if colorCode not match', () => {
    // 準備
    const colorSize = { colorCode: '01', size: 'S' } as ColorSize;
    const form = new FormBuilder().group({
      colorCode: ['02'],
      size: ['S']
    });

    // 実行
    const result = skuService.isMatchSkuForm(colorSize, form);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * isMatchSkuForm
   * 2つの引数のsizeが不一致であればfalseを返す.
   */
  it('should isMatchSkuForm return false if size not match', () => {
    // 準備
    const colorSize = { colorCode: '01', size: 'S' } as ColorSize;
    const form = new FormBuilder().group({
      colorCode: ['01'],
      size: ['M']
    });

    // 実行
    const result = skuService.isMatchSkuForm(colorSize, form);

    // 確認
    expect(result).toBe(false);
  });
  /**
   * isMatchSkuForm
   * 2つの引数のcolorCodeとsizeが不一致であればfalseを返す.
   */
  it('should isMatchSkuForm return false if both not match', () => {
    // 準備
    const colorSize = { colorCode: '01', size: 'S' } as ColorSize;
    const form = new FormBuilder().group({
      colorCode: ['02'],
      size: ['M']
    });

    // 実行
    const result = skuService.isMatchSkuForm(colorSize, form);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * isMatchSku
   * 2つの引数のcolorCodeとsizeがともに同一であればtrueを返す.
   */
  it('should isMatchSku return true if both match', () => {
    // 準備
    const colorSize1 = { colorCode: '03', size: 'L' } as ColorSize;
    const colorSize2 = { colorCode: '03', size: 'L' } as ColorSize;

    // 実行
    const result = skuService.isMatchSku(colorSize1, colorSize2);

    // 確認
    expect(result).toBe(true);
  });

  /**
   * isMatchSku
   * 2つの引数のcolorCodeが不一致であればfalseを返す.
   */
  it('should isMatchSku return false if colorCode not match', () => {
    // 準備
    const colorSize1 = { colorCode: '02', size: 'S' } as ColorSize;
    const colorSize2 = { colorCode: '22', size: 'S' } as ColorSize;

    // 実行
    const result = skuService.isMatchSku(colorSize1, colorSize2);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * isMatchSku
   * 2つの引数のsizeが不一致であればfalseを返す.
   */
  it('should isMatchSku return false if size not match', () => {
    // 準備
    const colorSize1 = { colorCode: '01', size: 'L' } as ColorSize;
    const colorSize2 = { colorCode: '01', size: 'LL' } as ColorSize;

    // 実行
    const result = skuService.isMatchSku(colorSize1, colorSize2);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * isMatchSku
   * 2つの引数のcolorCodeとsizeが不一致であればfalseを返す.
   */
  it('should isMatchSku return false if both not match', () => {
    // 準備
    const colorSize1 = { colorCode: '03', size: 'M' } as ColorSize;
    const colorSize2 = { colorCode: '02', size: 'MM' } as ColorSize;

    // 実行
    const result = skuService.isMatchSku(colorSize1, colorSize2);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * findColorName
   * 指定したcolorCodeが存在しなければnullを返す.
   */
  it('should findColorName return null if colorCode not exists', () => {
    // 実行
    const result = skuService.findColorName(NO_01_COLORS, '01');

    // 確認
    expect(result).toBe(null);
  });

  /**
   * findColorName
   * 指定したcolorCodeに合致するカラー名を返す.
   */
  it('should findColorName return colorName', () => {
    // 実行
    const result = skuService.findColorName(NO_01_COLORS, '05');

    // 確認
    expect(result).toBe('カラー５');
  });

  /**
   * isFirstColor
   * 指定したindexのカラーがSKUリストの並び順で最初のカラーの場合、trueを返す.
   */
  it('should isFirstColor return true if color is first', () => {
    // 実行
    const result = skuService.isFirstColor(SKUS, { colorCode: '04' } as ColorSize, 5);

    // 確認
    expect(result).toBe(true);
  });

  /**
   * isFirstColor
   * 指定したindexのカラーがSKUリストの並び順で最初のカラーではない場合、falseを返す.
   */
  it('should isFirstColor return true if color is first', () => {
    // 実行
    const result = skuService.isFirstColor(SKUS, { colorCode: '04' } as ColorSize, 6);

    // 確認
    expect(result).toBe(false);
  });

  /**
   * isFirstColor
   * 指定したindexが0の場合、trueを返す.
   */
  it('should isFirstColor return true if index is 0', () => {
    // 実行
    const result = skuService.isFirstColor(SKUS, { colorCode: '03' } as ColorSize, 0);

    // 確認
    expect(result).toBe(true);
  });
});
