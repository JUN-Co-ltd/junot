import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, AbstractControl } from '@angular/forms';

import { ExceptionUtils } from '../../util/exception-utils';
import { Const } from '../../const/const';
import { StringUtils } from '../../util/string-utils';

import { Item } from '../../model/item';
import { OrderSku } from '../../model/order-sku';
import { Order } from '../../model/order';
import { DeliveryPlan } from '../../model/delivery-plan';
import { DeliveryPlanSku } from '../../model/delivery-plan-sku';
import { DeliveryPlanDetail } from '../../model/delivery-plan-detail';
import { DeliveryRequestSearchConditions } from '../../model/search-conditions';
import { DeliverySku } from '../../model/delivery-sku';
import { DeliveryPlanSearchConditions } from '../../model/search-conditions';

import { HeaderService } from '../../service/header.service';
import { ItemService } from '../../service/item.service';
import { OrderService } from '../../service/order.service';
import { DeliveryPlanService } from '../../service/delivery-plan.service';
import { DeliveryRequestService } from '../../service/delivery-request.service';

@Component({
  selector: 'app-delivery-plan-detail',
  templateUrl: './delivery-plan-detail.component.html',
  styleUrls: ['./delivery-plan-detail.component.scss']
})
export class DeliveryPlanDetailComponent implements OnInit {
  itemData: Item;                 // 品番情報
  orderData: Order;               // 発注情報
  deliveryPlanData: DeliveryPlan; // 納品予定情報

  private deliverySkuList: DeliverySku[] = [];      // 納品依頼SKUリスト
  private arrivalDeliveryDetailIds: number[] = [];  // 仕入れ確定済みの納品明細IDリスト(納品済数計算時に使用)

  isInitDataSetted = false;     // 初期表示で必要なデータ取得済

  mainForm: FormGroup;          // メインのフォーム

  constructor(
    private headerService: HeaderService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private itemService: ItemService,
    private orderService: OrderService,
    private deliveryPlanService: DeliveryPlanService,
    private deliveryRequestService: DeliveryRequestService
  ) { }

  ngOnInit() {
    // ヘッダーは表示しない
    this.headerService.hide();

    const orderId = this.route.snapshot.params['orderId']; // URLから発注IDを取得
    this.initFromGroup();

    this.getDeliveryPlanData(orderId).then(deliveryPlanData => {
      if (deliveryPlanData == null) {
        this.setInitData(orderId);
      } else {
        this.setInitData(orderId, deliveryPlanData);
      }
    });
  }

  /**
   * 初期表示で必要なデータを取得して設定する。
   * @param orderId 発注ID
   * @param deliveryPlanData 納品予定情報
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private setInitData(orderId: number, deliveryPlanData?: DeliveryPlan): void {
    this.getOrderData(orderId).then(orderData => { // 発注情報の取得
      this.getDeliverySkuList(orderId).then(() => { // 納品依頼よりSKUを取得
        this.getItemData(orderData.partNoId).then(itemData => {　// 品番情報の取得
          this.setDeliveryPlanCutsFormArray(orderData, deliveryPlanData);
          this.setDeliveryPlanDetailFormArray(orderData, deliveryPlanData);
          this.isInitDataSetted = true; // フォームを表示
        });
      });
    });
  }

  /**
   * メインのFormGroupを初期化する。
   */
  private initFromGroup(): void {
    this.mainForm = this.formBuilder.group(
      {
        id: [null],                       // 納品予定Id
        orderId: [null],                  // 発注ID
        partNoId: [null],                 // 品番ID
        deliveryPlanDetails: [null],      // 納品予定明細FormArray
        deliveryPlanCuts: [null],         // 納品予定裁断FormArray
      }
    );
  }

  /**
 * mainFormの項目の状態を取得する。
 * @return mainForm.controls
 */
  get f(): any { return this.mainForm.controls; }

  /**
   * mainFormのdeliveryPlanCutsの項目の状態を取得する。
   * @return mainForm.get('deliveryPlanCuts').controls
   */
  get fDeliveryPlanCuts(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('deliveryPlanCuts')).controls;
  }

  /**
   * mainFormのdeliveryPlanDetailsの項目の状態を取得する。
   * @return mainForm.get('deliveryPlanDetails').controls
   */
  get fDeliveryPlanDetails(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('deliveryPlanDetails')).controls;
  }

  /**
   * 発注情報取得処理
   * @param orderId 発注ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getOrderData(orderId: number): Promise<Order> {
    return await this.orderService.getOrderForId(orderId).toPromise().then(
      order => {
        console.debug('getOrderData success:', order);
        this.orderData = order;
        this.mainForm.patchValue({
          orderId: this.orderData.id,
          partNoId: this.orderData.partNoId,
        });
        return Promise.resolve(this.orderData);
      },
      error => {
        console.debug('getOrderData error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      });
  }

  /**
   * 納品依頼情報を取得して、SKUだけ取り出し変数に設定する。
   * @param orderId 発注ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getDeliverySkuList(orderId: number): Promise<any> {
    const requestParam = { orderId: orderId, idSortDesc: true } as DeliveryRequestSearchConditions;
    this.deliverySkuList = [];  // 初期化
    this.arrivalDeliveryDetailIds = [];  // 初期化
    return await this.deliveryRequestService.getDeliveryRequestList(requestParam).toPromise().then(
      deliveryDataList => {
        // 納品依頼情報からSKUだけを抽出・格納する
        deliveryDataList.items.forEach(delivery => {
          delivery.deliveryDetails.forEach(deliveryDetail => {
            Array.prototype.push.apply(this.deliverySkuList, deliveryDetail.deliverySkus);
            if (deliveryDetail.arrivalFlg) {
              // 仕入れが確定している納品明細IDをリストにセット
              this.arrivalDeliveryDetailIds.push(deliveryDetail.id);
            }
          });
        });
        console.debug('納品依頼SKU情報リスト:', this.deliverySkuList);
        console.debug('仕入れが確定している納品明細IDリスト:', this.arrivalDeliveryDetailIds);
        return Promise.resolve(this.deliverySkuList);
      },
      error => {
        console.debug('getDeliverySkuList error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * 品番データ取得処理
   * @param partNoId 品番ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getItemData(partNoId: number): Promise<Item> {
    return await this.itemService.getItemForId(partNoId).toPromise().then(
      (item: Item) => {
        console.debug('getItemData success:', item);
        this.itemData = item;
        return Promise.resolve(item);
      },
      error => {
        console.debug('getItemData error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      });
  }

  /**
   * 納品予定情報取得処理（リストの先頭に格納されている）
   * @param orderId 発注ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getDeliveryPlanData(orderId: number): Promise<DeliveryPlan> {
    const deliveryPlanListSearchConditions = this.generateApiSearchConditions(orderId);
    return await this.deliveryPlanService.getDeliveryPlanList(deliveryPlanListSearchConditions).toPromise().then(
      deliveryPlanList => {
        console.debug('getDeliveryPlanData success:', deliveryPlanList);
        this.deliveryPlanData = deliveryPlanList['items'][0];
        this.mainForm.patchValue({
          // 納品予定がない場合は、nullをセット
          id: [this.deliveryPlanData == null ? null : this.deliveryPlanData.id],
        });
        return Promise.resolve(this.deliveryPlanData);
      },
      error => {
        console.debug('getDeliveryPlanData error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * API検索用Modelを設定する。
   * @param orderId 発注ID
   */
  private generateApiSearchConditions(orderId: number): DeliveryPlanSearchConditions {
    const deliveryPlanListSearchConditions = new DeliveryPlanSearchConditions(); // 納品予定一覧検索条件
    deliveryPlanListSearchConditions.orderId = orderId;
    return deliveryPlanListSearchConditions;
  }

  /**
   * 納品予定裁断FormArray設定処理。
   * @param order Order
   * @param deliveryPlan DeliveryPlan
   */
  private setDeliveryPlanCutsFormArray(order: Order, deliveryPlan?: DeliveryPlan): void {
    const orderSkuFormArray = new FormArray([]);
    const orderSkuList: OrderSku[] = order.orderSkus;

    // 発注SKUごとに作成
    orderSkuList.forEach(orderSku => {
      // 納品予定裁断FormGroup設定
      const fg = this.generateDeliveryPlanCutFormGroup(orderSku, deliveryPlan);
      orderSkuFormArray.push(fg);
    });
    this.mainForm.setControl('deliveryPlanCuts', orderSkuFormArray);
  }

  /**
   * 納品予定裁断FormGroup作成処理。
   * @param orderSku OrderSku
   * @param deliveryPlan DeliveryPlan
   * @return FormGroup
   */
  private generateDeliveryPlanCutFormGroup(orderSku: OrderSku, deliveryPlan?: DeliveryPlan): FormGroup {
    const extractData = this.extractIdAndCutLot(orderSku, deliveryPlan);

    return this.formBuilder.group({
      id: [extractData.deliveryPlanCutId],  // 納品予定裁断ID
      deliveryPlanId: [deliveryPlan == null ? null : deliveryPlan.id],  // 納品予定ID
      colorCode: [orderSku.colorCode],  // 色コード
      size: [orderSku.size],  // サイズ
      productOrderLot: [orderSku.productOrderLot],  // 発注数
      deliveryPlanCutLot: [extractData.cutLot] // 生産数(裁断数)
    });
  }

  /**
   * 納品予定裁断idと生産数の取得処理。
   * 納品予定情報がない場合、idはnull、生産数は発注SKUの製品発注数を返す。
   * ある場合、生産数は納品予定裁断の裁断数を返す。
   * @param orderSku 処理中の発注sku
   * @param deliveryPlan DeliveryPlan
   * @return 納品予定裁断id,生産数
   */
  private extractIdAndCutLot(orderSku: OrderSku, deliveryPlan?: DeliveryPlan): { deliveryPlanCutId: number, cutLot: number } {
    if (deliveryPlan == null) {
      // 発注SKUの製品発注数を返す
      return { deliveryPlanCutId: null, cutLot: orderSku.productOrderLot };
    }
    let deliveryPlanCutId = null;
    let cutLot: number;
    const deliveryPlanCuts = deliveryPlan.deliveryPlanCuts;
    deliveryPlanCuts.some(deliveryPlanCut => {
      if (orderSku.colorCode === deliveryPlanCut.colorCode
        && orderSku.size === deliveryPlanCut.size) {
        deliveryPlanCutId = deliveryPlanCut.id;
        // 納品予定裁断の裁断数を返す
        cutLot = deliveryPlanCut.deliveryPlanCutLot;
        return true;
      }
    });
    return { deliveryPlanCutId: deliveryPlanCutId, cutLot: cutLot };
  }

  /**
   * 納品予定明細FormArray設定処理。
   * @param order Order
   * @param deliveryPlan DeliveryPlan
   */
  private setDeliveryPlanDetailFormArray(order: Order, deliveryPlan?: DeliveryPlan): void {
    const deliveryPlanDetailsFormArray = new FormArray([]);
    const orderSkuList: OrderSku[] = order.orderSkus;

    // 納品予定明細ごとに作成
    let deliveryPlanDetails: DeliveryPlanDetail[];
    if (deliveryPlan != null && (deliveryPlanDetails = deliveryPlan.deliveryPlanDetails) != null) {
      deliveryPlanDetails.forEach(deliveryPlanDetail => {
        const existsDataFg = this.generateDeliveryPlanDetailFormGroup(orderSkuList, deliveryPlanDetail);
        deliveryPlanDetailsFormArray.push(existsDataFg);
      });
    }

    // デフォルトの納品予定明細件数未満であれば、空の明細グループを列末に追加
    const detailLength = deliveryPlanDetailsFormArray.length;
    for (let idx = detailLength; idx < Const.DELIVERY_PLAN_DETAILS_DEFAULT_CNT; idx++) {
      const blankDataFg = this.generateDeliveryPlanDetailFormGroup(orderSkuList);
      deliveryPlanDetailsFormArray.insert(idx, blankDataFg);
    }

    this.mainForm.setControl('deliveryPlanDetails', deliveryPlanDetailsFormArray);
  }

  /**
   * 納品予定明細FormGroup作成処理。
   * @param orderSkuList OrderSku[]
   * @param deliveryPlanDetail DeliveryPlanDetail
   * @return FormGroup
   */
  private generateDeliveryPlanDetailFormGroup(orderSkuList: OrderSku[], deliveryPlanDetail?: DeliveryPlanDetail): FormGroup {
    const deliveryPlanSkusFormArray = this.generateDeliveryPlanSkusFormArray(orderSkuList, deliveryPlanDetail);
    // 納品予定明細FormGroup設定
    return this.formBuilder.group({
      id: [deliveryPlanDetail == null ? null : deliveryPlanDetail.id],  // 納品予定明細ID
      deliveryPlanId: [deliveryPlanDetail == null ? null : deliveryPlanDetail.deliveryPlanId],  // 納品予定ID
      deliveryPlanAt: [deliveryPlanDetail == null ? null : deliveryPlanDetail.deliveryPlanAt], // 納品予定日
      deliveryPlanSkus: deliveryPlanSkusFormArray, // 納品予定数FormArray
    });
  }

  /**
   * 納品予定明細単位で全発注Skuごとの納品予定SkuFormArrayを作成する。
   * @param orderSkuList OrderSku[]
   * @param deliveryPlanSkus DeliveryPlanSku[]
   */
  private generateDeliveryPlanSkusFormArray(orderSkuList: OrderSku[], deliveryPlanDetail?: DeliveryPlanDetail): FormArray {
    const deliveryPlanSkusFormArray = new FormArray([]);
    // 発注Skuごとに作成
    orderSkuList.forEach(orderSku => {
      let deliveryPlanLot = null;
      let id = null;
      let deliveryPlanId = null;
      let deliveryPlanDetailId = null;
      // 納品予定数取得
      let deliveryPlanSkus: DeliveryPlanSku[];
      if (deliveryPlanDetail != null && (deliveryPlanSkus = deliveryPlanDetail.deliveryPlanSkus) != null) {
        deliveryPlanSkus.some(deliveryPlanSku => {
          if (orderSku.colorCode === deliveryPlanSku.colorCode && orderSku.size === deliveryPlanSku.size) {
            id = deliveryPlanSku.id;
            deliveryPlanId = deliveryPlanSku.deliveryPlanId;
            deliveryPlanDetailId = deliveryPlanSku.deliveryPlanDetailId;
            deliveryPlanLot = deliveryPlanSku.deliveryPlanLot;
            return true;
          }
        });
      }

      const deliveryPlanSkuFg = this.formBuilder.group({
        id: id, // 納品予定SkuID
        deliveryPlanId: deliveryPlanId, // 納品予定ID
        deliveryPlanDetailId: deliveryPlanDetailId, // 納品予定明細ID
        colorCode: orderSku.colorCode,  // 色コード
        size: orderSku.size,  // サイズ
        deliveryPlanLot: deliveryPlanLot  // 納品予定数
      });
      deliveryPlanSkusFormArray.push(deliveryPlanSkuFg);
    });
    return deliveryPlanSkusFormArray;
  }

  /**
   * APIエラー処理
   * @param error エラー情報
   */
  private handleApiError(error: any): void {
    console.debug('API Request error:', error);
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      let errorCode = '';
      if (apiError.viewErrors == null || apiError.viewErrors[0].viewErrorMessageCode == null) {
        // viewErrorMessageCodeがある(＝errorsがない、またはerrrosに表示用コードが設定されていない)
        errorCode = apiError.viewErrorMessageCode;
      } else {
        errorCode = apiError.viewErrors[0].viewErrorMessageCode;
      }
      ExceptionUtils.displayErrorInfo('apiErrorInfo', errorCode);
    }
  }

  /**
   * 生産数を算出する。
   * カラーコード、サイズコードが指定されていない場合は、生産数の合計を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 生産数を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 生産数
   */
  calcDeliveryPlanCutLot(colorCode?: string, sizeCode?: string): number {
    let deliveryPlanCutLot = 0;
    const deliveryPlanCuts = this.mainForm.get('deliveryPlanCuts') as FormArray;

    if (deliveryPlanCuts.value == null) {
      return deliveryPlanCutLot;
    }
    deliveryPlanCutLot = this.deliveryPlanService.calcDeliveryPlanCutLot(deliveryPlanCuts.getRawValue(), colorCode, sizeCode);

    return deliveryPlanCutLot;
  }

  /**
   * 生産率を算出する。
   * カラーコード、サイズコードが指定されていない場合は、発注合計に対する生産数合計の率を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 発注数に対する生産数の率を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 生産数
   */
  calcDeliveryPlanCutRate(colorCode?: string, sizeCode?: string): number {
    let rate = 0;   // 生産率
    const deliveryPlanCuts = this.mainForm.get('deliveryPlanCuts') as FormArray;

    if (deliveryPlanCuts.value == null) {
      return rate;
    }
    rate = this.deliveryPlanService.calcDeliveryPlanCutRate(this.orderData, deliveryPlanCuts.getRawValue(), colorCode, sizeCode);

    if (StringUtils.isEmpty(colorCode) && StringUtils.isEmpty(sizeCode)) {
      this.mainForm.patchValue({ deliveryPlanCutRate: rate });
    }
    return rate;
  }

  /**
   * 納品済数の算出を行う。
   * カラーコード、サイズコードが指定されていない場合は、納品済数合を返却し、
   * カラーコード、サイズコードが指定されている場合は、カラーコード、サイズコードの
   * 納品済数合計を返却する
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 納品済数
   */
  calcCompletedDelivery(colorCode?: string, sizeCode?: string): number {
    let completedDeliveryLot = 0;

    // 納品SKUリストまたは発注情報が取得出来ていない場合は計算しない。
    if (this.deliverySkuList == null || this.orderData == null) {
      return completedDeliveryLot;
    }
    completedDeliveryLot =
      this.deliveryPlanService.calcCompletedDelivery(this.deliverySkuList, this.orderData.orderSkus,
        this.arrivalDeliveryDetailIds, colorCode, sizeCode);

    return completedDeliveryLot;
  }

  /**
   * 納品残数の算出を行う。
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 納品残数を返却する。
   * カラーコード、サイズが指定されていない場合は、納品残数合計を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @returns 納品残数
   */
  calcRemainingDeliveryPlanLot(colorCode?: string, sizeCode?: string): number {
    let remainingDeliveryPlanLot = 0;
    const deliveryPlanCuts = this.mainForm.get('deliveryPlanCuts') as FormArray;
    const deliveryPlanDetails = this.mainForm.get('deliveryPlanDetails') as FormArray;

    // 納品予定裁断数または納品予定明細または発注情報が取得出来ていない場合は計算しない。
    if (deliveryPlanCuts.value == null || deliveryPlanDetails.value == null || this.orderData == null) {
      return remainingDeliveryPlanLot;
    }
    remainingDeliveryPlanLot = this.deliveryPlanService.calcRemainingDeliveryPlanLot(
      deliveryPlanDetails.getRawValue(), this.deliverySkuList, this.orderData.orderSkus,
      this.arrivalDeliveryDetailIds, colorCode, sizeCode);

    return remainingDeliveryPlanLot;
  }

  /**
   * 納品可能数の算出を行う。
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 納品残数を返却する。
   * カラーコード、サイズが指定されていない場合は、納品残数合計を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 納品可能数
   */
  calcDeliverableDeliveryPlanLot(colorCode?: string, sizeCode?: string): number {
    // 計算式は納品残数と同じなので、calcRemainingDeliveryPlanLotを呼ぶ。
    return this.calcRemainingDeliveryPlanLot(colorCode, sizeCode);
  }

  /**
   * 増減産数を算出
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 増減産数を返却する。
   * カラーコード、サイズが指定されていない場合は、増減産数合計を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 増減産数
   */
  calcIncreaseOrDecreaseLot(colorCode?: string, sizeCode?: string): number {
    let increaseOrDecreaseLot = 0;
    const deliveryPlanDetails = this.mainForm.get('deliveryPlanDetails') as FormArray;

    // 発注情報または納品明細が取得出来ていない場合は計算しない。
    if (this.orderData == null || deliveryPlanDetails.value == null) {
      return increaseOrDecreaseLot;
    }
    increaseOrDecreaseLot = this.deliveryPlanService.calcIncreaseOrDecreaseLot(
      deliveryPlanDetails.getRawValue(), this.orderData, colorCode, sizeCode);

    return increaseOrDecreaseLot;
  }

  /**
   * 増減産率を算出
   * カラーコード、サイズが指定されている場合は、指定されたカラーコード・サイズの
   * 発注数に対する増減産数の率を返却する。
   * カラーコード、サイズが指定されていない場合は、発注数合計に対する増減産数合計の率を返却する。
   *
   * @param colorCode カラーコード
   * @param sizeCode サイズコード
   * @retruns 増減産数
   */
  calcIncreaseOrDecreaseRate(colorCode?: string, sizeCode?: string): number {
    let rate = 0;
    const deliveryPlanDetails = this.mainForm.get('deliveryPlanDetails') as FormArray;

    // 発注情報または納品明細が取得出来ていない場合は計算しない。
    if (this.orderData == null || deliveryPlanDetails.value == null) {
      return rate;
    }
    rate = this.deliveryPlanService.calcIncreaseOrDecreaseRate(deliveryPlanDetails.getRawValue(), this.orderData, colorCode, sizeCode);

    if (StringUtils.isEmpty(colorCode) && StringUtils.isEmpty(sizeCode)) {
      this.mainForm.patchValue({ increaseOrDecreaseLotRate: rate });
    }
    return rate;
  }
}
