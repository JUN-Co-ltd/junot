import { TestBed } from '@angular/core/testing';
import { NumberUtilsService } from './number-utils.service';

describe('NumberUtilsService', () => {
  let numberUtilsService: NumberUtilsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NumberUtilsService]
    });

    numberUtilsService = TestBed.get(NumberUtilsService);
  });

  /**
   * toInteger
   * 数値を返す.
   */
  it('should toInteger return integer', () => {
    // 実行
    const resultOf1 = numberUtilsService.toInteger(1);
    const resultOfStr1 = numberUtilsService.toInteger('1');
    const resultOfStr0 = numberUtilsService.toInteger('0');
    const resultOfStrMinus1 = numberUtilsService.toInteger('-1');

    // 確認
    expect(resultOf1).toBe(1, '1 is not convert to 1');
    expect(resultOfStr1).toBe(1, '"1" is not convert to 1');
    expect(resultOfStr0).toBe(0, '"0" is not convert to 0');
    expect(resultOfStrMinus1).toBe(-1, '"-1" is not convert to -1');
  });

  /**
   * toInteger
   * nullを返す.
   */
  it('should toInteger return number', () => {
    // 実行
    const resultOfNull = numberUtilsService.toInteger(null);
    const resultOfUndefined = numberUtilsService.toInteger(undefined);
    const resultOfOneArray = numberUtilsService.toInteger([1]);
    const resultOfArray = numberUtilsService.toInteger([1, 2]);
    const resultOfA = numberUtilsService.toInteger('a');
    const resultOfMinus = numberUtilsService.toInteger('-');

    // 確認
    expect(resultOfNull).toBe(null, 'null is not return null');
    expect(resultOfUndefined).toBe(null, 'undefined is not return null');
    expect(resultOfOneArray).toBe(null, '[1] is not return null');
    expect(resultOfArray).toBe(null, '[1, 2] is not return null');
    expect(resultOfA).toBe(null, 'a is not return null');
    expect(resultOfMinus).toBe(null, '"-" is not return null');
  });

  /**
   * isNumber
   * trueを返す.
   */
  it('should isNumber return true', () => {
    // 実行
    const resultOf1 = numberUtilsService.isNumber(1);
    const resultOfStr1 = numberUtilsService.isNumber('1');
    const resultOf0 = numberUtilsService.isNumber(0);
    const resultOfMinus = numberUtilsService.isNumber(-1);

    // 確認
    expect(resultOf1).toBe(true, '1 is not true');
    expect(resultOfStr1).toBe(true, '"1" is not true');
    expect(resultOf0).toBe(true, '0 is not true');
    expect(resultOfMinus).toBe(true, '-1 is not true');
  });

  /**
   * isNumber
   * falseを返す.
   */
  it('should isNumber return false', () => {
    // 実行
    const resultOfNull = numberUtilsService.isNumber(null);
    const resultOfUndefined = numberUtilsService.isNumber(undefined);
    const resultOfStrA = numberUtilsService.isNumber('a');
    const resultOfMinus = numberUtilsService.isNumber('-');
    const resultOfOneList = numberUtilsService.isNumber([1]);
    const resultOfList = numberUtilsService.isNumber([1, 2]);

    // 確認
    expect(resultOfNull).toBe(false, 'null is not false');
    expect(resultOfUndefined).toBe(false, 'undefined is not false');
    expect(resultOfStrA).toBe(false, 'a is not false');
    expect(resultOfMinus).toBe(false, '- is not false');
    expect(resultOfOneList).toBe(false, '1 list is not false');
    expect(resultOfList).toBe(false, 'list is not false');
  });

  /**
   * defaultNull
   * number型を返す.
   */
  it('should defaultNull return number', () => {
    // 実行
    const resultOf1 = numberUtilsService.defaultNull(1);
    const resultOfStr1 = numberUtilsService.defaultNull('1');
    const resultOfStr0 = numberUtilsService.defaultNull('0');
    const resultOfStrMinus1 = numberUtilsService.defaultNull('-1');

    // 確認
    expect(resultOf1).toBe(1, '1 is not convert to 1');
    expect(resultOfStr1).toBe(1, '"1" is not convert to 1');
    expect(resultOfStr0).toBe(0, '"0" is not convert to 0');
    expect(resultOfStrMinus1).toBe(-1, '"-1" is not convert to -1');
  });

  /**
   * defaultNull
   * nullを返す.
   */
  it('should defaultNull return number', () => {
    // 実行
    const resultOfA = numberUtilsService.defaultNull('a');
    const resultOfMinus = numberUtilsService.defaultNull('-');
    const resultOfNull = numberUtilsService.defaultNull(null);
    const resultOfUndefined = numberUtilsService.defaultNull(undefined);
    const resultOfOneArray = numberUtilsService.defaultNull([1]);
    const resultOfArray = numberUtilsService.defaultNull([1, 2]);

    // 確認
    expect(resultOfA).toBe(null, '"a" is not return null');
    expect(resultOfMinus).toBe(null, '"a" is not return null');
    expect(resultOfNull).toBe(null, 'null is not return null');
    expect(resultOfUndefined).toBe(null, 'undefined is not return null');
    expect(resultOfOneArray).toBe(null, '[1] is not return null');
    expect(resultOfArray).toBe(null, '[1, 2] is not return null');
  });

  /**
   * defaultZero
   * number型を返す.
   */
  it('should defaultZero return number', () => {
    // 実行
    const resultOf1 = numberUtilsService.defaultZero(1);
    const resultOfStr1 = numberUtilsService.defaultZero('1');
    const resultOfStr0 = numberUtilsService.defaultZero('0');
    const resultOfStrMinus1 = numberUtilsService.defaultZero('-1');

    // 確認
    expect(resultOf1).toBe(1, '1 is not convert to 1');
    expect(resultOfStr1).toBe(1, '"1" is not convert to 1');
    expect(resultOfStr0).toBe(0, '"0" is not convert to 0');
    expect(resultOfStrMinus1).toBe(-1, '"-1" is not convert to -1');
  });

  /**
   * defaultNull
   * 0を返す.
   */
  it('should defaultNull return number', () => {
    // 実行
    const resultOfA = numberUtilsService.defaultZero('a');
    const resultOfMinus = numberUtilsService.defaultZero('-');
    const resultOfNull = numberUtilsService.defaultZero(null);
    const resultOfUndefined = numberUtilsService.defaultZero(undefined);
    const resultOfOneArray = numberUtilsService.defaultZero([1]);
    const resultOfArray = numberUtilsService.defaultZero([1, 2]);

    // 確認
    expect(resultOfA).toBe(0, '"a" is not return 0');
    expect(resultOfMinus).toBe(0, '"-" is not return 0');
    expect(resultOfNull).toBe(0, 'null is not return 0');
    expect(resultOfUndefined).toBe(0, 'undefined is not return 0');
    expect(resultOfOneArray).toBe(0, '[1] is not return 0');
    expect(resultOfArray).toBe(0, '[1, 2] is not return 0');
  });
});
