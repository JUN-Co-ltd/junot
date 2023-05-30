import { Injectable } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { JunotApiService } from './junot-api.service';
import { DateUtilsService } from './bo/date-utils.service';
import { DistributionShipmentSearchCondition } from '../model/distribution-shipment-search-condition';
import { DistributionShipmentConfirmList } from '../model/distribution-shipment-confirm-list';
import { DistributionShipmentSearchResult } from '../model/distribution-shipment-search-result';
import { StringUtilsService } from './bo/string-utils.service';

const BASE_URL = '/distributionShipments';

@Injectable({
  providedIn: 'root'
})
export class DistributionShipmentHttpService {

  constructor(
    private junotApiService: JunotApiService,
    private stringUtils: StringUtilsService,
    private dateUtils: DateUtilsService
  ) { }


  /**
   * 検索処理.
   * @param searchCondition 検索条件
   * @return 検索結果
   */
  search(searchCondition: DistributionShipmentSearchCondition):
      Observable<GenericList<DistributionShipmentSearchResult>> {
    const url = `${ BASE_URL }/search`;
    // デバグ用↓
    // console.log(searchCondition);
    // console.log('URL:' + url);

    return this.junotApiService.list(url, this.convertSearchParam(searchCondition));
  }

  /**
   * LG送信(確定)処理
   * @param form form入力データ
   * @returns レスポンス
   */
  confirm(form: DistributionShipmentConfirmList): Observable<DistributionShipmentConfirmList> {
    const URL = `${ BASE_URL }/confirm`;
    // デバグ用↓
    // console.log(form);
    // console.log(this.convertLgRequestData(form));
    return this.junotApiService.update(URL, this.convertLgRequestData(form));
  }

  /**
   * 検索パラメータ整形処理.
   * @param form form入力データ
   * @returns 整形後入力データ
   */
  private convertSearchParam(form: DistributionShipmentSearchCondition): DistributionShipmentSearchCondition {
    const copyItem = Object.assign({}, form); // deep copy

    copyItem.shippingAtFrom = this.dateUtils.toString(copyItem.shippingAtFrom as NgbDateStruct);
    copyItem.shippingAtTo = this.dateUtils.toString(copyItem.shippingAtTo as NgbDateStruct);
    /* PRD_0005 add SIT start */
    copyItem.arrivalAtFrom = this.dateUtils.toString(copyItem.arrivalAtFrom as NgbDateStruct);
    copyItem.arrivalAtTo = this.dateUtils.toString(copyItem.arrivalAtTo as NgbDateStruct);
    /* PRD_0005 add SIT end */
    /* PRD_0004 add SIT start */
    copyItem.deliveryRequestAtFrom = this.dateUtils.toString(copyItem.deliveryRequestAtFrom as NgbDateStruct);
    copyItem.deliveryRequestAtTo = this.dateUtils.toString(copyItem.deliveryRequestAtTo as NgbDateStruct);
    /* PRD_0004 add SIT emd */
    if (this.stringUtils.isNotEmpty(copyItem.shpcd)) {
      copyItem.shpcd = copyItem.shpcd.substring(4, 6); // 物流コードだけ送る(これだけが必要な情報のため)
    }
    return copyItem;
  }

  /**
   * @pram form form入力データ
   * @return LG送信リクエストデータ
   */
  private convertLgRequestData(form: DistributionShipmentConfirmList): DistributionShipmentConfirmList {
    // チェックがついているレコードのみ
    return { distributionShipmentConfirms: form.distributionShipmentConfirms.filter(d => d.check) };
  }
}
