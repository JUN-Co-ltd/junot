import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { VDelischeOrder } from '../model/v-delische-order';
import { DelischeOrderSearchConditions } from '../model/delische-order-search-conditions';

import { JunotApiService } from './junot-api.service';
import { DelischeService } from './delische.service';

const BASE_URL = '/delischeOrders';

/**
 * DelischeOrder操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class DelischeOrderService {

  constructor(
    private junotApiService: JunotApiService,
    private delischeService: DelischeService
  ) { }

  /**
   * デリスケ発注情報リスト取得処理
   * @param searchConditions デリスケ発注情報検索条件
   * @returns デリスケ発注情報取得結果
   */
  listDelischeOrder(searchConditions: DelischeOrderSearchConditions): Observable<GenericList<VDelischeOrder>> {
    const formatConditions = this.delischeService.convertDelischeOrderRequestData(searchConditions);
    return this.junotApiService.listByPost(BASE_URL, formatConditions);
  }
}
