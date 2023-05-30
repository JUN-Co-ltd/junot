import { TestBed } from '@angular/core/testing';
import { ListUtilsService } from './list-utils.service';

describe('ListUtilsService', () => {
  let listUtilsService: ListUtilsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ListUtilsService]
    });

    listUtilsService = TestBed.get(ListUtilsService);
  });

  /**
   * isEmpty
   * trueを返す.
   */
  it('should isEmpty return true', () => {
    // 実行
    const resultOfEmptyList = listUtilsService.isEmpty([]);
    const resultOfNull = listUtilsService.isEmpty(null);
    const resultOfUndefined = listUtilsService.isEmpty(undefined);

    // 確認
    expect(resultOfEmptyList).toBe(true, '[] is not true');
    expect(resultOfNull).toBe(true, 'null is not true');
    expect(resultOfUndefined).toBe(true, 'undefined is not true');
  });

  /**
   * isEmpty
   * falseを返す.
   */
  it('should isEmpty return false', () => {
    // 実行
    const resultOf1List = listUtilsService.isEmpty([1]);
    const resultOfStrList = listUtilsService.isEmpty(['a', 'b']);

    // 確認
    expect(resultOf1List).toBe(false, '[1] is not false');
    expect(resultOfStrList).toBe(false, '["a", "b"] is not false');
  });

  /**
   * isNotEmpty
   * trueを返す.
   */
  it('should isNotEmpty return true', () => {
    // 実行
    const resultOf1List = listUtilsService.isNotEmpty([1]);
    const resultOfStrList = listUtilsService.isNotEmpty(['a', 'b']);

    // 確認
    expect(resultOf1List).toBe(true, '[1] is not true');
    expect(resultOfStrList).toBe(true, '["a", "b"] is not true');
  });

  /**
   * isNotEmpty
   * falseを返す.
   */
  it('should isNotEmpty return false', () => {
    // 実行
    const resultOfEmptyList = listUtilsService.isNotEmpty([]);
    const resultOfNull = listUtilsService.isNotEmpty(null);
    const resultOfUndefined = listUtilsService.isNotEmpty(undefined);

    // 確認
    expect(resultOfEmptyList).toBe(false, '[] is not false');
    expect(resultOfNull).toBe(false, 'null is not false');
    expect(resultOfUndefined).toBe(false, 'undefined is not false');
  });
});
