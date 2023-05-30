import { Component, OnInit, OnDestroy } from '@angular/core';
// PRD_0044 del SIT start
// 未使用のためコメントアウト
//import { d, L } from '@angular/core/src/render3';
//import { detachProjectedView } from '@angular/core/src/view/view_attach';
// PRD_0044 del SIT end
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, AbstractControl, Validators, ValidationErrors, FormControl } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';

import { Observable, of, combineLatest, Subscription, forkJoin, from } from 'rxjs';
import { map, catchError, tap, flatMap, filter, finalize, reduce } from 'rxjs/operators';

// PRD_0031 add SIT start
import * as iconv from 'iconv-lite';
// PRD_0031 add SIT end

import {
  AuthType, DeliveryApprovalStatus, Path, PreEventParam,
  //PRD_0044 mod SIT start
  //LastDeliveryStatus, ValidatorsPattern, SpecialtyQubeCancelStatusType,
  //SubmitType, CarryType, OrderApprovalStatus
  LastDeliveryStatus, ValidatorsPattern, SubmitType, CarryType, OrderApprovalStatus
  //PRD_0044 mod SIT end
  //PRD_0123 #7054 add JFE start
  ,AllocationCode
  //PRD_0123 #7054 add JFE end
} from '../../const/const';
// PRD_0031 add SIT start
import { DistributeCsvColumn } from 'src/app/const/distributeCsv';
// PRD_0031 add SIT end

import { ExceptionUtils } from '../../util/exception-utils';
import { BusinessCheckUtils } from '../../util/business-check-utils';
import { StringUtils } from '../../util/string-utils';
import { ListUtils } from '../../util/list-utils';
import { NumberUtils } from '../../util/number-utils';
import { ObjectUtils } from '../../util/object-utils';
import { AuthUtils } from 'src/app/util/auth-utils';
import { FormUtils } from 'src/app/util/form-utils';
// PRD_0031 add SIT start
import { DateUtils } from 'src/app/util/date-utils';
// PRD_0031 add SIT end

import { DeliverySubmitConfirmModalComponent } from '../delivery-submit-confirm-modal/delivery-submit-confirm-modal.component';
import { MessageConfirmModalComponent } from '../message-confirm-modal/message-confirm-modal.component';

import { DeliveryRequestValidatorDirective } from '../delivery-request/validator/delivery-request-validator.directive';
import { deliveryAtLotValidator } from '../derivery-store/validator/derivery-store-validator.directive';

import { NewBaseRecordGet } from './bo/new-base-record-get';
import { EditOrViewBaseRecordGet } from './bo/edit-or-view-base-record-get';

import { DeliveryStorePathState } from './state/delivery-store-path-state';
import { NewState } from './state/new-state';
import { EditState } from './state/edit-state';
import { ViewState } from './state/view-state';
import { CorrectState } from './state/correct-state';

import { LoadingService } from '../../service/loading.service';
import { SessionService } from '../../service/session.service';
import { HeaderService } from '../../service/header.service';
// PRD_0044 del SIT start
//import { SpecialtyQubeService } from '../../service/specialty-qube.service';
// PRD_0044 del SIT end
import { ScreenSettingService } from '../../service/screen-setting.service';
import { DeliveryRequestService } from '../../service/delivery-request.service';
import { DeliveryService } from '../../service/bo/delivery.service';
import { ItemService } from '../../service/item.service';
import { OrderService } from '../../service/order.service';
import { DeliveryPlanService } from '../../service/delivery-plan.service';
import { MessageConfirmModalService } from '../../service/message-confirm-modal.service';
import { PurchaseHttpService } from 'src/app/service/purchase-http.service';

import { SkuDeliveryFormValue, DeliveryStoreFormValue, BaseDataOfDeliveryStoreScreen } from './interface/delivery-store-interface';

import { Item } from '../../model/item';
import { Order } from '../../model/order';
import { OrderSku } from '../../model/order-sku';
import { DeliveryPlan } from '../../model/delivery-plan';
import { Delivery } from '../../model/delivery';
import { DeliveryDetail } from '../../model/delivery-detail';
import { DeliverySku } from '../../model/delivery-sku';
import { DeliveryStore } from '../../model/delivery-store';
import { Session } from '../../model/session';
import { JunpcStoreHrtmst } from '../../model/junpc-store-hrtmst';
import { JunpcTnpmst } from '../../model/junpc-tnpmst';
import { BusinessError } from 'src/app/model/bussiness-error';
import { DeliveryVoucherFileInfo } from 'src/app/model/delivery-voucher-file-info';
// PRD_0031 add SIT start
import { ShopStock } from 'src/app/model/shop-stock';
// PRD_0120 add JFE Start
// import { DeliveryStoreUploadCsv } from '../../model/delivery-store-upload-csv';
import { DeliveryStoreSkuForm,DeliveryStoreUploadCsv ,DeliveryStoreUploadCsvStore } from '../../model/delivery-store-upload-csv';
// PRD_0120 add JFE End
// PRD_0031 add SIT end

import { Purchase } from '../purchase/interface/purchase';
// PRD_0031 add SIT start
import { DeliveryStoreSkuFormValue } from './interface/delivery-store-interface';
import { PosOrderDetail } from 'src/app/model/pos-order-detail';
// PRD_0031 add SIT end
// PRD_0123 #7054 add JFE start
import { MdeliveryLocation } from 'src/app/model/m-delivery-location';
// PRD_0123 #7054 add JFE end

@Component({
  selector: 'app-derivery-store',
  templateUrl: './derivery-store.component.html',
  styleUrls: ['./derivery-store.component.scss']
})
export class DeriveryStoreComponent implements OnInit, OnDestroy {

  /** 送信の種類定数 */
  readonly SUBMIT_TYPE = SubmitType;
  /** pathの定数 */
  readonly PATH = Path;

  /** 自動遷移 */
  private readonly AUTO_MOVE = '1';

  /** Submitメッセージディクショナリ */
  private readonly SUCSESS_MSG_DICTIONARY = {
    /** 店舗配分登録後のメッセージ */
    [PreEventParam.CREATE]: 'SUCSESS.DELIVERY_STORE_ENTRY',
    /** 店舗配分更新後のメッセージ */
    [PreEventParam.UPDATE]: 'SUCSESS.DELIVERY_STORE_UPDATE',
    /** 店舗配分削除後のメッセージ */
    [PreEventParam.DELETE]: 'SUCSESS.DELIVERY_STORE_DELETE',
    /** 納品依頼承認後のメッセージ */
    [PreEventParam.APPROVE]: 'SUCSESS.DELIVERY_ACCEPT',
    /** 店舗配分訂正後のメッセージ */
    [PreEventParam.CORRECT]: 'SUCSESS.DELIVERY_STORE_CORRECT',
    /** 直送確定後のメッセージ */
    [PreEventParam.DIRECT_CONFIRM]: 'SUCSESS.DELIVERY_DIRECT_CONFIRM',
    //PRD_0044 add SIT start
    /** 店舗配分訂正一時保存後のメッセージ */
    [PreEventParam.SAVE_CORRECT]: 'SUCSESS.DELIVERY_STORE_CORRECT_SAVE'
    //PRD_0044 add SIT end
  };

  // PRD_0044 del SIT start
  ///** SQエラーメッセージディクショナリ */
  //private readonly SQ_CANCEL_MSG_DICTIONARY = {
  //  /** SQキャンセルNGのエラーメッセージ */
  //  [SpecialtyQubeCancelStatusType.CANCEL_NG]: 'ERRORS.SQ_CANCEL_NG_ERROR',
  //  /** SQデータ無しのエラーメッセージ */
  //  [SpecialtyQubeCancelStatusType.NO_DATA]: 'ERRORS.SQ_NO_DATA_ERROR',
  //  /** SQその他エラーのエラーメッセージ */
  //  [SpecialtyQubeCancelStatusType.OTHER_ERROR]: 'ERRORS.ANY_ERROR'
  //  // ※SQロックのメッセージはログインユーザーとの合致判断後に表示する.
  //};

  /** 課別への遷移URL */
  private readonly NEXT_DIVISION_URL = 'deliveries';
  /** 店舗別への遷移URL */
  private readonly NEXT_DIVISION_STORE_URL = 'deliveryStores';

  /** 正規表現チェック: 整数2桁まで小数1桁まで、または100。負数または先頭0不可 */
  private readonly REG_EXP_INTEGER_SECOND_DECIMAL_FIRST_OR_100 = new RegExp(ValidatorsPattern.INTEGER_SECOND_DECIMAL_FIRST_OR_100);
  /** 正規表現チェック: 0以上の正数のみ */
  private readonly REG_EXP_NON_NEGATIVE_INTEGER = new RegExp(ValidatorsPattern.NON_NEGATIVE_INTEGER);

  /** ログインユーザーのセッション情報 */
  private session: Session;
  /** ユーザ権限 */
  affiliation: AuthType;

  /** Pathの状態 */
  private _pathState: DeliveryStorePathState;

  /** 画面を非表示にする */
  invisibled = true;
  /** 課別配分遷移先URL */
  private nextDivisionUrl = '';
  /** 得意先配分遷移先URL */
  private nextStoreUrl = '';

  /** 正常系のメッセージコード */
  overallSuccessMsgCode = '';
  /** エラーメッセージ用 */
  overallErrorMsgCode = '';
  /** メインのフォーム */
  mainForm: FormGroup;
  /** submitボタン押下したか */
  submitted = false;
  /** 登録処理中にボタンをロックするためのフラグ */
  isBtnLock = false;
  /** 承認ボタン押下可否フラグ */
  isApprovable = true;
  /** 優良誤認承認済フラグ */
  isQualityApproved = true;
  /** 操作ボタン表示フラグ */
  isShowEdiButton = false;

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

  // PRD_0044 del SIT start
  ///** 訂正時の警告メッセージ */
  //private alertMessageAtCorrect = '';
  // PRD_0044 del SIT end
  /** 納品依頼書発行済警告メッセージ */
  private sheetOutedAlertMessage = '';
  /** 納品数量未入力時の削除確認メッセージ */
  private noDeliveryLotConfirmMessage = '';
  /** 課別入力遷移確認メッセージ */
  private moveDeliveryRequestConfirmMessage = '';

  /** 店舗別配分率リスト */
  distributionRatioMastaList: JunpcStoreHrtmst[] = [];
  /** 品番情報 */
  itemData: Item;
  /** 発注情報 */
  orderData: Order;
  /** 納品承認ステータス */
  deliveryApproveStatus: string;
  /** 納品依頼に登録されている発注Id */
  private orderIdInDelivery: number;
  /** 納品履歴リスト */
  deliveryHistoryList: Delivery[] = [];
  /** 納品予定情報 */
  deliveryPlan: DeliveryPlan;
  /** 仕入情報 */
  purchase: Purchase;
  /** 納品明細リスト */
  deliveryDetailList: DeliveryDetail[] = [];
  // PRD_0031 add SIT start
  /** 店別在庫情報リスト */
  shopStockList: ShopStock[] = [];
  // PRD_0031 add SIT end
  // PRD_0033 add SIT start
  posOrderDetailList: PosOrderDetail[] = [];
  // PRD_0033 add SIT end
  /** 納品得意先登録済フラグ */
  existsRegistedDeliveryStore = false;
  // PRD_0044 del SIT start
  ///** SQロック中のユーザーアカウント名 */
  //sqLockUserAccountName = '';
  ///** 訂正ボタンロックフラグ */
  //isCorrectLock = false;
  // PRD_0044 del SIT end
  /** 閾値 */
  private threshold: number = null;
  /** 仕入数超過 */
  overPerchase = false;
  /** 入荷済 */
  isArrived = false;
  /** 直送フラグ */
  isDirectDelivery = false;
  /** 直送確定ボタン表示フラグ */
  isShowDirectConfirmButton = false;
  /** 納品伝票ファイルリスト */
  deliveryVoucherFileInfos: DeliveryVoucherFileInfo[] = [];
  // PRD_0123 #7054 add JFE start
  /** 納入場所リスト */
  deliveryLocationList: MdeliveryLocation[] = [];
  /** 納入場所リスト表示フラグ */
  isShowDeliveryLocationList = true;
  // PRD_0123 #7054 add JFE end

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private messageConfirmModalService: MessageConfirmModalService,
    private formBuilder: FormBuilder,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private modalService: NgbModal,
    private translate: TranslateService,
    private deliveryRequestService: DeliveryRequestService,
    private deliveryService: DeliveryService,
    // PRD_0044 del SIT start
    //private specialtyQubeService: SpecialtyQubeService,
    // PRD_0044 del SIT end
    private loadingService: LoadingService,
    private screenSettingService: ScreenSettingService,
    private itemService: ItemService,
    private orderService: OrderService,
    private deliveryPlanService: DeliveryPlanService,
    private purchaseHttpService: PurchaseHttpService
  ) {
  }

  /**
   * mainFormの項目の状態を取得する.
   * @return this.mainForm.controls
   */
  private get fCtrl(): { [key: string]: AbstractControl } {
    return this.mainForm.controls;
  }

  /**
   * mainFormの値(非活性項目含む)を取得する.
   * @return this.mainForm.getRawValue()
   */
  private get fVal(): any {
    return this.mainForm.getRawValue();
  }

  /**
   * mainFormのerrorsを取得する.
   * @return this.mainForm.errors
   */
  get fErr(): ValidationErrors {
    return this.mainForm.errors;
  }

  /**
   * mainFormのskuFormArrayを取得する.
   * @return this.mainForm.get('skuFormArray')
   */
  get skuFormArray(): FormArray {
    return this.mainForm.get('skuFormArray') as FormArray;
  }

  /**
   * mainFormのskuFormArrayの項目の状態を取得する.
   * @return this.mainForm.get('skuFormArray').controls
   */
  get fCtrlSkus(): AbstractControl[] {
    const skuFormArray = this.mainForm.get('skuFormArray') as FormArray;
    return skuFormArray == null ? null : skuFormArray.controls;
  }

  /**
   * mainFormのdeliveryStoresを取得する.
   * @return this.mainForm.get('deliveryStores')
   */
  get deliveryStoreFormArray(): FormArray {
    return this.mainForm.get('deliveryStores') as FormArray;
  }

  /**
   * mainFormの納品得意先リストの項目の状態を取得する.
   * @return this.mainForm.get('deliveryStores').controls
   */
  get fCtrlDeliveryStores(): AbstractControl[] {
    const deliveryStores = this.mainForm.get('deliveryStores') as FormArray;
    return deliveryStores == null ? null : deliveryStores.controls;
  }

  /**
   * mainFormの納品得意先リストのvalueを返す.
   * @return this.mainForm.getRawValue().deliveryStores
   */
  private get fValDeliveryStores(): DeliveryStoreFormValue[] {
    return this.fVal.deliveryStores;
  }

  /**
   * Pathを返す.
   * @returns path
   */
  get path(): string {
    return this._pathState.getPath();
  }

  /**
   * EditOrViewBaseRecordGetのインスタンスを返す.
   * @returns EditOrViewBaseRecordGet
   */
  private get editOrViewBaseRecordGet(): EditOrViewBaseRecordGet {
    return new EditOrViewBaseRecordGet(this.screenSettingService, this.itemService,
      this.orderService, this.deliveryRequestService, this.deliveryPlanService, this.purchaseHttpService);
  }

  /** PRD_0123 #7054 JFE add start
   * mainformから納品先のvalueを取得する。
   * @return mainform.get('delivery_location_id').value */
  get fVallocationid(): AbstractControl[] {
    const deliveryLocationCode = this.mainForm.get('deliveryLocationCode').value;
      return deliveryLocationCode;
  }
  //PRD_0123 #7054 JFE add end

  /**
   * pathの状態をセットする.
   * @param path path
   */
  set pathState(path: string) {
    switch (path) {
      case Path.NEW:
        const newBaseRecordGet = new NewBaseRecordGet(this.screenSettingService, this.itemService,
          this.orderService, this.deliveryRequestService, this.deliveryPlanService, this.purchaseHttpService);
        this._pathState = new NewState(this.deliveryService, newBaseRecordGet);
        return;
      case Path.EDIT:
        // PRD_0044 mod SIT start
        //this._pathState = new EditState(this.deliveryService, this.editOrViewBaseRecordGet, this.specialtyQubeService);
        this._pathState = new EditState(this.deliveryService, this.editOrViewBaseRecordGet);
        // PRD_0044 mod SIT end
        return;
      case Path.VIEW:
        this._pathState = new ViewState(this.deliveryService, this.editOrViewBaseRecordGet);
        return;
      case Path.CORRECT:
        // PRD_0044 mod SIT start
        //this._pathState = new CorrectState(this.deliveryService, this.editOrViewBaseRecordGet, this.specialtyQubeService);
        this._pathState = new CorrectState(this.deliveryService, this.editOrViewBaseRecordGet);
        // PRD_0044 mod SIT end
        return;
      default:
        return;
    }
  }

  ngOnInit() {
    this.headerService.show();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.isBtnLock = isLoading);

    // URLとクエリパラメータの変更を検知
    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap
    ]).subscribe(([paramMap, queryParamMap]) => {
      this.session = this.sessionService.getSaveSession();
      this.affiliation = this.session.affiliation;
      // フッター表示条件: ROLE_EDIまたはROLE_DISTA
      this.isShowEdiButton = AuthUtils.isEdi(this.session) || AuthUtils.isDista(this.session);

      const path: string = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      this.pathState = path;
      const orderId = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('orderId'), null);

      // PRD_0135 #10039 JFE mod start
      // const deliveryId = NumberUtils.toNumberDefaultIfEmpty(paramMap.get('id'), null);
      const pDeliveryId = NumberUtils.toNumberDefaultIfEmpty(paramMap.get('id'), null);
      const deliveryId = queryParamMap.get('bol') ? NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('deliveryId'), null) : pDeliveryId;
      // PRD_0135 #10039 JFE mod end

      const preEvent = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('preEvent'), null);
      //PRD_0123 #7054 JFE add start
      const itemID = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('id'),null);
      //PRD_0123 #7054 JFE mod end
      let loadingToken = null;
      let errorInfo = null;
      this.loadingService.start().pipe(
        tap(token => loadingToken = token),
        tap(() => this.setNextUrl(window.location.pathname)),
        tap(() => this.clearAllMessage()),
        tap(() => this.invisibled = true),
        flatMap(() => this.translateMessage()),
        tap(() => this.showPreEventMessage(preEvent)),
        filter(() => ObjectUtils.isNotNullAndNotUndefined(orderId)),
        //PRD_0123 #7054 JFE mod start
        // flatMap(() => this._pathState.getBaseRecord(orderId, deliveryId)),
        flatMap(() => this._pathState.getBaseRecord(orderId, deliveryId,itemID)),
        //PRD_0123 #7054 JFE mod end
        map((data) => {
          this.isBaseRecordValid(data);
          return data;
        }),
        map((data) => {
          this.setScreenSettingDataToField(data);
          return data;
        }),
        flatMap((data) => this.generateMainForm(data)),
        tap(() => this.isShowDirectConfirmButton = (this.isDirectDelivery && AuthUtils.isEdi(this.session) && this.purchase == null)),
        catchError(error => {
          errorInfo = error;
          return this.messageConfirmModalService.openErrorModal(error);
        }),
        finalize(() => {
          this.autoMoveAtError(errorInfo, { order: orderId, delivery: deliveryId });
          this.invisibled = ObjectUtils.isNotNullAndNotUndefined(errorInfo);
          this.loadingService.stop(loadingToken);
        })
      ).subscribe();
    });
  }

  ngOnDestroy(): void {
    this.loadingSubscription.unsubscribe();
  }

  /**
   * 遷移後のURL設定
   * @param pathname pathname
   */
  private setNextUrl(pathname: string): void {
    if (pathname.match(/delische/)) {
      this.nextDivisionUrl = 'delische/' + this.NEXT_DIVISION_URL;
      this.nextStoreUrl = 'delische/' + this.NEXT_DIVISION_STORE_URL;
    } else if (pathname.match(/deliverySearchList/)) {
      this.nextDivisionUrl = 'deliverySearchList/' + this.NEXT_DIVISION_URL;
      this.nextStoreUrl = 'deliverySearchList/' + this.NEXT_DIVISION_STORE_URL;
    } else {
      this.nextDivisionUrl = this.NEXT_DIVISION_URL;
      this.nextStoreUrl = this.NEXT_DIVISION_STORE_URL;
    }
  }

  /**
   * 定型メッセージを翻訳する.
   * @returns Observable<any>
   */
  private translateMessage(): Observable<any> {
    return forkJoin(
      // PRD_0044 del SIT start
      //this.translate.get('INFO.DELIVERY_CORRECT_ALERT_MESSAGE'),
      // PRD_0044 del SIT end
      this.translate.get('INFO.DELIVERY_CORRECT_SHEET_OUTED_COMFIRM_MESSAGE'),
      this.translate.get('INFO.NO_DELIVERY_DETAIL_AT_SUBMIT_ALERT_MESSAGE'),
      this.translate.get('INFO.MOVE_DELIVERY_REQUEST_COMFIRM_MESSAGE')
    ).pipe(tap(msg => {
      // PRD_0044 mod SIT start
      //this.alertMessageAtCorrect = msg[0];
      //this.sheetOutedAlertMessage = '<br><br><span style="color: red;">' + msg[1] + '</span>';
      //this.noDeliveryLotConfirmMessage = msg[2];
      //this.moveDeliveryRequestConfirmMessage = msg[3];
      this.sheetOutedAlertMessage = '<span style="color: red;">' + msg[0] + '</span>';
      this.noDeliveryLotConfirmMessage = msg[1];
      this.moveDeliveryRequestConfirmMessage = msg[2];
      // PRD_0044 mod SIT end
    }));
  }

  /**
   * メッセージクリア.
   */
  private clearAllMessage(): void {
    this.overallErrorMsgCode = '';    // エラーメッセージクリア
    this.overallSuccessMsgCode = '';  // 成功メッセージクリア
    ExceptionUtils.clearErrorInfo();  // カスタムエラーメッセージクリア
  }

  /**
   * 遷移前イベント結果のメッセージ表示.
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private showPreEventMessage(preEvent: number): void {
    const msg = this.SUCSESS_MSG_DICTIONARY[preEvent];
    this.overallSuccessMsgCode = msg != null ? msg : '';
  }

  /**
   * 画面表示用のデータ正当性チェック.
   * @param data 取得データ
   * @param nextUrl 遷移後URL
   * @param orderId 発注ID
   * @returns Observable<void>
   */
  private isBaseRecordValid(data: BaseDataOfDeliveryStoreScreen): Observable<void> {
    // 閾値がなければエラー
    const screenSetting = data.screenSetting;
    if (ObjectUtils.isNullOrUndefined(screenSetting.threshold)) {
      throw new BusinessError('400_D_23');
    }

    // 店舗リストがなければエラー
    if (ListUtils.isEmpty(screenSetting.tnpmstList)) {
      throw new BusinessError('400_D_24');
    }

    // 発注SKUがなければエラー
    const order = data.order;
    if (ListUtils.isEmpty(order.orderSkus)) {
      throw new BusinessError('400_03');
    }

    // MD(発注)承認済みでなければエラー
    if (order.orderApproveStatus !== OrderApprovalStatus.ACCEPT) {
      throw new BusinessError('ORDER_APPROVE_ERROR');
    }

    // 取得した納品依頼データが不正であればエラー処理して終了
    const delivery = data.delivery;
    const errorCode = this._pathState.isDeliveryDataInValid(delivery);
    if (StringUtils.isNotEmpty(errorCode)) {
      throw new BusinessError(errorCode);
    }

    // PRD_0044 del SIT start
    //const sqResponseData = data.specialtyQubeCancelResponse;
    //if (ObjectUtils.isNotNullAndNotUndefined(sqResponseData)) {
    //  if (sqResponseData.sqLockUserId != null) {
    //    // SQロックユーザーIDがある場合、別ユーザがロック中
    //    throw new BusinessError('SQ_LOCKED_BY_OTHER_USER_ERROR2', StringUtils.toStringSafe(delivery.sqLockUserAccountName), this.AUTO_MOVE);
    //  }
    //
    //  // ステータスがSQキャンセルOK以外の場合は、納品依頼参照画面へ遷移し、エラーを表示
    //  const sqCancelStatus = sqResponseData.status;
    //  if (SpecialtyQubeCancelStatusType.CANCEL_OK !== sqCancelStatus) {
    //    throw new BusinessError(this.SQ_CANCEL_MSG_DICTIONARY[Number(sqCancelStatus)], null, this.AUTO_MOVE);
    //  }
    //}
    // PRD_0044 del SIT end

    return of(null);
  }

  /**
   * エラー時に自動で遷移する.
   * @param errorInfo エラー情報
   * @param id { order: number, delivery: number }
   */
  private autoMoveAtError(errorInfo: Error, id: { order: number, delivery: number }): void {
    if (errorInfo instanceof BusinessError && errorInfo.type === this.AUTO_MOVE) {
      this.router.navigate(
        [this.nextStoreUrl, id.delivery, Path.VIEW], { queryParams: { orderId: id.order, t: new Date().valueOf() } });
    }
  }

  /**
   * 納品依頼画面基本データをメンバ変数に設定する.
   * @param data 取得データ
   */
  private setScreenSettingDataToField(data: BaseDataOfDeliveryStoreScreen): void {
    this.itemData = data.item;
    this.isQualityApproved = BusinessCheckUtils.isQualityApprovalOk(data.item); // 優良承認済判定
    this.threshold = data.screenSetting.threshold;
    this.distributionRatioMastaList = data.screenSetting.storeHrtmstList;
    this.deliveryHistoryList = data.deliveryHistory;
    this.orderData = data.order;
    this.deliveryPlan = data.deliveryPlan;
    // PRD_0031 add SIT start
    this.shopStockList = data.screenSetting.shopStockList;
    // PRD_0031 add SIT end
    // PRD_0033 add SIT start
    this.posOrderDetailList = data.screenSetting.posOrderDetailList;
    // PRD_0033 add SIT end
    // PRD_0123 #7054 add JFE start
    this.deliveryLocationList = data.screenSetting.deliveryLocationList;
    // PRD_0123 #7054 add JFE end
    const deliveryData = data.delivery;
    if (ObjectUtils.isNullOrUndefined(deliveryData)) {
      return;
    }

    this.deliveryApproveStatus = deliveryData.deliveryApproveStatus;
    this.orderIdInDelivery = deliveryData.orderId;
    this.deliveryDetailList = deliveryData.deliveryDetails;
    this.existsRegistedDeliveryStore = this.deliveryDetailList.some(dd => ListUtils.isNotEmpty(dd.deliveryStores));
    // PRD_0044 del SIT start
    //this.sqLockUserAccountName = deliveryData.sqLockUserAccountName;
    // PRD_0044 del SIT end
    this.purchase = data.purchase;
    this.deliveryVoucherFileInfos = deliveryData.deliveryVoucherFileInfos;

    // 入荷済の場合の仕入数整合性チェック
    this.isArrived = this.deliveryService.isArrived(deliveryData);
    this.overPerchase = this.isArrived
      && this.deliveryService.isOverPurchase(data.purchase, this.orderData.orderSkus, this.deliveryDetailList);
    // PRD_0044 del SIT start
    //this.isCorrectLock = this.deliveryService.isSQLock(this.session.accountName, deliveryData.sqLockUserAccountName);
    // PRD_0044 del SIT end
  }

  /**
   * 並び順で最後の配分課か判別.
   * @param stores 店舗マスタリスト
   * @param store 処理中の店舗マスタ
   * @param index index
   * @return true:最後
   */
  private isLastDivisionCode(stores: JunpcTnpmst[], store: JunpcTnpmst, index: number): boolean {
    const next = stores[index + 1];
    // 次の要素がない、または課が変わるか
    return next == null || next.hka !== store.hka;
  }

  /**
   * メインのFormGroupを設定する.
   *
   * @param data: BaseDataOfDeliveryStoreScreen
   * @returns Observable<void>
   */
  private generateMainForm(data: BaseDataOfDeliveryStoreScreen): Observable<void> {
    const orderData = data.order;
    const stores = data.screenSetting.tnpmstList;
    const deliveryData = data.delivery;
    const deliveryDetails = ObjectUtils.isNullOrUndefined(deliveryData) ? null : deliveryData.deliveryDetails;

    // SKUFormArray作成
    const skuFormArray = this.generateSkuFormArray(orderData.orderSkus, deliveryDetails);
    // 店舗FormArray作成
    const deliveryStoreFormArray = this.generateStoreFormArray(orderData, stores, deliveryDetails);

    const drv = new DeliveryRequestValidatorDirective();
    this.mainForm = this.formBuilder.group(
      {
        id: [null], // 納品依頼Id
        orderId: [orderData.id],  // 発注ID
        orderNumber: [orderData.orderNumber], // 発注No
        partNoId: [orderData.partNoId], // 品番ID
        partNo: [orderData.partNo], // 品番
        deliveryCount: [null],  // 納品依頼回数
        lastDeliveryStatus: [false], // 最終納品ステータス
        deliveryApproveStatus: [DeliveryApprovalStatus.UNAPPROVED], // 承認ステータス
        distributionRatioType: [null], // 配分率区分(画面表示用)
        nonConformingProductType: [false], // B級品区分
        nonConformingProductUnitPrice: [{ value: null, disabled: true }], // B級品単価
        faxSend: [true],  // 通知メール送信(ファックス送信フラグ)
        deliveryRequestAt: [null],  // 納品依頼日
        photoDeliveryAt: [null, drv.forbiddenSundayValidator],  // 撮影納期
        sewingDeliveryAt: [null, drv.forbiddenSundayValidator], // 縫検納期
        deliveryAt: [null, drv.forbiddenSundayValidator], // 製品納期
        fromStoreScreenFlg: true, // 店舗別登録フラグ
        skuFormArray: skuFormArray, // SKUFormArray
        deliveryStores: deliveryStoreFormArray, // 納品得意先情報リスト
        //PRD_0123 #7054 JFE mod start
        // allDirectCheckbox: [false]  // 直送チェックボックス(全体)
        allDirectCheckbox: [false],  // 直送チェックボックス(全体)
        deliveryLocationCode:this.deliveryLocationList //納品先セレクトボックス
        //PRD_0123 #7054 JFE mod end
      }, { validator: deliveryAtLotValidator }
    );
    this.setUpRelativeValidator(this.mainForm);

    if (deliveryData != null) {
      this.patchDeliveryValueToForm(deliveryData);  // 納品情報取得済の場合は値をformに設定する.
    }

    //PRD_0123 #7054 add JFE start
    // NEWでかつ、TC運用のあるブランドの場合はドロップダウンリストの初期選択
    //
    if (this.path === Path.NEW && this.deliveryLocationList.length > 0) {
      let logisticsCode: string = stores[0].logisticsCode;
      this.mainForm.patchValue({
        deliveryLocationCode: logisticsCode
      });
    }
    //訂正もしくは参照時かつ、納品明細情報.課コードが縫製検品、本社撮影の場合はリストを非表示にする
    if ((this.path === Path.CORRECT || this.path === Path.VIEW) && (deliveryDetails[0].divisionCode === AllocationCode.SEWING || deliveryDetails[0].divisionCode === AllocationCode.PHOTO)) {
      this.isShowDeliveryLocationList = false
    }
    //PRD_0123 #7054 add JFE end
    this._pathState.disableFormAtScreenInit(this.mainForm, deliveryDetails);
    this._pathState.setUpDynamicDisable(this.mainForm, deliveryDetails);

    return of(null);
  }

  /**
   * - 正規表現に一致する場合、値をnumber型に変換して返却する
   * - 正規表現に一致しない場合、targetにデフォルト値を設定して返却する
   *
   * @param target inputフォーム
   * @param regExp 正規表現
   * @param defaultValue デフォルト値
   * @returns value または、defaultValue
   */
  private defaultValueIfRegExpMismatch(target: any, regExp: RegExp, defaultValue: number): any {
    if (!regExp.test(target.value)) {
      // 正規表現に一致しない場合、デフォルト値を設定
      target.value = defaultValue;
    }

    // number型に変換
    return NumberUtils.toNumberDefaultIfEmpty(target.value, defaultValue);
  }

  /**
   * SKUの配分数Blur時の処理
   * @param target inputフォーム（HTMLInputElement型は指定しない。typeにnumberを指定しておらず、valueAsNumberが使用できないため）
   * @param formControl 配分数フォームコントロール
   */
  onBlurDistribution(target: any, formControl: FormControl): void {
    // 配分数を設定（正規表現に一致しない場合、nullを設定）
    formControl.patchValue(this.defaultValueIfRegExpMismatch(target, this.REG_EXP_NON_NEGATIVE_INTEGER, null));
  }

  /**
   * 配分率Blur時の処理
   * @param target inputフォーム（HTMLInputElement型は指定しない。typeにnumberを指定しておらず、valueAsNumberが使用できないため）
   * @param formControl 配分率フォームコントロール
   */
  onBlurRatio(target: any, formControl: FormControl): void {
    // 配分率を設定（正規表現に一致しない場合、0を設定）
    formControl.patchValue(this.defaultValueIfRegExpMismatch(target, this.REG_EXP_INTEGER_SECOND_DECIMAL_FIRST_OR_100, 0));
  }

  /**
   * 納品数量Blur時の処理
   * @param target inputフォーム（HTMLInputElement型は指定しない。typeにnumberを指定しておらず、valueAsNumberが使用できないため）
   * @param store 店舗フォームグループ
   * @param storeSku 店舗SKUフォームグループ
   */
  onBlurLot(target: any, store: FormGroup, storeSku: FormGroup): void {
    // 納品依頼数を設定（正規表現に一致しない場合、nullを設定）
    storeSku.patchValue({ deliveryLot: this.defaultValueIfRegExpMismatch(target, this.REG_EXP_NON_NEGATIVE_INTEGER, null) });

    // カラーコードを取得
    const colorCode = storeSku.get('colorCode').value;
    // サイズを取得
    const size = storeSku.get('size').value;
    // 納品得意先リストを取得
    const fValDeliveryStores = this.fValDeliveryStores;

    store.patchValue({ totalLot: this.sumInputLotByStore(store.get('storeCode').value, fValDeliveryStores) });

    const totalStoreSkuLot = this.sumInputLotBySku(colorCode, size, fValDeliveryStores);
    const targetSku = this.fCtrlSkus.find(sku => size === sku.get('size').value && colorCode === sku.get('colorCode').value);

    targetSku.patchValue({
      totalLot: totalStoreSkuLot,
      isNotMatchLotBetweenDivisionAndStore: totalStoreSkuLot !== NumberUtils.toInteger(targetSku.get('distribution').value)
    });
  }

  /**
   * SkuFormArrayを作成する.
   * @param orderSkus 発注SKU情報リスト
   * @param deliveryDetails 納品明細リスト
   * @returns SKUFormArray
   */
  private generateSkuFormArray(orderSkus: OrderSku[], deliveryDetails: DeliveryDetail[]): FormArray {
    return this.formBuilder.array(
      orderSkus.map(orderSku => {
        const colorCode = orderSku.colorCode;
        const size = orderSku.size;
        const totalStoreSkuLot = this.deliveryService.totalStoreSkuLot(deliveryDetails, colorCode, size);

        return this.formBuilder.group({
          colorCode: [colorCode],  // カラーコード
          size: [size],  // サイズ
          returnLot: [orderSku.returnLot],  // 返品数
          productOrderLot: [orderSku.productOrderLot],  // 発注数
          // ※formにdiableかけるとts内で値が取得できなくなるのでHTML側で制御する
          distribution: [null], // 配分数
          totalLot: [totalStoreSkuLot], // 納品数量合計
          // 指定したSKUの得意先合計数量が課別の合計数量と合致しないか
          isNotMatchLotBetweenDivisionAndStore:
            [this.deliveryService.isNotMatchLotBetweenDivisionAndStore(totalStoreSkuLot, deliveryDetails, orderSku)]
        });
      })
    );
  }

  /**
   * 店舗FormArrayを作成する.
   * @param order 発注情報
   * @param stores 店舗リスト
   * @param deliveryDetails 納品明細リスト
   * @returns 店舗FormArray
   */
  private generateStoreFormArray(order: Order, stores: JunpcTnpmst[], deliveryDetails: DeliveryDetail[]): FormArray {
    // 新規登録の時は、配分順が取得できなければその店舗は表示しない
    if (this.path === Path.NEW) {
      stores = stores.filter(store => StringUtils.isNotEmpty(store.hjun));
    }

    // 店舗(得意先)リストごとに作成
    return this.formBuilder.array(
      stores.map((store: JunpcTnpmst, index: number) => {
        // 処理中の得意先の納品得意先情報を所有したセット対象の納品明細情報
        const deliveryDetail = deliveryDetails == null ? null :
          deliveryDetails.find(dd => dd.deliveryStores.some(ds => ds.storeCode === store.shpcd));

        const deliveryStoreData = this.extractDeliveryStorePatchData(store, deliveryDetail, deliveryDetails);
        this.isDirectDelivery = deliveryStoreData.isDirect;

        // 納品得意先SKUFormArray作成
        const deliveryStoreSkuFormArray = this.formBuilder.array(
          order.orderSkus.map(orderSku => this.generateDeliveryStoreSkuFormGroup(orderSku, store, deliveryDetail))
        );

        const lastDivisionCode = this.isLastDivisionCode(stores, store, index);
        return this.generateStoreFormGroup(deliveryStoreData, store, lastDivisionCode, deliveryStoreSkuFormArray, deliveryDetails);
      })
    );
  }

  /**
   * formにセットする登録済の納品得意先情報を取得する.
   * @param store 処理中の店舗
   * @param deliveryDetail セット対象の納品明細情報
   * @param deliveryDetails 納品明細リストデータ(キャリー区分セット用。セット対象の納品明細情報がnullの場合もあるので必要)
   * @returns DeliveryStore, isDirect
   */
  private extractDeliveryStorePatchData(store: JunpcTnpmst, deliveryDetail: DeliveryDetail, deliveryDetails: DeliveryDetail[])
    : { deliveryStore: DeliveryStore, isDirect: boolean } {
    let deliveryStoreId: number = null;
    let deliveryDetailId: number = null;
    let storeDistributionRatioId: number = null;
    let storeDistributionRatioType: string = null;
    let storeDistributionRatio: number = null;
    let distributionSort: number = null;

    if (deliveryDetail != null) {
      const dbData = deliveryDetail.deliveryStores.find(ds => ds.storeCode === store.shpcd);
      deliveryStoreId = dbData.id;
      deliveryDetailId = deliveryDetail.id;
      storeDistributionRatioId = dbData.storeDistributionRatioId;
      storeDistributionRatioType = dbData.storeDistributionRatioType;
      storeDistributionRatio = dbData.storeDistributionRatio;
      distributionSort = dbData.distributionSort;
    }

    const deliveryStore = {
      id: deliveryStoreId,
      deliveryDetailId: deliveryDetailId,
      storeDistributionRatioId: storeDistributionRatioId,
      storeDistributionRatioType: storeDistributionRatioType,
      storeDistributionRatio: storeDistributionRatio,
      distributionSort: distributionSort
    } as DeliveryStore;

    return {
      deliveryStore: deliveryStore,
      isDirect: ObjectUtils.isNotNullAndNotUndefined(deliveryDetails)
        && deliveryDetails[0].carryType === CarryType.DIRECT,
    };
  }

  /**
   * 納品得意先のFormGroupを作成する.
   * @param deliveryStore 納品得意先にpatchするデータ
   * @param store 店舗情報
   * @param lastDivisionCode (並び順が)最後の配分課
   * @param deliveryStoreSkuFormArray 納品得意先SKUのFormArray
   * @param deliveryDetailList 納品明細リスト
   * @returns 納品得意先のFormGroup
   */
  private generateStoreFormGroup(deliveryStoreData: { deliveryStore: DeliveryStore, isDirect: boolean },
    store: JunpcTnpmst, lastDivisionCode: boolean, deliveryStoreSkuFormArray: FormArray, deliveryDetailList: DeliveryDetail[]): FormGroup {

    const deliveryStore = deliveryStoreData.deliveryStore;
    const isEditableStore = this._pathState.isEditableStore(deliveryStoreSkuFormArray, deliveryDetailList, store.hka);

    // 配分順マスタから消されている場合は登録済の納品得意先情報の配分順をセット
    const distributionSort = store.hjun != null ? store.hjun : deliveryStore.distributionSort;

    const totalLot = deliveryStoreSkuFormArray.value.reduce((total, { deliveryLot }) =>
      total += NumberUtils.toNumberDefaultIfEmpty(deliveryLot, 0), 0);

    // 店舗FormGroup作成
    return this.formBuilder.group({
      id: [deliveryStore.id], // 納品得意先ID
      deliveryDetailId: [deliveryStore.deliveryDetailId], // 納品明細ID
      divisionCode: [store.hka],              // 課コード
      logisticsCode: [store.logisticsCode],   // 物流コード
      allocationCode: [store.allocationCode], // 場所コード
      lastDivisionCode: [lastDivisionCode],   // (並び順が)最後の配分課
      storeCode: [store.shpcd],               // 店舗コード
      sname: [store.sname],                   // 店舗名
      directDeliveryFlg: [store.directDeliveryFlg], // 直送可否フラグ
      direct: [
        // 参照モードの場合、直送フラグを設定
        // 参照モード以外かつ、編集可能な場合、直送フラグを設定
        this.path === Path.VIEW ? deliveryStoreData.isDirect : isEditableStore ? deliveryStoreData.isDirect : false],   // 直送フラグ
      allocationType: [store.distrikind],     // 配分区分
      storeDistributionRatioId: [deliveryStore.storeDistributionRatioId], // 店舗別配分率ID
      storeDistributionRatioType: [deliveryStore.storeDistributionRatioType], // 店舗別配分率区分
      storeDistributionRatio: [deliveryStore.storeDistributionRatio], // 店舗別配分率
      distributionSort: [distributionSort], // 配分順
      deliveryStoreSkus: deliveryStoreSkuFormArray,  // 納品得意先SKU情報のリスト
      isEditableStore: [isEditableStore],  // 編集可能店舗フラグ
      totalLot: [totalLot]  // 納品数量合計
    });
  }

  /**
   * 納品得意先SKUFormGroupを作成して返す.
   * @param orderSku 発注SKU
   * @param stores 処理中の店舗情報
   * @param deliveryDetail セット対象の納品明細情報
   * @returns 納品得意先SKUFormGroup
   */
  private generateDeliveryStoreSkuFormGroup(orderSku: OrderSku, stores: JunpcTnpmst, deliveryDetail: DeliveryDetail): FormGroup {
    const deliveryStoreSku = this.extractDeliveryStoreSkuPatchData(stores.shpcd, orderSku, deliveryDetail);
    const deliveryStoreSkuFormGroup = this.formBuilder.group(
      {
        id: [deliveryStoreSku.id],        // id
        deliveryStoreId: [deliveryStoreSku.deliveryStoreId],  // 納品得意先ID
        divisionCode: [stores.hka],       // 課コード
        storeCode: [stores.shpcd],        // 店舗コード
        size: [orderSku.size],            // サイズ
        colorCode: [orderSku.colorCode],  // カラーコード
        deliveryLot: [deliveryStoreSku.deliveryLot],  // 納品数量
        // PRD_0031 add SIT start
        stockLot: [deliveryStoreSku.stockLot],
        // PRD_0031 add SIT end
        // PRD_0033 add SIT start
        salesScore: [deliveryStoreSku.salesScore]
        // PRD_0033 add SIT end
      }
    );
    return deliveryStoreSkuFormGroup;
  }

  /**
   * formにセットする登録済の納品得意先SKU情報を取得する.
   * @param shpcd 処理中の店舗コード
   * @param orderSku 発注SKU
   * @param deliveryDetail セット対象の納品明細データ
   * @returns id, deliveryStoreId, deliveryLot
   */
  private extractDeliveryStoreSkuPatchData(shpcd: string, orderSku: OrderSku, deliveryDetail: DeliveryDetail)
    // PRD_0031 mod SIT start
    //: { id: number, deliveryStoreId: number, deliveryLot: number } {
    // PRD_0033 mod SIT start
    //: { id: number, deliveryStoreId: number, deliveryLot: number, stockLot: number } {
    //// PRD_0031 add SIT end
    : { id: number, deliveryStoreId: number, deliveryLot: number, stockLot: number, salesScore: number } {
    // PRD_0033 mod SIT end
    let deliveryStoreSkuId: number = null;
    let deliveryLot: number = null;

    const deliveryStore = deliveryDetail == null ? null : deliveryDetail.deliveryStores.find(ds => ds.storeCode === shpcd);
    const deliveryStoreSku = deliveryStore == null ? null :
      deliveryStore.deliveryStoreSkus.find(dss => dss.colorCode === orderSku.colorCode && dss.size === orderSku.size);
    // PRD_0031 add SIT start
    const shopStock = this.shopStockList == null ? null :
      this.shopStockList.find(ss => ss.shopCode === shpcd && ss.colorCode === orderSku.colorCode && ss.size === orderSku.size);
    // PRD_0031 add SIT end
    // PRD_0033 add SIT start
    const PosOrderDetail = this.posOrderDetailList == null ? null :
      this.posOrderDetailList.find(pod => pod.storeCode === shpcd && pod.colorCode === orderSku.colorCode && pod.sizeCode === orderSku.size);
    // PRD_0033 add SIT end

    if (deliveryStoreSku != null) {
      deliveryStoreSkuId = deliveryStoreSku.id;
      deliveryLot = deliveryStoreSku.deliveryLot;
    }

    return {
      id: deliveryStoreSkuId,
      deliveryStoreId: deliveryStore != null ? deliveryStore.id : null,
      deliveryLot: deliveryLot,
      // PRD_0031 add SIT start
      stockLot: shopStock != null ? shopStock.stockLot === 0 ? null : shopStock.stockLot : null,
      // PRD_0031 add SIT end
      // PRD_0033 add SIT start
      salesScore: PosOrderDetail != null ? PosOrderDetail.salesScore === 0 ? null : PosOrderDetail.salesScore : null
      // PRD_0033 add SIT end
    };
  }

  /**
   * 相関バリデーションの設定.
   * @param mainForm メインフォーム
   */
  private setUpRelativeValidator(mainForm: FormGroup): void {
    // B級品区分のチェックon・offでB級品単価のコントロールを変更する
    mainForm.controls.nonConformingProductType.valueChanges.subscribe(checkValue => {
      const prodUnitPrice = this.fCtrl.nonConformingProductUnitPrice;
      if (checkValue === true) {
        // ※setValidatorsはenableより前に記述しないと登録ボタン押下時に反応しません
        prodUnitPrice.setValidators([Validators.required, Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]);
        prodUnitPrice.enable();
        return;
      }
      prodUnitPrice.clearValidators();
      prodUnitPrice.reset();
      prodUnitPrice.disable();
    });
  }

  /**
   * 納品依頼情報をformに反映する.
   * @param delivery 納品依頼情報
   */
  private patchDeliveryValueToForm(delivery: Delivery): void {
    const deliveryDetails = delivery.deliveryDetails;
    const deliveryAtObject = this.deliveryService.extractDeliveryAtFromDeliveryDetails(deliveryDetails);

    this.mainForm.patchValue({
      id: delivery.id,  // 納品依頼Id
      orderId: delivery.orderId, // 発注ID
      orderNumber: delivery.orderNumber,  // 発注No
      partNoId: delivery.partNoId,  // 品番ID
      partNo: delivery.partNo,  // 品番
      deliveryCount: delivery.deliveryCount,  // 納品依頼回数
      lastDeliveryStatus: delivery.lastDeliveryStatus,  // 最終納品ステータス
      deliveryApproveStatus: delivery.deliveryApproveStatus, // 承認ステータス
      distributionRatioType: this.findDistributionRatioType(delivery), // 配分率区分(画面表示用)
      memo: delivery.memo,  // メモ
      deliveryDateChangeReasonId: delivery.deliveryDateChangeReasonId,  // 納期変更理由ID
      deliveryDateChangeReasonDetail: delivery.deliveryDateChangeReasonDetail,  // 納品変更理由詳細
      nonConformingProductType: delivery.nonConformingProductType,  // B級品区分
      nonConformingProductUnitPrice: delivery.nonConformingProductUnitPrice,  // B級品単価
      faxSend: deliveryDetails[0].faxSend, // 通知メール送信(ファックス送信フラグ)
      deliveryRequestAt: deliveryDetails[0].deliveryRequestAt,  // 納品依頼日
      photoDeliveryAt: deliveryAtObject.photoDataDeliveryAt, // 撮影納期
      sewingDeliveryAt: deliveryAtObject.sewingDataDeliveryAt, // 縫検納期
      deliveryAt: deliveryAtObject.divisionDataDeliveryAt,  // 納期(その他の課)
      // PRD_0123 #7054 mod JFE start
      //allDirectCheckbox: deliveryDetails[0].carryType === CarryType.DIRECT  // 直送チェックボックス(全体)
      allDirectCheckbox: deliveryDetails[0].carryType === CarryType.DIRECT,  // 直送チェックボックス(全体)
      deliveryLocationCode: deliveryDetails[0].logisticsCode //納品明細物流コード
      // PRD_0123 #7054 add JFE start
    });
    this.setUpDistribution(); // 配分数設定
  }

  /**
   * 店舗別配分率区分を抽出する.
   * @param delivery 納品依頼情報
   * @returns 店舗別配分率区分
   */
  private findDistributionRatioType = (delivery: Delivery): string => {
    const deliveryDetails = delivery.deliveryDetails;
    if (ListUtils.isEmpty(deliveryDetails)) { return null; }

    const deliveryStores = deliveryDetails[0].deliveryStores;
    if (ListUtils.isEmpty(deliveryStores)) { return null; }
    // PRD_0066 mod SIT start
    //return deliveryStores[0].storeDistributionRatioType;
    // 店舗配分率区分のリストを取得
    const distributionRatioMastaList = this.distributionRatioMastaList;
    let i = 0;
    let index = -1;
    let existFlag = false;
    // リストをループし、登録済の配分率区分IDとリストの配分率区分IDを比較する
    distributionRatioMastaList.forEach(val => {
      // 一致する場合はindex番号を保持する
      if (deliveryStores[0].storeDistributionRatioId === val.storeHrtmstDivisions[0].id)
      {
        index = i;
        existFlag = true;
        this.onChangeDistributeType(index);
      } else { i = i + 1; }
    })
    // 特定出来ない場合はＮＵＬＬを返す
    return existFlag === false ? null : StringUtils.toStringSafe(index);
    // PRD_0066 mod SIT end
  }

  /**
   * 納期入力時イベント.
   */
  onInputDeliveryAt(): void {
    // 課別登録済の場合は処理なし
    if (ListUtils.isNotEmpty(this.deliveryDetailList)) {
      return;
    }
    // 課別未登録時の配分数設定
    this.setUpDistributionAtNoDetail();
  }

  /**
   * 配分数設定.
   */
  private setUpDistribution(): void {
    if (ListUtils.isNotEmpty(this.deliveryDetailList)) {
      // 課別登録済時の配分数設定
      this.setUpDistributionAtExistsDetail();
      return;
    }

    // 課別未登録時の配分数設定
    this.setUpDistributionAtNoDetail();
  }

  /**
   * 日付がすべて空白か.
   * @returns true:空白
   */
  private isAllDeliveryAtBlank(): boolean {
    const format = this.deliveryService.getFormattedDeliveryAt(this.mainForm.getRawValue());
    return StringUtils.isEmpty(format.photoDeliveryAt)
      && StringUtils.isEmpty(format.sewingDeliveryAt)
      && StringUtils.isEmpty(format.deliveryAt);
  }

  /**
   * 課別未登録時の配分数設定.
   * SKUの配分数は、納品予定が登録されている場合は納品可能数(設定した日付までに納品できる数 - 納品済数)
   * を設定し、納品予定が登録されていない場合は
   * 各SKU単位で発注数 - 納品済数(入荷数があれば、入荷数。未入荷の場合は納品依頼数)を設定する.
   */
  private setUpDistributionAtNoDetail(): void {
    // 日付未入力時は、配分数をクリアして終了
    if (this.isAllDeliveryAtBlank()) {
      this.fCtrlSkus.forEach(sku => sku.patchValue({ distribution: null }));
      return;
    }

    this.fCtrlSkus.forEach(sku => {
      const val = {
        colorCode: sku.get('colorCode').value,
        size: sku.get('size').value,
        returnLot: sku.get('returnLot').value,
        productOrderLot: sku.get('productOrderLot').value,
      } as SkuDeliveryFormValue;
      const historyLotSum = this.deliveryService.sumLotFromHistoryListBySku(val.colorCode, val.size, this.deliveryHistoryList);

      // 配分数を算出
      let distribution;
      if (this.deliveryPlan != null) {
        // 納品可能数
        distribution = this.caluculateDeliverableLotBySku(val.colorCode, val.size,
          historyLotSum.deliveryHistoryLotMixArrivalLotSum, val.returnLot);
      } else {
        // 発注数 - 納品済数
        distribution = val.productOrderLot - historyLotSum.deliveryHistoryLotMixArrivalLotSum;
      }

      // SKU配分数を設定する(マイナス値になる場合は0を設定)
      sku.patchValue({ distribution: distribution < 0 ? 0 : distribution });
    });
  }

  /**
   * 課別登録済時の配分数設定.
   * 入荷済の場合は入荷数合計、未入荷の場合は配分数合計を設定する。
   */
  private setUpDistributionAtExistsDetail(): void {
    // SKUごとに納品SKUの合計を設定
    const allDeliverySkus = this.deliveryDetailList.reduce((list, { deliverySkus }) => [...list, ...deliverySkus], [] as DeliverySku[]);
    let skuMapArray;
    if (this.isArrived) {
      skuMapArray = this.deliveryService.groupArrivalLotBySku(allDeliverySkus);
    } else {
      skuMapArray = this.deliveryService.groupLotBySku(allDeliverySkus);
    }
    const groupingSkus = skuMapArray.map(data => {
      const sku: { colorCode: string, size: string } = JSON.parse(data.keyString);
      return { colorCode: sku.colorCode, size: sku.size, distribution: data.totalLot };
    });

    this.fCtrlSkus.forEach(sku => {
      const val = { colorCode: sku.get('colorCode').value, size: sku.get('size').value } as SkuDeliveryFormValue;
      const target = groupingSkus.find(gSku => gSku.colorCode === val.colorCode && gSku.size === val.size);
      // SKU配分数を設定する
      sku.patchValue({ distribution: target == null ? 0 : target.distribution });
    });
  }

  /**
   * 指定した店舗の納品数量の入力値を合計して返す.
   * @param storeCode 店舗コード
   * @param fValDeliveryStores 納品得意先フォーム値
   * @returns 配分課別の合計
   */
  private sumInputLotByStore(storeCode: string, fValDeliveryStores: DeliveryStoreFormValue[]): number {
    return this.deliveryService.sumInputLotByStore(storeCode, fValDeliveryStores);
  }

　/**
   * 指定したカラーサイズの納品数量の入力値を合計して返す.
   * @param colorCode カラーコード
   * @param size サイズ
   * @param fValDeliveryStores 納品得意先フォーム値
   * @returns カラーサイズ別の合計
   */
  private sumInputLotBySku(colorCode: string, size: string, fValDeliveryStores: DeliveryStoreFormValue[]): number {
    return this.deliveryService.sumInputLotBySku(colorCode, size, fValDeliveryStores);
  }

  /**
   * 指定したカラーサイズの納品可能数を計算して返す.
   * @param colorCode カラーコード
   * @param size サイズ
   * @param historyLot 過去の納品数量
   * @param returnLot SKU毎の返品数
   * @returns 納品可能数
   */
  private caluculateDeliverableLotBySku(colorCode: string, size: string, historyLot: number, returnLot: number): number {
    const deliveryPlanLot = this.extractDeliveryPlanLotWitinMaxDeliveryAt({ colorCode: colorCode, size: size });
    if (deliveryPlanLot == null) { return 0; }
    return deliveryPlanLot - historyLot + returnLot;
  }

  /**
   * 指定されたskuのうち、納期3種の中で最大日以内の納品予定数量合計を返す.
   * 正常な納期の入力が1つもなければnullを返す.
   * @param sku カラー・サイズ
   * @return 納品予定数量合計
   */
  private extractDeliveryPlanLotWitinMaxDeliveryAt(sku: { colorCode: string, size: string }): number {
    if (this.mainForm == null) {
      return null;
    }
    return this.deliveryService.extractDeliveryPlanLotWitinMaxDeliveryAt(
      this.mainForm.getRawValue(), this.deliveryPlan, sku);
  }

  /**
   * 配分率区分変更処理.
   * @param selectedRatioIndex 選択された配分率区分のindex
   */
  onChangeDistributeType(selectedRatioIndex: number): void {
    const storeHrtmst: JunpcStoreHrtmst = this.distributionRatioMastaList[selectedRatioIndex];
    if (storeHrtmst == null) { return; }

    const patchStoreDistributionRatio = this.patchStoreDistributionRatio(storeHrtmst, storeHrtmst.hrtkbn);
    this.fCtrlDeliveryStores
      .filter(store => store.get('isEditableStore').value === true)
      .forEach(patchStoreDistributionRatio);
  }

  /**
   * 店舗フォームの配分率関連の項目の値を設定する.
   * @param storeHrtmst 店舗配分率マスタ
   * @param selectedRatioType 選択された配分率区分
   * @param storeCtrl 店舗フォームコントロール
   */
  private patchStoreDistributionRatio = (storeHrtmst: JunpcStoreHrtmst, selectedRatioType: string) =>
    (storeCtrl: AbstractControl): void => {
      const ratio = storeHrtmst.storeHrtmstDivisions.find(hrtmst => hrtmst.shpcd === storeCtrl.get('storeCode').value);
      storeCtrl.patchValue({
        storeDistributionRatioId: ratio == null ? null : ratio.id,
        storeDistributionRatioType: selectedRatioType,
        storeDistributionRatio: ratio == null ? 0 : ratio.hritu
      });
    }

  /**
   * 配分ボタン押下時の処理.
   * @param selectedRatioIndex 選択された配分率区分のindex
   */
  onClickDistribute(selectedRatioIndex: number): void {
    // 選択した配分区分のIDを取得
    const storeDistributionRatioId = this.distributionRatioMastaList[selectedRatioIndex].storeHrtmstDivisions[0].id;

    // 選択した配分区分のIDを取得して店舗フォームリストにpatchValue
    this.fCtrlDeliveryStores.forEach(ds => ds.patchValue({ storeDistributionRatioId: storeDistributionRatioId }));
    // 配分処理
    this._pathState.distribute(this.mainForm, this.deliveryDetailList);

    // 合計数量設定
    this.calculateTotalLot();
  }

  /**
   * 合計数量の計算.
   */
  private calculateTotalLot(): void {
    // ※develop-2-4に(orから)マージする際はdevelop-2-4にあるdeliveryServiceのsumStoresFValDeliveryLotBySku、sumStoresFValDeliveryLotBySkuを呼んでください。
    // 納品得意先リストを取得
    const fValDeliveryStores = this.fValDeliveryStores;

    this.fCtrlDeliveryStores.forEach(ds =>
      ds.patchValue({ totalLot: this.sumInputLotByStore(ds.get('storeCode').value, fValDeliveryStores) }));

    this.fCtrlSkus.forEach(sku => {
      const totalStoreSkuLot = this.deliveryService.sumInputLotBySku(sku.get('colorCode').value, sku.get('size').value, fValDeliveryStores);

      sku.patchValue({
        totalLot: totalStoreSkuLot,
        isNotMatchLotBetweenDivisionAndStore: totalStoreSkuLot !== NumberUtils.toInteger(sku.get('distribution').value)
      });
    });
  }

  /**
   * 納品数量の入力値を全てクリアする
   */
  onClearDeliveryLotValue(): void {
    this.mainForm.patchValue(this.generateClearDeliveryLotValue(this.mainForm.getRawValue()));
  }

  /**
   * 納品数量の入力値をクリアしたオブジェクトを生成する
   *
   * @param mainFormValue フォームの値
   */
  private generateClearDeliveryLotValue(value: any): any {
    return {
      /** SKUリスト */
      skuFormArray: value.skuFormArray.map(sku => {
        return {
          /** 納品数量合計 */
          totalLot: 0,
          /** 指定したSKUの得意先合計数量が課別の合計数量と合致しないか */
          isNotMatchLotBetweenDivisionAndStore: 0 !== sku.distribution
        };
      }),
      /** 納品得意先情報リスト */
      deliveryStores: value.deliveryStores.map(store => {
        return {
          /** 納品数量合計 */
          totalLot: 0,
          /** 納品得意先SKU情報のリスト */
          deliveryStoreSkus: store.deliveryStoreSkus.map(() => {
            return {
              /** 納品数量 */
              deliveryLot: null
            };
          })
        };
      })
    };
  }

  /**
   * 課別配分ボタン押下時の処理
   */
  onClickDivisionDistribute(): void {
    if (this.path === Path.VIEW) {
      this.movePagesToDeliveryRequest();
      return;
    }

    // 確認モーダルを表示
    const modalRef = this.modalService.open(MessageConfirmModalComponent);
    modalRef.componentInstance.message = this.moveDeliveryRequestConfirmMessage;
    modalRef.result.then((result: string) => {
      if (result !== 'OK') { return; }
      // 確認OKの場合、納品依頼登録画面(課別配分)へ遷移
      this.movePagesToDeliveryRequest();
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 納品依頼登録画面(課別配分)へ遷移する.
   */
  private movePagesToDeliveryRequest(): void {
    const orderId = this.path === Path.NEW ? Number(this.route.snapshot.queryParamMap.get('orderId')) : this.orderIdInDelivery;
    const deliveryId = this.fVal.id;
    if (deliveryId == null) {
      this.router.navigate(
        [this.nextDivisionUrl, Path.NEW],
        { queryParams: { orderId: orderId, t: new Date().valueOf() } });
      return;
    }

    const nextPath = this.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT ? Path.EDIT : Path.VIEW;
    this.router.navigate(
      [this.nextDivisionUrl, deliveryId, nextPath],
      { queryParams: { orderId: orderId, t: new Date().valueOf() } });
  }

  /**
   * 登録・更新・訂正保存ボタン押下時の処理.
   * @param SubmitType 送信ボタンの種類
   */
  onSubmitUpsert(submitType: SubmitType): void {
    this.clearAllMessage();
    if (this.isStoreRegistedAndNoLotAtEdit()) {
      // 編集時に得意先登録済で数量入力がない場合、削除確認モーダルを表示する
      this.mainForm.setErrors(null);
      this.setConfirmModalAndDelete(this.noDeliveryLotConfirmMessage);
      return;
    }

    this.submitted = true;

    // 過去納品数量と今回納品数量合計 - 返品数合計
    // PRD_0070 mod SIT start
    //const allDeliveryLot = this.deliveryService.sumAllDeliveryStoreLot(this.fValDeliveryStores, this.deliveryHistoryList, this.fVal.id);
    const allDeliveryLot = this.deliveryService.sumAllDeliveryStoreLot(this.fValDeliveryStores, this.deliveryHistoryList, this.fVal.id)
      - this.orderData.orderSkus.reduce((total, { returnLot }) => total += returnLot, 0);
    // PRD_0070 mod SIT end
    const isNotMatch = this._pathState.notMatchLotToDistribution(this.mainForm, this.deliveryDetailList, this.purchase); // 納品数量と配分数比較

    if (this.isInvalid(allDeliveryLot, isNotMatch)) {
      this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      return;
    }

    const isLotOverQuantity = allDeliveryLot > this.orderData.quantity;
    const isLastDeliveryRegistered = LastDeliveryStatus.LAST === (<LastDeliveryStatus> this.fVal.lastDeliveryStatus);

    if (this.needShowSubmitModal(isLotOverQuantity, isNotMatch, isLastDeliveryRegistered)) {
      // モーダル表示
      this.openDeliverySubmitConfirmModal(isLotOverQuantity, isNotMatch, isLastDeliveryRegistered, submitType);
      return;
    }

    this.handleSubmit(submitType);
  }

  /**
   * 編集時に得意先登録済で納品数量がないか.
   * @returns true:編集時に納品数量がない
   */
  private isStoreRegistedAndNoLotAtEdit(): boolean {
    return this.path === Path.EDIT  // 編集
      && this.deliveryDetailList.some(dd => ListUtils.isNotEmpty(dd.deliveryStores))  // 得意先登録済
      && this.fErr && this.fErr.deliveryAtLotRequired;  // 数量入力なし
  }

  /**
   * Submit時モーダル表示必要か.
   * 納品数量が発注数超過(閾値以内)
   *  または 納品数量が配分数不足(新規登録画面の場合のみtrueになる可能性あり)
   *  または 最終納品として未登録
   * の場合、モーダルを表示する
   * @param isLotOverQuantity 発注数超過
   * @param isNotMatch 配分数と納品数量不一致フラグ
   * @param isLastDeliveryRegistered 最終納品として登録済
   * @return true:必要
   */
  private needShowSubmitModal(isLotOverQuantity: boolean, isNotMatch: boolean, isLastDeliveryRegistered: boolean): boolean {
    return isLotOverQuantity || isNotMatch || !isLastDeliveryRegistered;
  }

  /**
   * @param allDeliveryLot 過去納品数と今回の入力数合計
   * @param isNotMatch 納品数量と配分数不一致フラグ
   * @readonly true:バリデーションエラーあり
   */
  private isInvalid(allDeliveryLot: number, isNotMatch: boolean): boolean {
    if (this.mainForm.invalid) {
      return true;
    }

    // 課ごとの納品数合計が配分数と不一致の課がある場合エラー
    // ※新規登録時の場合は不一致でもエラーにしない
    if (isNotMatch && this.path !== Path.NEW) {
      const msg = 'ERRORS.VALIDATE.DELIVERY_LOT_NOT_MATCH_DISTRIBUTION';
      ExceptionUtils.displayErrorInfo('deliveryLotErrorInfo', msg);
      return true;
    }

    // 納品数合計が閾値上限を超過する場合エラー(新規登録時のみ該当する可能性あり)
    if (this.deliveryService.isThresholdRateOver(allDeliveryLot, this.orderData.quantity, this.threshold)) {
      ExceptionUtils.displayErrorInfo('deliveryLotErrorInfo', 'ERRORS.VALIDATE.DELIVERY_LOT_RATE_THRESHOLD_OVER');
      return true;
    }

    return false;
  }

  /**
   * Submit時の確認モーダルを表示する.
   * @param isLotOverQuantity 発注数超過
   * @param isNotMatch 配分数と納品数量不一致フラグ
   * @param isLastDeliveryRegistered 最終納品フラグ
   * @param submitType 送信ボタンの種類
   */
  private openDeliverySubmitConfirmModal(isLotOverQuantity: boolean, isNotMatch: boolean,
    isLastDeliveryRegistered: boolean, submitType: SubmitType): void {
    const modalRef = this.modalService.open(DeliverySubmitConfirmModalComponent);
    modalRef.componentInstance.isOverLot = isLotOverQuantity;
    modalRef.componentInstance.isLowerLot = isNotMatch; // 新規登録時のみ不足でtrueがくる可能性あり
    modalRef.componentInstance.isLastDeliveryRegistered = isLastDeliveryRegistered;
    modalRef.componentInstance.submitType = submitType;
    // PRD_0044 add SIT start
    if (submitType === SubmitType.SAVE_CORRECT) {
      modalRef.componentInstance.submitType = SubmitType.CORRECT;
    }
    // PRD_0044 add SIT end
    modalRef.result.then((isLastDelivery: boolean) => {
      this.mainForm.patchValue({ lastDeliveryStatus: isLastDelivery ? LastDeliveryStatus.LAST : LastDeliveryStatus.NORMAL });
      this.handleSubmit(submitType);
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * Submitボタンの種類による処理の切り分け.
   * @param submitType 送信ボタンの種類
   */
  private handleSubmit(submitType: SubmitType): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;

    const formValue = this.mainForm.getRawValue();
    formValue.deliveryDetails = this.deliveryRequestService.prepareDeliveryDetailsDataForSubmit(formValue, this.deliveryDetailList);
    formValue.distributionRatioType = null;  // 店舗配分画面ではt_deliveryの配分率区分には登録なし

    switch (submitType) {
      case SubmitType.ENTRY:
        this.postProcessSubmit(
          this.deliveryRequestService.postDeliveryRequest(formValue),
          { preEvent: PreEventParam.CREATE, nextPath: Path.EDIT, deliveryId: null }
        );
        break;
      case SubmitType.UPDATE:
        this.postProcessSubmit(
          this.deliveryRequestService.putDeliveryRequest(formValue),
          { preEvent: PreEventParam.UPDATE, nextPath: Path.EDIT, deliveryId: null }
        );
        break;
      case SubmitType.CORRECT:
        // PRD_0044 add SIT start
        formValue.storeScreenSaveCorrectFlg = false;
        // PRD_0044 add SIT end
        this.postProcessSubmit(
          this.deliveryRequestService.correctDeliveryRequest(formValue),
          { preEvent: PreEventParam.CORRECT, nextPath: Path.VIEW, deliveryId: null }
        );
        break;
      case SubmitType.APPROVE:
        formValue.deliveryApproveStatus = DeliveryApprovalStatus.ACCEPT;
        this.postProcessSubmit(
          this.deliveryRequestService.approvalDeliveryRequest(formValue),
          { preEvent: PreEventParam.APPROVE, nextPath: Path.VIEW, deliveryId: formValue.id }
        );
        break;
      case SubmitType.DIRECT_CONFIRM:
        this.postProcessSubmit(
          this.deliveryRequestService.directConfirmDeliveryRequest(formValue),
          { preEvent: PreEventParam.DIRECT_CONFIRM, nextPath: Path.VIEW, deliveryId: formValue.id }
        );
        break;
      // PRD_0044 add SIT start
      case SubmitType.SAVE_CORRECT:
        formValue.storeScreenSaveCorrectFlg = true;
        this.postProcessSubmit(
          this.deliveryRequestService.correctDeliveryRequest(formValue),
          { preEvent: PreEventParam.SAVE_CORRECT, nextPath: Path.VIEW, deliveryId: null }
        );
        break;
      // PRD_0044 add SIT end
      default:
        break;
    }
  }

  /**
   * Submit後の処理.
   * @param observable Observable<Delivery>
   * @param option
   *  処理したイベント
   *  遷移先パス
   *  処理中の納品ID
   */
  private postProcessSubmit(observable: Observable<Delivery>, option: { preEvent: number, nextPath: Path, deliveryId: number }): void {
    observable.subscribe(
      result => {
        this.loadingService.loadEnd();
        this.isBtnLock = false;

        // submit後の画面表示
        const orderId = Number(this.route.snapshot.queryParamMap.get('orderId'));

        if (PreEventParam.DELETE === option.preEvent) {
          this.routeAfterDelete(orderId, option.deliveryId);
          return;
        }

        // 承認時と直送確定時はレスポンスがこないのでformのIDを使う
        const deliveryId = (PreEventParam.APPROVE === option.preEvent || PreEventParam.DIRECT_CONFIRM === option.preEvent) ?
          option.deliveryId : result.id;
        this.router.navigate([this.nextStoreUrl, deliveryId, option.nextPath],
          // PRD_0135 #10039 JFE mod start
          //  { queryParams: { orderId: orderId, preEvent: option.preEvent, t: new Date().valueOf()} });
          { queryParams: { orderId: orderId, preEvent: option.preEvent, t: new Date().valueOf(), bol: true, deliveryId: deliveryId } });
          // PRD_0135 #10039 JFE mod end
      }, (error: HttpErrorResponse) => this.messageConfirmModalService.openErrorModal(error).subscribe(() => this.loadingService.loadEnd())
    );
  }

  /**
   * 削除処理後の遷移
   * @param orderId 発注ID
   * @param deliveryId 納品ID
   */
  private routeAfterDelete(orderId: number, deliveryId: number): void {
    const nextPath = this.deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT ? Path.VIEW : Path.EDIT;
    this.router.navigate(
      [this.nextStoreUrl, deliveryId, nextPath],
      { queryParams: { orderId: orderId, preEvent: PreEventParam.DELETE, t: new Date().valueOf() } });
  }

  /**
   * 削除ボタン押下時の処理.
   */
  onDeleteConfirmModal(): void {
    this.clearAllMessage();
    let modalMessage = '';
    this.translate.get('INFO.DELIVERY_DELETE_CONFIRM_MESSAGE').subscribe((msg: string) => modalMessage = msg);
    this.setConfirmModalAndDelete(modalMessage);
  }

  /**
   * 確認モーダルを表示し、削除処理を行う.
   * @param message モーダルに表示するメッセージ
   */
  private setConfirmModalAndDelete(message: string): void {
    // 確認モーダルを表示
    const modalRef = this.modalService.open(MessageConfirmModalComponent);
    modalRef.componentInstance.message = message;
    modalRef.result.then((result: string) => {
      if (result === 'OK') { this.submitDelete(); }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 削除処理実行.
   */
  private submitDelete(): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;
    const deliveryId = this.mainForm.get('id').value;
    const obs = this.deliveryRequestService.deleteDeliveryStoreRequest(deliveryId);
    this.postProcessSubmit(obs, { preEvent: PreEventParam.DELETE, nextPath: null, deliveryId: deliveryId });
  }

  /**
   * 納品依頼承認処理
   */
  onSubmitApproveDeliveryData(): void {
    this.clearAllMessage();

    // 入力フォームに変更があるか判断する.変更がある場合承認ボタンをロックし、承認前に更新するメッセージを表示
    if (this.mainForm.dirty) {
      this.submitted = true;
      this.isApprovable = false;
      this.overallErrorMsgCode = 'ERRORS.VALIDATE.UPDATE_BEFORE_APPROVE';
      return;
    }

    this.handleSubmit(SubmitType.APPROVE);
  }

  /**
   * 直送確定処理
   */
  onSubmitDirectDeliveryData(): void {
    this.clearAllMessage();
    this.handleSubmit(SubmitType.DIRECT_CONFIRM);
  }

  /**
   * 訂正時の警告モーダルを表示する.
   */
  openCorrectAlertModal(): void {
    this.clearAllMessage();

    // 確認モーダルを表示
    // PRD_0044 mod SIT start
    //let modalMessage = this.alertMessageAtCorrect;
    //const isSheetOut = this.deliveryDetailList.some(deliveryDetail => deliveryDetail.deliverySheetOut === true);
    //if (isSheetOut) { // 納品明細情報の納品依頼書発行フラグ=trueの場合、既に納品依頼書が発行済の旨のメッセージモーダルを表示
    //  modalMessage = this.sheetOutedAlertMessage;
    //}
    //const modalRef = this.modalService.open(MessageConfirmModalComponent);
    //modalRef.componentInstance.message = modalMessage;
    const isSheetOut = this.deliveryDetailList.some(deliveryDetail => deliveryDetail.deliverySheetOut === true);
    if (isSheetOut) { // 納品明細情報の納品依頼書発行フラグ=trueの場合、既に納品依頼書が発行済の旨のメッセージモーダルを表示
      const modalRef = this.modalService.open(MessageConfirmModalComponent);
      modalRef.componentInstance.message = this.sheetOutedAlertMessage;
    // PRD_0044 mod SIT end
      modalRef.result.then((result: string) => {
        if (result !== 'OK') { return; }
        // 確認OKの場合、URLから納品依頼IDを取得して訂正画面へ遷移
        const deliveryId: number = Number(this.route.snapshot.params['id']);
        this.router.navigate(
          [this.nextStoreUrl, deliveryId, Path.CORRECT],
          { queryParams: { orderId: this.orderData.id, t: new Date().valueOf() } });
      }, () => { });  // バツボタンクリック時は何もしない
      // PRD_0044 add SIT start
    } else {
      // URLから納品依頼IDを取得して訂正画面へ遷移
      const deliveryId: number = Number(this.route.snapshot.params['id']);
      this.router.navigate(
        [this.nextStoreUrl, deliveryId, Path.CORRECT],
          { queryParams: { orderId: this.orderData.id, t: new Date().valueOf() } });
    }
    // PRD_0044 add SIT end
  }

  /**
   * 個別の店舗の直送チェックボックス変更時の処理.
   * @param checked チェック
   * @param direct 直送フラグフォームコントロール
   */
  onChangeDirectAtIndividual(checked: boolean, direct: FormControl): void {
    // 直送フラグに設定
    direct.patchValue(checked);

    // 更新または訂正の場合は全て変更
    if (this.path === Path.EDIT || this.path === Path.CORRECT) {
      this.mainForm.patchValue({ allDirectCheckbox: checked });
      this.onChangeDirect(checked);
    }
  }

  /**
   * 直送チェックボックス(全体)変更時の処理.
   * @param checked チェック
   */
  onChangeDirect(checked: boolean): void {
    // 直送フラグ変更
    this.fCtrlDeliveryStores
      .filter(this.isAbleToChangeDirect)
      .forEach(store => store.patchValue({ direct: checked }));

    if (this.path === Path.NEW || !checked) { return; }

    // 編集または訂正時、直送チェックONの場合、直送可否フラグがfalseの店舗は入力値クリア
    this.fCtrlDeliveryStores
      .filter(store => store.get('directDeliveryFlg').value === false)
      .forEach(this.clearStoreDeliveryLotInputValue);

    // 合計数量設定
    this.calculateTotalLot();
  }

  /**
   * 直送フラグ変更可否判定.
   * @param storeCtrl 店舗フォームコントロール
   * @returns true:変更可能
   */
  private isAbleToChangeDirect(storeCtrl: AbstractControl): boolean {
    return storeCtrl.enabled && storeCtrl.get('directDeliveryFlg').value === true;
  }

  /**
   * 店舗フォームの納品数量入力値クリアする.
   * @param storeCtrl 店舗フォームコントロール
   */
  private clearStoreDeliveryLotInputValue(storeCtrl: AbstractControl): void {
    (storeCtrl.get('deliveryStoreSkus') as FormArray).controls.forEach(skuCtrl => skuCtrl.patchValue({ deliveryLot: null }));
  }

  /**
   * 削除ボタン表示判定.
   * @returns true:表示
   */
  showDeleteBtn(): boolean {
    return this._pathState.showDeleteBtn(this.existsRegistedDeliveryStore, this.affiliation, this.deliveryDetailList);
  }

  /**
   * 登録ボタン表示判定.
   * @returns true:表示
   */
  showRegistBtn(): boolean {
    return this._pathState.showRegistBtn();
  }

  /**
   * 訂正ボタン表示判定.
   * @returns true:表示
   */
  showUpdateBtn(): boolean {
    return this._pathState.showUpdateBtn();
  }

  /**
   * 訂正ボタン表示判定.
   * @returns true:表示
   */
  showCorrectBtn(): boolean {
    return this._pathState.showCorrectBtn(this.affiliation, this.deliveryDetailList);
  }

  /**
   * 訂正保存ボタン表示判定.
   * @returns true:表示
   */
  showCorrecSaveBtn(): boolean {
    return this._pathState.showCorrectSaveBtn();
  }

  /**
   * 承認ボタン表示判定.
   * @returns true:表示
   */
  showApproveBtn(): boolean {
    return this._pathState.showApproveBtn(this.existsRegistedDeliveryStore, this.affiliation);
  }

  // PRD_0031 add SIT start
  /**
   * ダウンロード処理
   */
  onDistributeDownload(): void{
    const blob = new Blob([iconv.encode(this.arrToCSV(), 'Shift_JIS')], { 'type': 'text/csv' });
    const blobURL = window.URL.createObjectURL(blob);

    // a要素を作成する
    let ele = document.createElement('a');
    // a要素に出力情報を追加
    ele.setAttribute('download', this.fVal.partNo + '-' + this.fVal.orderNumber + '-' +
      (FormUtils.isEmpty(this.fVal.deliveryCount) ? '0' : this.fVal.deliveryCount) + '_' + DateUtils.convertDate(new Date()) + '.csv');
    //a要素に出力データを追加
    ele.setAttribute('href', blobURL);
    ele.style.visibility = 'hidden';
    // HTMLドキュメントにa要素を追加
    document.body.appendChild(ele);
    // HTMLドキュメントに追加したa要素を実行(clickイベント実行)
    ele.click();
    // HTMLドキュメントに追加したa要素を削除
    document.body.removeChild(ele);
   }

  /**
   * 出力データ作成
   * @returns 出力テキスト
   */
  private arrToCSV(): string {
    let outputData: string[][] = [];
    let columnData: string[][] = [];
    let stockData: string[][] = [];
    let salesData: string[][] = [];
    let cnt = 1;
    let cntSku = 0;
    let divisionTotal: number[] = [];
    let deliveryStoreSku: DeliveryStoreSkuFormValue;

    // ヘッダー(1～5行目)作成
    outputData.push(
      [DistributeCsvColumn.ORDER_NUMBER, this.fVal.orderNumber],
      [DistributeCsvColumn.DELIVERY_COUNT, FormUtils.isNotEmpty(this.fVal.deliveryCount) ? this.fVal.deliveryCount : '0'],
      [DistributeCsvColumn.DELIVERY_NUMBER, FormUtils.isNotEmpty(this.deliveryDetailList) ? this.deliveryDetailList[0].deliveryNumber : ''],
      [DistributeCsvColumn.PART_NO, this.fVal.partNo],
      [DistributeCsvColumn.PRODUCT_NAME, this.itemData.productName]
    );

    // SKU(6～10行目)作成
    // PRD_0119#8396 mod JFE start
    /*
    columnData.push(
      [DistributeCsvColumn.TITLE, DistributeCsvColumn.NO, DistributeCsvColumn.SHOP_CODE, DistributeCsvColumn.SHOP_NAME, DistributeCsvColumn.DISTRIBUTE_RATIO],
      [DistributeCsvColumn.COLOR, '', '', '', ''],
      [DistributeCsvColumn.SIZE, '', '', '', ''],
      [DistributeCsvColumn.TOTAL, '', '', '', '']
    );

    // 配分数作成
    this.fVal.skuFormArray.forEach(sku => {
      columnData[0].push(DistributeCsvColumn.DISTRIBUTE + cnt.toString());
      columnData[1].push(sku.colorCode);
      columnData[2].push(sku.size);
      columnData[3].push(FormUtils.isEmpty(sku.totalLot) ? "0" : sku.totalLot.toString());

      if (FormUtils.isEmpty(stockData)) {
        stockData.push([DistributeCsvColumn.STOCK + cnt.toString()], [sku.colorCode], [sku.size]);
        salesData.push([DistributeCsvColumn.SALES + cnt.toString()], [sku.colorCode], [sku.size]);
      }
      else {
        stockData[0].push(DistributeCsvColumn.STOCK + cnt.toString());
        stockData[1].push(sku.colorCode);
        stockData[2].push(sku.size);
        salesData[0].push(DistributeCsvColumn.SALES + cnt.toString());
        salesData[1].push(sku.colorCode);
        salesData[2].push(sku.size);
      }
      cnt++;
    });
    */
    columnData.push(
      [DistributeCsvColumn.TITLE, DistributeCsvColumn.NO, DistributeCsvColumn.SHOP_CODE, DistributeCsvColumn.SHOP_NAME, DistributeCsvColumn.DISTRIBUTE_RATIO],
      [DistributeCsvColumn.SKU, '', '', '', ''],
      [DistributeCsvColumn.COLOR, '', '', '', ''],
      [DistributeCsvColumn.SIZE, '', '', '', ''],
      [DistributeCsvColumn.TOTAL, '', '', '', '']
    );

    // 配分数作成
    this.fVal.skuFormArray.forEach(sku => {
      const skuData = this.fVal.partNo + '-' + sku.colorCode + '-' + sku.size;
      columnData[0].push(DistributeCsvColumn.DISTRIBUTE + cnt.toString());
      columnData[1].push(skuData);
      columnData[2].push(sku.colorCode);
      columnData[3].push(sku.size);
      columnData[4].push(FormUtils.isEmpty(sku.totalLot) ? '0' : sku.totalLot.toString());

      if (FormUtils.isEmpty(stockData)) {
        stockData.push([DistributeCsvColumn.STOCK + cnt.toString()], [skuData], [sku.colorCode], [sku.size]);
        salesData.push([DistributeCsvColumn.SALES + cnt.toString()], [skuData], [sku.colorCode], [sku.size]);
      } else {
        stockData[0].push(DistributeCsvColumn.STOCK + cnt.toString());
        stockData[1].push(skuData);
        stockData[2].push(sku.colorCode);
        stockData[3].push(sku.size);
        salesData[0].push(DistributeCsvColumn.SALES + cnt.toString());
        salesData[1].push(skuData);
        salesData[2].push(sku.colorCode);
        salesData[3].push(sku.size);
      }
      cnt++;
    });

    outputData.push(columnData[0].concat([''], stockData[0], [''], salesData[0]));
    outputData.push(columnData[1].concat([''], stockData[1], [''], salesData[1]));
    outputData.push(columnData[2].concat([''], stockData[2], [''], salesData[2]));
    outputData.push(columnData[3].concat([''], stockData[3], [''], salesData[3]));
    outputData.push(columnData[4]);
    // PRD_0119#8396 mod JFE end

    // 得意先出力(11行目～)
    columnData = [];
    stockData = [];
    salesData = [];
    cnt = 0;
    this.fValDeliveryStores.forEach(store => {
      // 直送チェックボックス(全体)がTrueの場合
      if (this.fVal.allDirectCheckbox === false || store.direct === true) {
        // 得意先情報を設定
        columnData.push(
          [DistributeCsvColumn.DETAIL,
            (cnt + 1).toString(),
            store.storeCode,
            store.sname,
            FormUtils.isEmpty(store.storeDistributionRatio) ? "0" : store.storeDistributionRatio.toString()]);
        stockData.push([""]);
        salesData.push([""]);

        // 得意先SKU情報を設定
        cntSku = 0;
        this.fVal.skuFormArray.forEach(sku => {
          if (FormUtils.isNotEmpty(store.deliveryStoreSkus)) {
            deliveryStoreSku = store.deliveryStoreSkus.find((storeSku: DeliveryStoreSkuFormValue) =>
              storeSku.colorCode === sku.colorCode && storeSku.size === sku.size
            );
          }
          columnData[cnt].push(FormUtils.isEmpty(deliveryStoreSku.deliveryLot) ? "0" : deliveryStoreSku.deliveryLot.toString());
          stockData[cnt].push(FormUtils.isEmpty(deliveryStoreSku.stockLot) ? "0" : deliveryStoreSku.stockLot.toString());
          salesData[cnt].push(FormUtils.isEmpty(deliveryStoreSku.salesScore) ? "0" : deliveryStoreSku.salesScore.toString());

          if (cnt === 0) {
            divisionTotal.push(FormUtils.isEmpty(deliveryStoreSku.deliveryLot) ? 0 : deliveryStoreSku.deliveryLot);
          }
          else {
            divisionTotal[cntSku] += FormUtils.isEmpty(deliveryStoreSku.deliveryLot) ? 0 : deliveryStoreSku.deliveryLot;
          }
          cntSku ++;
          deliveryStoreSku = null;
        })
        cnt ++;
      }

      // 最後の配分課の場合、outputDataにpushする
      if (store.lastDivisionCode) {
        if (FormUtils.isNotEmpty(columnData)) {
          // 課合計
          outputData.push([store.divisionCode + DistributeCsvColumn.DIVISION_TOTAL, "", "", "", ""].concat(divisionTotal.toString()));

          // 明細
          for (let i = 0; i < cnt; i++){
            outputData.push(columnData[i].concat(stockData[i], salesData[i]));
          }

          // 初期化
          divisionTotal = [];
          columnData = [];
          stockData = [];
          salesData = [];
          cnt = 0;
        }
      }
    })
    return outputData.map(row => row.join(',')).join('\r\n');
  }

  /**
   * アップロード処理
   * @param file 取込ファイル
   */
  onDistributeUpload(file: any): void {
    let isErr: boolean = false;
    const allow_exts = '.csv$';

    // メッセージクリア
    this.clearAllMessage();

    if (FormUtils.isEmpty(file)) {
      return;
    }

    // 拡張子チェック
    if (!file.name.match(allow_exts)) {
      this.overallErrorMsgCode = "ERRORS.FILE_UL_UNMATCH_EXTENSION"
    } else {
      // ファイル読み込み
      const csvData = this.deliveryRequestService.deliveryStoreUploadRead(file);

      csvData.subscribe(data => {
        // エラーチェック
        isErr = this.isUploadValid(data);
        if (!isErr) {
          // 配分数設定
          // PRD_0120#8343 mod JFE start
          // this.mainForm.controls.deliveryStores['controls'].forEach((store: FormGroup) => {
          //   store.controls.deliveryStoreSkus['controls'].forEach((storeSku: FormGroup) => {
          //     this.setUploadValue(data, store, storeSku);
          //   });
          // });

          // const firstStore = this.mainForm.controls.deliveryStores['controls'][0];
          // const firstStoreSku = firstStore.controls.deliveryStoreSkus['controls'][0];
          // this.setUploadValue(data, firstStore, firstStoreSku);
          this.mainForm.controls.deliveryStores['controls'].forEach((store: FormGroup) => {
            const dataStore = data.stores.find(d =>
              d.storeCode === store.controls.storeCode.value);
            this.setUploadValue(dataStore, store);
          });
          // PRD_0120#8343 mod JFE end
          this.overallSuccessMsgCode = 'SUCSESS.FILE_UL_COMPLETE';
        }
      })
    }
  }

  /**
   * アップロードデータチェック処理
   * @param csvData 取込データ
   * @return エラー有無
   */
    // PRD_0120#8343 mod JFE start
     //isUploadValid(csvData: DeliveryStoreUploadCsv[]): boolean {
        isUploadValid(csvData: DeliveryStoreUploadCsv): boolean {
    // PRD_0120#8343 mod JFE End
    let isErr: boolean = false;
    const regex = new RegExp(ValidatorsPattern.NUMERIC);
    let distributeSkus: { colorCode: string, size: string, deliveryLot: number }[] = [];


    if (FormUtils.isEmpty(csvData)) {
      // ファイルの読み込みに失敗した場合
      this.overallErrorMsgCode = "ERRORS.FILE_UL_ERROR";
      isErr = true;
    } else {
      // PRD_0120#8343 mod JFE start
      // // 発注番号チェック
      // if (csvData[0].orderNo !== this.fVal.orderNumber.toString()) {
      //   this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_ORDER_NUMBER';
      //   isErr = true;
      // }

      // // 納品依頼回数チェック
      // if (!isErr && (csvData[0].deliveryCount === '' ? '0' : csvData[0].deliveryCount
      //   !== (FormUtils.isEmpty(this.fVal.deliveryCount) ? '0' : this.fVal.deliveryCount.toString()))) {
      //     this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_DELIVERY_COUNT';
      //     isErr = true;
      // }

      // // 品番チェック
      // if (!isErr && csvData[0].partNo !== this.fVal.partNo) {
      //   this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_PART_NO';
      //   isErr = true;
      // }

      // // 店舗不正入力チェック
      // if (!isErr &&
      //   csvData.some(d => FormUtils.isEmpty(this.fValDeliveryStores.find(store => store.storeCode === d.storeCode)))) {
      //   this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_STORE';
      //   isErr = true;
      // }

      // // SKU不正入力チェック
      // if (!isErr &&
      //   csvData.some(d =>
      //     FormUtils.isEmpty(this.fVal.skuFormArray.find(sku => sku.colorCode === d.colorCode && sku.size === d.size)))) {
      //       this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_SKU';
      //       isErr = true;
      // }

      // // 配分数に数値、ブランク以外の値が入力されている場合エラー
      // if (!isErr &&
      //   csvData.some(d => d.deliveryLot !== '' && regex.test(d.deliveryLot) === false)) {
      //       this.overallErrorMsgCode = 'ERRORS.FILE_UL_PATTERN_NUMBER_NOT_LESS_THAN0';
      //       isErr = true;
      // }

      // // 入力不可な店舗・SKUに値が入力されている場合エラー
      // if (!isErr) {
      //   csvData.filter(csv => csv.deliveryLot !== '0' && csv.deliveryLot !== null)
      //     .forEach(d => {
      //       const delStore = this.deliveryStoreFormArray.controls.find(store => store.get('storeCode').value === d.storeCode);
      //       const storeSku = (delStore.get('deliveryStoreSkus') as FormArray).controls.find(sku =>
      //         sku.get('colorCode').value === d.colorCode && sku.get('size').value === d.size);

      //       // 非活性の店舗に入力している場合エラー
      //       if (!isErr && delStore.disabled === true) {
      //         this.overallErrorMsgCode = 'ERRORS.FILE_UL_DISABLED_STORE';
      //         isErr = true;
      //       }

      //       // 非活性のSKUに入力している場合エラー
      //       if (!isErr && storeSku.disabled === true) {
      //         this.overallErrorMsgCode = 'ERRORS.FILE_UL_DISABLED_SKU';
      //         isErr = true;
      //       }

      //       // SKU別数量取得
      //       distributeSkus.push({colorCode: d.colorCode, size: d.size, deliveryLot: NumberUtils.toInteger(d.deliveryLot)});
      //     });
      // }
      // 発注番号チェック
      if (csvData.orderNo !== this.fVal.orderNumber.toString()) {
        this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_ORDER_NUMBER';
        isErr = true;
      }

      // 納品依頼回数チェック
      if (!isErr && (csvData.deliveryCount === '' ? '0' : csvData.deliveryCount
        !== (FormUtils.isEmpty(this.fVal.deliveryCount) ? '0' : this.fVal.deliveryCount.toString()))) {
          this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_DELIVERY_COUNT';
          isErr = true;
      }

      // 品番チェック
      if (!isErr && csvData.partNo !== this.fVal.partNo) {
        this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_PART_NO';
        isErr = true;
      }

      // 店舗不正入力チェック
      if (!isErr &&
        csvData.stores.some(d => FormUtils.isEmpty(this.fValDeliveryStores.find(store => store.storeCode === d.storeCode)))) {
        this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_STORE';
        isErr = true;
      }

      // SKU不正入力チェック
      if (!isErr &&
        csvData.stores[0].deliveryStoreSkuFormValues.some(d =>
          FormUtils.isEmpty(this.fVal.skuFormArray.find(sku => sku.colorCode === d.colorCode && sku.size === d.size)))) {
            this.overallErrorMsgCode = 'ERRORS.FILE_UL_UNMATCH_SKU';
            isErr = true;
      }

      // 配分数に数値、ブランク以外の値が入力されている場合エラー
      if (!isErr &&
        // tslint:disable-next-line:max-line-length
        csvData.stores.some(d => d.deliveryStoreSkuFormValues.some(val => val.deliveryLot !== '' && regex.test(val.deliveryLot) === false))) {
            this.overallErrorMsgCode = 'ERRORS.FILE_UL_PATTERN_NUMBER_NOT_LESS_THAN0';
            isErr = true;
      }

      // 入力不可な店舗・SKUに値が入力されている場合エラー
      if (!isErr) {
        csvData.stores.forEach(s => s.deliveryStoreSkuFormValues.forEach(val => {
          if (val.deliveryLot !== '0' && val.deliveryLot !== null) {
            const delStore = this.deliveryStoreFormArray.controls.find(store => store.get('storeCode').value === s.storeCode);
            const storeSku = (delStore.get('deliveryStoreSkus') as FormArray).controls.find(sku =>
              sku.get('colorCode').value === val.colorCode && sku.get('size').value === val.size);
            // 非活性の店舗に入力している場合エラー
            if (!isErr && delStore.disabled === true) {
              this.overallErrorMsgCode = 'ERRORS.FILE_UL_DISABLED_STORE';
              isErr = true;
            }

            // 非活性のSKUに入力している場合エラー
            if (!isErr && storeSku.disabled === true) {
              this.overallErrorMsgCode = 'ERRORS.FILE_UL_DISABLED_SKU';
              isErr = true;
            }

            // SKU別数量取得
            distributeSkus.push({ colorCode: val.colorCode, size: val.size, deliveryLot: NumberUtils.toInteger(val.deliveryLot) });
          }
        }));
      }
      // PRD_0120#8343 mod JFE end

      // 数量未入力の場合エラー
      if (!isErr && FormUtils.isEmpty(distributeSkus)) {
        this.overallErrorMsgCode = "ERRORS.FILE_UL_DELIVERY_LOT_EMPTY";
        isErr = true;
      }
    }
    return isErr;
  }
  /**
   * アップロードデータをメインフォームに反映
   * @param csvData 取込データ
   * @return エラー有無
   */
  // PRD_0120#8343 mod JFE start
  //  setUploadValue(csvData: DeliveryStoreUploadCsv[], store: FormGroup, storeSku: FormGroup): void {
  //   const targetLot = csvData.find(d =>
  //     d.storeCode === store.controls.storeCode.value &&
  //     d.colorCode === storeSku.controls.colorCode.value &&
  //     d.size === storeSku.controls.size.value);
  //   const target: HTMLInputElement = <HTMLInputElement> document.getElementsByName('deliveryLot').item(0);
  //   target.value = FormUtils.isEmpty(targetLot) ? null :
  //     targetLot.deliveryLot === '0' ? null : targetLot.deliveryLot;
  //   this.onBlurLot(target, store, storeSku);
  setUploadValue(csvData: DeliveryStoreUploadCsvStore, store: FormGroup): void {
    let targetLot: DeliveryStoreSkuForm ;
    store.controls.deliveryStoreSkus['controls'].forEach((storeSku: FormGroup) => {
      targetLot = csvData.deliveryStoreSkuFormValues.find(d =>
      d.colorCode === storeSku.controls.colorCode.value &&
      d.size === storeSku.controls.size.value);
    const target: HTMLInputElement = <HTMLInputElement> document.getElementsByName('deliveryLot').item(0);
    target.value = FormUtils.isEmpty(targetLot) ? null :
      targetLot.deliveryLot === '0' ? null : targetLot.deliveryLot;
    this.onBlurLot(target, store, storeSku);
    });
  // PRD_0120#8343 mod JFE end
  }
  // PRD_0031 add SIT end
}
