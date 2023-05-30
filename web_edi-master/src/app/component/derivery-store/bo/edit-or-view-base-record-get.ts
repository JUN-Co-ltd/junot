import { Observable, forkJoin } from 'rxjs';
import { map, flatMap } from 'rxjs/operators';

import { BaseRecordGet } from './base-record-get';

import { BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

import { Delivery } from '../../../model/delivery';
import { DeliveryStoreInfo } from '../../../model/delivery-store-info';
import { Path } from 'src/app/const/const';

export class EditOrViewBaseRecordGet extends BaseRecordGet {
//PRD_0123 #7054 JFE mod start
// getBaseRecord(orderId: number, deliveryId: number, path: Path): Observable<BaseDataOfDeliveryStoreScreen> {
  getBaseRecord(orderId: number, deliveryId: number, id: number, path: Path): Observable<BaseDataOfDeliveryStoreScreen> {
//PRD_0123 #7054 JFE mod end
    return forkJoin(
      [
        super.getOrderAndItem(orderId),
        this.getDelivery(deliveryId),
        super.getDeliveryHistory(orderId),
        super.getDeliveryPlan(orderId),
        super.fetchPurchase(deliveryId, path)
      ]
    ).pipe(
      //PRD_0123 #7054 JFE mod start
        //map(([orderAndItem, delivery, deliveryHistory, deliveryPlan, purchase]) =>
        // super.getScreenSetting(orderAndItem.item, this.extractDeliveryStoreInfoList(delivery)).pipe(
      map(([orderAndItem, delivery, deliveryHistory, deliveryPlan, purchase]) =>
          super.getScreenSetting(orderAndItem.item,id, this.extractDeliveryStoreInfoList(delivery)).pipe(
        //PRD_0123 #7054 JFE mod end
          map(screenSetting => ({
            order: orderAndItem.order,
            item: orderAndItem.item,
            screenSetting,
            delivery,
            //PRD_0123 #7054 JFE mod start
            id: orderAndItem.item.id,
            //PRD_0123 #7054 JFE mod end
            deliveryHistory,
            deliveryPlan,
            purchase
            // PRD_0044 del SIT start
            //specialtyQubeCancelResponse: null
            // PRD_0044 del SIT end
          })))),
      flatMap(data => data));
  }

  /**
   * 納品得意先情報から店舗情報を取得する.
   * @param delivery 納品情報
   * @returns 店舗情報リスト
   */
  private extractDeliveryStoreInfoList(delivery: Delivery): DeliveryStoreInfo[] {
    return delivery.deliveryDetails.reduce((preVal, { divisionCode, deliveryStores }) => {
      const codeList = deliveryStores.map(store => ({ divisionCode: divisionCode, storeCode: store.storeCode }));
      return [...preVal, ...codeList];
    }, [] as { divisionCode: string, storeCode: string }[]);
  }

  /**
   * 納品依頼データを取得.
   * @param deliveryId 納品ID
   * @param orderId 発注ID
   * @param nextUrl 遷移先URL
   * @returns Observable<Item>
   */
  private getDelivery(deliveryId: number): Observable<Delivery> {
    return this.deliveryRequestService.getDeliveryRequestForId(deliveryId).pipe(map(
      response => {
        console.debug('getDelivery:', response);
        return response;
      }));
  }
}
