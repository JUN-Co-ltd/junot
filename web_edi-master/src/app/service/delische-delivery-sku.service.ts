import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { VDelischeDeliverySku } from '../model/v-delische-delivery-sku';
import { DelischeDeliverySkuSearchConditions } from '../model/delische-delivery-sku-search-conditions';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/delischeDeliverySkus';

/**
 * DelischeDeliverySku操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class DelischeDeliverySkuService {

  constructor(
    private junotApiService: JunotApiService
  ) { }


  /**
   * デリスケ納品SKUリスト取得処理
   * @param searchConditions デリスケ納品SKU検索条件
   * @returns デリスケ納品SKU取得結果
   */
  listDelischeDeliverySku(searchConditions: DelischeDeliverySkuSearchConditions)
    : Observable<GenericList<VDelischeDeliverySku>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }
}
