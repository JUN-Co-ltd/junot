import { Observable, of } from 'rxjs';
import { map, flatMap } from 'rxjs/operators';

import { ScreenSettingDeliveryMasterType, Path } from '../../../const/const';

import { ScreenSettingService } from '../../../service/screen-setting.service';
import { ItemService } from '../../../service/item.service';
import { OrderService } from '../../../service/order.service';
import { DeliveryRequestService } from '../../../service/delivery-request.service';
import { DeliveryPlanService } from '../../../service/delivery-plan.service';
import { PurchaseHttpService } from 'src/app/service/purchase-http.service';

import { BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

import { Item } from '../../../model/item';
import { Order } from '../../../model/order';
import { Delivery } from '../../../model/delivery';
import { ScreenSettingDeliverySearchCondition } from '../../../model/screen-setting-delivery-search-condition';
import { ScreenSettingDelivery } from '../../../model/screen-setting-delivery';
import { DeliveryRequestSearchConditions, DeliveryPlanSearchConditions } from '../../../model/search-conditions';
import { DeliveryPlan } from '../../../model/delivery-plan';
import { DeliveryStoreInfo } from '../../../model/delivery-store-info';

import { Purchase } from '../../purchase/interface/purchase';

export abstract class BaseRecordGet {
  private readonly SCREEN_SETTING_MASTER_TYPE = [
    ScreenSettingDeliveryMasterType.THRESHOLD,
    ScreenSettingDeliveryMasterType.STORE_HRTMST,
    ScreenSettingDeliveryMasterType.TNPMST,
    // PRD_0031 add SIT start
    ScreenSettingDeliveryMasterType.STORE_STOCK,
    // PRD_0031 add SIT end
    // PRD_0123 add SIT start
    // PRD_0033 add SIT start
    //ScreenSettingDeliveryMasterType.POS_SALES_QUANTITY
    ScreenSettingDeliveryMasterType.POS_SALES_QUANTITY,
    // PRD_0123 #7054 add JFE start
    ScreenSettingDeliveryMasterType.DELIVERY_LOCATION
    // PRD_0123 #7054 add JFE end
  ];

  constructor(
    private _screenSettingService: ScreenSettingService,
    private _itemService: ItemService,
    private _orderService: OrderService,
    private _deliveryRequestService: DeliveryRequestService,
    private _deliveryPlanService: DeliveryPlanService,
    private _purchaseHttpService: PurchaseHttpService
  ) {
  }

  get deliveryRequestService(): DeliveryRequestService {
    return this._deliveryRequestService;
  }

  get deliveryPlanService(): DeliveryPlanService {
    return this._deliveryPlanService;
  }

  get purchaseHttpService(): PurchaseHttpService {
    return this._purchaseHttpService;
  }

  /**
   * 画面で必要なレコードを取得する.
   * @param orderId 発注ID
   * @param deliveryId 納品ID
   * @param path パス
   * @returns BaseDataOfDeliveryStoreScreen
   */
  //PRD_0123 #7054 JFE mod start
  // abstract getBaseRecord(orderId: number, deliveryId: number, path: Path): Observable<BaseDataOfDeliveryStoreScreen>;
  abstract getBaseRecord(orderId: number, deliveryId: number,id: number, path: Path): Observable<BaseDataOfDeliveryStoreScreen>;
  //PRD_0123 #7054 JFE mod end

  /**
   * 納品依頼画面用マスタデータを取得.
   * @param item 品番情報
   * @param deliveryStoreInfos 納品得意先情報登録済の店舗情報リスト
   * @returns Observable<ScreenSettingDelivery>
   */
  //PRD_0123 #7054 JFE mod start
  //getScreenSetting(item: Item, deliveryStoreInfos?: DeliveryStoreInfo[]): Observable<ScreenSettingDelivery> {
    getScreenSetting(item: Item,id: number, deliveryStoreInfos?: DeliveryStoreInfo[]): Observable<ScreenSettingDelivery> {
  //PRD_0123 #7054 JFE mod end
    const searchConditions: ScreenSettingDeliverySearchCondition = {
      listMasterType: this.SCREEN_SETTING_MASTER_TYPE,
      // PRD_0031 add SIT start
      partNo: item.partNo,
      // PRD_0031 add SIT end
      brandCode: item.brandCode,
      itemCode: item.itemCode,
      seasonCode: item.seasonCode,
      //PRD_0123 #7054 JFE mod start
      // deliveryStoreInfos: deliveryStoreInfos
      deliveryStoreInfos: deliveryStoreInfos,
      id:item.id
      //PRD_0123 #7054 JFE mod end
    };
    return this._screenSettingService.getDelivery(searchConditions).pipe(map(
      screenSettingDeliveryList => {
        console.debug('getScreenSetting:', screenSettingDeliveryList);
        return screenSettingDeliveryList.items[0];
      }));
  }

  /**
   * @param orderId 発注ID
   * @returns 発注情報,品番情報
   */
  getOrderAndItem(orderId: number): Observable<{ order: Order, item: Item }> {
    return this.getOrder(orderId).pipe(
      map(order => this.getItem(order.partNoId).pipe(map(item => ({ order: order, item: item })))),
      flatMap(data => data));
  }

  /**
   * 発注データを取得.
   * @param orderId 発注ID
   * @returns Observable<Order>
   */
  private getOrder(orderId: number): Observable<Order> {
    return this._orderService.getOrderForId(orderId).pipe(map(
      response => {
        console.debug('getOrder:', response);
        return response;
      }));
  }

  /**
   * 品番データを取得.
   * @param partNoId 発注ID
   * @returns Observable<Item>
   */
  private getItem(partNoId: number): Observable<Item> {
    return this._itemService.getItemForId(partNoId).pipe(map(
      response => {
        console.debug('getItem:', response);
        return response;
      }));
  }

  /**
   * 過去納品データを取得.
   * @param orderId 発注ID
   * @returns Observable<Item>
   */
  getDeliveryHistory(orderId: number): Observable<Delivery[]> {
    const condtions = { orderId: orderId } as DeliveryRequestSearchConditions;
    return this.deliveryRequestService.getDeliveryRequestList(condtions).pipe(map(
      response => {
        console.debug('getDeliveryHistory:', response);
        return response.items;
      }));
  }

  /**
   * 納品予定情報取得処理.
   * @param orderId 発注ID
   * @returns Observable<DeliveryPlan>
   */
  getDeliveryPlan(orderId: number): Observable<DeliveryPlan> {
    return this.deliveryPlanService.getDeliveryPlanList({ orderId: orderId } as DeliveryPlanSearchConditions).pipe(map(
      deliveryPlanList => {
        console.debug('getDeliveryPlan:', deliveryPlanList);
        return deliveryPlanList.items[0];
      }));
  }

  /**
   * @param deliveryId 納品ID
   * @param path パス
   * @return 仕入情報
   */
  fetchPurchase(deliveryId: number, path: Path): Observable<Purchase> {
    if (path !== Path.VIEW && path !== Path.CORRECT) {
      return of(null);
    }

    return this.purchaseHttpService.getByDeliveryId(deliveryId);
  }
}
