import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

import { GenericList } from '../model/generic-list';
import { ScreenSettingFukukiatru } from '../model/screen-setting-fukukitaru';

import { JunotApiService } from './junot-api.service';
import { FukukitaruOrder } from '../model/fukukitaru-order';
import { ScreenSettingFukukitaruOrderSearchCondition } from '../model/screen-setting-fukukitaru-order-search-condition';
import { FukukitaruOrderSearchCondition } from '../model/fukukitaru-order-search-condition';

const SCREEN_URL = `/screenSettings`;
const F_ORDER_WASH_NAME_URL = `/fukukitaru/orders/washName`;
const F_ORDER_BOTTOM_BILL_URL = `/fukukitaru/orders/bottomBill`;
const ORDER_URL = `/fukukitaru/orders`;

@Injectable({
  providedIn: 'root'
})
export class FukukitaruOrder01Service {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService
  ) { }

  /**
   * 会社情報をリストで取得します。
   *
   * @param searchCondition 検索条件
   * - searchText 検索文字列 (必須)
   * @return 会社情報のリスト
   */
  getAddress(searchConditions?: ScreenSettingFukukitaruOrderSearchCondition): Observable<GenericList<ScreenSettingFukukiatru>> {
    return this.junotApiService.listByPost(`${ SCREEN_URL }${ F_ORDER_WASH_NAME_URL }`, searchConditions);
  }

  /**
   * フクキタル洗濯ネーム発注画面情報リスト取得処理
   * @param searchCondition フクキタル連携情報検索モデル
   * @returns フクキタル連携情報取得結果
   */
  listFukukitaruOrderWashNameMaster(searchConditions: ScreenSettingFukukitaruOrderSearchCondition):
    Observable<GenericList<ScreenSettingFukukiatru>> {
    return this.junotApiService.listByPost(`${ SCREEN_URL }${ F_ORDER_WASH_NAME_URL }`, searchConditions);
  }

  /**
   * フクキタル下札発注画面情報リスト取得処理
   * @param searchCondition フクキタル連携情報検索モデル
   * @returns フクキタル連携情報取得結果
   */
  listFukukitaruOrderBottomBillMaster(searchConditions: ScreenSettingFukukitaruOrderSearchCondition):
    Observable<GenericList<ScreenSettingFukukiatru>> {
    return this.junotApiService.listByPost(`${ SCREEN_URL }${ F_ORDER_BOTTOM_BILL_URL }`, searchConditions);
  }

  /**
   * フクキタル用発注情報取得処理
   * @param id 発注ID
   * @returns 取得結果
   */
  getFukukitaruOrderForId(id: number): Observable<FukukitaruOrder> {
    const URL = `${ ORDER_URL }/${ id }`;
    return this.junotApiService.get(URL);
  }

  /**
   * フクキタル用発注情報リスト取得処理
   * @param searchCondition フクキタル連携情報検索モデル
   * @returns 取得結果
   */
  listFukukitaruOrders(searchCondition: FukukitaruOrderSearchCondition): Observable<GenericList<FukukitaruOrder>> {
    return this.junotApiService.list(ORDER_URL, searchCondition);
  }

  /**
   * フクキタル発注情報登録処理
   * @param postItem 登録データ
   * @returns 取得結果
   */
  postFukukitaruOrder(postItem: any): Observable<FukukitaruOrder> {
    const body = this.convertRequestData(postItem);
    return this.junotApiService.create(ORDER_URL, body);
  }

  /**
   * フクキタル発注情報更新処理
   * @param putItem 更新データ
   * @returns 取得結果
   */
  putFukukitaruOrder(putItem: FukukitaruOrder): Observable<FukukitaruOrder> {
    const URL = `${ ORDER_URL }/${ putItem.id }`;
    const body = this.convertRequestData(putItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * フクキタル発注確定処理
   * @param putItem 更新データ
   * @returns 取得結果
   */
  confirmFukukitaruOrder(putItem: FukukitaruOrder): Observable<FukukitaruOrder> {
    const URL = `${ ORDER_URL }/${ putItem.id }/confirm`;
    const body = this.convertRequestData(putItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * フクキタル発注承認処理
   * @param putItem 更新データ
   * @returns 承認結果
   */
  approveFukukitaruOrder(putItem: FukukitaruOrder): Observable<FukukitaruOrder> {
    const URL = `${ ORDER_URL }/${ putItem.id }/approve`;
    const body = this.convertRequestData(putItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * データ整形処理
   * @param inputData 画面の入力データ
   * @returns 整形後データ
   */
  private convertRequestData(inputData: any): FukukitaruOrder {
    let copyItem = JSON.parse(JSON.stringify(inputData));

    // 日付変換処理
    copyItem = this.convertDate(copyItem);

    return copyItem;
  }

  /**
   * 日付変換処理
   * @returns 変換後データ
   */
  private convertDate(copyItem: any): any {
    // 希望出荷日
    if (copyItem.preferredShippingAt) {
      copyItem.preferredShippingAt = this.ngbDateParserFormatter.format(copyItem.preferredShippingAt).replace(/-/g, '/');
    }
    // 発注日
    if (copyItem.orderAt) {
      copyItem.orderAt = this.ngbDateParserFormatter.format(copyItem.orderAt).replace(/-/g, '/');
    }
    return copyItem;
  }

}
