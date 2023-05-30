import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { JunotApiService } from './junot-api.service';

import { GenericList } from '../model/generic-list';
import { ScreenSettingFukukitaruOrderSearchCondition } from '../model/screen-setting-fukukitaru-order-search-condition';
import { ScreenSettingFukukiatru } from '../model/screen-setting-fukukitaru';
import { ScreenSettingDeliverySearchCondition } from '../model/screen-setting-delivery-search-condition';
import { ScreenSettingDelivery } from '../model/screen-setting-delivery';

const BASE_URL = '/screenSettings';
const ORDERS_WASH_NAME_URL = BASE_URL + '/orders/washName';
const ORDERS_BOTTOM_BILL_URL = BASE_URL + '/fukukitaru/orders/bottomBill';
const FUKUKITARU_ITEMS_URL = BASE_URL + '/fukukitaru/items';
const DELIVERY_URL = BASE_URL + '/delivery';

@Injectable({
  providedIn: 'root'
})
export class ScreenSettingService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * フクキタル発注の洗濯ネームマスタ情報リスト取得
   * @param searchConditions 検索条件
   * @returns 取得結果
   */
  getOrdersWashNameList(searchConditions: ScreenSettingFukukitaruOrderSearchCondition): Observable<GenericList<ScreenSettingFukukiatru>> {
    return this.junotApiService.listByPost(ORDERS_WASH_NAME_URL, searchConditions);
  }

  /**
   * フクキタル発注の下げ札マスタ情報リスト取得
   * @param searchConditions 検索条件
   * @returns 取得結果
   */
  getOrdersBottomBillList(searchConditions: ScreenSettingFukukitaruOrderSearchCondition): Observable<GenericList<ScreenSettingFukukiatru>> {
    return this.junotApiService.listByPost(ORDERS_BOTTOM_BILL_URL, searchConditions);
  }

  /**
   * フクキタル品番のマスタ情報リスト取得
   * @param searchConditions 検索条件
   * @returns 取得結果
   */
  getFukukitaruItemList(searchConditions: ScreenSettingFukukitaruOrderSearchCondition): Observable<GenericList<ScreenSettingFukukiatru>> {
    return this.junotApiService.listByPost(FUKUKITARU_ITEMS_URL, searchConditions);
  }

  /**
   * 納品依頼画面基本データ取得
   * @param searchConditions 検索条件
   * @returns 取得結果
   */
  getDelivery(searchConditions: ScreenSettingDeliverySearchCondition): Observable<GenericList<ScreenSettingDelivery>> {
    return this.junotApiService.listByPost(DELIVERY_URL, searchConditions);
  }
}
