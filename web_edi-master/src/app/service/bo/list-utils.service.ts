import { Injectable } from '@angular/core';
import { FormArray } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class ListUtilsService {
  constructor() { }

  /**
   * @param list チェックするリスト
   * @returns true: listがnullまたはundefined、または長さが0の場合
   */
  isEmpty(list: any[] | FormArray): boolean {
    return list == null || list.length === 0;
  }

  /**
   * @param list チェックするリスト
   * @returns true : listがnull、undefined、長さ0のいずれでもない場合
   */
  isNotEmpty(list: any[] | FormArray): boolean {
    return !this.isEmpty(list);
  }
}
