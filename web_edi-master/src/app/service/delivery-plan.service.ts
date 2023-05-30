import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { StringUtils } from '../util/string-utils';
import { CalculationUtils } from '../util/calculation-utils';

import { GenericList } from '../model/generic-list';
import { DeliveryPlanSearchConditions } from '../model/search-conditions';
import { DeliveryPlan } from '../model/delivery-plan';
import { DeliveryPlanCut } from '../model/delivery-plan-cut';
import { DeliveryPlanDetail } from '../model/delivery-plan-detail';
import { DeliverySku } from '../model/delivery-sku';
import { Order } from '../model/order';
import { OrderSku } from '../model/order-sku';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/deliveryPlans';
@Injectable({
  providedIn: 'root'
})
export class DeliveryPlanService {
  constructor(
    private junotApiService: JunotApiService
  ) { }
  /**
   * 納品予定情報取得処理
   * @param id 納品予定Id
   */
  getDeliveryPlanById(id): Observable<DeliveryPlan> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(URL);
  }
  /**
   * 納品予定情報登録処理
   * @param postItem 登録パラメータ
   */
  postDeliveryPlanRequest(postItem: any): Observable<DeliveryPlan> {
    const body = this.convertRequestData(postItem);
    console.debug('postDeliveryPlanRequest body:', body);
    return this.junotApiService.create(BASE_URL, body);
  }
  /**
   * 納品予定情報一覧取得
   * @param searchConditions 検索条件
   */
  getDeliveryPlanList(searchConditions: DeliveryPlanSearchConditions): Observable<GenericList<DeliveryPlan>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }
  /**
   * 納品予定情報更新処理
   * @param postItem 更新パラメータ
   */
  putDeliveryPlanRequest(postItem): Observable<DeliveryPlan> {
    const URL = `${ BASE_URL }/${ postItem.id }`;
    const body = this.convertRequestData(postItem);
    console.debug('putDeliveryPlanRequest body:', body);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 画面の入力データをrequestパラメータの型に整形して返す
   * @param inputData 画面の入力データ
   */
  convertRequestData(inputData: any): string {
    const copyItem = JSON.parse(JSON.stringify(inputData));
    return copyItem;
  }

  /**
   * 生産数を算出する。
   * カラーコード、サイズコードが指定されていない場合は、生産数の合計を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 生産数を返却する。
   *
   * @param deliveryPlanCuts 生産数のリスト
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 生産数
   */
  calcDeliveryPlanCutLot(deliveryPlanCuts: DeliveryPlanCut[], colorCode?: string, sizeCode?: string): number {
    let deliveryPlanCutLot = 0;
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      // 指定した色・サイズと合致する裁断数を取得
      deliveryPlanCuts.some(deliveryPlanCut => {
        if (deliveryPlanCut.colorCode === colorCode && deliveryPlanCut.size === sizeCode) {
          deliveryPlanCutLot = deliveryPlanCut.deliveryPlanCutLot;
          return true;
        }
      });
    } else {
      // 生産数合計を算出する
      deliveryPlanCuts.forEach(deliveryPlanCut => {
        deliveryPlanCutLot += Number(deliveryPlanCut.deliveryPlanCutLot ? deliveryPlanCut.deliveryPlanCutLot : 0);
      });
    }
    return deliveryPlanCutLot;
  }

  /**
   * 生産率を算出する。
   * カラーコード、サイズコードが指定されていない場合は、発注合計に対する生産数合計の率を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 発注数に対する生産数の率を返却する。
   *
   * @param orderData 発注情報
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 生産数
   */
  calcDeliveryPlanCutRate(orderData: Order, deliveryPlanCuts: DeliveryPlanCut[],
    colorCode?: string, sizeCode?: string): number {
    let deliveryPlanCutLot = 0;    // 生産数
    let productOrderLot = 0;      // 発注数
    let rate = 0;
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      // 指定した色・サイズと合致する生産数を取得
      deliveryPlanCutLot = this.calcDeliveryPlanCutLot(deliveryPlanCuts, colorCode, sizeCode);
      // 指定した色・サイズの発注数を取得
      orderData.orderSkus.some(orderSku => {
        if (orderSku.colorCode === colorCode && orderSku.size === sizeCode) {
          productOrderLot = orderSku.productOrderLot;
          return true;
        }
      });
      rate = CalculationUtils.calcRateIsNumber(deliveryPlanCutLot - productOrderLot, productOrderLot);
    } else {
      // 生産数合計と発注数合計を取得
      deliveryPlanCutLot = this.calcDeliveryPlanCutLot(deliveryPlanCuts);
      productOrderLot = orderData.quantity;
      rate = CalculationUtils.calcRateIsNumber(deliveryPlanCutLot - productOrderLot, productOrderLot);
    }
    return rate;
  }

  /**
   * 納品済数の算出を行う。
   * カラーコード、サイズコードが指定されていない場合は、納品済数合計を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 納品済数合計を返却する
   *
   * @param deliverySkuList 納品SKUのリスト
   * @param orderSkuList 発注SKU情報のリスト
   * @param arrivalDeliveryDetailIds 仕入れが確定している納品明細IDリスト
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 納品済数の合計
   */
  calcCompletedDelivery(deliverySkuList: DeliverySku[], orderSkuList: OrderSku[],
    arrivalDeliveryDetailIds: number[], colorCode?: string, sizeCode?: string): number {
    let sumdeliveryLot = 0;    // 納品依頼数合計
    let sumReturnLot = 0;      // 返品数合計
    let eachDeliverySkus: DeliverySku[];

    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      // 指定した色・サイズと合致する納品SKU取得
      eachDeliverySkus = deliverySkuList.filter(deliverySku => {
        return deliverySku.colorCode === colorCode && deliverySku.size === sizeCode;
      });
      // 返品数を計算する
      sumReturnLot = this.sumReturnLot(orderSkuList, colorCode, sizeCode);
    } else {
      // 納品依頼SKU全てを集計対象にする
      eachDeliverySkus = deliverySkuList;
      // 返品数を計算する
      sumReturnLot = this.sumReturnLot(orderSkuList);
    }

    // 納品依頼数(仕入が確定している場合は入荷数量)合計を計算する
    eachDeliverySkus.forEach(deliverySku => {
      // 仕入が確定している場合は入荷数量を足す。未確定の場合は納品依頼数を足す。
      sumdeliveryLot +=
        Number((arrivalDeliveryDetailIds.includes(deliverySku.deliveryDetailId)) ? deliverySku.arrivalLot
          : (deliverySku.deliveryLot ? deliverySku.deliveryLot : 0));
    });

    return sumdeliveryLot - sumReturnLot;
  }

  /**
   * 返品数の算出を行う。
   * カラーコード、サイズコードが指定されていない場合は、返品数合計を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 返品数合計を返却する
   *
   * @param orderSkuList 発注SKU情報のリスト
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 返品数合計
   */
  private sumReturnLot(orderSkuList: OrderSku[], colorCode?: string, sizeCode?: string): number {
    if (!orderSkuList && orderSkuList.length === 0) { return 0; }
    let sumReturnLot = 0;
    let eachOrderSkus: OrderSku[];
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      // 指定した色・サイズと合致する発注SKU取得
      eachOrderSkus = orderSkuList.filter(orderSku => {
        return orderSku.colorCode === colorCode && orderSku.size === sizeCode;
      });
    } else {
      // 発注SKU全てを集計対象にする
      eachOrderSkus = orderSkuList;
    }
    eachOrderSkus.forEach(orderSku => sumReturnLot += orderSku.returnLot);
    return sumReturnLot;
  }

  /**
   * mainFormから日付が入力されている納品予定数合計を算出する。
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 日付が入力されている納品予定数の合計を返却する。
   * カラーコード、サイズが指定されていない場合は、すべての日付が入力されている
   * 納品予定数の合計を返却する。
   * ※日付ありのみ
   *
   * @param deliveryPlanDetails 納品予定明細のリスト
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 日付が入力されている全ての納品予定SKU配列のvalue
   */
  private sumInputtedDateDeliveryPlanLot(deliveryPlanDetails: DeliveryPlanDetail[], colorCode?: string, sizeCode?: string): number {
    let deliverableDeliveryPlanDetails = [];
    if (deliveryPlanDetails == null) { return 0; }
    // 日付が入力されている納品予定明細を取得する
    deliverableDeliveryPlanDetails = deliveryPlanDetails.filter(deliveryPlanDetail => {
      if (deliveryPlanDetail.deliveryPlanAt !== null) {
        return deliveryPlanDetail;
      }
    });
    return this.sumDeliveryPlanLot(deliverableDeliveryPlanDetails, colorCode, sizeCode);
  }

  /**
   * 納品予定明細配列に格納されいてる納品予定SKUの納品予定数の合計を算出する。
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 納品予定数の合計を返却する。
   * カラーコード、サイズが指定されていない場合は、全ての納品予定数の合計を返却する。
   * ※日付なし含む
   *
   * @param deliveryPlanDetails 納品予定明細のリスト
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 納品予定数の合計
   */
  private sumDeliveryPlanLot(deliveryPlanDetails: DeliveryPlanDetail[], colorCode?: string, sizeCode?: string): number {
    let deliveryPlanLot = 0;
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      deliveryPlanDetails.forEach(deliverableDeliveryPlanDetail => {
        deliverableDeliveryPlanDetail.deliveryPlanSkus.some(deliveryPlanSku => {
          if (deliveryPlanSku.colorCode === colorCode && deliveryPlanSku.size === sizeCode) {
            deliveryPlanLot +=
              Number(deliveryPlanSku.deliveryPlanLot ? deliveryPlanSku.deliveryPlanLot : 0);
            return true;
          }
        });
      });
    } else {
      // 納品予定明細SKUを集計対象とする。
      deliveryPlanDetails.forEach(deliveryPlanDetail => {
        deliveryPlanDetail.deliveryPlanSkus.forEach(deliveryPlanSku => {
          deliveryPlanLot +=
            Number(deliveryPlanSku.deliveryPlanLot ? deliveryPlanSku.deliveryPlanLot : 0);
        });
      });
    }
    return deliveryPlanLot;
  }

  /**
   * 納品残数の算出を行う。
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 納品残数を返却する。
   * カラーコード、サイズが指定されていない場合は、納品残数合計を返却する。
   *
   * @param deliveryPlanDetails 納品予定明細のリスト
   * @param deliverySkuList 納品SKUのリスト
   * @param orderSkuList 発注SKUのリスト
   * @param arrivalDeliveryDetailIds 仕入れが確定している納品明細IDリスト
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 納品残数の合計
   */
  calcRemainingDeliveryPlanLot(deliveryPlanDetails: DeliveryPlanDetail[], deliverySkuList: DeliverySku[], orderSkuList: OrderSku[],
    arrivalDeliveryDetailIds: number[], colorCode?: string, sizeCode?: string): number {
    let remainingDeliveryPlanLot = 0;
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      remainingDeliveryPlanLot = this.sumInputtedDateDeliveryPlanLot(deliveryPlanDetails, colorCode, sizeCode)
        - this.calcCompletedDelivery(deliverySkuList, orderSkuList, arrivalDeliveryDetailIds, colorCode, sizeCode);
    } else {
      remainingDeliveryPlanLot = this.sumInputtedDateDeliveryPlanLot(deliveryPlanDetails)
        - this.calcCompletedDelivery(deliverySkuList, orderSkuList, arrivalDeliveryDetailIds);
    }
    return remainingDeliveryPlanLot;
  }

  /**
   * 増減産数を算出
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 増減産数を返却する。
   * カラーコード、サイズが指定されていない場合は、増減産数合計を返却する。
   *
   * @param deliveryPlanDetails 納品予定明細のリスト
   * @param orderData 発注情報
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 増減産数
   */
  calcIncreaseOrDecreaseLot(deliveryPlanDetails: DeliveryPlanDetail[], orderData: Order,
    colorCode?: string, sizeCode?: string): number {
    let deliverableDeliveryPlanLot = 0;
    let productOrderLot = 0;
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      // 指定した色・サイズの納品予定明細の合計数を取得
      deliverableDeliveryPlanLot = this.sumDeliveryPlanLot(deliveryPlanDetails, colorCode, sizeCode);
      // 指定した色・サイズの発注数を取得
      orderData.orderSkus.some(orderSku => {
        if (orderSku.colorCode === colorCode && orderSku.size === sizeCode) {
          productOrderLot = orderSku.productOrderLot;
          return true;
        }
      });
    } else {
      // 納品予定明細の合計と発注数合計を取得
      deliverableDeliveryPlanLot = this.sumDeliveryPlanLot(deliveryPlanDetails);
      productOrderLot = orderData.quantity;
    }
    return deliverableDeliveryPlanLot - productOrderLot;
  }

  /**
   * 増減産率を算出
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 発注数に対する増減産数の率を返却する。
   * カラーコード、サイズが指定されていない場合は、発注数合計に対する増減産数合計の率を返却する。
   *
   * @param deliveryPlanDetails 納品予定明細のリスト
   * @param orderData 発注情報
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 増減産数
   */
  calcIncreaseOrDecreaseRate(deliveryPlanDetails: DeliveryPlanDetail[], orderData: Order,
    colorCode?: string, sizeCode?: string): number {
    let sumIncreaseOrDecreaseLot = 0;
    let productOrderLot = 0;
    let rate = 0;
    if (this.isSpecifiedColorAndSize(colorCode, sizeCode)) {
      // 指定した色・サイズの納品予定明細の合計数を取得
      sumIncreaseOrDecreaseLot = this.calcIncreaseOrDecreaseLot(deliveryPlanDetails, orderData, colorCode, sizeCode);
      // 指定した色・サイズの発注数を取得
      orderData.orderSkus.some(orderSku => {
        if (orderSku.colorCode === colorCode && orderSku.size === sizeCode) {
          productOrderLot = orderSku.productOrderLot;
          return true;
        }
      });
      rate = CalculationUtils.calcRateIsNumber(sumIncreaseOrDecreaseLot, productOrderLot);
    } else {
      // 納品予定明細の合計と発注数合計を取得
      sumIncreaseOrDecreaseLot = this.calcIncreaseOrDecreaseLot(deliveryPlanDetails, orderData);
      productOrderLot = orderData.quantity;
      rate = CalculationUtils.calcRateIsNumber(sumIncreaseOrDecreaseLot, productOrderLot);
    }
    return rate;
  }

  /**
   * 色・サイズが指定されているか判断
   * カラーコード、サイズが指定されている場合は、true
   * 指定されていない場合は、false
   * を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 判断結果
   */
  private isSpecifiedColorAndSize(colorCode?: string, sizeCode?: string): boolean {
    return StringUtils.isNotEmpty(colorCode) && StringUtils.isNotEmpty(sizeCode);
  }
}
