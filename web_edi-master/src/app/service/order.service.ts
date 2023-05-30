import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

import { Order } from '../model/order';
import { VOrder } from '../model/v-order';
import { OrderSearchConditions } from '../model/search-conditions';
import { GenericList } from '../model/generic-list';
import { OrderSku } from '../model/order-sku';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/orders';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService
  ) { }

  /**
   * 製品発注数を合計する.
   * @returns 製品発注数合計
   */
  sumAllOrderQuantity(orderSkus: OrderSku[]): number {
    let qantity = 0;
    if (orderSkus === undefined || orderSkus.length === 0) { return qantity; }
    orderSkus.forEach(sku => qantity += Number(sku.productOrderLot ? sku.productOrderLot : 0));
    return qantity;
  }

  /**
   * 発注情報登録処理
   * @param postItem 登録データ
   * @returns 取得結果
   */
  postOrder(postItem: any): Observable<Order> {
    const body = this.convertRequestData(postItem);
    console.debug('body:', body);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 発注情報取得処理
   * @param id 発注ID
   * @returns 取得結果
   */
  getOrderForId(id: number): Observable<Order> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(URL);
  }

  /**
   * 発注情報一覧取得
   * @param searchConditions 検索条件
   * @returns 取得結果
   */
  getOrderList(searchConditions: OrderSearchConditions): Observable<GenericList<VOrder>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }

  /**
   * IDリストで発注情報一覧を取得する.
   * @param ids 発注IDリスト
   * @returns レスポンス
   */
  fetchOrderListByIds(ids: number[]): Observable<GenericList<Order>> {
    return this.junotApiService.listByPost(`${ BASE_URL }/ids`, { ids });
  }

  /**
   * 発注更新処理
   * @param id 発注ID
   * @returns 取得結果
   */
  updateOrder(postItem: any): Observable<Order> {
    const URL = `${ BASE_URL }/${ postItem.id }`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 発注情報削除処理
   * @param id 発注ID
   */
  deleteOrder(id: number): Observable<Order> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.delete(URL);
  }

  /**
   * 発注確定処理
   * @param id 発注ID
   * @returns 取得結果
   */
  confirmOrder(postItem: any): Observable<Order> {
    const URL = `${ BASE_URL }/${ postItem.id }/confirm`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 発注承認処理
   * @param id 発注ID
   * @returns 承認結果
   */
  approveOrder(postItem: any): Observable<Order> {
    const URL = `${ BASE_URL }/${ postItem.id }/approve`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * データ整形処理
   * @param inputData 画面の入力データ
   * @returns 整形後データ
   */
  private convertRequestData(inputData: any): Order {
    let copyItem = JSON.parse(JSON.stringify(inputData));

    // 金額のカンマ除去
    // 単価
    if (copyItem.unitPrice != null && typeof copyItem.unitPrice === 'string') {
      copyItem.unitPrice = copyItem.unitPrice.replace(/,/g, '');
    }
    // 上代
    if (copyItem.retailPrice != null && typeof copyItem.retailPrice === 'string') {
      copyItem.retailPrice = copyItem.retailPrice.replace(/,/g, '');
    }
    // 原価
    if (copyItem.otherCost != null && typeof copyItem.otherCost === 'string') {
      copyItem.otherCost = copyItem.otherCost.replace(/,/g, '');
    }

    // SKUリストを送信用フォーマットに変更
    copyItem.orderSkus = this.convertSku(copyItem.orderSkus);

    // 日付変換処理
    copyItem = this.convertDate(copyItem);

    // 製品原価
    copyItem.productCost = (copyItem.processingCost == null ? 0 : Number(copyItem.processingCost))  // 加工賃
      + (copyItem.attachedCost == null ? 0 : Number(copyItem.attachedCost))     // 附属代
      + (copyItem.otherCost == null ? 0 : Number(copyItem.otherCost));          // その他原価

    return copyItem;
  }

  /**
   * 日付変換処理
   * @returns 変換後データ
   */
  private convertDate(copyItem: any): any {
    // 製品修正納期
    if (copyItem.productCorrectionDeliveryAt) {
      copyItem.productCorrectionDeliveryAt = this.ngbDateParserFormatter.format(copyItem.productCorrectionDeliveryAt).replace(/-/g, '/');
    }
    // 製品発注日
    if (copyItem.productOrderAt) {
      copyItem.productOrderAt = this.ngbDateParserFormatter.format(copyItem.productOrderAt).replace(/-/g, '/');
    }
    return copyItem;
  }

  /**
   * formのvalueからorderSkuをすべて取得する。
   *
   * @param orderSkus 色別に分類されたorderSkuのリスト
   * @returns orderSkuリスト
   */
  convertSku(orderSkus: any): OrderSku[] {
    const reqColorList: OrderSku[] = [];
    const colorList = orderSkus.filter(sku => sku.colorCode !== '');
    colorList.forEach(sku => {
      // 製品発注数がNullのデータは除外する。
      sku.sizeList = sku.sizeList.filter(size => size.productOrderLot > 0);
      Array.prototype.push.apply(reqColorList, sku.sizeList);
    });
    return reqColorList;
  }
}
