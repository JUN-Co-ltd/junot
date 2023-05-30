import { FormArray } from '@angular/forms';

/**
 * リストユーティリティ.
 */
export class ListUtils {
  constructor(
  ) { }

  /**
   * listの長さが0または、null、またはundefinedかをチェックします。
   *
   * @param list チェックするリスト
   * @returns
   * - true : listが0またはnullまたはundefinedの場合
   */
  static isEmpty(list: any[] | FormArray): boolean {
    return list === null || list === undefined || list.length === 0;
  }

  /**
   * listの長さが0ではない、かつnullではない、かつundefinedではないかをチェックします。
   *
   * @param list チェックするリスト
   * @returns
   * - true : listが0、null、undefinedのいずれでもない場合
   */
  static isNotEmpty(list: any[] | FormArray): boolean {
    return !ListUtils.isEmpty(list);
  }
}
