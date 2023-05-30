import { Injectable } from '@angular/core';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';

import { DelischeSearchType } from '../const/const';

import { JunotApiService } from './junot-api.service';

import { GenericList } from '../model/generic-list';
import { VDelischeDeliveryRequest } from '../model/v-delische-delivery-request';
import { DelischeDeliveryRequestSearchConditions } from '../model/delische-delivery-request-search-conditions';

const BASE_URL = '/delischeDeliveryRequests';

/**
 * DelischeDeliveryRequest操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class DelischeDeliveryRequestService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService
  ) { }

  /**
   * デリスケ納品依頼情報リスト取得処理
   * @param searchConditions デリスケ納品依頼情報検索モデル
   * @returns デリスケ納品依頼情報取得結果
   */
  listDelischeDeliveryRequest(searchConditions: DelischeDeliveryRequestSearchConditions)
    : Observable<GenericList<VDelischeDeliveryRequest>> {
    const formatConditions = this.convertRequestData(searchConditions);
    return this.junotApiService.list(BASE_URL, formatConditions);
  }

  /**
   * データ整形処理
   * @param searchConditions 検索条件
   * @returns 整形後検索条件
   */
  private convertRequestData(searchConditions: DelischeDeliveryRequestSearchConditions): string {
    const copyItem = JSON.parse(JSON.stringify(searchConditions));

    if (copyItem.searchSelect === DelischeSearchType.DERIVERY) {
      // 納品日from
      if (copyItem.deliveryAtFrom && typeof copyItem.deliveryAtFrom !== 'string') {
        copyItem.deliveryAtFrom = this.ngbDateParserFormatter.format(copyItem.deliveryAtFrom).replace(/-/g, '/');
      }
      // 納品日to
      if (copyItem.deliveryAtTo && typeof copyItem.deliveryAtTo !== 'string') {
        copyItem.deliveryAtTo = this.ngbDateParserFormatter.format(copyItem.deliveryAtTo).replace(/-/g, '/');
      }
    }

    return copyItem;
  }
}
