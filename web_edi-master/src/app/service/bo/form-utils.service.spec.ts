import { TestBed } from '@angular/core/testing';
import { FormGroup, FormBuilder, FormArray } from '@angular/forms';
import { FormUtilsService } from './form-utils.service';

describe('FormUtilsService', () => {
  let formUtilsService: FormUtilsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FormUtilsService]
    });

    formUtilsService = TestBed.get(FormUtilsService);
  });

  /**
   * isEmpty
   * trueを返す.
   */
  it('should isEmpty return true', () => {
    // 実行
    const resultOfEmptyList = formUtilsService.isEmpty([]);
    const resultOfNull = formUtilsService.isEmpty(null);
    const resultOfUndefined = formUtilsService.isEmpty(undefined);

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
    const resultOf1List = formUtilsService.isEmpty([1]);
    const resultOfStrList = formUtilsService.isEmpty(['a', 'b']);

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
    const resultOf1List = formUtilsService.isNotEmpty([1]);
    const resultOfStrList = formUtilsService.isNotEmpty(['a', 'b']);

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
    const resultOfEmptyList = formUtilsService.isNotEmpty([]);
    const resultOfNull = formUtilsService.isNotEmpty(null);
    const resultOfUndefined = formUtilsService.isNotEmpty(undefined);

    // 確認
    expect(resultOfEmptyList).toBe(false, '[] is not false');
    expect(resultOfNull).toBe(false, 'null is not false');
    expect(resultOfUndefined).toBe(false, 'undefined is not false');
  });

  /**
   * markAsTouchedAllFields
   * touched状態にする.
   */
  it('should markAsTouchedAllFields return false', () => {
    // 準備
    const fb = new FormBuilder();
    const formGroup = fb.group({
      id: [1],
      name: ['name'],
      group: fb.group({
        childId: [1],
        childeName: ['childName'],
      }),
      array: fb.array([1, 2].map((val => fb.group({
        arrayId: [val],
        arrayName: ['name' + val]
      }))))
    });

    // 実行
    formUtilsService.markAsTouchedAllFields(formGroup);

    // 確認
    expect(formGroup.get('id').touched).toBe(true, 'id is not touched');
    expect(formGroup.get('name').touched).toBe(true, 'name is not touched');
    const group = formGroup.get('group') as FormGroup;
    expect(group.get('childId').touched).toBe(true, 'childId is not touched');
    expect(group.get('childeName').touched).toBe(true, 'childeName is not touched');
    const arrayCtrls = (formGroup.get('array') as FormArray).controls;
    expect(arrayCtrls[0].get('arrayId').touched).toBe(true, 'array[0] arrayId is not touched');
    expect(arrayCtrls[0].get('arrayName').touched).toBe(true, 'array[0] arrayName is not touched');
    expect(arrayCtrls[1].get('arrayId').touched).toBe(true, 'array[1] arrayId is not touched');
    expect(arrayCtrls[1].get('arrayName').touched).toBe(true, 'array[1] arrayName is not touched');
  });
});
