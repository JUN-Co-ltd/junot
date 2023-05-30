import { TestBed } from '@angular/core/testing';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DateUtilsService } from './date-utils.service';
import { NumberUtilsService } from './number-utils.service';

describe('DateUtilsService', () => {
  let dateUtilsService: DateUtilsService;
  // let numberUtilsSpy: any;

  beforeEach(() => {
    // numberUtilsSpy = jasmine.createSpyObj('NumberUtilsService', ['toInteger', 'isNumber']);

    TestBed.configureTestingModule({
      providers: [
        DateUtilsService,
        NumberUtilsService // spyにすべき？
        // { provide: NumberUtilsService, useValue: numberUtilsSpy }
      ]
    });

    dateUtilsService = TestBed.get(DateUtilsService);
  });

  /**
   * parse
   * Date型を渡し、year, month, dayが数値で返る.
   */
  it('should parse return year, month, day pass date', () => {
    // 実行
    const result = dateUtilsService.parse(new Date('2020/3/14'));

    // 確認
    expect(result.year).toBe(2020, 'year is unexpected');
    expect(result.month).toBe(3, 'month is unexpected');
    expect(result.day).toBe(14, 'day is unexpected');
  });

  /**
   * parse
   * slash区切りのstring型を渡し、year, month, dayが数値で返る.
   */
  it('should parse return year, month, day pass slash', () => {
    // 実行
    const result = dateUtilsService.parse('2020/11/04');

    // 確認
    expect(result.year).toBe(2020, 'year is unexpected');
    expect(result.month).toBe(11, 'month is unexpected');
    expect(result.day).toBe(4, 'day is unexpected');
  });

  /**
   * parse
   * hyphen区切りのstring型を渡し、year, month, dayが数値で返る.
   */
  it('should parse return year, month, day pass hyphen', () => {
    // 実行
    const result = dateUtilsService.parse('2020-3-2');

    // 確認
    expect(result.year).toBe(2020, 'year is unexpected');
    expect(result.month).toBe(3, 'month is unexpected');
    expect(result.day).toBe(2, 'day is unexpected');
  });

  /**
   * parse
   * 区切り文字なしのstring型を渡し、year, month, dayが数値で返る.
   */
  it('should parse return year, month, day pass no separator', () => {
    // 実行
    const result = dateUtilsService.parse('20200301');

    // 確認
    expect(result.year).toBe(2020, 'year is unexpected');
    expect(result.month).toBe(3, 'month is unexpected');
    expect(result.day).toBe(1, 'day is unexpected');
  });

  /**
   * parse
   * dayのみnullで返す.
   */
  it('should parse return only day is null', () => {
    // 実行
    const result = dateUtilsService.parse('2020/3/a');

    // 確認
    expect(result.year).toBe(2020, 'year is unexpected');
    expect(result.month).toBe(3, 'month is unexpected');
    expect(result.day).toBe(null, 'day is unexpected');
  });

  /**
   * parse
   * month, dayをnullで返す.
   */
  it('should parse return month & day is null', () => {
    // 実行
    const result1Slash = dateUtilsService.parse('2020/');
    const result2Slash = dateUtilsService.parse('2020//');

    // 確認
    expect(result1Slash.year).toBe(2020, 'result1Slash year is unexpected');
    expect(result1Slash.month).toBe(null, 'result1Slash month is unexpected');
    expect(result1Slash.day).toBe(null, 'result1Slash day is unexpected');
    expect(result2Slash.year).toBe(2020, 'result2Slash year is unexpected');
    expect(result2Slash.month).toBe(null, 'result2Slash month is unexpected');
    expect(result2Slash.day).toBe(null, 'result2Slash day is unexpected');
  });

  /**
   * parse
   * year, month, dayをnullで返す.
   */
  it('should parse return month & day is null', () => {
    // 実行
    const result = dateUtilsService.parse('/');

    // 確認
    expect(result.year).toBe(null, 'year is unexpected');
    expect(result.month).toBe(null, 'month is unexpected');
    expect(result.day).toBe(null, 'day is unexpected');
  });

  /**
   * parse
   * nullで返す.
   */
  it('should parse return month & day is null', () => {
    // 実行
    const result = dateUtilsService.parse(null);

    // 確認
    expect(result).toBe(null, 'result is not null');
  });

  /**
   * toString
   * yyyy/MM/dd形式の文字列で返す.
   */
  it('should toString return yyyy/MM/dd formatted string type', () => {
    // 実行
    const resultFull = dateUtilsService.toString({ year: 2021, month: 2, day: 1 } as NgbDateStruct);
    const resultNoDay = dateUtilsService.toString({ year: 2021, month: 12 } as NgbDateStruct);
    const resultNoMonth = dateUtilsService.toString({ year: 2021, day: 11 } as NgbDateStruct);
    const resultOnlyYear = dateUtilsService.toString({ year: 2021 } as NgbDateStruct);

    // 確認
    expect(resultFull).toBe('2021/02/01', 'Full string is unexpected');
    expect(resultNoDay).toBe('2021/12/', 'NoDay string is unexpected');
    expect(resultNoMonth).toBe('2021/11', 'resultNoMonth string is unexpected');
    expect(resultOnlyYear).toBe('2021/', 'resultNoMonth string is unexpected');
  });

  /**
   * toString
   * nullで返す.
   */
  it('should toString return null', () => {
    // 実行
    const result = dateUtilsService.toString(null);

    // 確認
    expect(result).toBe(null, 'result is not null');
  });

  /**
   * padding0ToYYYMM
   * 2桁0埋めで返す.
   */
  it('should padding0ToYYYMM return 2digits padding0', () => {
    // 実行
    const result1 = dateUtilsService.padding0ToYYYMM(1);
    const resultStr1 = dateUtilsService.padding0ToYYYMM('1');
    const result11 = dateUtilsService.padding0ToYYYMM(11);

    // 確認
    expect(result1).toBe('01', '1 is unexpected');
    expect(resultStr1).toBe('01', '"1" is unexpected');
    expect(result11).toBe('11', '11 is unexpected');
  });

  /**
   * onBlurDate
   * NgbDateStructで返す.
   */
  it('should onBlurDate return NgbDateStruct', () => {
    // 実行
    const resultSlash = dateUtilsService.onBlurDate('2019/1/11');
    const resultHyphen = dateUtilsService.onBlurDate('2019-1-11');
    const resultNoSeparator = dateUtilsService.onBlurDate('20190111');

    // 確認
    expect(resultSlash.year).toBe(2019, 'slash year is unexpected');
    expect(resultSlash.month).toBe(1, 'slash month is unexpected');
    expect(resultSlash.day).toBe(11, 'slash day is unexpected');
    expect(resultHyphen.year).toBe(2019, 'hyphen year is unexpected');
    expect(resultHyphen.month).toBe(1, 'hyphen month is unexpected');
    expect(resultHyphen.day).toBe(11, 'hyphen day is unexpected');
    expect(resultNoSeparator.year).toBe(2019, 'NoSeparator year is unexpected');
    expect(resultNoSeparator.month).toBe(1, 'NoSeparator month is unexpected');
    expect(resultNoSeparator.day).toBe(11, 'NoSeparator day is unexpected');
  });

  /**
   * onBlurDate
   * nullを返す.
   */
  it('should onBlurDate return null', () => {
    // 実行
    const resultNull = dateUtilsService.onBlurDate(null);
    const resultYear0 = dateUtilsService.onBlurDate('0/1/11');
    const resultYearNull = dateUtilsService.onBlurDate('/1/11');
    const resultMonthNull = dateUtilsService.onBlurDate('2019//11');
    const resultDayNull = dateUtilsService.onBlurDate('2019/1/');

    // 確認
    expect(resultNull).toBe(null, 'null is unexpected');
    expect(resultYear0).toBe(null, '"0/1/11" year is unexpected');
    expect(resultYearNull).toBe(null, '"/1/11" year is unexpected');
    expect(resultMonthNull).toBe(null, '"2019//11" year is unexpected');
    expect(resultDayNull).toBe(null, '"2019/1/" year is unexpected');
  });

  /**
   * generateCurrentDate
   * 現在日を返す.
   */
  it('should generateCurrentDate return currentDate', () => {
    // 準備
    const current = new Date();

    // 実行
    const result = dateUtilsService.generateCurrentDate();

    // 確認
    expect(result.year).toBe(current.getFullYear(), 'year is unexpected');
    expect(result.month).toBe(current.getMonth() + 1, 'month is unexpected');
    expect(result.day).toBe(current.getDate(), 'day is unexpected');
  });
});
