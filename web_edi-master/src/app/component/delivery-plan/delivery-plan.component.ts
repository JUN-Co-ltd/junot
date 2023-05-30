import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Location } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators, AbstractControl } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { ExceptionUtils } from '../../util/exception-utils';
import { StringUtils } from '../../util/string-utils';
import { DateUtils } from '../../util/date-utils';
import { AuthUtils } from 'src/app/util/auth-utils';
import { Const, Path, SubmitType, PreEventParam, EntryStatus, AuthType } from '../../const/const';

import { Item } from '../../model/item';
import { OrderSku } from '../../model/order-sku';
import { Order } from '../../model/order';
import { DeliveryPlan } from '../../model/delivery-plan';
import { DeliveryPlanSku } from '../../model/delivery-plan-sku';
import { DeliveryPlanDetail } from '../../model/delivery-plan-detail';
import { ThresholdSearchConditions, DeliveryRequestSearchConditions } from '../../model/search-conditions';
import { DeliverySku } from '../../model/delivery-sku';
import { Session } from 'src/app/model/session';

import { HeaderService } from '../../service/header.service';
import { ItemService } from '../../service/item.service';
import { OrderService } from '../../service/order.service';
import { ThresholdService } from '../../service/threshold.service';
import { DeliveryPlanService } from '../../service/delivery-plan.service';
import { LoadingService } from '../../service/loading.service';
import { DeliveryRequestService } from '../../service/delivery-request.service';
import { SessionService } from '../../service/session.service';

import {
  DeliveryPlanSubmitConfirmModalComponent
} from './modal/delivery-plan-submit-confirm-modal/delivery-plan-submit-confirm-modal.component';

import {
  deliveryPlanCutRateValidator, increaseOrDecreaseLotRateValidator, allDeliveryPlanLotRequiredValidator, deliveryPlanLotRequiredValidator
} from './validator/delivery-plan-validator.directive';

@Component({
  selector: 'app-delivery-plan',
  templateUrl: './delivery-plan.component.html',
  styleUrls: ['./delivery-plan.component.scss']
})
export class DeliveryPlanComponent implements OnInit {
  // htmlから参照したい定数を定義
  readonly PATH = Path;
  readonly SUBMIT_TYPE = SubmitType;
  readonly ENTRY_STATUS = EntryStatus;
  readonly Math = Math;           // 絶対値計算用(htmlから参照用)

  itemData: Item;                 // 品番情報
  orderData: Order;               // 発注情報
  deliveryPlanData: DeliveryPlan; // 納品予定情報
  private deliverySkuList: DeliverySku[] = [];  // 納品依頼SKUリスト
  private arrivalDeliveryDetailIds: number[] = []; // 仕入れ確定済みの納品明細IDリスト(納品済数計算時に使用)

  entryStatus: number;            // 登録済ステータス(モーダルでチェックした瞬間に画面表示してしまう為、formとは別にクラス変数で用意する)

  path = '';                      // new,view,edit,delete
  private session: Session;
  private affiliation: AuthType;  // ログイン権限

  overall_susses_msg_code = ''; // 正常系のメッセージコード
  overall_error_msg_code = '';  // エラーメッセージ用

  mainForm: FormGroup;          // メインのフォーム
  submitted = false;            // submitボタン押下したか
  isBtnLock = false;            // 登録処理中にボタンをロックするためのフラグ
  isShowFooter = false;         // フッター表示フラグ
  addErrorMessage = '';         // 納品予定明細追加最大件数超えエラーメッセージ
  private submitType: SubmitType;

  // PRD_0145 #10776 add JFE start
  /** 関連No */
  relationNumber: string;
  // PRD_0145 #10776 add JFE end
  // PRD_0191 add JFE start
  /** 費目 */
  expenseItem: string;
  // PRD_0191 add JFE end

  constructor(
    private translate: TranslateService,
    private headerService: HeaderService,
    private modalService: NgbModal,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    private itemService: ItemService,
    private orderService: OrderService,
    private thresholdService: ThresholdService,
    private deliveryPlanService: DeliveryPlanService,
    private deliveryRequestService: DeliveryRequestService,
    private formBuilder: FormBuilder,
    private loadingService: LoadingService,
    private sessionService: SessionService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();
    this.affiliation = this.session.affiliation;  // ログインユーザの権限を取得する
    this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
    // フッター表示条件: ROLE_EDIまたはROLE_MAKER
    this.isShowFooter = AuthUtils.isEdi(this.session) || AuthUtils.isMaker(this.session);

    this.initFromGroup();
    this.translate.get('ERRORS.VALIDATE.NO_MORE_ADD').subscribe((value: string) => {
      this.addErrorMessage = value;
    });
    if (this.path === Path.NEW) {
      const orderId = Number(this.route.snapshot.queryParamMap.get('orderId'));
      this.setInitData(orderId);
    } else {
      const deliveryPlanId = Number(this.route.snapshot.params['id']);
      const preEvent = Number(this.route.snapshot.queryParamMap.get('preEvent'));
      // 新規登録直後の遷移の場合
      if (preEvent === PreEventParam.CREATE) {
        this.location.replaceState((
          this.router.serializeUrl(this.router.createUrlTree(['/deliveryPlans', deliveryPlanId, Path.EDIT]))
        ));
        this.overall_susses_msg_code = 'SUCSESS.DELIVERY_PLAN_ENTRY';  // 新規登録後のメッセージ設定
      }
      this.getDeliveryPlanData(deliveryPlanId).then(deliveryPlanData => {
        this.setInitData(deliveryPlanData.orderId, deliveryPlanData);
      });
    }
  }

  /**
   * 初期表示で必要なデータ取得して設定する。
   * @param orderId 発注ID
   * @param deliveryPlanData 納品予定情報
   */
  private setInitData(orderId: number, deliveryPlanData?: DeliveryPlan): void {
    this.getOrderData(orderId).then(orderData => {
      this.getDeliverySkuList(orderId).then(() => {
        this.getItemData(orderData.partNoId).then(itemData => {
          this.getThresholdData(itemData.brandCode).then(() => {
            this.setDeliveryPlanCutsFormArray(orderData, deliveryPlanData);
            this.setDeliveryPlanDetailFormArray(orderData, deliveryPlanData);
            // PRD_0145 #10776 add JFE start
            this.setRelationNumber(orderData);
            // PRD_0145 #10776 add JFE end
            // PRD_0191 add JFE start
            this.setExpenseItem(orderData);
            // PRD_0191 add JFE end
          });
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
        // PRD_0145 #10776 add JFE start
        necessaryLengthActual: [0, [Validators.pattern(/^([1-9][0-9]{0,5}|0)(\.[0-9]{1,2})?$/)]],   // 実用尺
        // PRD_0145 #10776 add JFE end
        // PRD_0191 add JFE start
        expenseItem: [null],              // 費目
        // PRD_0191 add JFE end
        partNoId: [null],                 // 品番ID
        entryStatus: [null],              // 登録済ステータス
        memo: [null],                     // メモ
        threshold: [null],                // 閾値(バリデーションで必要)
        deliveryPlanDetails: [null],      // 納品予定明細FormArray
        deliveryPlanCuts: [null],         // 納品予定裁断FormArray
        deliveryPlanCutRate: [null],      // 生産率(バリデーションで必要)
        increaseOrDecreaseLotRate: [null] // 増減産率(バリデーションで必要)
      }, {
        validator: Validators.compose(
          [
            deliveryPlanCutRateValidator, increaseOrDecreaseLotRateValidator,
            allDeliveryPlanLotRequiredValidator, deliveryPlanLotRequiredValidator
          ]
        )
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
          // PRD_0145 #10776 add JFE start
          // PRD_0192 mod JFE start
          //necessaryLengthActual: this.orderData.necessaryLengthActual,
          necessaryLengthActual: this.floor(this.orderData.necessaryLengthActual),
          // PRD_0192 mod JFE end
          // PRD_0145 #10776 add JFE end
          // PRD_0191 add JFE start
          expenseItem: this.orderData.expenseItem,
          // PRD_0191 add JFE end
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
   * 納品依頼情報を取得して変数に設定する。
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
   * 閾値情報取得処理
   * @param brandCode ブランドコード
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getThresholdData(brandCode: string): Promise<number> {
    return await this.thresholdService.getThresholdByBrandCode({ brandCode: brandCode } as ThresholdSearchConditions).toPromise().then(
      thresholdList => {
        console.debug('getThresholdData success:', thresholdList);
        const threshold = thresholdList['items'][0].threshold;
        // 百分率に変換して保存
        this.mainForm.patchValue({ threshold: threshold * 100 });
        return Promise.resolve(threshold);
      },
      error => {
        console.debug('getThresholdData error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * 納品予定情報取得処理
   * @param deliveryPlanId 納品予定ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getDeliveryPlanData(deliveryPlanId: number): Promise<DeliveryPlan> {
    return await this.deliveryPlanService.getDeliveryPlanById(deliveryPlanId).toPromise().then(
      deliveryPlan => {
        console.debug('getDeliveryPlanData success:', deliveryPlan);
        this.deliveryPlanData = deliveryPlan;
        this.entryStatus = deliveryPlan.entryStatus;
        this.mainForm.patchValue({
          id: deliveryPlan.id,
          entryStatus: deliveryPlan.entryStatus,
          memo: deliveryPlan.memo,
        });
        return Promise.resolve(deliveryPlan);
      },
      error => {
        console.debug('getDeliveryPlanData error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  // PRD_0145 #10776 add JFE start
   /**
   * 関連番号設定処理。
   * @param order Order
   */
  private setRelationNumber(order: Order): void {
    if (order.relationNumber != 0 && order.relationNumber != null) {
      this.relationNumber = order.relationNumber.toString().padStart(6, '0');
    } else if (order.relationNumber === 0) {
      this.relationNumber = '0';
    } else {
      this.relationNumber = null;
    }
  }
  // PRD_0145 #10776 add JFE end

  // PRD_0191 add JFE start
   /**
   * 費目設定処理。
   * @param order Order
   */
  private setExpenseItem(order: Order): void {
    this.expenseItem = order.expenseItem;
  }
  // PRD_0191 add JFE end

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
      deliveryPlanCutLot: [ // 生産数(裁断数)
        {
          // PRD_0145 #10776 mod JFE start
          //value: extractData.cutLot,
          value: this.path === Path.NEW && this.orderData.expenseItem === '04' ? null : extractData.cutLot,
          // PRD_0145 #10776 mod JFE end
          disabled: this.affiliation === AuthType.AUTH_SUPPLIERS
            && this.deliverySkuList.length > 0 // ログインユーザがメーカーの場合、納品依頼登録済であれば変更不可
        },
        [
          Validators.required,
          Validators.pattern(/^([0]|[1-9][0-9]*)$/) // 負数または先頭0不可※0のみは可
        ]
      ],
    });
  }

  /**
   * 納品予定裁断idと生産数の取得処理。
   * 新規の場合(納品予定情報がない場合)、idはnull、生産数は発注SKUの製品発注数を返す。
   * 編集の場合、生産数は納品予定裁断の裁断数を返す。
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
    if (Const.DELIVERY_PLAN_DETAILS_MAX_CNT > deliveryPlanDetailsFormArray.length) {
      // 納品予定明細の件数が最大件数未満であれば末尾に空の明細FormGroup設定
      const defaultBlankDataFg = this.generateDeliveryPlanDetailFormGroup(orderSkuList);
      deliveryPlanDetailsFormArray.push(defaultBlankDataFg);
    }
    // 納品予定明細の件数がデフォルトの納品予定明細件数未満であれば末尾に空の明細グループ追加
    const detailLength = deliveryPlanDetailsFormArray.length;
    for (let idx = detailLength; idx < Const.DELIVERY_PLAN_DETAILS_DEFAULT_CNT; idx++) {
      const blankDataFg = this.generateDeliveryPlanDetailFormGroup(orderSkuList);
      deliveryPlanDetailsFormArray.push(blankDataFg);
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
    const deliveryPlanDetailFormGroup = this.formBuilder.group({
      id: [deliveryPlanDetail == null ? null : deliveryPlanDetail.id],  // 納品予定明細ID
      deliveryPlanId: [deliveryPlanDetail == null ? null : deliveryPlanDetail.deliveryPlanId],  // 納品予定ID
      deliveryPlanAt: [ // 納品予定日
        deliveryPlanDetail == null ? null : deliveryPlanDetail.deliveryPlanAt,
        [Validators.pattern(/^[1-2][0-9]{3}\/[0-9]{2}\/[0-9]{2}$/)] // yyyy/MM/dd
      ],
      deliveryPlanSkus: deliveryPlanSkusFormArray, // 納品予定数FormArray
    });

    // 過去日の納品明細は編集不可にする
    if (DateUtils.isPastDate(deliveryPlanDetailFormGroup.value.deliveryPlanAt)) {
      deliveryPlanDetailFormGroup.disable();
    }

    return deliveryPlanDetailFormGroup;
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
        deliveryPlanLot: [  // 納品予定数
          deliveryPlanLot,
          [Validators.pattern(/^(|[0]|-?[1-9][0-9]{0,4})$/)] // 入力範囲「-99999～99999」。先頭0不可。※0のみは可
        ]
      });
      deliveryPlanSkusFormArray.push(deliveryPlanSkuFg);
    });
    return deliveryPlanSkusFormArray;
  }

  /**
   * 追加ボタン押下処理
   * @param order 発注情報
   */
  onAddDetailForm(order: Order): void {
    if (Const.DELIVERY_PLAN_DETAILS_MAX_CNT <= this.fDeliveryPlanDetails.length) {
      // 最大件数オーバー
      alert(this.addErrorMessage);
      return;
    }
    // 末尾に空の明細FormGroup設定
    const defaultBlankDataFg = this.generateDeliveryPlanDetailFormGroup(order.orderSkus);
    (<FormArray> this.mainForm.get('deliveryPlanDetails')).push(defaultBlankDataFg);
  }

  /**
   * 登録処理
   */
  private submitRegister(): void {
    const requestValue = this.generateRequestFormValue(this.mainForm.getRawValue());
    this.deliveryPlanService.postDeliveryPlanRequest(requestValue).toPromise().then(
      result => {
        this.overall_susses_msg_code = 'SUCSESS.DELIVERY_PLAN_ENTRY';
        this.submitted = false;
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 登録後の画面表示。pathをeditにする
        this.router.navigate(['/deliveryPlans', result['id'], Path.EDIT],
          { queryParams: { preEvent: PreEventParam.CREATE } });
      },
      error => { this.handleApiError(error); }
    );
  }

  /**
   * 更新処理
   */
  private submitUpdate(): void {
    const requestValue = this.generateRequestFormValue(this.mainForm.getRawValue());
    this.deliveryPlanService.putDeliveryPlanRequest(requestValue).toPromise().then(
      result => {
        this.overall_susses_msg_code = 'SUCSESS.DELIVERY_PLAN_UPDATE';
        // 画面再表示
        this.submitted = false;
        this.getDeliveryPlanData(result['id']).then(deliveryPlanData => {
          this.setInitData(deliveryPlanData.orderId, deliveryPlanData);
          this.loadingService.loadEnd();
          this.isBtnLock = false;
        });
      },
      error => { this.handleApiError(error); }
    );
  }

  /**
   * APIエラー処理
   * @param error エラー情報
   */
  private handleApiError(error: any): void {
    console.debug('API Request error:', error);
    this.overall_error_msg_code = 'ERRORS.500_01';
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
    this.loadingService.loadEnd();
    if (this.submitted) { // submitted状態の場合(=登録・更新エラー)はボタン制御解除
      this.isBtnLock = false;
    } else {  // submitted状態ではない場合(=取得エラー)はform入力、ボタン制御
      this.mainForm.disable();
      this.isBtnLock = true;
    }
  }

  /**
   * 登録・更新submit時の確認モーダルを表示する。
   * 登録済ステータスが登録済であればモーダル表示しない。
   * @return Promise<{ isCancel: boolean, isFinal: boolean }>
   */
  private async openSubmitConfirmModal(): Promise<{ isCancel: boolean, isFinal: boolean }> {
    // 登録済ステータスが登録済であればモーダル表示しない
    if (this.deliveryPlanData != null
      && this.mainForm.get('entryStatus').value === EntryStatus.REGISTERED) {
      return Promise.resolve({ isCancel: false, isFinal: true });
    }
    const modalRef = this.modalService.open(DeliveryPlanSubmitConfirmModalComponent);
    // モーダルからの値を設定する。
    return await modalRef.result
      .then((result: boolean) => {  // result:全予定登録済として登録するか
        return { isCancel: false, isFinal: result };
      }, () => { // キャンセル。処理しない
        return { isCancel: true, isFinal: false };
      })
      .catch(() => { // エラー。処理しない
        return { isCancel: true, isFinal: false };
      });
  }

  /**
   * リクエスト用のデータを作成する。
   * @param formValue フォームの値
   * @return requestValue
   */
  private generateRequestFormValue(formValue: any): any {
    const requestValue = JSON.parse(JSON.stringify(formValue));
    const filteredDeliveryPlanDetailList = [];
    requestValue.deliveryPlanDetails.forEach((deliveryPlanDetail: DeliveryPlanDetail) => {
      // 入力がある納品予定Skuのみ抽出する
      const filteredSkuList = deliveryPlanDetail.deliveryPlanSkus.filter(deliveryPlanSku => deliveryPlanSku.deliveryPlanLot != null);
      // 1つでも納品予定数の入力がある納品明細をリストに格納する
      if (filteredSkuList.length > 0) {
        deliveryPlanDetail.deliveryPlanSkus = filteredSkuList;
        filteredDeliveryPlanDetailList.push(deliveryPlanDetail);
      }
    });
    requestValue.deliveryPlanDetails = filteredDeliveryPlanDetailList.length > 0 ?
      filteredDeliveryPlanDetailList : null;
    return requestValue;
  }

  /**
   * submitボタン押下時の処理
   */
  onSubmit(): void {
    this.submitted = true;
    this.overall_susses_msg_code = '';
    this.overall_error_msg_code = '';
    ExceptionUtils.clearErrorInfo();    // カスタムエラーメッセージクリア
    if (this.mainForm.invalid) {
      console.debug('バリデーションエラー:', this.mainForm);
      this.overall_error_msg_code = 'ERRORS.VALID_ERROR';
      this.isBtnLock = false;
      this.loadingService.loadEnd();
      return;
    }
    // モーダル表示
    this.openSubmitConfirmModal().then(userSelect => {
      // キャンセルであれば処理しない
      if (userSelect.isCancel) { return; }
      this.isBtnLock = true;
      this.loadingService.loadStart();
      // 登録済とするかのチェック状態をformに設定
      this.mainForm.patchValue({
        entryStatus: userSelect.isFinal ? EntryStatus.REGISTERED : EntryStatus.UNREGISTERED
      });
      switch (this.submitType) {
        case SubmitType.ENTRY:  // 登録
          this.submitRegister();
          break;
        case SubmitType.UPDATE: // 更新
          this.submitUpdate();
          break;
        default:
          break;
      }
    });
  }

  /**
   * 納品予定日フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param xIdx 明細Index
   */
  onBlurDeliveryPlanAt(maskedValue: string, xIdx: number): void {
    // patchValueしないとpipeの変換値がformにセットされない
    this.fDeliveryPlanDetails[xIdx].get('deliveryPlanAt').patchValue(maskedValue);
  }

  /**
   * 納品予定明細のエラーメッセージを返す。
   * @returns エラーメッセージリスト
   */
  get deliveryPlanDetailErrorCodeList(): string[] {
    let isDeliveryPlanAtError = false;
    let isDeliveryPlanSkusError = false;
    this.fDeliveryPlanDetails.some(fDeliveryPlanDetail => {
      if (!fDeliveryPlanDetail.invalid) { return false; } // エラーなし。次の明細チェック
      if (fDeliveryPlanDetail.get('deliveryPlanAt').invalid) { isDeliveryPlanAtError = true; }
      if (fDeliveryPlanDetail.get('deliveryPlanSkus').invalid) { isDeliveryPlanSkusError = true; }
    });
    const codeList = [];
    if (isDeliveryPlanAtError) { codeList.push('ERRORS.VALIDATE.DELIVERY_PLAN_LOT_DATE_FORMAT'); }
    if (isDeliveryPlanSkusError) { codeList.push('ERRORS.VALIDATE.DELIVERY_PLAN_LOT_PATTERN_NUMBER'); }
    return codeList;
  }

  /**
   * 生産数のエラーメッセージを返す。
   * @returns エラーメッセージリスト
   */
  get deliveryPlanCutErrorCodeList(): any[] {
    let isRequired = false;
    let isPattern = false;
    this.fDeliveryPlanCuts.forEach(fDeliveryPlanCut => {
      const errors = fDeliveryPlanCut.get('deliveryPlanCutLot').errors;
      if (errors == null) { return false; } // エラーなし。次の生産数チェック
      if (errors.required) { isRequired = true; }
      if (errors.pattern) { isPattern = true; }
    });
    const codeList = [];
    if (isRequired) { codeList.push('ERRORS.VALIDATE.CUT_LOT_EMPTY'); }
    if (isPattern) { codeList.push('ERRORS.VALIDATE.CUT_LOT_PATTERN_NUMBER'); }
    return codeList;
  }

  /**
   * SubmitTypeを設定する
   * @param submitType
   */
  setSubmitType(submitType: SubmitType): void {
    this.submitType = submitType;
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
    // カラーコードとサイズコードの指定がない場合は、合計の生産率を書き換える
    if (StringUtils.isEmpty(colorCode) && StringUtils.isEmpty(sizeCode)) {
      this.mainForm.patchValue({ deliveryPlanCutRate: rate });
    }
    return rate;
  }

  /**
   * 納品済数の算出を行う。
   * カラーコード、サイズコードが指定されていない場合は、納品済数合計を返却し、
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
    // カラーコードとサイズの指定がない場合は、合計の増減産率を書き換える
    if (StringUtils.isEmpty(colorCode) && StringUtils.isEmpty(sizeCode)) {
      this.mainForm.patchValue({ increaseOrDecreaseLotRate: rate });
    }
    return rate;
  }

  // PRD_0192 add JFE start
  /**
   * 実用尺小数第三位以下切り捨て
   * @param necessaryLengthActual 実用尺切り捨て前
   * @retruns 実用尺切り捨て後
   */
  floor(necessaryLengthActual: number): number {
    return Math.floor(necessaryLengthActual * 100) / 100;
  }
  // PRD_0192 add JFE end
}
