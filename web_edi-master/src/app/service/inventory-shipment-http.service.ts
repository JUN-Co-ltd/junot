import { Injectable } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { GenericList } from '../model/generic-list';
import { JunotApiService } from './junot-api.service';
import { DateUtilsService } from './bo/date-utils.service';
import { InventoryShipmentSearchCondition } from '../model/inventory-shipment-search-condition';
import { InventoryShipmentConfirmList } from '../model/inventory-shipment-confirm-list';
import { InventoryShipmentSearchResult } from '../model/inventory-shipment-search-result';
import { InventoryShipmentSearchResultMock } from './mocks/inventory-shipment-http.service.mock';
import { InstructorSystemType } from '../const/const';

const BASE_URL = '/inventoryShipment';

@Injectable({
  providedIn: 'root'
})
export class InventoryShipmentHttpService {

  constructor(
    private junotApiService: JunotApiService,
    private dateUtils: DateUtilsService
  ) { }


  /**
   * 検索処理.
   * @param searchCondition 検索条件
   * @return 検索結果
   */
  search(searchCondition: InventoryShipmentSearchCondition):
      Observable<GenericList<InventoryShipmentSearchResult>> {
    const url = `${ BASE_URL }/search`;

    return this.junotApiService.listByPost(url, this.convertSearchParam(searchCondition));
  }

  /**
   * LG送信(確定)処理.
   * @param form form入力データ
   * @returns レスポンス
   */
  confirm(form: InventoryShipmentConfirmList): Observable<InventoryShipmentConfirmList> {
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
  private convertSearchParam(form: InventoryShipmentSearchCondition): InventoryShipmentSearchCondition {
    const copyItem = Object.assign({}, form); // deep copy
    copyItem.cargoAtFrom = this.dateUtils.toString(copyItem.cargoAtFrom as NgbDateStruct);
    copyItem.cargoAtTo = this.dateUtils.toString(copyItem.cargoAtTo as NgbDateStruct);

    return copyItem;
  }

  /**
  * @pram searchCondition 検索条件
   * @pram form form入力データ
   * @return LG送信リクエストデータ
   */
  private convertLgRequestData(form: InventoryShipmentConfirmList): InventoryShipmentConfirmList {
    // チェックがついているレコードのみ
    return {
      inventoryShipmentConfirms: form.inventoryShipmentConfirms.filter(i => i.check)
    };
  }
}
