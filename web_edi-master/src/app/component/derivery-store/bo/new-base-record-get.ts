import { forkJoin, Observable } from 'rxjs';
import { map, flatMap } from 'rxjs/operators';

import { BaseRecordGet } from './base-record-get';

import { BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

export class NewBaseRecordGet extends BaseRecordGet {

  //PRD_0123 #7054 JFE mod start
  // getBaseRecord(orderId: number, _: number): Observable<BaseDataOfDeliveryStoreScreen> {
  getBaseRecord(orderId: number, _: number, id: number): Observable<BaseDataOfDeliveryStoreScreen> {
  //PRD_0123 #7054 JFE mod end
    return forkJoin(
      [
        super.getOrderAndItem(orderId),
        super.getDeliveryHistory(orderId),
        super.getDeliveryPlan(orderId)
      ]
    ).pipe(
      map(([orderAndItem, deliveryHistory, deliveryPlan]) =>
        //PRD_0123 #7054 JFE mod start
        // super.getScreenSetting(orderAndItem.item).pipe(map(screenSetting => ({
          super.getScreenSetting(orderAndItem.item,id).pipe(map(screenSetting => ({
        //PRD_0123 #7054 JFE mod end
          order: orderAndItem.order,
          item: orderAndItem.item,
          screenSetting,
          delivery: null,
          deliveryHistory,
          deliveryPlan,
          //PRD_0123 #7054 JFE mod start
            // purchase: null
            purchase: null,
            id:null
          //PRD_0123 #7054 JFE mod end
          // PRD_0044 del SIT start
          //specialtyQubeCancelResponse: null
          // PRD_0044 del SIT end
        }))
        )
      ),
      flatMap(data => data));
  }
}
