import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, AbstractControl, Validators, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';

import { Observable, forkJoin, of, noop } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import * as Moment_ from 'moment';

import {
  Const, AuthType, DeliveryApprovalStatus, Path, PreEventParam, AllocationCode,
  // PRD_0044 mod SIT start
  //LastDeliveryStatus, OrderApprovalStatus, ValidatorsPattern, SpecialtyQubeCancelStatusType,
  //SubmitType, CarryType, DeliveryDistributionSpecificationType
  LastDeliveryStatus, OrderApprovalStatus, ValidatorsPattern, SubmitType, CarryType,
  DeliveryDistributionSpecificationType
  // PRD_0044 mod SIT end
} from '../../const/const';

import { ExceptionUtils } from '../../util/exception-utils';
import { BusinessCheckUtils } from '../../util/business-check-utils';
import { StringUtils } from '../../util/string-utils';
import { FormUtils } from '../../util/form-utils';
import { NumberUtils } from '../../util/number-utils';
import { ListUtils } from '../../util/list-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

import { DeliverySubmitConfirmModalComponent } from '../delivery-submit-confirm-modal/delivery-submit-confirm-modal.component';
import { MessageConfirmModalComponent } from '../message-confirm-modal/message-confirm-modal.component';
import { DeliveryDistributionModalComponent } from '../delivery-distribution-modal/delivery-distribution-modal.component';

import { deliveryAtLotValidator, DeliveryRequestValidatorDirective } from './validator/delivery-request-validator.directive';

import { LoadingService } from '../../service/loading.service';
import { SessionService } from '../../service/session.service';
import { JunpcCodmstService } from '../../service/junpc-codmst.service';
import { JunpcHrtmstService } from '../../service/junpc-hrtmst.service';
import { ItemService } from '../../service/item.service';
import { OrderService } from '../../service/order.service';
import { DeliveryRequestService } from '../../service/delivery-request.service';
import { HeaderService } from '../../service/header.service';
// PRD_0044 del SIT start
//import { SpecialtyQubeService } from '../../service/specialty-qube.service';
// PRD_0044 del SIT end
import { ThresholdService } from '../../service/threshold.service';
import { DeliveryService } from '../../service/bo/delivery.service';
import { DeliveryPlanService } from '../../service/delivery-plan.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { PurchaseHttpService } from 'src/app/service/purchase-http.service';

import { JunpcCodmst } from '../../model/junpc-codmst';
import { JunpcCodmstSearchCondition } from '../../model/junpc-codmst-search-condition';
import { JunpcHrtmst } from '../../model/junpc-hrtmst';
import { JunpcHrtmstSearchCondition } from '../../model/junpc-hrtmst-search-condition';
import { Item } from '../../model/item';
import { Order } from '../../model/order';
import { OrderSku } from '../../model/order-sku';
import { Delivery } from '../../model/delivery';
import { DeliveryDetail } from '../../model/delivery-detail';
import { DeliverySku } from '../../model/delivery-sku';
import { DeliveryRequestSearchConditions, ThresholdSearchConditions, DeliveryPlanSearchConditions } from '../../model/search-conditions';
import { DeliveryPlan } from '../../model/delivery-plan';
// PRD_0044 del SIT start
//import { SpecialtyQubeCancelResponse } from '../../model/specialty-qube-cancel-response';
//import { SpecialtyQubeRequest } from '../../model/specialty-qube-request';
// PRD_0044 del SIT end
import { Session } from '../../model/session';
import { DeliveryVoucherFileInfo } from 'src/app/model/delivery-voucher-file-info';

import { DeliveryRequestUrlParams } from './interface/delivery-request-url-params';
import { DeliveryOrderSkuFormValue } from 'src/app/interface/delivery-order-sku-form-value';
import { Allocations } from 'src/app/interface/allocations';
import { DeliverySkuFormValue } from 'src/app/interface/delivery-sku-form-value';
import { DistributionValue } from 'src/app/interface/distribution-value';

import { Purchase } from '../purchase/interface/purchase';
import { DeliveryHeaderComponent } from '../delivery-header/delivery-header.component';
  // PRD_0123 #7054 add JFE start
import { MdeliveryLocation } from 'src/app/model/m-delivery-location';
  // PRD_0123 #7054 add JFE end

const Moment = Moment_;

@Component({
  selector: 'app-delivery-request',
  templateUrl: './delivery-request.component.html',
  styleUrls: ['./delivery-request.component.scss']
})
export class DeliveryRequestComponent implements OnInit {

  readonly DELIVERY_CHANGE_REASON_LIST = Const.DELIVERY_CHANGE_REASON_LIST;
  readonly AUTH_INTERNAL: AuthType = AuthType.AUTH_INTERNAL;

  readonly APPROVAL_STATUS = DeliveryApprovalStatus;
  readonly PATH = Path;
  readonly SUBMIT_TYPE = SubmitType;

  /** セッション */
  private session: Session;

  /** ユーザ権限 */
  affiliation: AuthType;

  /** パス */
  path = '';

  /** 初期表示で必要なデータ取得済 */
  isInitDataSetted = false;

  /** 操作ボタン表示フラグ */
  isShowEdiButton = false;

  /** 遷移先URL */
  nextUrl = '';

  /** 得意先配分遷移先URL */
  nextStoreUrl = '';

  /** 正常系のメッセージコード */
  overallSuccessMsgCode = '';

  /** エラーメッセージ用 */
  overallErrorMsgCode = '';

  /** 納品済数：納品依頼済数 */
  allDeliveredLot = 0;

  /** 納品残数：裁断数 - 日付が決まっている納品予定skuの合計数 */
  remainingLot = 0;

  /** 増減産数：納品予定skuの合計数  - 発注数 */
  changesInLot = 0;

  /** 増減産数(％)：増減産数 / 発注数 */
  changesInLotRatio = '';

  /** 返品数(発注SKUの全returnLot合計) */
  private totalReturnLot = 0;

  /** 指定可能な配分数合計 */
  private totalDistributionable = 0;

  /** 致命的エラーメッセージ */
  fatalErrorMsgCode = '';

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

  /** 更新処理フラグ */
  isUpdate = false;

  /** 仕入数超過エラーあり */
  isDeliveryLotMaxError = false;

  /** 納品数量形式エラーあり */
  isDeliveryLotPatternError = false;

  /** 全配分課リスト */
  allDeliveryAllocationList: JunpcCodmst[] = [];

  /** 納品場所の配分課リスト(本社撮影、縫製検品以外) */
  deliveryAllocationList: JunpcCodmst[] = [];

  /** 納品場所以外の配分課リスト(本社撮影、縫製検品のみ) */
  noDeliveryAllocationList: JunpcCodmst[] = [];

  /**  配分率リスト */
  distributionRatioMastaList: JunpcHrtmst[] = [];

  /** 品番 */
  itemData: Item;

  /** 発注 */
  orderData: Order;

  /** 発注SKUリスト */
  orderSkuList: OrderSku[] = [];

  /** 納品承認ステータス */
  deliveryApproveStatus: string;

  /** 納品依頼に登録されている発注Id */
  orderIdInDelivery: number;

  /** 納品履歴リスト */
  deliveryHistoryList: Delivery[] = [];

  /** 納品SKU */
  deliverySku: DeliverySku;

  /** 納品予定 */
  deliveryPlan: DeliveryPlan;

  /** 納品明細リスト */
  deliveryDetailList: DeliveryDetail[] = [];

  /** 納品伝票ファイルリスト */
  deliveryVoucherFileInfos: DeliveryVoucherFileInfo[] = [];

  /** SQロック中のユーザーアカウント名 */
  sqLockUserAccountName = '';

  // PRD_0044 del SIT start
  ///** 訂正ボタンロックフラグ */
  //isCorrectLock = false;
  // PRD_0044 del SIT end

  /** 未入荷フラグ */
  nonArrived = true;

  /** ゼロ確 */
  isZeroFix = false;

  /** 直送フラグ */
  isDirectDelivery = false;

  /** 直送確定ボタン表示フラグ */
  isShowDirectConfirmButton = false;

  // PRD_0125 #9079 add JFE start
  /** 店舗別登録済フラグ */
  storeRegisteredFlg = false;
  // PRD_0125 #9079 add JFE end

  /** 閾値 */
  private threshold: number = null;

  // PRD_0123 #7054 add JFE start
  /** 納入場所リスト */
  deliveryLocationList: MdeliveryLocation[] = [];
  /** 納入場所リスト表示フラグ */
  isShowDeliveryLocationList = true;
  // PRD_0123 #7054 add JFE end

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private modalService: NgbModal,
    private translate: TranslateService,
    private junpcCodmstService: JunpcCodmstService,
    private junpcHrtmstService: JunpcHrtmstService,
    private itemService: ItemService,
    private orderService: OrderService,
    private deliveryRequestService: DeliveryRequestService,
    private deliveryService: DeliveryService,
    private deliveryPlanService: DeliveryPlanService,
    // PRD_0044 del SIT start
    //private specialtyQubeService: SpecialtyQubeService,
    // PRD_0044 del SIT end
    private thresholdService: ThresholdService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private purchaseHttpService: PurchaseHttpService
  ) { }

  /**
   * mainFormの項目の状態を取得する。
   * @return mainForm.controls
   */
  get fCtrl(): { [key: string]: AbstractControl } {
    return this.mainForm.controls;
  }

  /**
   * mainFormのerrorsを取得する。
   * @return mainForm.errors
   */
  get fErr(): ValidationErrors {
    return this.mainForm.errors;
  }

  /**
   * mainFormの分配先_納品場所(撮影、縫製)発注skuFormArrayの項目の状態を取得する。
   * @return this.mainForm.get('deliveryOrderSkuFormArray').controls
   */
  get fCtrlDeliveryOrderSkus(): AbstractControl[] {
    const deliveryOrderSkuFormArray = this.mainForm.get('deliveryOrderSkuFormArray');
    return deliveryOrderSkuFormArray == null ? null : (<FormArray> deliveryOrderSkuFormArray).controls;
  }

  /**
   * mainFormの分配先_納品場所(撮影、縫製)発注skuFormArrayのvalueを返す.
   * @return mainForm.get('deliveryOrderSkuFormArray').value
   */
  get fValDeliveryOrderSkus(): DeliveryOrderSkuFormValue[] {
    const deliveryOrderSkuFormArray = this.mainForm.get('deliveryOrderSkuFormArray');
    return deliveryOrderSkuFormArray == null ? null : deliveryOrderSkuFormArray.value;
  }

  /**
   * mainFormの非分配先_納品場所(撮影、縫製)発注skuFormArrayの項目の状態を取得する。
   * @return this.mainForm.get('noDeliveryOrderSkuFormArray').controls
   */
  get fCtrlNoDeliveryOrderSkus(): AbstractControl[] {
    const noDeliveryOrderSkuFormArray = this.mainForm.get('noDeliveryOrderSkuFormArray');
    return noDeliveryOrderSkuFormArray == null ? null : (<FormArray> noDeliveryOrderSkuFormArray).controls;
  }

  /**
   * mainFormの非分配先_納品場所(撮影、縫製)発注skuFormArrayのvalueを返す.
   * @return mainForm.get('noDeliveryOrderSkuFormArray').value
   */
  get fValNoDeliveryOrderSkus(): DeliveryOrderSkuFormValue[] {
    const noDeliveryOrderSkuFormArray = this.mainForm.get('noDeliveryOrderSkuFormArray');
    return noDeliveryOrderSkuFormArray == null ? null : noDeliveryOrderSkuFormArray.value;
  }

  /**
   * mainFormの配分数FormArrayを取得する。
   * @return this.mainForm.get('distributionFormArray')
   */
  get distributionFormArray(): FormArray {
    return this.mainForm.get('distributionFormArray') as FormArray;
  }

  /**
   * mainFormの配分数FormArrayの項目の状態を取得する。
   * @return this.mainForm.get('distributionFormArray').controls
   */
  get fCtrlDistributions(): AbstractControl[] {
    const distributionFormArray = this.mainForm.get('distributionFormArray');
    return distributionFormArray == null ? null : (<FormArray> distributionFormArray).controls;
  }

  /**
   * mainFormの配分数FormArrayのvalueを返す.
   * @return mainForm.get('distributionFormArray').value
   */
  get fValDistributions(): DistributionValue[] {
    const distributionFormArray = this.mainForm.get('distributionFormArray');
    return distributionFormArray == null ? null : distributionFormArray.value;
  }

  /**
   * mainFormから全ての納品SKU配列のcontrolを取得する。
   * @returns 全ての納品SKU配列のcontrol
   */
  get fCtrlAllOrderSkus(): AbstractControl[] {
    const deliverySkusCtrls: AbstractControl[] = [];
    // 納品場所の納品SKU取得
    if (this.fCtrlDeliveryOrderSkus != null) { Array.prototype.push.apply(deliverySkusCtrls, this.fCtrlDeliveryOrderSkus); }

    // 納品場所以外の納品SKU取得
    if (this.fCtrlNoDeliveryOrderSkus != null) { Array.prototype.push.apply(deliverySkusCtrls, this.fCtrlNoDeliveryOrderSkus); }
    return deliverySkusCtrls;
  }

  /**
   * mainFormから全ての納品SKU配列のvalueを取得する。
   * @returns 全ての納品SKU配列のvalue
   */
  get fValAllOrderSkus(): DeliveryOrderSkuFormValue[] {
    const deliverySkusValues: DeliveryOrderSkuFormValue[] = [];
    // 納品場所の納品SKU取得
    if (this.fValDeliveryOrderSkus != null) { Array.prototype.push.apply(deliverySkusValues, this.fValDeliveryOrderSkus); }

    // 納品場所以外の納品SKU取得
    if (this.fValNoDeliveryOrderSkus != null) { Array.prototype.push.apply(deliverySkusValues, this.fValNoDeliveryOrderSkus); }
    return deliverySkusValues;
  }

  /** PRD_0123 #7054 JFE add start
   * mainformから納品先のvalueを取得する。
   * @return mainform.get('deliveryLocationCode').value */
  get fVallocationid(): AbstractControl[] {
    const deliveryLocationCode = this.mainForm.get('deliveryLocationCode').value;
    return deliveryLocationCode;
  }
  //PRD_0123 #7054 JFE add end

  /**
   * @return true:削除ボタン表示
   */
  get showDeleteBtn(): boolean {
    if (!this.nonArrived) {
      return false; // 入荷済：非表示
    }

    switch (this.path) {
      case Path.EDIT: // 編集画面：表示
        return true;
      case Path.VIEW: // 参照画面：JUN権限であれば表示
        return AuthType.AUTH_INTERNAL === this.affiliation;
      default: // その他：非表示
        return false;
    }
  }

  ngOnInit() {
    // 過去納品参照時にidだけが異なる同一URLでも遷移可能にする為、subscribeで取得する。
    this.route.paramMap.subscribe(paramsMap => {
      this.headerService.show();
      this.isInitDataSetted = false;
      this.clearAllMessage();
      this.session = this.sessionService.getSaveSession(); // ログインユーザの情報を取得する
      this.affiliation = this.session.affiliation;
      // フッター表示条件: ROLE_EDIまたはROLE_MAKERまたはROLE_DISTA
      this.isShowEdiButton = AuthUtils.isEdi(this.session)
        || AuthUtils.isMaker(this.session)
        || AuthUtils.isDista(this.session);

      this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      this.nextUrl = 'deliveries';
      this.nextStoreUrl = 'deliveryStores';
      if (window.location.pathname.match(/delische/)) {
        this.nextUrl = 'delische/' + this.nextUrl;
        this.nextStoreUrl = 'delische/' + this.nextStoreUrl;
      } else if (window.location.pathname.match(/deliverySearchList/)) {
        this.nextUrl = 'deliverySearchList/' + this.nextUrl;
        this.nextStoreUrl = 'deliverySearchList/' + this.nextStoreUrl;
      }

      // PRD_0044 del SIT start
      //const sqCancelStatusStr = this.route.snapshot.queryParamMap.get('sqCancelStatus');
      // PRD_0044 del SIT end
      const urlParams = {
        path: this.path,
        deliveryId: Number(paramsMap.get('id')),
        errorCode: this.route.snapshot.queryParamMap.get('errorCode'),
        // PRD_0044 del SIT start
        //sqCancelStatus: sqCancelStatusStr != null ? Number(sqCancelStatusStr) : null,
        // PRD_0044 del SIT end
        preEvent: Number(this.route.snapshot.queryParamMap.get('preEvent'))
      };
      this.initializeDisplayByRouting(urlParams);
    });
  }

  /**
   * 初期表示処理.
   * @param urlParams
   *  path URLパス
   *  deliveryId 当画面で処理する納品ID
   *  errorCode エラーメッセージコード
   *  preEvent URLクエリパラメータpreEvent
   */
  private initializeDisplayByRouting(urlParams: DeliveryRequestUrlParams): void {

    // 遷移前のイベントの結果メッセージ表示
    // PRD_0044 mod SIT start
    //this.showPreEventMessage(urlParams.preEvent, urlParams.errorCode, urlParams.sqCancelStatus);
    this.showPreEventMessage(urlParams.preEvent, urlParams.errorCode);
    // PRD_0044 mod SIT end
    switch (urlParams.path) {
      case Path.NEW:  // 新規登録
        this.initializeNewDisplay();
        break;
      case Path.VIEW: // 参照
        // PRD_0044 mod SIT start
        //this.initializeViewDisplay(urlParams.deliveryId, urlParams.sqCancelStatus);
        this.initializeViewDisplay(urlParams.deliveryId);
        // PRD_0044 mod SIT end
        break;
      case Path.EDIT: // 編集
        this.initializeEditDisplay(urlParams.deliveryId);
        break;
      case Path.CORRECT: // 訂正
        this.initializeCorrectDisplay(urlParams.deliveryId);
        break;
      default:
        break;
    }
  }

  /**
   * 遷移前イベント結果のメッセージ表示.
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   * @param errorCode エラーメッセージコード
   */
  // PRD_0044 mod SIT start
  //private showPreEventMessage(preEvent: number, errorCode: string, sqCancelStatus: number): void {
  private showPreEventMessage(preEvent: number, errorCode: string): void {
  // PRD_0044 mod SIT end
    if (StringUtils.isNotEmpty(errorCode)) {  // エラー時
      this.overallErrorMsgCode = 'ERRORS.' + errorCode;
      return;
    }

    // PRD_0044 del SIT start
    //if (sqCancelStatus != null) { // SQキャンセルAPIエラーメッセージを設定
    //  this.generateSqCancelErrorMessage(sqCancelStatus);
    //  return;
    //}
    // PRD_0044 del SIT end

    // 成功時
    switch (preEvent) {
      case PreEventParam.CREATE:
        // 納品依頼登録後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.DELIVERY_REQUEST_ENTRY';
        break;
      case PreEventParam.UPDATE:
        // 納品依頼更新後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.DELIVERY_REQUEST_UPDATE';
        break;
      case PreEventParam.DELETE:
        // 納品依頼削除後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.DELIVERY_REQUEST_DELTE';
        break;
      case PreEventParam.APPROVE:
        // 納品依頼承認後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.DELIVERY_ACCEPT';
        break;
      case PreEventParam.CORRECT:
        // 納品依頼訂正後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.DELIVERY_CORRECT';
        break;
      case PreEventParam.DIRECT_CONFIRM:
        // 直送確定後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.DELIVERY_DIRECT_CONFIRM';
        break;
      default:
        break;
    }
  }

  // PRD_0044 del SIT start
  ///**
  // * SQキャンセルAPIエラーメッセージの設定.
  // * @param sqCancelStatus SQキャンセルAPIレスポンスステータス
  // */
  //private generateSqCancelErrorMessage(sqCancelStatus: number): void {
  //  switch (sqCancelStatus) {
  //    case SpecialtyQubeCancelStatusType.CANCEL_NG:
  //      // キャンセルNGのエラーメッセージを設定
  //      this.overallErrorMsgCode = 'ERRORS.SQ_CANCEL_NG_ERROR';
  //      break;
  //    case SpecialtyQubeCancelStatusType.NO_DATA:
  //      // SQデータ無しのエラーメッセージを設定
  //      this.overallErrorMsgCode = 'ERRORS.SQ_NO_DATA_ERROR';
  //      break;
  //    case SpecialtyQubeCancelStatusType.OTHER_ERROR:
  //      // SQその他エラーのエラーメッセージを設定
  //      this.overallErrorMsgCode = 'ERRORS.ANY_ERROR';
  //      break;
  //    default:
  //      // ※SQロックのメッセージはログインユーザーとの合致判断後に表示する.
  //      break;
  //  }
  //}
  // PRD_0044 del SIT end

  /**
   * pathがNEW時の表示処理。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private initializeNewDisplay(): void {
    this.setInitData(Number(this.route.snapshot.queryParamMap.get('orderId')));
  }

  /**
   * pathがVIEW時の表示処理。
   * @param deliveryId 当画面で処理する納品ID
   * @returns Promise<void>
   */
  // PRD_0044 mod SIT start
  //private async initializeViewDisplay(deliveryId: number, sqCancelStatus?: number): Promise<void> {
    private async initializeViewDisplay(deliveryId: number): Promise<void> {
  // PRD_0044 mod SIT end
    // 納品依頼情報取得
    const deliveryData = await this.getDeliveryData(deliveryId, 'NO_DELIVERY_FOR_VIEW').toPromise();

    // PRD_0044 del SIT start
    //// SQロックによる訂正ボタン制御フラグの設定
    //this.sqLockUserAccountName = deliveryData.sqLockUserAccountName;
    //this.isCorrectLock = this.deliveryService.isSQLock(this.session.accountName, this.sqLockUserAccountName);
    //if (sqCancelStatus != null && SpecialtyQubeCancelStatusType.LOCKED_BY_OTHER_USER === sqCancelStatus) {
    //  // ログインユーザーがSQロックユーザーでない場合のエラーメッセージを設定
    //  this.overallErrorMsgCode = 'ERRORS.SQ_LOCKED_BY_OTHER_USER_ERROR';
    //}
    // PRD_0044 del SIT end

    this.nonArrived = !this.deliveryService.isArrived(deliveryData);

    // 仕入情報取得
    const purchase = await this.purchaseHttpService.getByDeliveryId(deliveryId).toPromise();
    // 直送 かつ EDI権限 かつ 未仕入の場合、直送確定ボタン表示
    this.isShowDirectConfirmButton = (this.isDirectDelivery && AuthUtils.isEdi(this.session) && purchase == null);

    this.setInitData(deliveryData.orderId, deliveryData);
  }

  /**
   * pathがEDIT時の表示処理。
   * @param deliveryId 当画面で処理する納品ID
   * @returns Promise<void>
   */
  private async initializeEditDisplay(deliveryId: number): Promise<void> {
    // 納品依頼情報取得
    const deliveryData = await this.getDeliveryData(deliveryId, 'NO_DELIVERY_FOR_UPDATE').toPromise();
    if (deliveryData.deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT) {
      // 納品依頼が承認済の場合は、納品依頼参照画面(VIEW)へ遷移
      this.router.navigate([this.nextUrl, deliveryData.id, Path.VIEW], { queryParams: { orderId: deliveryData.orderId } });
      return;
    }

    this.setInitData(deliveryData.orderId, deliveryData);
  }

  /**
   * pathがCORRECT時の表示処理。
   * @param deliveryId 当画面で処理する納品ID
   * @returns Promise<void>
   */
  private async initializeCorrectDisplay(deliveryId: number): Promise<void> {
    // ログイン権限をチェック。メーカー権限の場合は参照画面へ遷移し、エラーを表示
    if (this.affiliation === AuthType.AUTH_SUPPLIERS) {
      this.router.navigate([this.nextUrl, deliveryId, Path.VIEW], {
        queryParams: { orderId: Number(this.route.snapshot.queryParamMap.get('orderId')), errorCode: '400_D_17' }
      });
      return;
    }

    // 納品依頼情報取得
    const deliveryData = await this.getDeliveryData(deliveryId, '400_D_15').toPromise();
    // 納品依頼の承認ステータスをチェック。未承認の場合は編集画面へ遷移し、エラーを表示
    if (deliveryData.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT) {
      this.router.navigate([this.nextUrl, deliveryData.id, Path.EDIT], {
        queryParams: { orderId: deliveryData.orderId, errorCode: '400_D_16' }
      });
      return;
    }

    // 入荷済の場合は参照画面へ遷移し、エラーを表示
    if (this.deliveryService.isArrived(deliveryData)) {
      this.router.navigate([this.nextUrl, deliveryId, Path.VIEW], {
        queryParams: { orderId: Number(this.route.snapshot.queryParamMap.get('orderId')), errorCode: '400_D_26' }
      });
      return;
    }

    // PRD_0044 del SIT start
    //// SQロック取得
    //const sqResponseData = await this.getSqLock(deliveryData.id).toPromise();
    //// SQレスポンスによる処理の分岐
    //this.organizeBySqCancelResponse(sqResponseData, deliveryData.id);
    // PRD_0044 del SIT end

    // 仕入情報取得
    const purchase = await this.purchaseHttpService.getByDeliveryId(deliveryId).toPromise();
    this.setInitData(deliveryData.orderId, deliveryData, purchase);
  }

  // PRD_0044 del SIT start
  ///**
  // * SQロック取得.
  // * @param deliveryId 当画面で処理する納品ID
  // * @returns Observable<SpecialtyQubeCancelResponse>
  // */
  //private getSqLock(deliveryId: number): Observable<SpecialtyQubeCancelResponse> {
  //  return this.specialtyQubeService.cancelSq({ deliveryId: deliveryId } as SpecialtyQubeRequest).pipe(
  //    map(sqLockResponse => sqLockResponse),
  //    catchError((error: HttpErrorResponse) => { // SQ接続エラー
  //      this.handleApiError(error);
  //      return of(null);
  //    })
  //  );
  //}
  // PRD_0044 del SIT end

  /**
   * 閾値情報を取得.
   * @param brandCode ブランドコード
   */
  private getThresholdData(brandCode: string): void {
    this.thresholdService.getThresholdByBrandCode({ brandCode: brandCode } as ThresholdSearchConditions).subscribe(
      thresholdList => this.threshold = thresholdList['items'][0].threshold,
      error => this.showBaseDataGetApiErrorMessage(error));
  }

  /**
   * 納品依頼情報を取得.
   * @param deliveryId 納品ID
   * @param errorCode エラー時のコード
   * @returns Observable<Delivery>
   */
  private getDeliveryData(deliveryId: number, errorCode: string): Observable<Delivery> {
    return this.deliveryRequestService.getDeliveryRequestForId(deliveryId).pipe(
      map(deliveryData => {
        // PRD_0125 #9079 add JFE start
        deliveryData.deliveryDetails.forEach(dd => {
          if (dd.storeRegisteredFlg === true) {
            this.storeRegisteredFlg = true;
          }
        }
        );
        // PRD_0125 #9079 add JFE start
        this.deliveryApproveStatus = deliveryData.deliveryApproveStatus;
        this.orderIdInDelivery = deliveryData.orderId;
        this.deliveryDetailList = deliveryData.deliveryDetails;
        this.isDirectDelivery = (deliveryData.deliveryDetails[0].carryType === CarryType.DIRECT);
        this.deliveryVoucherFileInfos = deliveryData.deliveryVoucherFileInfos;
        return deliveryData;
      }), catchError((error: HttpErrorResponse) => {
        this.handleApiError(error, { code: errorCode });
        throw new Error('getDeliveryData error');
      }));
  }

  /**
   * 納品予定情報取得処理.
   * @param orderId 発注ID
   * @returns Observable<DeliveryPlan>
   */
  private getDeliveryPlanData(orderId: number): Observable<DeliveryPlan> {
    return this.deliveryPlanService.getDeliveryPlanList({ orderId: orderId } as DeliveryPlanSearchConditions).pipe(
      map(deliveryPlanList => deliveryPlanList.items[0]),
      catchError((error: HttpErrorResponse) => {
        this.showBaseDataGetApiErrorMessage(error);
        throw new Error('getDeliveryPlanData error');
      }));
  }

  /**
   * 初期表示設定を行う。
   * @param orderId 発注ID
   * @param deliveryData 納品依頼情報
   * @param purchase 仕入情報
   * @returns Promise<void>
   */
  private async setInitData(orderId: number, deliveryData?: Delivery, purchase?: Purchase): Promise<void> {
    // 発注情報の取得
    const orderData = await this.getOrder(orderId).toPromise();
    if (orderData.orderApproveStatus !== OrderApprovalStatus.ACCEPT) {  // MD(発注)承認済みでなければエラー
      this.fatalErrorMsgCode = 'ERRORS.ORDER_APPROVE_ERROR';
      return;
    }

    this.isZeroFix = this.deliveryService.isZeroFix(deliveryData);

    // 品番情報の取得
    const itemData = await this.getItem(orderData.partNoId).toPromise();
    this.getDistributionRatio(itemData);  // 配分率取得。非同期処理
    this.getThresholdData(itemData.brandCode);  // 閾値取得。非同期処理
    // PRD_0123 #7054 add JFE start
    const itemID = itemData.id; //品番情報.ID
    //品番情報のIDを使用してコードマスタ2を検索
    this.getDeliveryLocation(itemID) //納入場所リスト取得
    // PRD_0123 #7054 add JFE end

    // 納品予定取得
    this.deliveryPlan = await this.getDeliveryPlanData(this.orderData.id).toPromise();

    const lists = await forkJoin(
      this.getAllocations(itemData.brandCode),  // 配分課
      this.getDeliveryHistoryList(orderData.id) // 過去の納品依頼情報
    ).toPromise();
    const dividedAllocationList = lists[0];
    const deliveryHistoryList = lists[1];

    // mainフォームの設定
    this.generateMainFormGroup(orderData, dividedAllocationList, deliveryData, deliveryHistoryList, purchase);
    // 納品依頼訂正画面の場合は、画面項目の非活性制御を行う
    if (this.path === Path.CORRECT) {
      this.disableDeliveryCorrectDisplayItem(deliveryData.deliveryDetails);
    }

    this.isInitDataSetted = true; // フォームを表示
  }

  /**
   * 発注情報を取得.
   * @param orderId 発注ID
   * @returns Observable<Order>
   */
  private getOrder(orderId: number): Observable<Order> {
    return this.orderService.getOrderForId(orderId).pipe(
      map(orderData => {
        if (ListUtils.isEmpty(orderData.orderSkus)) {
          this.showBaseDataGetApiErrorMessage(null, 'ERRORS.400_03');  // 発注skuがない場合エラー
          throw new Error('getOrder no sku error');
        }
        this.orderData = orderData;
        this.orderSkuList = orderData.orderSkus;
        return orderData;
      }), catchError((error: HttpErrorResponse) => {
        this.showBaseDataGetApiErrorMessage(error);
        throw new Error('getOrder error');
      })
    );
  }

  /**
   * 品番情報を取得.
   * @param partNoId 品番ID
   * @returns Observable<Item>
   */
  private getItem(partNoId: number): Observable<Item> {
    return this.itemService.getItemForId(partNoId).pipe(
      map(itemData => {
        this.itemData = itemData;
        this.isQualityApproved = BusinessCheckUtils.isQualityApprovalOk(itemData); // 優良承認済判定
        return itemData;
      }), catchError((error: HttpErrorResponse) => {
        this.showBaseDataGetApiErrorMessage(error);
        throw new Error('getItem error');
      }));
  }

  /**
   * 配分課リストを取得.
   * @param brandCode ブランドコード
   * @returns Observable<Allocations>
   */
  private getAllocations(brandCode: string): Observable<Allocations> {
    return this.junpcCodmstService.getAllocations({ brand: brandCode, } as JunpcCodmstSearchCondition).pipe(
      map(junpcCodmst => {
        this.allDeliveryAllocationList = junpcCodmst.items;
        const dividedAllocationList = this.divideAllocationToDelivery(this.allDeliveryAllocationList);
        this.deliveryAllocationList = dividedAllocationList.deliveryAllocationList;
        this.noDeliveryAllocationList = dividedAllocationList.noDeliveryAllocationList;
        return dividedAllocationList;
      }), catchError((error: HttpErrorResponse) => {
        this.showBaseDataGetApiErrorMessage(error);
        throw new Error('getAllocations error');
      })
    );
  }

  /**
   * 配分率リストを取得.
   * @param itemData 品番情報
   */
  private getDistributionRatio(itemData: Item): void {
    this.junpcHrtmstService.getHrtmst({
      brandCode: itemData.brandCode,
      itemCode: itemData.itemCode,
      season: itemData.seasonCode
    } as JunpcHrtmstSearchCondition).subscribe(
      junpcHrtmst => this.distributionRatioMastaList = junpcHrtmst.items,
      error => this.showBaseDataGetApiErrorMessage(error));
  }

  // PRD_0123 #7054 add JFE start
  /**
   *  納入場所リストを取得.
   * @param id DBの納品依頼明細リスト
   */
    private getDeliveryLocation(id: number): void{
      this.deliveryRequestService.getDeliveryLocationList(id).subscribe(
        MdeliveryLocation => this.deliveryLocationList = MdeliveryLocation.items,
        error => this.showBaseDataGetApiErrorMessage(error));
  }
  //PRD_0123 #7054 add JFE end

  /**
   * 場所リストを納品場所とその他で2分割して返す。
   * 本社撮影、縫製検品は納品場所ではない。その他は納品場所。
   * 納品場所以外の配列は、本社撮影→縫製検品の順で作成する。
   * @param allocationList 場所(課)リスト
   * @returns 納品場所とその他の場所リスト
   */
  private divideAllocationToDelivery(allocationList: JunpcCodmst[]): Allocations {
    let deliveryAllocationList = allocationList.concat();       // 本社撮影、縫製検品以外の配分課リスト
    const noDeliveryAllocationList = new Array<JunpcCodmst>();  // 本社撮影、縫製検品のみの配分課リスト

    // 縫製検品をdeliveryAllocationListyから除去
    deliveryAllocationList = deliveryAllocationList.filter(allocation => {
      const divisionCode = this.junpcCodmstService.getDivisionCode(allocation);  // 課コード
      if (divisionCode === AllocationCode.SEWING) {
        noDeliveryAllocationList.push(allocation);  // 縫製検品をnoDeliveryAllocationListへ設定
      } else { return true; }
    });

    // 本社撮影をdeliveryAllocationListから除去
    deliveryAllocationList = deliveryAllocationList.filter(allocation => {
      const divisionCode = this.junpcCodmstService.getDivisionCode(allocation);  // 課コード
      if (divisionCode === AllocationCode.PHOTO) {
        noDeliveryAllocationList.push(allocation);  // 本社撮影をnoDeliveryAllocationListへ設定
      } else { return true; }
    });

    return {
      deliveryAllocationList: deliveryAllocationList,
      noDeliveryAllocationList: noDeliveryAllocationList
    };
  }

  /**
   * 画面表示の基礎となるデータ取得APIエラーメッセージを表示する。
   * @param error エラー情報
   * @param errorCode エラーコード
   */
  showBaseDataGetApiErrorMessage(error?: any, errorCode?: string): void {
    this.isInitDataSetted = false; // フォーム非表示
    let overallErrorMsgCode: string = null;
    if (error != null) {
      const apiError = ExceptionUtils.apiErrorHandler(error);
      if (apiError != null) {
        errorCode = apiError.viewErrorMessageCode;
        const firstError = apiError.viewErrors[0];
        overallErrorMsgCode = firstError == null ? null : firstError.viewErrorMessageCode;
      }
    }
    this.fatalErrorMsgCode = errorCode;
    this.loadingService.loadEnd();
    this.overallErrorMsgCode = overallErrorMsgCode == null ? 'ERRORS.ANY_ERROR' : overallErrorMsgCode;
  }

  /**
   * 過去の納品依頼情報を取得.
   * @param orderId 発注ID
   * @returns Observable<Delivery[]>
   */
  private getDeliveryHistoryList(orderId: number): Observable<Delivery[]> {
    const requestParam = { orderId: orderId, idSortDesc: true } as DeliveryRequestSearchConditions;
    return this.deliveryRequestService.getDeliveryRequestList(requestParam).pipe(
      map(deliveryDataList => this.deliveryHistoryList = deliveryDataList.items),
      catchError((error: HttpErrorResponse) => {
        this.showBaseDataGetApiErrorMessage(error);
        throw new Error('getDeliveryHistoryList error');
      }));
  }

  /**
   * メインのFormGroupを作成して設定する。
   * @param orderData 発注情報
   * @param dividedAllocationList 場所リスト
   * @param deliveryData 納品依頼情報
   * @param deliveryHistoryList 納品依頼履歴情報リスト
   * @param purchase 仕入情報
   * @returns FormGroup
   */
  private generateMainFormGroup(orderData: Order, dividedAllocationList: Allocations,
    deliveryData: Delivery, deliveryHistoryList: Delivery[], purchase: Purchase): void {
    const deliveryAllocationList = dividedAllocationList.deliveryAllocationList;
    const noDeliveryAllocationList = dividedAllocationList.noDeliveryAllocationList;

    // 全納品SKUリストを取得
    const deliverySkus = deliveryData == null ? null : this.getAllDeliverySkuList(deliveryData.deliveryDetails);

    // 納品場所、納品場所以外それぞれの発注SKUFormArray作成
    const deliveryOrderSkuFormArray = deliveryAllocationList == null ? null :
      this.generateOrderSkuFormArray(orderData, deliveryAllocationList, deliverySkus, deliveryData, purchase);
    const noDeliveryOrderSkuFormArray = noDeliveryAllocationList == null ? null :
      this.generateOrderSkuFormArray(orderData, noDeliveryAllocationList, deliverySkus, deliveryData, purchase);

    // 発注SKUリスト分、配分数Form作成
    const distributionFormArray = new FormArray([]);
    orderData.orderSkus.forEach(() => {
      distributionFormArray
        .push(this.formBuilder.group({ distribution: [null], initialDistribution: [null] }));
    });
    const drv = new DeliveryRequestValidatorDirective();
    let formGroup = this.formBuilder.group(
      {
        id: [null], // 納品依頼Id
        orderId: [orderData.id],  // 発注ID
        orderNumber: [orderData.orderNumber], // 発注No
        partNoId: [orderData.partNoId], // 品番ID
        partNo: [orderData.partNo], // 品番
        lastDeliveryStatus: [null],
        deliveryRequestAt: [null],  // 納品依頼日
        photoDeliveryAt: [null, drv.forbiddenSundayValidator],  // 撮影納期
        sewingDeliveryAt: [null, drv.forbiddenSundayValidator], // 縫検納期
        deliveryAt: [null, drv.forbiddenSundayValidator], // 製品納期
        deliveryApproveStatus: [0], // 承認ステータス
        memo: [null], // メモ
        distributionRatioType: [null], // 配分率区分
        deliveryDateChangeReasonId: [null], // 納期変更理由Id
        deliveryDateChangeReasonDetail: [null], // 納期変更理由詳細
        nonConformingProductType: [{
          value: orderData.nonConformingProductUnitPrice != null,
          disabled: orderData.nonConformingProductUnitPrice != null
        }], // B級品区分
        nonConformingProductUnitPrice: [{
          value: orderData.nonConformingProductUnitPrice,
          disabled: true
        }], // B級品単価
        faxSend: [true],  // 通知メール送信(ファックス送信フラグ)
        distributionFormArray: distributionFormArray, // 配分数のFormArray
        deliveryOrderSkuFormArray: deliveryOrderSkuFormArray, // 納品場所の発注SKUFormArray
        noDeliveryOrderSkuFormArray: noDeliveryOrderSkuFormArray, // 納品場所以外の発注SKUFormArray
        // PRD_0123 #7054 add JFE start
        deliveryLocationCode: this.deliveryLocationList
        // PRD_0123 #7054 add JFE end

      }, { validator: deliveryAtLotValidator }
    );

    if (deliveryData != null) {
      formGroup = this.setDeliveryValueToForm(orderData, deliveryData, formGroup);  // 納品情報取得済の場合は値をformに設定する。
    }
    this.mainForm = formGroup;

    // VIEW または (!CORRECT かつ 承認済み)の場合は非活性
    if (this.path === Path.VIEW
      || (this.path !== Path.CORRECT && this.deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT)) {
      this.mainForm.disable();
    }
    //PRD_0123 #7054 add JFE start
    // NEWでかつ、TC運用のあるブランドの場合はドロップダウンリストの初期選択は配列の先頭にする
    if (this.path === Path.NEW && this.deliveryLocationList.length > 0) {
      let logisticsCode: string = this.deliveryLocationList[0].logisticsCode;
      formGroup.patchValue({
        deliveryLocationCode: logisticsCode
      });
    }
    //PRD_0123 #7054 add JFE end
    this.onChanges(); // データ変更監視

    this.orderSkuList.forEach(orderSku => {
      const historyLotSum = this.deliveryService.sumLotFromHistoryListBySku(orderSku.colorCode, orderSku.size, deliveryHistoryList);
      // SKU毎の納品依頼済数設定(過去納品数の合計表示に使用)
      orderSku.historyLot = historyLotSum.deliveryHistoryLotSum;

      // 納品可能数設定(formの納期を取得する為this.mainForm設定後に呼ばなければならない)
      orderSku.deliverableLot = this.caluculateDeliverableLotBySku(orderSku.colorCode, orderSku.size,
        historyLotSum.deliveryHistoryLotMixArrivalLotSum, orderSku.returnLot);
    });
    this.onInputDeliveryAt(); // 配分数反映
  }

  /**
   * データ変更監視
   * B級品区分のチェックon・offでB級品単価のコントロールを変更する。
   */
  private onChanges(): void {
    this.mainForm.get('nonConformingProductType').valueChanges.subscribe(checkValue => {
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
   * 納品依頼情報をformに反映する。
   * @param orderData 発注情報
   * @param delivery 納品依頼情報
   * @param form mainForm
   * @returns 納品依頼情報を設定したform
   */
  private setDeliveryValueToForm(orderData: Order, delivery: Delivery, form: FormGroup): FormGroup {
    const deliveryDetails = delivery.deliveryDetails;
    const deliveryAtAry = this.deliveryService.extractDeliveryAtFromDeliveryDetails(deliveryDetails);
    //PRD_0123 #7054 JFE add start
    //納品明細があれば、セレクトボックスの初期選択は納品明細.物流コードにする。
    let logisticsCode : string = ''
    if (deliveryDetails.length > 0) {
      logisticsCode = deliveryDetails[0].logisticsCode;
    }
    //PRD_0123 #7054 JFE add end
    form.patchValue({
      id: delivery.id,  // 納品依頼Id
      orderId: delivery.orderId, // 発注ID
      orderNumber: delivery.orderNumber,  // 発注No
      partNoId: delivery.partNoId,  // 品番ID
      partNo: delivery.partNo,  // 品番
      lastDeliveryStatus: delivery.lastDeliveryStatus,  // 最終納品ステータス
      deliveryRequestAt: deliveryDetails[0].deliveryRequestAt,  // 納品依頼日
      photoDeliveryAt: deliveryAtAry.photoDataDeliveryAt, // 撮影納期
      sewingDeliveryAt: deliveryAtAry.sewingDataDeliveryAt, // 縫検納期
      deliveryAt: deliveryAtAry.divisionDataDeliveryAt, // 納期(その他の課)
      faxSend: deliveryDetails[0].faxSend, // 通知メール送信(ファックス送信フラグ)
      memo: delivery.memo,  // メモ
      distributionRatioType: delivery.distributionRatioType,  // 配分率区分
      deliveryDateChangeReasonId: delivery.deliveryDateChangeReasonId,  // 納期変更理由ID
      deliveryDateChangeReasonDetail: delivery.deliveryDateChangeReasonDetail,  // 納品変更理由詳細
      nonConformingProductType: delivery.nonConformingProductType,  // B級品区分
      //PRD_0123 #7054 JFE mod start
      // nonConformingProductUnitPrice: delivery.nonConformingProductUnitPrice  // B級品単価
      nonConformingProductUnitPrice: delivery.nonConformingProductUnitPrice,  // B級品単価
      deliveryLocationCode: logisticsCode, //最新納品先
      //PRD_0123 #7054 JFE mod end
    });

    // PRD_0123 #7054 add JFE start
    //参照かつ縫製検品もしくは本社撮影で登録されていた場合は納品先選択を非表示にする。
    // 撮影、縫検納品数があるか判定する
    // 縫検納期
    const isSewingDeliveryExist = deliveryDetails.some(deliveryDetail => deliveryDetail.divisionCode === AllocationCode.SEWING);
    // 本社撮影
    const isPhotoDeliveryExist = deliveryDetails.some(deliveryDetail => deliveryDetail.divisionCode === AllocationCode.PHOTO);
    if (this.path === Path.VIEW && (isSewingDeliveryExist || isPhotoDeliveryExist)) {
      this.isShowDeliveryLocationList = false;
    }
    // PRD_0123 #7054 add JFE end

    // t_orderにB級品単価未登録
    // かつB級品区分true時はB級品単価を活性にし、バリデーションチェックを有効にする
    if (orderData.nonConformingProductUnitPrice == null
      && delivery.nonConformingProductType === true) {
      const prodUnitPrice = form.get('nonConformingProductUnitPrice');
      prodUnitPrice.setValidators([Validators.required, Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]);
      prodUnitPrice.enable();
    }

    return form;
  }

  /**
   * 発注SKUFormArrayを作成して返す。
   * @param order 発注情報
   * @param allocationList 場所リスト
   * @param deliverySkus 全納品SKUリスト
   * @param deliveryData 納品依頼情報
   * @param purchase 仕入情報
   * @returns 発注SKUFormArray
   */
  private generateOrderSkuFormArray(order: Order, allocationList: JunpcCodmst[],
    deliverySkus: DeliverySku[], deliveryData: Delivery, purchase: Purchase): FormArray {
    // 発注SKUリストごとに作成
    return this.formBuilder.array(order.orderSkus.map(orderSku =>
      this.formBuilder.group({
        colorCode: [orderSku.colorCode],
        size: [orderSku.size],
        // 納品SKUFormArray
        deliverySkus: this.generateDeliverySkuFormArray(orderSku, order, allocationList, deliverySkus, deliveryData, purchase)
      })
    ));
  }

  /**
   * 納品SKUFormArrayを作成して返す。
   * @param orderSku 発注SKU
   * @param order 発注情報
   * @param allocationList 場所リスト
   * @param deliverySkus 全納品SKUリスト
   * @param deliveryData 納品依頼情報
   * @param purchase 仕入情報
   * @returns 納品SKUFormArray
   */
  private generateDeliverySkuFormArray(orderSku: OrderSku, order: Order, allocationList: JunpcCodmst[],
    deliverySkus: DeliverySku[], deliveryData: Delivery, purchase: Purchase): FormArray {

    // 場所(課)ごとに納品SKUFormArray作成
    return this.formBuilder.array(allocationList.map(allocation => {

      const divisionCode = this.junpcCodmstService.getDivisionCode(allocation);  // 課コード
      const data = this.extractSkuPatchData(deliverySkus, orderSku, divisionCode);

      const deliverySkuForm = this.formBuilder.group(
        {
          id: [data.id], // id
          deliveryDetailId: [data.deliveryDetailId], // 納品明細ID
          deliveryRequestNumber: [data.deliveryRequestNumber], // 納品依頼No
          divisionCode: [divisionCode],     // 課コード
          allocationCode: [this.junpcCodmstService.getAllocationCode(allocation)], // 場所コード
          size: [orderSku.size],            // サイズ
          colorCode: [orderSku.colorCode],  // カラーコード
          deliveryLot: [data.deliveryLot, [ // 納品数量
            Validators.pattern(ValidatorsPattern.POSITIVE_INTEGER_)
          ]],
          deliveryId: [null],               // 納品ID
          orderId: [order.id],              // 発注ID
          orderNumber: [order.orderNumber], // 発注No
          partNoId: [order.partNoId],       // 品番ID
          partNo: [order.partNo],           // 品番No
          distributionRatioId: [this.deliveryService.findDistributionRatioId(deliveryData, divisionCode)] // 配分率ID
        }
      );

      const arrivalCount = this.deliveryService.extractPurchaseSkuArrivalCount(purchase, orderSku, divisionCode);
      if (arrivalCount != null) {
        deliverySkuForm.get('deliveryLot').setValidators([
          Validators.max(arrivalCount),
          Validators.pattern(ValidatorsPattern.POSITIVE_INTEGER_)
        ]);
      }

      return deliverySkuForm;
    }));
  }

  /**
   * @param deliverySkus 全納品SKUリスト
   * @param orderSku 発注SKU
   * @param divisionCode 課コード
   * @returns SKUフォームにpatchするデータ
   */
  private extractSkuPatchData(deliverySkus: DeliverySku[], orderSku: OrderSku, divisionCode: string): DeliverySku {
    // 処理中の課コード及び発注情報のカラーサイズと合致した納品SKU情報をformに設定
    const deliverySku = deliverySkus == null ? null :
      deliverySkus.find(x => this.deliveryService.isMatchDivisionCodeAndSku(x, orderSku, divisionCode));

    return deliverySku == null ? {
      id: null,
      deliveryDetailId: null,
      deliveryRequestNumber: null,
      deliveryLot: null
    } as DeliverySku :
      {
        id: deliverySku.id, // id
        deliveryDetailId: deliverySku.deliveryDetailId, // 納品明細Id
        deliveryRequestNumber: deliverySku.deliveryRequestNumber, // 納品依頼No
        deliveryLot: deliverySku.deliveryLot // 納品数量
      } as DeliverySku;
  }

  /**
   * 指定した配分課の納品数量の入力値を合計して返す。
   * @param allocationTblCode1 配分課テーブルcode1
   * @returns 配分課別の合計
   */
  sumInputLotByDivision(allocationTblCode1: string): number {
    return this.deliveryService.sumInputLotByDivision(allocationTblCode1,
      this.fValNoDeliveryOrderSkus, this.fValDeliveryOrderSkus);
  }

  /**
   * 各1ボタン押下時の処理。
   * 対応する課の納品数量テキスト入力欄全てに+1を設定する。
   * (各1ボタンを押すたびに納品数が1増える)
   * ・空または0の場合、納品数は1
   * ・入力が数字でない場合、処理しない
   * ・9999(限界値)以上の場合、処理しない
   * @param allocationTblCode1 配分課テーブルcode1
   */
  onClickOneEachBtn(allocationTblCode1: string): void {
    const divisionCode = allocationTblCode1.substring(2, 4);  // 課コード(配分課テーブルcode1の末尾2桁)
    this.fCtrlNoDeliveryOrderSkus.forEach(orderSku => {
      const deliverySkusFormArray = orderSku.get('deliverySkus') as FormArray;
      deliverySkusFormArray.controls.forEach(deliverySku => {
        if (deliverySku.get('divisionCode').value === divisionCode) {
          const nowDeliveryLot = Number(deliverySku.get('deliveryLot').value); // 入力された納品数を取得
          // 入力が数字ではない場合、または9999以上の場合、+1しない
          if (isNaN(nowDeliveryLot) || nowDeliveryLot > 9998) {
            return;
          }
          // 空または0の場合、納品数に1をセット
          if (nowDeliveryLot === 0) {
            deliverySku.patchValue({ deliveryLot: 1 });
            return;
          }
          // 空または0でない場合、納品数+1
          deliverySku.patchValue({ deliveryLot: nowDeliveryLot + 1 });
        }
      });
    });
  }

  /**
   * 指定したカラーサイズの納品依頼数(現在のformの入力値)を合計して返す。
   * @param colorCode カラーコード
   * @param size サイズ
   * @returns カラーサイズ別の合計
   */
  sumInputLotBySku(colorCode: string, size: string): number {
    // 指定したカラーサイズと合致する納品SKU取得(納品場所のSKU、納品場所以外のSKU2つ)
    const eachSkus = this.fValAllOrderSkus.filter(deliverySku => deliverySku.colorCode === colorCode && deliverySku.size === size);

    let lot = 0;
    eachSkus.forEach(sku => sku.deliverySkus.forEach(deliverySku => lot += Number(deliverySku.deliveryLot ? deliverySku.deliveryLot : 0)));
    return lot;
  }

  /**
   * 納期入力時イベント
   * 納品可能数と配分数を設定する。
   *
   * 配分数は、納品予定が登録されている場合は納品可能数を設定し
   * 納品予定が登録されていない場合は各SKU単位で発注数 - 納品済数(入荷数があれば、入荷数/未入荷の場合は納品依頼数)を
   * 設定する。
   */
  onInputDeliveryAt(): void {
    // 配分数の配列
    const formattedDeliveryAt = this.deliveryService.getFormattedDeliveryAt(this.mainForm.value);
    const photoDeliveryAt = formattedDeliveryAt.photoDeliveryAt;
    const sewingDeliveryAt = formattedDeliveryAt.sewingDeliveryAt;
    const deliveryAt = formattedDeliveryAt.deliveryAt;

    // 日付をすべてクリアした時は、配分数もクリア
    if (StringUtils.isEmpty(photoDeliveryAt) && StringUtils.isEmpty(sewingDeliveryAt) && StringUtils.isEmpty(deliveryAt)) {
      this.distributionFormArray.reset();
      return;
    }

    // 納品予定がない場合は配分数合計、ある場合は納品可能数合計
    this.totalDistributionable = 0;

    // 納品可能数設定
    this.orderSkuList.forEach((orderSku: OrderSku, index) => {
      const historyLotSum = this.deliveryService.sumLotFromHistoryListBySku(orderSku.colorCode, orderSku.size, this.deliveryHistoryList);

      orderSku.deliverableLot = this.caluculateDeliverableLotBySku(orderSku.colorCode, orderSku.size,
        historyLotSum.deliveryHistoryLotMixArrivalLotSum, orderSku.returnLot);

      // 配分数を算出
      const distribution = this.deliveryPlan == null ?
        orderSku.productOrderLot - historyLotSum.deliveryHistoryLotMixArrivalLotSum : orderSku.deliverableLot;
      this.totalDistributionable += distribution;

      // 配分数を設定する
      // マイナス値になる場合は0を設定する。
      const initialDistribution = distribution < 0 ? 0 : distribution;
      this.fCtrlDistributions[index].patchValue({ distribution: initialDistribution, initialDistribution });
    });
  }

  /**
   * 指定したカラーサイズの納品可能数を計算して返す。
   * @param colorCode カラーコード
   * @param size サイズ
   * @param historyLot 過去の納品数量
   * @param returnLot SKU毎の返品数
   * @returns 納品可能数
   */
  private caluculateDeliverableLotBySku(colorCode: string, size: string, historyLot: number, returnLot: number): number {
    const deliveryPlanLot = this.deliveryService
      .extractDeliveryPlanLotWitinMaxDeliveryAt(this.mainForm.value, this.deliveryPlan, ({ colorCode, size }));
    if (deliveryPlanLot == null) { return 0; }
    return deliveryPlanLot - historyLot + returnLot;
  }

  /**
   * 返品数を全て合計して返す。
   * @param orderSkus 発注SKU情報リスト
   * @returns 返品数合計
   */
  private sumReturnLot(orderSkus: OrderSku[]): number {
    let returnLotSum = 0;
    orderSkus.forEach(sku => returnLotSum += sku.returnLot);
    return returnLotSum;
  }

  /**
   * 納品明細情報リストから納品SKUを全て取得し、リストに格納して返す。
   * @param deliveryDetails 納品明細情報リスト
   * @returns 納品SKUリスト
   */
  private getAllDeliverySkuList(deliveryDetails: DeliveryDetail[]): DeliverySku[] {
    const deliverySkus = [] as DeliverySku[];
    deliveryDetails.forEach(deliveryDetail => Array.prototype.push.apply(deliverySkus, deliveryDetail.deliverySkus));
    return deliverySkus;
  }

  /**
   * 配分ボタン押下処理
   * @param selectedRatioIndex 選択された配分率区分のindex
   */
  onDistribute(selectedRatioIndex: number): void {
    const selectedDivisions = this.extractDistributionRateList(selectedRatioIndex); // 選択された配分率区分の課別情報リスト
    // 納品数量form入力値クリア
    this.onClearDeliveryLotValue();

    // sku1行ごとに処理※非分配先_納品場所(撮影、縫製)と分配先_納品場所は別々
    this.fValDistributions.forEach((distributionValue, index) => {
      const distribution = distributionValue.distribution;  // 入力された配分数
      let totalPatchedLot = 0;  // 配分される数量合計
      const noDeliResult = this.patchDistribution(selectedDivisions, this.fCtrlNoDeliveryOrderSkus[index],
        distribution, this.fValNoDeliveryOrderSkus[index].deliverySkus);
      const deliResult = this.patchDistribution(selectedDivisions, this.fCtrlDeliveryOrderSkus[index],
        distribution);
      totalPatchedLot += noDeliResult.totalPatchedLot;
      totalPatchedLot += deliResult.totalPatchedLot;

      // 配分される数量合計が入力された配分数と差分がある場合は先頭の課で差分調整を行う
      const diff = distribution - totalPatchedLot;  // 差分

      // 配分数が指定されている時に差分が0以外の場合は差分調整を行う
      if (distribution > 0 && diff !== 0) {
        let patchTarget;  // 補正対象の課のform
        let patchedLot;   // 補正対象の課の現在の配分数
        if (noDeliResult.firstIndex != null) {
          patchTarget = (<FormArray> this.fCtrlNoDeliveryOrderSkus[index].get('deliverySkus')).controls[noDeliResult.firstIndex];
          patchedLot = noDeliResult.firstPatchedLot;
        } else {
          patchTarget = (<FormArray> this.fCtrlDeliveryOrderSkus[index].get('deliverySkus')).controls[deliResult.firstIndex];
          patchedLot = deliResult.firstPatchedLot;
        }
        patchTarget.patchValue({ deliveryLot: (patchedLot + diff) });
      }
    });
  }

  /**
   * 納品数量の入力値を全てクリアする
   */
  onClearDeliveryLotValue(): void {
    const orderSkuFormArray: AbstractControl[] = [];
    Array.prototype.push.apply(orderSkuFormArray, this.fCtrlNoDeliveryOrderSkus);
    Array.prototype.push.apply(orderSkuFormArray, this.fCtrlDeliveryOrderSkus);
    orderSkuFormArray.forEach(orderSkuForm =>
      (<FormArray> orderSkuForm.get('deliverySkus')).controls.forEach(deliverySkuForm =>
        deliverySkuForm.patchValue({ deliveryLot: null })));
  }

  /**
   * 指定された配分率区分の課別配分率リストを抽出する。
   * @param ratioIndex 配分率区分のindex
   * @returns 課別配分率リスト
   */
  private extractDistributionRateList(ratioIndex: number): { id: number, shpcd: string, rate: number }[] {
    const selectedDivisions: { id: number, shpcd: string, rate: number }[] = [];
    // 選択された配分区率 ※1件のみ
    const distributionRatioMasta: JunpcHrtmst = this.distributionRatioMastaList[ratioIndex];
    distributionRatioMasta.hrtmstDivisions.forEach(hrtmstDivision => {  // 課別配分率リスト
      selectedDivisions.push(
        {
          id: hrtmstDivision.id,  // 配分率id
          shpcd: hrtmstDivision.shpcd,  // 店舗コード
          // 全体に対しての配分率計算※小数4桁以下切り捨て(1000桁の入力に対して一の位まで計算)
          rate: Math.floor((hrtmstDivision.hritu / distributionRatioMasta.totalHritu) * Math.pow(1000, 1)) / Math.pow(1000, 1)
        }
      );
    });
    return selectedDivisions;
  }

  /**
   * 配分値をformに設定する処理。
   * @param selectedDivisions 選択された配分率区分の課別情報リスト
   * @param orderSkus 発注skuFormArray
   * @param distributionValue 配分数入力値
   * @param beforeDistributionDeliverySkus 配分前に入力されていた納品数量のリスト(任意項目)
   * @return 最初にpatchされた課のindex、最初にpatchされた納品数量、patchした納品数量合計
   */
  private patchDistribution(selectedDivisions: { id: number, shpcd: string, rate: number }[],
    orderSkus: AbstractControl, distributionValue: number, beforeDistributionDeliverySkus?: DeliverySkuFormValue[])
    : { firstIndex: number, firstPatchedLot: number, totalPatchedLot: number } {
    let firstIndex = null;
    let firstPatchedLot = null;
    let totalPatchedLot = 0;
    const deliverySkusFormArray = (<FormArray> orderSkus.get('deliverySkus')).controls;
    deliverySkusFormArray.forEach((deliverySku, index) => {  // 列ごとに処理
      selectedDivisions.some(selectedDivision => {  // 選択された配分率区分の課と合致した場合に値設定する
        if (selectedDivision.shpcd === deliverySku.get('divisionCode').value) {
          const setValue = Math.floor(distributionValue * selectedDivision.rate);
          deliverySku.patchValue({
            distributionRatioId: selectedDivision.id,
            deliveryLot: setValue > 0 ? setValue : null
          });
          firstIndex = firstIndex == null ? index : firstIndex;
          firstPatchedLot = firstPatchedLot == null ? setValue : firstPatchedLot;
          totalPatchedLot += setValue;
          return true;
        }
      });
      // 配分数が設定されていないかつ、配分前に入力されていた納品数量のリストがある場合は、配分前に入力されていた数量をセットする
      if (deliverySku.get('deliveryLot').value == null && beforeDistributionDeliverySkus != null) {
        const beforDeliveryLot = Number(beforeDistributionDeliverySkus[index].deliveryLot);
        // デバッグしたいときはコメント外してください. push時はコメントアウトしてください.
        // console.log('beforDeliveryLot', beforDeliveryLot);
        deliverySku.patchValue({ deliveryLot: beforDeliveryLot > 0 ? beforDeliveryLot : null });
        totalPatchedLot += beforDeliveryLot;
      }
    });
    return { firstIndex: firstIndex, firstPatchedLot: firstPatchedLot, totalPatchedLot: totalPatchedLot };
  }

  /**
   * formの値からPOSTで必要な納品明細情報リストを作成して返す。
   * 納品明細情報は課ごとに1レコード。
   * @param formValue 発注SKUFormArray
   * @param deliveryAllocationList 納品場所の場所リスト
   * @param noDeliveryAllocationList 納品場所以外の場所リスト
   * @returns 納品明細情報リスト
   */
  private prepareDeliveryDetailsPostData(formValue: any, deliveryAllocationList: JunpcCodmst[],
    noDeliveryAllocationList: JunpcCodmst[]): any[] {
    const retDeliveryDetails = [];
    const currentDate = Moment(new Date()).format('YYYY/MM/DD').toString();

    // 納品SKUリストから納品数量が入力されているデータを抽出しリストに格納する。
    const orderSkuValues = [];
    const deliveryOrderSkuValues = formValue.deliveryOrderSkuFormArray;     // 納品場所の発注SKUFormArrayのvalue
    const noDeliveryOrderSkuValues = formValue.noDeliveryOrderSkuFormArray; // 納品場所以外の発注SKUFormArrayのvalue
    Array.prototype.push.apply(orderSkuValues, deliveryOrderSkuValues);
    Array.prototype.push.apply(orderSkuValues, noDeliveryOrderSkuValues);
    const allDeliverySkus = this.extractInputtedDeliverySku(orderSkuValues);

    // 納品SKUのデータを場所リストごとにまとめる
    const allocationList = [];
    Array.prototype.push.apply(allocationList, deliveryAllocationList);
    Array.prototype.push.apply(allocationList, noDeliveryAllocationList);
    allocationList.forEach(allocation => {
      const divisionCode = this.junpcCodmstService.getDivisionCode(allocation);  // 課コード
      let deliverydetailValue = null;
      if (this.deliveryDetailList != null && this.deliveryDetailList.length > 0) {
        // 課の納品明細を取得
        this.deliveryDetailList.some(deliveryDetail => {
          if (deliveryDetail.divisionCode === divisionCode) {
            deliverydetailValue = deliveryDetail;
            return true;
          }
        });
      }
      const deliverySkusValue = allDeliverySkus.filter(deliverySku => deliverySku.divisionCode === divisionCode); // 処理中の場所のレコードを抽出する。

      // postに必要な納品明細情報の項目を設定する。
      if (deliverySkusValue.length > 0) {
        const deliveryAt = this.deliveryService.getDeliveryAtByDivisionCode(divisionCode, formValue);
        //PRD_0129 #9946 JFE del start
        //const allocationCode = this.junpcCodmstService.getAllocationCode(allocation);  // 場所コード
        //PRD_0129 #9946 JFE del end
        // // PRD_0123 #7054 mod JFE start
        // // const logisticsCode = this.junpcCodmstService.getLogisticsCode(allocation);   // 物流コード
        //物流コード
        let logisticsCode: string = "";
        if (this.mainForm.get('deliveryLocationCode').value == null) {
           //表示されていない場合は、現行通りにする。
          logisticsCode = this.junpcCodmstService.getLogisticsCode(allocation);   // 物流コード
        } else {
          //課コードが17:本社撮影もしくは、18：縫製検品の場合は現行通り
          if (divisionCode === AllocationCode.PHOTO || divisionCode === AllocationCode.SEWING) {
          logisticsCode = this.junpcCodmstService.getLogisticsCode(allocation);   // 物流コード
          } else {
          //本社撮影、縫製検品以外なら、画面で選択した納品先
          logisticsCode = this.mainForm.get('deliveryLocationCode').value;
          }
        }
        // // PRD_0123 #7054 mod JFE end
        //PRD_0129 #9946 JFE add start
        const allocationCode = logisticsCode.substring(0, 1);  // 場所コード
        //PRD_0129 #9946 JFE add end
        retDeliveryDetails.push({
          id: deliverydetailValue != null ? deliverydetailValue.id : null, // 納品明細id
          deliveryId: deliverydetailValue != null ? deliverydetailValue.deliveryId : null, // 納品id
          deliveryNumber: deliverydetailValue != null ? deliverydetailValue.deliveryNumber : null, // 納品No
          deliveryRequestNumber: deliverydetailValue != null ? deliverydetailValue.deliveryRequestNumber : null, // 納品依頼No
          deliveryRequestAt: currentDate,   // 納品依頼日
          divisionCode: divisionCode,       // 課コード
          allocationCode: allocationCode,   // 場所コード
          carryType: deliverydetailValue != null ? deliverydetailValue.carryType : CarryType.NORMAL,  // キャリー区分
          logisticsCode: logisticsCode,     // 物流コード
          correctionAt: deliveryAt,         // 修正納期
          deliverySkus: deliverySkusValue,  // 納品SKU
          faxSend: formValue.faxSend,       // ファックス送信フラグ
          distributionRatioId: deliverySkusValue[0].distributionRatioId  // 配分率ID
        });
      }
    });
    return retDeliveryDetails;
  }

  /**
   * 納品SKUリストから納品数量が入力されているデータを抽出しリストに格納する。
   * @param orderSkuValues 発注SKUFormArrayのValue
   * @returns allDeliverySkus
   */
  private extractInputtedDeliverySku(orderSkuValues: any): any[] {
    const allDeliverySkus = [];
    orderSkuValues.forEach(orderSku => {
      const deliverySkusValues = orderSku.deliverySkus;
      const inputtedDeliverySkus = deliverySkusValues.filter(deliverySkus => deliverySkus.deliveryLot);
      Array.prototype.push.apply(allDeliverySkus, inputtedDeliverySkus);
    });
    return allDeliverySkus;
  }

  /**
   * 処理中の発注IDの新規登録画面を表示する。
   */
  showNewRegistPage(): void {
    const orderId = this.path === Path.NEW ? Number(this.route.snapshot.queryParamMap.get('orderId')) : this.orderIdInDelivery;
    this.router.navigate([this.nextUrl, Path.NEW], { queryParams: { orderId: orderId } });
  }

  /**
   * 得意先配分画面へ遷移する.
   */
  //PRD_0123 #7054 JFE mod start
  // movePagesToDeliveryStore(): void {
  //   const orderId = this.path === Path.NEW ? Number(this.route.snapshot.queryParamMap.get('orderId')) : this.orderIdInDelivery;
  //   const deliveryId = this.mainForm.value.id as number;
  //   if (deliveryId == null) {
  //     this.router.navigate([this.nextStoreUrl, Path.NEW], { queryParams: { orderId: orderId } });
  //     return;
  //   }

  //   const nextPath = this.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT ? Path.EDIT : Path.VIEW;
  //   this.router.navigate([this.nextStoreUrl, deliveryId, nextPath], { queryParams: { orderId: orderId } });
  // }
  movePagesToDeliveryStore(): void {
    const orderId = this.path === Path.NEW ? Number(this.route.snapshot.queryParamMap.get('orderId')) : this.orderIdInDelivery;
    const deliveryId = this.mainForm.value.id as number;
    const id = this.itemData.id
    if (deliveryId == null) {
      this.router.navigate([this.nextStoreUrl, Path.NEW], { queryParams: { orderId: orderId,id:id}});
      return;
    }

    const nextPath = this.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT ? Path.EDIT : Path.VIEW;
    this.router.navigate([this.nextStoreUrl, deliveryId, nextPath], { queryParams: { orderId: orderId,id:id } });
  }
  //PRD_0123 #7054 JFE mod end

  /**
   * 発注時納期より遅れる納期が存在するか
   * @return true:遅れる納期あり/false:遅れる納期なし
   */
  isDeliveryAtLate(): boolean {
    const productDeliveryAt = this.orderData.productCorrectionDeliveryAt; // 製品修正納期を使用
    return this.deliveryService.isDeliveryAtLate(productDeliveryAt, this.mainForm.value);
  }

  /**
   * 登録・更新・訂正保存ボタン押下時の処理。
   * @param SubmitType 送信ボタンの種類
   */
  onSubmitUpsert(submitType: SubmitType): void {
    this.clearAllMessage();
    this.submitted = true;

    if (submitType === SubmitType.UPDATE) {
      this.isUpdate = true;
    }

    if (this.mainForm.invalid) {
      // デバッグしたいときはコメント外してください. push時はコメントアウトしてください.
      // this.formUtils.logValidationErrors(this.mainForm);

      this.isDeliveryLotMaxError = this.deliveryService.isDeliveryLotMaxError(this.mainForm, this.path);
      this.isDeliveryLotPatternError = this.deliveryService.isDeliveryLotPatternError(this.mainForm);

      if (submitType === SubmitType.UPDATE && this.fErr && this.fErr.deliveryAtLotRequired) { // 納品明細がない場合、納品依頼削除と同じ扱い
        // 確認モーダルを表示する：OK→納品依頼を削除し新規登録画面へ遷移、キャンセル→何もしない
        let modalMessage = '';
        this.translate.get('INFO.NO_DELIVERY_DETAIL_AT_SUBMIT_ALERT_MESSAGE').subscribe((msg: string) => modalMessage = msg);
        this.setConfirmModalAndDelete(modalMessage);
      } else {
        this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      }
      return;
    }

    // 今回入力された納品数量取得
    const sumCurrentInputLot = this.deliveryService.sumDivisionScreenInputLot(this.allDeliveryAllocationList,
      this.fValNoDeliveryOrderSkus, this.fValDeliveryOrderSkus);

    // 納品依頼済数を取得(過去納品数).ただし編集中の納品依頼は除く
    const deliveryId = this.mainForm.get('id').value;
    const excludedHistoryList = this.deliveryHistoryList.filter(deliveryHistory =>
      deliveryHistory.id !== NumberUtils.toInteger(deliveryId));
    const historyLot = this.deliveryService.sumLotFromHistoryList(excludedHistoryList);
    // 返品数
    const totalReturnLot = this.sumReturnLot(this.orderData.orderSkus);

    // 過去納品数と今回の入力数合計 - 返品数合計
    const allDeliveryLot = historyLot + sumCurrentInputLot - totalReturnLot;

    // 納品数合計が発注数を超えるか
    const orderLot = this.orderData.quantity;
    if (this.deliveryService.isThresholdRateOver(allDeliveryLot, orderLot, this.threshold)) {
      // 閾値超過エラー
      this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      ExceptionUtils.displayErrorInfo('deliveryLotErrorInfo', 'ERRORS.VALIDATE.DELIVERY_LOT_RATE_THRESHOLD_OVER');
      return;
    }

    const lastDeliveryStatus = this.mainForm.get('lastDeliveryStatus').value as LastDeliveryStatus;
    const isLastDeliveryRegistered = LastDeliveryStatus.LAST === lastDeliveryStatus;

    // 納品数が発注数を超えていない、かつ最終納品として登録済の場合、確認モーダルを表示しない
    const isOverLot = allDeliveryLot > orderLot;
    if (!isOverLot && isLastDeliveryRegistered) {
      this.handleSubmit(submitType);
      return;
    }

    const modalRef = this.modalService.open(DeliverySubmitConfirmModalComponent);
    // PRD_0125 #9079 add JFE start
    modalRef.componentInstance.isDRUpDate = this.storeRegisteredFlg;
    // PRD_0125 #9079 add JFE end
    modalRef.componentInstance.isOverLot = isOverLot;
    modalRef.componentInstance.isLastDeliveryRegistered = isLastDeliveryRegistered;
    modalRef.componentInstance.submitType = submitType;
    modalRef.result.then((isLastDelivery: boolean) => {
      this.mainForm.patchValue({ lastDeliveryStatus: isLastDelivery ? LastDeliveryStatus.LAST : LastDeliveryStatus.NORMAL });
      this.handleSubmit(submitType);
    }, noop);  // バツボタンクリック時は何もしない
  }

  /**
   * Submitボタンの種類による処理の切り分け.
   * @param submitType 送信ボタンの種類
   */
  private handleSubmit(submitType: SubmitType): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;

    const formValue = this.mainForm.getRawValue();
    formValue.deliveryDetails = this.prepareDeliveryDetailsPostData(formValue, this.deliveryAllocationList, this.noDeliveryAllocationList);

    switch (submitType) {
      case SubmitType.ENTRY:
        this.postProcessSubmit(
          this.deliveryRequestService.postDeliveryRequest(formValue), PreEventParam.CREATE, Path.EDIT
        );
        break;
      case SubmitType.UPDATE:
        this.postProcessSubmit(
          this.deliveryRequestService.putDeliveryRequest(formValue), PreEventParam.UPDATE, Path.EDIT
        );
        break;
      case SubmitType.CORRECT:
        this.postProcessSubmit(
          this.deliveryRequestService.correctDeliveryRequest(formValue), PreEventParam.CORRECT, Path.VIEW
        );
        break;
      case SubmitType.APPROVE:
        formValue.deliveryApproveStatus = DeliveryApprovalStatus.ACCEPT;
        // formValue.idは承認時必要(レスポンスにない為)
        this.postProcessSubmit(
          this.deliveryRequestService.approvalDeliveryRequest(formValue), PreEventParam.APPROVE, Path.VIEW, formValue
        );
        break;
      case SubmitType.DELETE:
        this.postProcessSubmit(
          this.deliveryRequestService.deleteDeliveryRequest(formValue.id), PreEventParam.DELETE, Path.NEW
        );
        break;
      case SubmitType.DIRECT_CONFIRM:
        // formValue.idは直送確定時必要(レスポンスにない為)
        this.postProcessSubmit(
          this.deliveryRequestService.directConfirmDeliveryRequest(formValue), PreEventParam.DIRECT_CONFIRM, Path.VIEW, formValue
        );
        break;
      default:
        break;
    }
  }

  /**
   * Submit後の処理.
   * @param observable Observable<FukukitaruOrder>
   * @param preEvent 処理したイベント
   * @param nextPath 遷移先パス
   * @param formValue フォーム値(承認時のみ必要)
   */
  private postProcessSubmit(observable: Observable<Delivery>, preEvent: number, nextPath: Path, formValue?: any) {
    observable.subscribe(
      result => {
        this.loadingService.loadEnd();
        this.isBtnLock = false;

        // submit後の画面表示
        const orderId = Number(this.route.snapshot.queryParamMap.get('orderId'));

        // 削除後は新規登録画面へ遷移
        if (PreEventParam.DELETE === preEvent) {
          this.router.navigate([this.nextUrl, nextPath], { queryParams: { orderId: orderId, preEvent: preEvent } });
          return;
        }

        // 承認時と直送確定時はレスポンスがこないのでformのIDを使う
        const deliveryId = (PreEventParam.APPROVE === preEvent || PreEventParam.DIRECT_CONFIRM === preEvent) ? formValue.id : result.id;
        this.router.navigate([this.nextUrl, deliveryId, nextPath], { queryParams: { orderId: orderId, preEvent: preEvent } })
          .then(() => {
            if (PreEventParam.UPDATE === preEvent || PreEventParam.DIRECT_CONFIRM === preEvent) {
              window.location.reload(); // 同じパスでは遷移しないため、リロードを実施
            }
          });
      }, error => this.handleApiError(error));
  }

  /**
   * APIエラーハンドル
   * @param error APIレスポンスエラー情報
   * @param optionParam 補足パラメータ
   */
  private handleApiError(error: any, optionParam?: { code: string }): void {
    const apiError = ExceptionUtils.apiErrorHandler(error);
    this.loadingService.loadEnd();
    this.isBtnLock = false;

    if (apiError != null && apiError.viewErrors != null) {
      const firstError = apiError.viewErrors[0];
      if (firstError != null) {
        switch (firstError.code) {
          /* 新規登録画面へ遷移し、エラーを表示。または遷移なしでエラーを表示 */
          case '400_02':
            let optionParamCode = '';
            if (optionParam == null || StringUtils.isEmpty(optionParamCode = optionParam.code)) {
              // optionParamなし(想定外)
              this.showBaseDataGetApiErrorMessage(error);
              return;
            }
            // (編集画面表示・参照画面表示・訂正可否確認時)納品依頼が既に削除
            this.routeNewAfterError(optionParamCode);
            break;

          /* 遷移なしでエラーを表示 */
          case '400_D_06': // B級品単価requiredエラー
            this.fCtrl.nonConformingProductUnitPrice.setErrors({ 'required': true });
            this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
            break;
          case '400_D_07': // B級品単価patternエラー
            this.fCtrl.nonConformingProductUnitPrice.setErrors({ 'pattern': true });
            this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
            break;
          case '400_D_23': // 納品数量が閾値超過エラー
            ExceptionUtils.displayErrorInfo('deliveryLotErrorInfo', 'ERRORS.VALIDATE.DELIVERY_LOT_RATE_THRESHOLD_OVER');
            this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
            break;

          /* 参照画面または編集画面へ遷移し、エラーを表示 */
          case '400_D_11': // (更新、訂正時)納品依頼削除不可
            // 承認済の場合、参照へ。未承認の場合、編集へ
            const path = this.deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT ? Path.VIEW : Path.EDIT;
            this.routeEditOrViewAfterError(path, firstError.code);
            break;

          /* 編集画面へ遷移し、エラーを表示 */
          case '400_D_16': // (訂正時)納品依頼未承認
            this.routeEditOrViewAfterError(Path.EDIT, firstError.code);
            break;

          /* 新規登録画面へ遷移し、エラーを表示 */
          case '400_D_15': // (SQキャンセル、納品依頼訂正時)納品依頼が既に削除
          case '400_D_09': // (更新時)納品依頼が既に削除
          case '400_D_10': // (承認時)納品依頼が既に削除
            this.routeNewAfterError(firstError.code);
            break;

          /* 新規登録画面へ遷移.※エラーは表示しない */
          case '400_D_18': // (削除時)納品依頼が既に削除
            this.router.navigate([this.nextUrl, Path.NEW], {
              queryParams: { orderId: this.mainForm.get('orderId').value, preEvent: PreEventParam.DELETE }
            });
            break;

          /* 参照画面へ遷移し、エラーを表示 */
          /*
           * (承認時)納品依頼が既に承認
           * queryParamsがAPPROVEの場合は成功MSGのため、承認エラーの場合queryParamsはAPPROVE_ERRORを渡す
           * 承認は責任問題がかかわってくるため、重複削除と同じ動きにはせず、承認済みのMSGを出す必要がある
           */
          case '400_D_04':
          case '400_D_05':  // (削除時)納品依頼が既に承認(※メーカー権限のみエラー)
          case '400_D_08':  // (更新時)納品依頼が既に承認(※メーカー権限のみエラー)
          case '400_D_12':  // (訂正時)納品依頼追加不可
          case '400_D_17':  // (訂正時)取引先権限
          case '400_D_19':  // 訂正不可データ(全済)
          case '400_D_20':  // 訂正不可データ(費目)
          case '400_D_21':  // 訂正不可データ(送信済)
          case '400_S_01':  // (承認後削除時)SQ疎通エラー
          case '400_D_13':  // (承認後削除時)SQその他エラー
          case '400_D_14':  // (承認後削除時)SQNG
          case '400_D_22':  // (承認後削除時)SQ未連携
            this.routeEditOrViewAfterError(Path.VIEW, firstError.code);
            break;

          case '400_D_25':  // (訂正時)仕入数量超過
          case '400_D_26':  // (削除時・訂正保存時)入荷済
            this.showErrorModal(error);
            break;

          default:
            this.overallErrorMsgCode = 'ERRORS.ANY_ERROR';
            break;
        }
      }
    }
  }

  /**
   * エラーモーダルを表示する.
   * @param error エラー情報
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  private showErrorModal = (error: any): Observable<boolean> => this.messageConfirmModalService.openErrorModal(error);

  /**
   * エラーハンドルから新規登録画面へ遷移する.
   * @param errorCode errorCode
   */
  private routeNewAfterError(errorCode: string) {
    let orderId: number = null;
    if (this.mainForm != null) {
      orderId = this.mainForm.get('orderId').value as number; // 発注ID
    }
    // formにセットされてない場合はURLから発注IDを取得する
    orderId = orderId != null ? orderId : Number(this.route.snapshot.queryParamMap.get('orderId'));

    this.router.navigate([this.nextUrl, Path.NEW], { queryParams: { orderId: orderId, errorCode: errorCode } });
  }

  /**
   * エラーハンドル後、編集または参照画面へ遷移する.
   * @param path path
   * @param errorCode errorCode
   */
  private routeEditOrViewAfterError(path: string, errorCode: string = null) {
    let deliveryId: number = null;
    let orderId: number = null;
    if (this.mainForm != null) {
      deliveryId = this.mainForm.get('id').value as number; // 納品ID
      orderId = this.mainForm.get('orderId').value as number; // 発注ID
    }
    // formにセットされてない場合はURLから納品IDを取得する
    const routeId = Number(this.route.snapshot.params['id']);
    deliveryId = deliveryId != null ? deliveryId : routeId;
    // formにセットされてない場合はURLから発注IDを取得する
    orderId = orderId != null ? orderId : Number(this.route.snapshot.queryParamMap.get('orderId'));

    // URLが同じで遷移できない場合は初期化関数を呼ぶ
    const routePath = this.route.snapshot.url[this.route.snapshot.url.length - 1].path as string;
    if (deliveryId === routeId && path === routePath) {
      const urlParams: DeliveryRequestUrlParams = {
        path: path,
        deliveryId: deliveryId,
        errorCode: errorCode,
        // PRD_0044 del SIT start
        //sqCancelStatus: null,
        // PRD_0044 del SIT end
        preEvent: null
      };
      this.initializeDisplayByRouting(urlParams);
      return;
    }

    // 遷移(納品依頼が削除されていた場合に新規登録画面へ遷移する為、queryParamsにorderIdをつける)
    this.router.navigate([this.nextUrl, deliveryId, path], { queryParams: { orderId: orderId, errorCode: errorCode } });
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
   * 確認モーダルを表示し、削除処理を行う。
   * @param message モーダルに表示するメッセージ
   */
  private setConfirmModalAndDelete(message: string): void {
    // 確認モーダルを表示
    const modalRef = this.modalService.open(MessageConfirmModalComponent);
    modalRef.componentInstance.message = message;
    modalRef.result.then((result: string) => {
      if (result === 'OK') { this.handleSubmit(SubmitType.DELETE); }
    }, noop);  // バツボタンクリック時は何もしない
  }

  /**
   * 納品依頼承認処理
   */
  onSubmitApproveDeliveryData(): void {
    this.clearAllMessage();

    // 入力フォームに変更があるか判断する。変更がある場合承認ボタンをロックし、承認前に更新するメッセージを表示
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
   * 訂正ボタン押下時の警告モーダルを表示する。
   */
  openCorrectAlertModal(): void {
    this.clearAllMessage();
    this.submitted = true;

    // 確認モーダルを表示
    let modalMessage = '';
    // PRD_0044 del SIT start
    //this.translate.get('INFO.DELIVERY_CORRECT_ALERT_MESSAGE').subscribe((msg: string) => modalMessage = msg);
    // PRD_0044 del SIT end

    // 納品明細情報の納品依頼書発行フラグ=trueの場合、既に納品依頼書が発行済の旨のメッセージモーダルを表示
    this.deliveryDetailList.some(deliveryDetail => {
      if (deliveryDetail.deliverySheetOut === true) {
        // PRD_0044 mod SIT start
        //modalMessage += '<br><br><span style="color: red;">';
        modalMessage += '<span style="color: red;">';
        // PRD_0044 mod SIT end
        this.translate.get('INFO.DELIVERY_CORRECT_SHEET_OUTED_COMFIRM_MESSAGE').subscribe((value: string) => modalMessage += value);
        modalMessage += '</span>';
        return true;
      }
    });

    // PRD_0044 add SIT start
    if (FormUtils.isNotEmpty(modalMessage)) {
      // PRD_0044 add SIT end
      const modalRef = this.modalService.open(MessageConfirmModalComponent);
      modalRef.componentInstance.message = modalMessage;
      modalRef.result.then((result: string) => {
        if (result !== 'OK') { return; }
        // 確認OKの場合、URLから納品依頼IDを取得して訂正画面へ遷移
        const deliveryId: number = Number(this.route.snapshot.params['id']);
        this.router.navigate([this.nextUrl, deliveryId, Path.CORRECT], { queryParams: { orderId: this.orderData.id } });
      }, noop);  // バツボタンクリック時は何もしない
      // PRD_0044 add SIT start
    } else {
        // URLから納品依頼IDを取得して訂正画面へ遷移
        const deliveryId: number = Number(this.route.snapshot.params['id']);
        this.router.navigate([this.nextUrl, deliveryId, Path.CORRECT], { queryParams: { orderId: this.orderData.id } });
    }
    // PRD_0044 add SIT end
  }

  // PRD_0044 del SIT start
  ///**
  // * SQキャンセルレスポンスによる処理の分岐
  // * 以下場合は納品依頼参照画面へ遷移し、エラーを表示
  // * ・ロック中(ログインユーザー以外)
  // * ・その他の場合、ステータスがSQキャンセルOK以外
  // *
  // * @param sqResponseData SQのレスポンスデータ
  // * @param deliveryId 納品依頼ID
  // */
  //private organizeBySqCancelResponse(sqResponseData: SpecialtyQubeCancelResponse, deliveryId: number): void {
  //  const orderId = Number(this.route.snapshot.queryParamMap.get('orderId'));
  //  if (sqResponseData.sqLockUserId != null) {
  //    // SQロックユーザーIDがある場合、別ユーザがロック中
  //    this.router.navigate([this.nextUrl, deliveryId, Path.VIEW],
  //      { queryParams: { orderId: orderId, sqCancelStatus: SpecialtyQubeCancelStatusType.LOCKED_BY_OTHER_USER } });
  //    return;
  //  }
  //
  //  // その他の場合、ステータスがSQキャンセルOK以外の場合は、納品依頼参照画面へ遷移し、エラーを表示：
  //  const sqCancelStatus = Number(sqResponseData.status);
  //  if (SpecialtyQubeCancelStatusType.CANCEL_OK !== sqCancelStatus) {
  //    this.router.navigate([this.nextUrl, deliveryId, Path.VIEW], { queryParams: { orderId: orderId, sqCancelStatus: sqCancelStatus } });
  //  }
  //}
  // PRD_0044 del SIT end

  /**
   * 合計配分指定モーダル表示.
   */
  openDistributionModal(): void {

    const modalRef = this.modalService.open(DeliveryDistributionModalComponent);

    modalRef.componentInstance.totalDistributionValue = this.totalDistributionable;

    // モーダル閉じた時の戻り
    modalRef.result.then((result) => {
      /** 配分率 */
      let distributionRate = 0;
      /** 一回あたりの配分合計数 */
      let total = 0;

      if (result.distributionType === DeliveryDistributionSpecificationType.DISTRIBUTION_RATE) {
        // 配分率で指定
        const rate = result.specifiedValue > 0 ? result.specifiedValue / 100 : 0;  // ％で入力された値を計算用に変換する
        distributionRate = Math.floor(rate * 100) / 100;    // 小数点第三位以下を切捨て
        total = Math.floor(this.totalDistributionable * distributionRate);
      } else {
        // 配分数で指定
        const rate = result.specifiedValue > 0 ? result.specifiedValue / this.totalDistributionable : 0; // 切り捨て前の配分率
        distributionRate = Math.floor(rate * 100) / 100;    // 小数点第三位以下を切捨て
        total = result.specifiedValue;
      }
      this.setDistribution(total, distributionRate);

    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 配分率指定の配分数計算処理
   * @param total 一回あたりの配分合計数
   * @param distributionRate 一回あたりの配分率(配分数指定した場合のみ使用する)
   */
  private setDistribution(total: number, distributionRate: number): void {
    /** 差分確認用 */
    let distributionTotal = 0;

    const distributionFormArray = (<FormArray> this.mainForm.get('distributionFormArray')).controls;

    // 配分数を設定する
    this.orderSkuList.forEach((orderSku: OrderSku, index) => {
      // 納品可能数と配分率を乗算
      const multiplicand = this.deliveryPlan == null ? distributionFormArray[index].get('initialDistribution').value
        : orderSku.deliverableLot;
      const distributionValue = Math.floor(multiplicand * distributionRate);

      distributionTotal += distributionValue;

      distributionFormArray[index].patchValue(
        { 'distribution': distributionValue < 0 ? 0 : distributionValue });  // 配分数に設定
    });

    // 差分があれば先頭に加算する
    const diff = total - distributionTotal;
    if (diff > 0) {
      const firstIndexValue = distributionFormArray[0].get('distribution').value;
      const firstIndexAddValue = firstIndexValue + Math.ceil(diff);
      distributionFormArray[0].patchValue({ 'distribution': firstIndexAddValue });
    }
  }

  /**
   * 納品依頼訂正画面項目の非活性制御をおこなう。
   */
  private disableDeliveryCorrectDisplayItem(deliveryDetails: DeliveryDetail[]): void {
    // 納期の非活性制御
    this.disableDeliveryAt();
    // 配分関連の非活性制御
    this.disableDistribution();
    // 納品場所・納品場所以外の発注SKUフォームの非活性制御
    this.disableDeliveryOrderSkus(deliveryDetails);
  }

  /**
   * 納期を非活性にする。
   */
  private disableDeliveryAt(): void {
    // 納期値3種類取得：
    const sewingDeliveryAt = this.mainForm.get('sewingDeliveryAt').value;  // 縫検納期
    const photoDeliveryAt = this.mainForm.get('photoDeliveryAt').value;    // 撮影納期
    const productDeliveryAt = this.mainForm.get('deliveryAt').value;       // 製品納期

    if (!FormUtils.isEmpty(sewingDeliveryAt) && FormUtils.isEmpty(photoDeliveryAt) && FormUtils.isEmpty(productDeliveryAt)) {
      // 縫検納期のみがある場合は、撮影納期と製品納期を非活性にする
      this.fCtrl.photoDeliveryAt.disable();
      this.fCtrl.deliveryAt.disable();
    }

    if (FormUtils.isEmpty(sewingDeliveryAt) && !FormUtils.isEmpty(photoDeliveryAt) && FormUtils.isEmpty(productDeliveryAt)) {
      // 撮影納期のみがある場合は、縫検納期と製品納期を非活性にする
      this.fCtrl.sewingDeliveryAt.disable();
      this.fCtrl.deliveryAt.disable();
    }

    if (FormUtils.isEmpty(sewingDeliveryAt) && FormUtils.isEmpty(photoDeliveryAt) && !FormUtils.isEmpty(productDeliveryAt)) {
      // 製品納期のみがある場合は、撮影納期と縫検納期を非活性にする
      this.fCtrl.photoDeliveryAt.disable();
      this.fCtrl.sewingDeliveryAt.disable();
    }
  }

  /**
   * 配分関連項目を非活性にする。
   * ・配分率区分プルダウン
   * ・配分数
   */
  private disableDistribution(): void {
    this.fCtrl.distributionRatioType.disable(); // 配分率区分プルダウン
    this.fCtrl.distributionFormArray.disable(); // 配分数
  }

  /**
   * 納品場所・納品場所以外の発注SKUフォームの非活性制御
   * 場所コードが登録済みの納品明細の場所コードと異なる課の納品数を非活性にする。
   * @param deliveryDetails DBの納品依頼明細リスト
   */
  private disableDeliveryOrderSkus(deliveryDetails: DeliveryDetail[]): void {
    // DBに登録されている納品依頼明細の先頭1件より、場所コードを取得する
    const registedAllocationCode = deliveryDetails[0].logisticsCode.substring(0, 1);  // 場所コード(納品依頼明細テーブル物流コードの先頭1桁)

    // 撮影、縫検納品数があるか判定する
    // 縫検納期
    const isSewingDeliveryExist = deliveryDetails.some(deliveryDetail => deliveryDetail.divisionCode === AllocationCode.SEWING);
    // 本社撮影
    const isPhotoDeliveryExist = deliveryDetails.some(deliveryDetail => deliveryDetail.divisionCode === AllocationCode.PHOTO);

    // 撮影、縫検の納品数がない場合は、
    // 納品場所以外(撮影+縫検)のOrderSkus、場所コードが登録値と異なる納品場所(製品)のOrderSkusを非活性にする
    if (!isSewingDeliveryExist && !isPhotoDeliveryExist) {
      this.disableNoDeliveryAndDiffrentAllocationDeliveryOrderSku(registedAllocationCode);
    }

    // 縫検の納品数がある場合は、本社撮影と納品場所(製品)のOrderSkusを非活性にする
    if (isSewingDeliveryExist) {
      this.disablePhotoAndDeliveryOrderSku();
    }

    // 撮影の納品数がある場合は、縫製検品と納品場所(製品)のOrderSkusを非活性にする
    if (isPhotoDeliveryExist) {
      this.disableSewingAndDeliveryOrderSku();
    }
  }

  /**
   * 納品場所以外(撮影+縫検)のOrderSkus と 場所コードが登録値と異なる納品場所(製品)のOrderSkusを非活性にする。
   * @param registedAllocationCode DBに登録されている納品明細の場所コード
   */
  private disableNoDeliveryAndDiffrentAllocationDeliveryOrderSku(registedAllocationCode: string): void {
    // 納品場所以外(撮影+縫検)のOrderSkusの非活性
    this.fCtrl.noDeliveryOrderSkuFormArray.disable();

    // 場所コードが登録値と異なる納品場所(製品)のOrderSkusの非活性
    //TC運用のあるブランドの場合は、処理を飛ばす。***ここに処理を追加！！！
    //PRD_0123 #7054 mod JFE start
    // this.disableDeliveryOrderSkuForm(registedAllocationCode);
    if (this.deliveryLocationList.length > 0) {
    } else {
    this.disableDeliveryOrderSkuForm(registedAllocationCode);
  }
    //PRD_0123 #7054 mod JFE end
  }

  /**
   * 本社撮影と納品場所(製品)のOrderSkusを非活性にする。
   */
  private disablePhotoAndDeliveryOrderSku(): void {
    // 本社撮影のOrderSkus非活性
    this.disableNoDeliveryOrderSkuForm(AllocationCode.PHOTO);

    // 納品場所(製品)のOrderSkus非活性
    this.fCtrl.deliveryOrderSkuFormArray.disable();

    //PRD_0123 #7054 add JFE start
    //縫製検品の場合は納品先は固定の為選択リストを非表示にする。
    this.isShowDeliveryLocationList = false;
    //PRD_0123 #7054 add JFE end
  }

  /**
   * 縫製検品と納品場所(製品)のOrderSkusを非活性にする。
   */
  private disableSewingAndDeliveryOrderSku(): void {
    // 縫製検品のOrderSkus非活性
    this.disableNoDeliveryOrderSkuForm(AllocationCode.SEWING);

    // 納品場所(製品)のOrderSkus非活性
    this.fCtrl.deliveryOrderSkuFormArray.disable();
    //PRD_0123 #7054 add JFE start
    //本社撮影の場合は納品先は固定の為選択リストを非表示にする。
    this.isShowDeliveryLocationList = false;
    //PRD_0123 #7054 add JFE end
  }

  /**
   * 納品場所以外の発注SKUフォームの非活性制御。
   * @param allocationCodeType 場所コード種類(本社撮影 or 縫製検品)
   */
  private disableNoDeliveryOrderSkuForm(allocationCodeType: string): void {
    this.fCtrlNoDeliveryOrderSkus.forEach(noDeliveryOrderSku => {
      const deliverySkuFormArray = noDeliveryOrderSku.get('deliverySkus') as FormArray;
      deliverySkuFormArray.controls.forEach(deliverySku => {
        // 指定した場所コード種類の納品依頼数量を非活性にする
        if (deliverySku.value.divisionCode === allocationCodeType) {
          deliverySku.disable();
        }
      });
    });
  }

  /**
   * 納品場所の発注SKUフォームの非活性制御。
   * @param registedAllocationCode DBに登録されている納品明細の場所コード
   */
  private disableDeliveryOrderSkuForm(registedAllocationCode: string): void {
    this.fCtrlDeliveryOrderSkus.forEach(deliveryOrderSku => {
      const deliverySkuFormArray = deliveryOrderSku.get('deliverySkus') as FormArray;
      deliverySkuFormArray.controls.forEach(deliverySku => {
        // 場所コードが異なる納品依頼数量を非活性にする
        if (deliverySku.value.allocationCode !== registedAllocationCode) {
          // PRD_0123 #7054 del JFE start
          //deliverySku.disable();
          // PRD_0123 #7054 del JFE end
        }
      });
    });
  }

  /**
   * メッセージクリア
   */
  private clearAllMessage(): void {
    this.overallErrorMsgCode = '';    // エラーメッセージクリア
    this.overallSuccessMsgCode = '';  // 成功メッセージクリア
    this.fatalErrorMsgCode = '';      // 致命的エラーメッセージクリア
    ExceptionUtils.clearErrorInfo();  // カスタムエラーメッセージクリア
  }
}
