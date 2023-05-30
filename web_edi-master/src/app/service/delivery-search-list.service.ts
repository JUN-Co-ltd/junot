import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

import { JunotApiService } from './junot-api.service';

import { StringUtils } from '../util/string-utils';

import { GenericList } from '../model/generic-list';
import { DeliveryListSearchConditions } from '../model/search-conditions';
import { DeliverySearchResult } from '../model/delivery-search-result';

const BASE_URL = '/deliveries';

/**
 * 配分一覧に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class DeliverySearchListService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService
  ) { }


  /**
   * 配分一覧(納品依頼)検索結果取得
   * @param searchConditions 検索条件
   * @returns レスポンス
   */
  listDeliverySearchResultList(searchConditions: DeliveryListSearchConditions): Observable<GenericList<DeliverySearchResult>> {
    const formatConditions = this.convertDeliverySearchListData(searchConditions);
    const URL = `${ BASE_URL }/searchList`;
    return this.junotApiService.listByPost(URL, formatConditions);
  }

  /**
   * 配分一覧検索のデータ整形処理
   * @param searchConditions 配分一覧検索条件
   * @returns 整形後検索条件
   */
  private convertDeliverySearchListData(searchConditions: DeliveryListSearchConditions): DeliveryListSearchConditions {
    const copyItem = JSON.parse(JSON.stringify(searchConditions));
    // 品番はハイフン除去して検索
    const partNo = copyItem.partNo;
    if (StringUtils.isNotEmpty(partNo)) {
      copyItem.partNo = partNo.replace(/-/g, '');
    }

    // 納品日from
    if (copyItem.deliveryAtFrom && typeof copyItem.deliveryAtFrom !== 'string') {
      copyItem.deliveryAtFrom = this.ngbDateParserFormatter.format(copyItem.deliveryAtFrom).replace(/-/g, '/');
    }
    // 納品日to
    if (copyItem.deliveryAtTo && typeof copyItem.deliveryAtTo !== 'string') {
      copyItem.deliveryAtTo = this.ngbDateParserFormatter.format(copyItem.deliveryAtTo).replace(/-/g, '/');
    }

    return copyItem;
  }
}
