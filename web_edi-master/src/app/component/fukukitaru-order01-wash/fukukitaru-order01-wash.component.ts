import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, FormArray, Validators, AbstractControl } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbDateParserFormatter, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { map, tap, filter, finalize, flatMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import {
  Path, FukukitaruMasterType, FukukitaruMasterDeliveryType, CompositionsCommon,
  PreEventParam, FukukitaruMasterOrderType, FukukitaruMasterOrderTypeName,
  FukukitaruMasterConfirmStatusTypeName,
  FukukitaruMasterConfirmStatusType,
  FukukitaruBrandCode,
  FukukitaruMasterMaterialType,
  ValidatorsPattern,
  SubmitType, OccupationType, ResourceType
} from '../../const/const';
import { ExceptionUtils } from '../../util/exception-utils';
import { FileUtils } from '../../util/file-utils';
import { ListUtils } from '../../util/list-utils';
import { StringUtils } from '../../util/string-utils';
import { NumberUtils } from '../../util/number-utils';
import { FormUtils } from '../../util/form-utils';
import { BusinessUtils } from '../../util/business-utils';
import { DateUtils } from '../../util/date-utils';
import { BusinessCheckUtils } from 'src/app/util/business-check-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

import { Item } from '../../model/item';
import { Order } from '../../model/order';
import { FukukitaruDestination } from '../../model/fukukitaru-destination';
import { ScreenSettingFukukitaruOrderSearchCondition } from '../../model/screen-setting-fukukitaru-order-search-condition';
import { FukukitaruMaster } from '../../model/fukukitaru-master';
import { FukukitaruOrder } from '../../model/fukukitaru-order';
import { Compositions } from '../../model/compositions';
import { OrderViewCompositions } from '../../model/order-view-compositions';
import { ScreenSettingFukukiatru } from '../../model/screen-setting-fukukitaru';
import { FukukitaruOrderSku } from '../../model/fukukitaru-order-sku';
import { MaterialFileInfo } from '../../model/material-file-info';
import { ScreenSettingFukukitaruSku } from '../../model/screen-setting-fukukitaru-sku';
import { FukukitaruItem } from '../../model/fukukitaru-item';
import { FukukitaruMaterialAttentionName } from '../../model/fukukitaru-material-attention-name';
import { FukukitaruItemWashAppendicesTerm } from '../../model/fukukitaru-item-wash-appendices-term';
import { FukukitaruItemWashPattern } from '../../model/fukukitaru-item-wash-pattern';
import { ErrorDetail } from '../../model/error-detail';
import { FukukitaruAppendicesTermByColor } from '../../interface/fukukitaru-appendices-term-by-color';
import { FukukitaruAppendicesTerm } from '../../interface/fukukitaru-appendices-term';
import { FukukitaruAttentionByColor } from '../../interface/fukukitaru-attention-by-color';
import { FukukitaruWashPatternByColor } from '../../interface/fukukitaru-wash-pattern-by-color';

import { FukukitaruOrder01Service } from '../../service/fukukitaru-order01.service';
import { LoadingService } from '../../service/loading.service';
import { FileService } from '../../service/file.service';
import { HeaderService } from '../../service/header.service';
import { SessionService } from '../../service/session.service';
import { MaterialOrderService } from '../../service/material-order/material-order.service';

import { SearchCompanyModalComponent } from '../search-company-modal/search-company-modal.component';
import { AttentionModalComponent } from '../attention-modal/attention-modal.component';
import {
  MaterialOrderSubmitConfirmModalComponent
} from '../material-order/material-order-submit-confirm-modal/material-order-submit-confirm-modal.component';

import { attentionNameValidator, auxiliaryMaterialValidator } from './validator/fukukitaru-order01-wash-validator.directive';
import { OrderSkuAttentionNameValue } from 'src/app/interface/order-sku-attention-name-value';
import { OrderSkuValue } from 'src/app/interface/order-sku-value';
import { OrderSkuWashAuxiliaryMaterialValue } from 'src/app/interface/order-sku-wash-auxiliary-material-value';
import { MaterialOrderDisplayFlag } from 'src/app/model/material-order-display-flag';

@Component({
  selector: 'app-fukukitaru-order01-wash',
  templateUrl: './fukukitaru-order01-wash.component.html',
  styleUrls: ['./fukukitaru-order01-wash.component.scss']
})
export class FukukitaruOrder01WashComponent implements OnInit {

  // htmlから参照したい定数を定義
  readonly FUKUKITARU_MASTER_TYPE = FukukitaruMasterType;
  readonly FUKUKITARU_DELIVERY_TYPE = FukukitaruMasterDeliveryType;
  readonly PATH = Path;
  readonly F_ORDER_TYPE = FukukitaruMasterOrderType;
  readonly F_ORDER_TYPE_NAME = FukukitaruMasterOrderTypeName;
  readonly F_CONFIRM_STATUS_TYPE_NAME = FukukitaruMasterConfirmStatusTypeName;
  readonly WASH_NAME_URL = 'fukukitaruOrder01Wash';
  readonly F_ORDER_TYPE_NAME_WASH_NAME = FukukitaruMasterOrderTypeName.ORDER_TYPE_MAP[1];
  readonly F_ORDER_TYPE_NAME_BOTTOM_BILL = FukukitaruMasterOrderTypeName.ORDER_TYPE_MAP[2];
  readonly FUKUKITARU_BRAND_CODE = FukukitaruBrandCode;
  readonly FUKUKITARU_MASTER_CONFIRM_STATUS_TYPE = FukukitaruMasterConfirmStatusType;
  readonly ORDER_CONFIRMED: FukukitaruMasterConfirmStatusType = FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED;
  readonly OCCUPATION_TYPE = OccupationType;
  readonly RESOURCE_TYPE = ResourceType;
  readonly COMPOSITIONS_COMMON = CompositionsCommon;

  screenSetttingData: ScreenSettingFukukiatru;  // フクキタル用マスタデータ
  itemData: Item;  // ScreenSettingで取得した品番情報のデータ
  orderData: Order;  // ScreenSettingで取得した発注情報のデータ
  fItemData: FukukitaruItem;    // フクキタル用品番情報のデータ
  fOrderData: FukukitaruOrder;  // フクキタル用発注情報のデータ
  fOrderPastList: FukukitaruOrder[] = [];  // フクキタル用発注情報のデータ(過去資材発注データ)
  attentionNameList: FukukitaruMaterialAttentionName[] = [];  // アテンションネームマスタリスト
  auxiliaryMaterialList: FukukitaruMaster[] = [];  // 同封副資材マスタリスト
  compositionViewList: OrderViewCompositions[] = [];  // 組成情報のデータリスト
  materialFileList: MaterialFileInfo[] = [];  // 資材発注ファイルリスト
  skuList: ScreenSettingFukukitaruSku[] = []; // SKU(カラーサイズ)リスト
  orderSkuValue: FukukitaruOrderSku[] = []; // フクキタル発注登録済のSKU(カラーサイズ)情報リスト
  registeredOrderSkuWash: FukukitaruOrderSku[] = []; // 登録済下げ札類SKU
  cnProductCategoryList: FukukitaruMaster[] = []; // 中国内販情報(製品分類)
  cnProductTypeList: FukukitaruMaster[] = [];     // 中国内販情報(製品種別)
  washAppendicesTermByColorList: FukukitaruAppendicesTermByColor[] = [];  // 洗濯ネーム付記用語リスト
  washPatternByColorList: FukukitaruWashPatternByColor[] = [];  // 絵表示リスト

  occupationType = '';      // ログインユーザーの職種

  path = '';                  // new,view,edit
  fukitaruOrderId = 0;        // 登録、更新用のフクキタル発注ID
  overallSuccessMsgCode = ''; // 画面全体にかかる正常系メッセージ
  overallErrorMsgCode = '';   // 画面全体にかかる異常系メッセージ
  seasonValue: string;        // サブシーズンのvalue値

  isShowFooter = false;       // フッター表示フラグ
  isDirty = false;            // 画面変更検知フラグ
  isBillingOrDeliveryAddressTouched = false; // 請求先・納入先変更検知フラグ
  isBtnLock = false;          // 処理中にボタンをロックするためのフラグ
  isInitDataSetted = false;   // 画面用データ取得完了フラグ
  submitted = false;          // submit可否フラグ
  isLossRateInvalid = false;  // ロス率バリデーションエラーフラグ

  mainForm: FormGroup;        // メインのフォーム

  formConditions: ScreenSettingFukukitaruOrderSearchCondition = new ScreenSettingFukukitaruOrderSearchCondition();  // Formの入力値

  /** フクキタルのバリデーション */
  private fkValidators = []; // 共通バリデーションを追加する場合はここに追加する

  // フクキタル検索マスタータイプ
  readonly LIST_MASTER_TYPE: FukukitaruMasterType[] = [
    FukukitaruMasterType.TAPE_WIDE, // テープ巾
    FukukitaruMasterType.TAPE_TYPE, // テープ種類
    FukukitaruMasterType.WASH_NAME_APPENDICES_TERM,  // 洗濯ネーム　付記用語
    FukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM,  // アテンションタグ付記用語
    FukukitaruMasterType.ATTENTION_SEAL_TYPE, // アテンションシールのシール種類
    FukukitaruMasterType.RECYCLE, // リサイクルマーク
    FukukitaruMasterType.ATTENTION_NAME, // アテンションネーム
    FukukitaruMasterType.WASH_PATTERN,  // 洗濯マーク,絵表示
    FukukitaruMasterType.AUXILIARY_MATERIAL,  // 同封副資材
    FukukitaruMasterType.SKU,  // SKU
    FukukitaruMasterType.ITEM,  // 品番情報
    FukukitaruMasterType.ORDER,  // 発注情報
    FukukitaruMasterType.FUKUKITARU_ITEM,  // フクキタル品番情報
    FukukitaruMasterType.BILLING_ADDRESS,  // 請求先
    FukukitaruMasterType.DERIVERY_ADDRESS,  // 納品先
    FukukitaruMasterType.SUPPLIER_ADDRESS,  // 発注先
    FukukitaruMasterType.ORDER_TYPE,  // 発注種別
    FukukitaruMasterType.MATERIAL_FILE_INFO,  // 資材ファイル情報
    FukukitaruMasterType.WASH_NAME, // 洗濯ネーム
    FukukitaruMasterType.CN_PRODUCT_CATEGORY, // 中国内販情報(製品分類)
    FukukitaruMasterType.CN_PRODUCT_TYPE      // 中国内販情報(製品種別)
  ];

  materialOrderDisplayFlg = new MaterialOrderDisplayFlag(false); // 資材発注(フクキタル)項目表示フラグ

  readonly URL_WASH = 'fukukitaruOrder01Wash';
  readonly URL_HANG_TAG = 'fukukitaruOrder01HangTag';

  private readonly ATTENTION_NAME_MAX_COUNT = 3;   // アテンションネーム入力項目の最大数

  apiValidateErrorsMap: Map<string, ErrorDetail[]> = new Map();   // APIのバリデーションエラーMap

  constructor(
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private fukukitaruOrder01Service: FukukitaruOrder01Service,
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private modalService: NgbModal,
    private router: Router,
    private fileService: FileService,
    private sessionService: SessionService,
    private materialOrderService: MaterialOrderService
  ) { }

  ngOnInit() {
    this.headerService.show();

    this.route.paramMap.subscribe(paramsMap => {
      this.isInitDataSetted = false;

      const session = this.sessionService.getSaveSession(); // ログインユーザの情報を取得する
      this.occupationType = session.occupationType;
      // フッター表示条件: ROLE_EDIまたはROLE_MAKER
      this.isShowFooter = AuthUtils.isEdi(session) || AuthUtils.isMaker(session);

      const preEvent: number = Number(this.route.snapshot.queryParamMap.get('preEvent'));
      const orderId: number = Number(this.route.snapshot.queryParams['orderId']);    // 発注Id
      const partNoId: number = Number(this.route.snapshot.queryParams['partNoId']);  // 品番Id

      // 検索条件
      this.formConditions.deliveryType = this.FUKUKITARU_DELIVERY_TYPE.DOMESTIC;  // デリバリ(初期値：国内)
      this.formConditions.searchCompanyName = '';  // 検索会社名
      this.formConditions.orderId = orderId;       // 発注ID
      this.formConditions.partNoId = partNoId;     // 品番ID

      this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      this.fukitaruOrderId = this.route.snapshot.params.id;
      // pathによる分岐表示
      this.initializeDisplayByRouting(this.path, this.fukitaruOrderId, preEvent);
    });
  }

  /**
   * アテンションネームの資材数量のカンマ処理
   */
  onBlurAttention(attentionName: FormGroup): void {
    const orderLot = attentionName.value.orderLot.replace(/,/g, '');
    attentionName.patchValue({ orderLot: orderLot });
  }

  /**
   * 資材数量の切り上げ
   */
  onBlurLot(auxiliaryMaterial: FormGroup): void {
    let orderLot = auxiliaryMaterial.value.orderLot;
    if (orderLot == null) {
      // 数量の入力がない場合は処理しない
      return;
    }
    if (auxiliaryMaterial.value.orderLot.match(/,/g)) {
      orderLot = orderLot.replace(/,/g, '');  // カンマが含まれている場合は取り除く
    }
    if (!NumberUtils.isNumber(orderLot) || orderLot <= 0) {
      return;
    }

    const moq = auxiliaryMaterial.value.moq;
    const divided = Math.floor(orderLot / moq);
    const mod = orderLot % moq;
    const result = ((divided > 0 ? divided : 0) + (mod !== 0 ? 1 : 0)) * moq;
    auxiliaryMaterial.patchValue({ orderLot: result });
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // pipeの変換値がformにセットされないので別途格納
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate != null) { this.mainForm.patchValue({ [type]: ngbDate }); }
  }

  /**
   * フクキタル発注情報登録処理
   */
  onSubmit(): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;
    // メッセージクリア
    this.clearErrorMessage();
    // バリデーションチェック
    console.debug('バリデーション', this.mainForm.invalid);
    if (this.isValidationError()) {
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    const formValue = this.mainForm.getRawValue(); // mainformの値を取得する
    formValue.orderSkuWashName = this.prepareWashListSkuPostData(); // 発注SKU
    formValue.orderSkuAttentionName = this.prepareAttentionNameListPostData();
    formValue.orderSkuWashAuxiliary = this.prepareWashAuxiliaryListPostData();
    formValue.orderSkuAttentionName = this.reduceDimension(formValue.attentionSaveList);

    this.fukukitaruOrder01Service.postFukukitaruOrder(formValue).toPromise().then(
      (result: FukukitaruOrder) => {
        console.debug('postFukukitaruOrder結果:', result);
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 新規登録後の編集画面表示
        this.router.navigate([this.WASH_NAME_URL, result.id, Path.EDIT],
          { queryParams: { preEvent: PreEventParam.CREATE, 'orderId': result.orderId, 'partNoId': result.partNoId } });
      },
      error => this.handleSubmitError(error)
    );
  }

  /**
   * フクキタル発注情報更新時処理
   */
  onUpdate(): void {
    // メッセージクリア
    this.clearErrorMessage();

    this.loadingService.loadStart();
    this.isBtnLock = true;
    // バリデーションチェック
    console.debug('バリデーション', this.mainForm.invalid);
    if (this.isValidationError()) {
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    const formValue = this.mainForm.getRawValue(); // mainformの値を取得する
    formValue.orderSkuWashName = this.prepareWashListSkuPostData(); // 発注SKU
    formValue.orderSkuAttentionName = this.prepareAttentionNameListPostData();
    formValue.orderSkuWashAuxiliary = this.prepareWashAuxiliaryListPostData();
    formValue.orderSkuAttentionName = this.reduceDimension(formValue.attentionSaveList);

    this.fukukitaruOrder01Service.putFukukitaruOrder(formValue).toPromise().then(
      (result: FukukitaruOrder) => {
        console.debug('updateFukukitaruOrder結果:', result);
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 更新後は編集画面表示
        this.router.navigate([this.WASH_NAME_URL, result.id, Path.EDIT],
          { queryParams: { preEvent: PreEventParam.UPDATE, 'orderId': result.orderId, 'partNoId': result.partNoId } });
        // 同じパスで遷移になるため、フォーム再描画を実施(更新時のメッセージを表示するためPreEventParam.UPDATEを設定)
        this.initializeEditDisplay(PreEventParam.UPDATE, formValue.id);
      },
      error => this.handleSubmitError(error)
    );
  }

  /**
   * 確定ボタン押下時の処理.
   * モーダル表示
   */
  onConfirmModal(): void {
    // メッセージクリア
    this.clearErrorMessage();

    // 入力フォームに変更があるか判断する。変更がある場合確定ボタンをロックし、確定前に更新するメッセージを表示
    if (this.isBillingOrDeliveryAddressTouched || this.mainForm.dirty) {
      this.isDirty = true;
      this.overallErrorMsgCode = 'ERRORS.VALIDATE.UPDATE_BEFORE_CONFIRM';
      return;
    }

    // バリデーションチェック
    console.debug('バリデーション', this.mainForm.invalid);
    if (this.isValidationError()) {
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    const modalRef = this.modalService.open(MaterialOrderSubmitConfirmModalComponent);
    modalRef.componentInstance.isQualityApprovalOk = BusinessCheckUtils.isQualityApprovalOk(this.itemData);
    modalRef.result.then((isOrderResponsible: boolean) => {
      this.mainForm.patchValue({ isResponsibleOrder : isOrderResponsible});
      this.onConfirm();
    }, () => {});  // バツボタンクリック時は何もしない
  }

  /**
   * フクキタル発注情報確定処理
   */
  private onConfirm(): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;

    const formValue = this.mainForm.getRawValue(); // mainformの値を取得する
    formValue.orderSkuWashName = this.prepareWashListSkuPostData(); // 発注SKU
    formValue.orderSkuAttentionName = this.prepareAttentionNameListPostData();
    formValue.orderSkuWashAuxiliary = this.prepareWashAuxiliaryListPostData();
    formValue.orderSkuAttentionName = this.reduceDimension(formValue.attentionSaveList);

    this.fukukitaruOrder01Service.confirmFukukitaruOrder(formValue).toPromise().then(
      (result: FukukitaruOrder) => {
        console.debug('confirmFukukitaruOrder結果:', result);
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 確定後も編集画面表示
        this.router.navigate([this.WASH_NAME_URL, result.id, Path.EDIT],
          { queryParams: { preEvent: PreEventParam.CONFIRM, 'orderId': result.orderId, 'partNoId': result.partNoId } });
        // 同じパスで遷移になるため、フォーム再描画を実施(確定時のメッセージを表示するためPreEventParam.CONFIRMを設定)
        this.initializeEditDisplay(PreEventParam.CONFIRM, formValue.id);
      },
      error => this.handleSubmitError(error)
    );
  }

  /**
   * フクキタル発注情報承認処理
   */
  onApprove(): void {
    let loadingToken = null;
    this.isDirty = (this.mainForm.dirty || this.isBillingOrDeliveryAddressTouched);

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearErrorMessage()),
      // 入力フォームに変更があるか判断する。変更ない場合、処理を続行
      filter(() => !this.isDirty),
      // バリデーションが正常の場合、処理を続行
      filter(() => !this.isValidationError()),
      // 登録データの取得
      map(() => {
        // mainformの値を取得する
        const formValue = this.mainForm.getRawValue();
        formValue.orderSkuWashName = this.prepareWashListSkuPostData(); // 発注SKU
        formValue.orderSkuAttentionName = this.prepareAttentionNameListPostData();
        formValue.orderSkuWashAuxiliary = this.prepareWashAuxiliaryListPostData();
        formValue.orderSkuAttentionName = this.reduceDimension(formValue.attentionSaveList);
        return formValue;
      }),
      // 資材発注承認
      flatMap((formValue) => this.fukukitaruOrder01Service.approveFukukitaruOrder(formValue)),
      tap((response) => {
        console.debug('approveFukukitaruOrder結果:', response);
        // 承認後の画面表示(承認後のURL書き換え(EDIT))
        this.router.navigate([this.WASH_NAME_URL, response.id, Path.EDIT],
          { queryParams: { preEvent: PreEventParam.APPROVE, 'orderId': response.orderId, 'partNoId': response.partNoId } });
        // 同じパスで遷移になるため、フォーム再描画を実施
        this.initializeEditDisplay(PreEventParam.APPROVE, response.id);
      }),
      // エラーが発生した場合は、エラーメッセージを表示
      catchError((error) => {
        return of(this.handleSubmitError(error));
      }),
      // ローディング停止
      finalize(() => {
        // 入力フォームに変更がある場合、承認前に更新するメッセージを表示
        if (this.isDirty) {
          this.setApproveOrConfirmDisableMessage(SubmitType.APPROVE);
        }
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  /**
   * 承認・確定不可メッセージの設定
   *
   * @param submitType SUBMIT種類
   */
  private setApproveOrConfirmDisableMessage(submitType: string) {
    switch (submitType) {
      case SubmitType.APPROVE:  // 承認
        this.overallErrorMsgCode = 'ERRORS.VALIDATE.UPDATE_BEFORE_APPROVE';
        break;
      case SubmitType.CONFIRM:  // 確定
        this.overallErrorMsgCode = 'ERRORS.VALIDATE.UPDATE_BEFORE_CONFIRM';
        break;
    }
  }

  /**
   * フクキタル用 付記用語　二次元配列から一次元に変換
   * @param attentionSaveList 二次元配列色リスト
   */
  private reduceDimension(attentionSaveList: FukukitaruAttentionByColor[]): FukukitaruOrderSku[] {
    let result: FukukitaruOrderSku[] = [];

    attentionSaveList.forEach((color) => {
      result = result.concat(color.attentionList);
    });

    return result;
  }

  /**
   * エラーメッセージをクリアする
   */
  private clearErrorMessage(): void {
    this.overallErrorMsgCode = '';
    this.overallSuccessMsgCode = '';
    ExceptionUtils.clearErrorInfo();
    this.apiValidateErrorsMap.clear(); // APIバリデーションエラーのクリア
  }

  /**
   * バリデーションチェックを行う。
   * @param viewMode 画面表示モード
   * @return isValidationNg
   */
  isValidationError() {
    let isValidationNg = false;
    this.submitted = true;

    // バリデーションエラーの時に画面に戻す
    if (this.mainForm.invalid) {
      console.debug('バリデーションエラー:', this.mainForm);
      this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      isValidationNg = true;
    }

    return isValidationNg;
  }

  /**
   * Submit時のAPIエラー処理
   * @param error APIレスポンスエラー情報
   */
  private handleSubmitError(error: any): void {
    const apiError = ExceptionUtils.apiErrorHandler(error);
    this.overallErrorMsgCode = 'ERRORS.ANY_ERROR';
    if (apiError != null && apiError.viewErrors != null) {
      const firstError = apiError.viewErrors[0];
      if (firstError != null) {
        switch (firstError.code) {
          case '400_FO_01':   // 品番情報または発注情報が既に削除されている場合は、エラーを表示
            this.overallErrorMsgCode = 'ERRORS.400_FO_01';
            break;
          case '400_FO_03': // (確定時)フクキタル発注情報確定済エラー
            this.overallErrorMsgCode = 'ERRORS.400_FO_03';
            break;
          case '400_FO_04': // (承認時)請求先エラー
            this.overallErrorMsgCode = 'ERRORS.400_FO_04';
            break;
          case '400_FO_05': // (承認時)フクキタル発注情報承認済エラー
            this.overallErrorMsgCode = 'ERRORS.400_FO_05';
            break;
          case '400_FO_06': // (承認時)フクキタル発注情報承認権限エラー
            this.overallErrorMsgCode = 'ERRORS.400_FO_06';
            break;
          default:
            // resourceに値がある場合はapiValidateErrorsMapに詰め込む
            apiError.errors.some(errorDetail => {
              if (StringUtils.isNotEmpty(errorDetail.resource)) {
                this.generateApiValidateErrorsMap(errorDetail);
                this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
              } else {
                this.overallErrorMsgCode = apiError.viewErrorMessageCode;
              }
              return true; // loop終了
            });
            break;
        }
      }
    }
    this.loadingService.loadEnd();
    this.isBtnLock = false;
  }

  /**
   * APIのバリデーションエラーMapをセットする
   * @param errorDetail エラー詳細情報
   */
  private generateApiValidateErrorsMap(errorDetail: ErrorDetail): void {
    const resource = errorDetail.resource;
    if (this.apiValidateErrorsMap.size === 0 || !this.apiValidateErrorsMap.has(resource)) {
      // apiValidateErrorsMapが空 または 指定したresourceのキーがない場合、(key, value)を追加
      this.apiValidateErrorsMap.set(resource, [errorDetail]);
    } else {
      // 指定したresourceのキーがある場合、対象keyのvalueに追加
      const values = this.apiValidateErrorsMap.get(resource);
      values.push(errorDetail);
    }
  }

  /**
   * pathによる分岐表示
   * @param path URLパス
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   * @param orderId 当画面で処理する発注ID
   */
  private initializeDisplayByRouting(path: string, fukitaruOrderId: number, preEvent: number): void {
    switch (path) {
      case Path.NEW:  // 新規登録
        this.initializeNewDisplay();
        break;
      case Path.EDIT: // 編集
        this.initializeEditDisplay(preEvent, fukitaruOrderId);
        break;
      default:
        break;
    }
  }

  /**
   * mainFormのcontrolを返す.
   * @returns this.mainForm.controls
   */
  get f(): any { return this.mainForm.controls; }

  /**
   * mainFormのorderSkuAttentionNameのvalueを返す.
   * @return mainForm.get('orderSkuAttentionName').value
   */
  get fValOrderSkuAttentionName(): OrderSkuAttentionNameValue[] {
    return this.mainForm.getRawValue()['orderSkuAttentionName'];
  }

  /**
   * mainFormのorderSkuWashAuxiliaryの項目の状態を取得する。
   * @return mainForm.get('orderSkuWashAuxiliary')['controls']
   */
  get formWashAuxiliary(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkuWashAuxiliary')).controls;
  }

  /**
   * mainFormのorderSkuWashAuxiliaryのvalueを返す.
   * @return mainForm.get('orderSkuBottomBillAuxiliaryMaterial').value
   */
  get fValOrderSkuWashAuxiliary(): OrderSkuWashAuxiliaryMaterialValue[] {
    return this.mainForm.getRawValue()['orderSkuWashAuxiliary'];
  }

  /**
   * mainFormのorderSkusの項目の状態を取得する。
   * @return mainForm.get('orderSkus')['controls']
   */
  get formOrderSkuArray(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkus')).controls;
  }

  /**
   * mainFormのattentionSaveListを取得する。
   * @return mainForm.get('attentionSaveList')['value']
   */
  get rawAttentionSaveList(): FukukitaruAttentionByColor[] {
    return (<FormArray> this.mainForm.controls.attentionSaveList).value;
  }

  /**
   * mainFormのattentionSaveListの項目の状態を取得する。
   * @return mainForm.get('attentionSaveList')
   */
  get formAttentionSaveList(): FormArray {
    return this.mainForm.controls.attentionSaveList as FormArray;
  }

  /**
   * mainFormのattentionListの項目の状態を取得する。
   * @return mainForm.get('attentionList')
   */
  formAttentionList(control: AbstractControl): FormArray {
    return control.get('attentionList') as FormArray;
  }

  /**
   * post用にアテンションタグSKUリストを作成する.
   * @returns アテンションタグSKUリスト
   */
  private prepareAttentionNameListPostData(): OrderSkuAttentionNameValue[] {
    // 数量が入力されているアテンションタグを抽出
    return this.fValOrderSkuAttentionName.filter(val => !FormUtils.isEmpty(val.orderLot));
  }

  /**
   * post用に同封副資材SKUリストを作成する.
   * @returns 同封副資材SKUリスト
   */
  private prepareWashAuxiliaryListPostData(): OrderSkuAttentionNameValue[] {
    // 数量が入力されている同封副資材を抽出
    return this.fValOrderSkuWashAuxiliary.filter(val => !FormUtils.isEmpty(val.orderLot));
  }

  /**
   * pathがNEW時の表示処理。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private initializeNewDisplay(): void {
    // フクキタル連携マスタデータを取得
    this.getFukukitaruMasterData().then(settingData => {
      // マスタデータ設定
      this.setDataToMemberVariable(settingData);
      this.mainForm = this.getFormGroup();  // フォーム作成
      this.setFormValue(settingData, null);  // formに品番情報セット
      this.setFukukitaruValidation(settingData.item.brandCode);  // フクキタルのバリデーションを設定
      this.isInitDataSetted = true;  // フォームを表示
      this.setDisabled();  // 編集不可項目のセット
      this.convertByColorList(settingData);  // APIから取得したデータを二次元配列に変換
    }).catch(reason => { this.showGetApiErrorMessage(reason); });

    this.listFukukitaruOrderData(); // 過去資材発注データ取得
  }

  /**
   * pathがEDIT時の表示処理。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   * @param orderId 当画面で処理する発注ID
   */
  private initializeEditDisplay(preEvent: number, fukitaruOrderId: number): void {
    this.isInitDataSetted = false; // フォームを非表示

    // 初期化
    this.isDirty = false;
    this.isBillingOrDeliveryAddressTouched = false;
    this.clearErrorMessage();

    switch (preEvent) {
      case PreEventParam.CREATE:
        // 発注登録後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_ENTRY';
        break;
      case PreEventParam.UPDATE:
        // 発注更新後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_UPDATE';
        break;
      case PreEventParam.CONFIRM:
        // 発注確定後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_CONFIRMED';
        break;
      case PreEventParam.APPROVE:
        // 発注承認後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_APPROVED';
        break;
      default:
        break;
    }

    // フクキタル用発注情報取得
    this.getFukukitaruOrderData(fukitaruOrderId).then(fOrder => {
      this.formConditions.deliveryType = fOrder.deliveryType;  // 抽出条件(発注種別)を設定
      // フクキタル連携マスタデータを取得
      this.getFukukitaruMasterData().then(settingData => {
        this.isInitDataSetted = true; // フォームを表示
        if (settingData.fkItem.id != null) {
          // マスタデータ設定
          this.setDataToMemberVariable(settingData);
          this.registeredOrderSkuWash = fOrder.orderSkuWashName;
          this.orderSkuValue = this.ganarateOrderSku(fOrder);
          this.generateCompositionsSkuList(settingData.item.compositions);
          this.mainForm = this.getFormGroup();  // フォーム作成
          this.setFormValue(settingData, fOrder);  // formに品番情報セット
          this.setFukukitaruValidation(settingData.item.brandCode);  // フクキタルのバリデーションを設定
          this.setDisabled();  // 編集不可項目のセット
          this.convertByColorList(settingData);  // APIから取得したデータを二次元配列に変換
        }
      }).catch(reason => { this.showGetApiErrorMessage(reason); });
    }).catch(reason => { this.showGetApiErrorMessage(reason); });

    // 過去資材発注データ取得
    this.listFukukitaruOrderData();
  }

  /**
   * マスタデータを取得する.
   */
  private async getFukukitaruMasterData(): Promise<ScreenSettingFukukiatru> {
    const conditions = {
      partNoId: this.formConditions.partNoId,
      orderId: this.formConditions.orderId,
      listMasterType: this.LIST_MASTER_TYPE,
      deliveryType: this.formConditions.deliveryType
    } as ScreenSettingFukukitaruOrderSearchCondition;
    console.debug('取得したフクキタル用マスタデータ(settingData)の抽出条件：', conditions);
    return await this.fukukitaruOrder01Service.listFukukitaruOrderWashNameMaster(conditions).toPromise().then(
      setttingData => {
        console.debug('取得したフクキタル用マスタデータ(settingData)：', setttingData);
        this.screenSetttingData = setttingData.items[0];
        return Promise.resolve(this.screenSetttingData);
      }, error => {
        return Promise.reject(error);
      }
    );
  }

  /**
   * フクキタル用発注情報取得処理
   * @param fOrderId 発注ID
   */
  private async getFukukitaruOrderData(fOrderId: number): Promise<FukukitaruOrder> {
    return await this.fukukitaruOrder01Service.getFukukitaruOrderForId(fOrderId).toPromise().then(
      fOrder => {
        console.debug('取得したフクキタル用発注データ(fOrder)：', fOrder);
        this.fOrderData = fOrder;
        return Promise.resolve(fOrder);
      }, error => {
        return Promise.reject(error);
      }
    );
  }

  /**
   * フクキタル過去資材発注情報取得処理
   * @param orderId 発注ID
   */
  private listFukukitaruOrderData(): void {
    const conditions = {
      partNoId: this.formConditions.partNoId,
      orderId: this.formConditions.orderId,
      listMasterType: this.LIST_MASTER_TYPE,
      deliveryType: this.formConditions.deliveryType
    } as ScreenSettingFukukitaruOrderSearchCondition;
    this.fukukitaruOrder01Service.listFukukitaruOrders(conditions).subscribe(
      data => {
        this.fOrderPastList = data.items;
        console.debug('取得した過去資材発注データ(fOrderPastList)：', this.fOrderPastList);
      }, error => {
        console.debug('error:', error);
      });
  }

  /**
   * 一次元配列のデータを色ごとに二次元配列に変換する
   */
  private convertByColorList(settingData: ScreenSettingFukukiatru): void {
    // 洗濯ネーム付記用語
    this.washAppendicesTermByColorList =
      this.convertAppendicesTermByColorList(settingData.fkItem.listItemWashAppendicesTerm);
    // 絵表示
    this.washPatternByColorList =
      this.convertWashPatternByColorList(settingData.fkItem.listItemWashPattern);
  }

  /**
   * 洗濯ネーム付記用語のデータを二次元配列に変換する
   * @param list
   */
  private convertAppendicesTermByColorList(list: FukukitaruItemWashAppendicesTerm[]): FukukitaruAppendicesTermByColor[] {
    // 配列がnullの場合、空の配列を返却する
    const washAppendicesTermList = ListUtils.isEmpty(list) ? [] : list;

    // APIから受け取ったデータが0件の場合は空の配列を返却する
    if (ListUtils.isEmpty(washAppendicesTermList)) {
      const emptyArray: FukukitaruAppendicesTermByColor[] = [];
      return emptyArray;
    }

    // 色コードのリストから、全色の洗濯ネーム 付記用語のリストを取得
    return this.convertSkuListToColorList().map(color => {
      return {
        colorCode: color.colorCode,
        colorName: color.colorName,
        appendicesTermList: washAppendicesTermList
          .filter((appendicesTerm) => (appendicesTerm.colorCode === color.colorCode))
          .map((appendicesTerm) => {
            return {
              id: appendicesTerm.id,
              appendicesTermId: appendicesTerm.appendicesTermId,
              appendicesTermCode: appendicesTerm.appendicesTermCode,
              appendicesTermCodeName: appendicesTerm.appendicesTermCodeName,
            } as FukukitaruAppendicesTerm;
          })
      } as FukukitaruAppendicesTermByColor;
    });
  }

  /**
 * 絵表示のデータを二次元配列に変換する
 * @param list
 */
  private convertWashPatternByColorList(list: FukukitaruItemWashPattern[]): FukukitaruWashPatternByColor[] {
    // 配列がnullの場合、空の配列を返却する
    const itemList = ListUtils.isEmpty(list) ? [] : list;
    const washPatternByColorList: FukukitaruWashPatternByColor[] = [];

    // APIから受け取ったデータが0件の場合は空の配列を返却する
    if (ListUtils.isEmpty(itemList)) {
      return washPatternByColorList;
    }

    // 色コードのリストから、全色の絵表示のリストを取得
    this.convertSkuListToColorList().map(color => {
      const item = itemList.filter(val => val.colorCode === color.colorCode);
      if (ListUtils.isNotEmpty(item)) {
        washPatternByColorList.push({
          colorCode: color.colorCode,
          colorName: color.colorName,
          washPatternId: item[0].washPatternId,
          washPatternName: item[0].washPatternName
        } as FukukitaruWashPatternByColor);
      }
    });

    // 登録したデータのみ返却する
    const result = washPatternByColorList.filter(val => val.washPatternId);
    return result;
  }

  /**
   * メンバ変数にScreenSettingFukukiatru取得データをセットする.※フクキタル発注データは除く
   * @param fukuMastaData ScreenSettingFukukiatru
   */
  private setDataToMemberVariable(fukuMastaData: ScreenSettingFukukiatru): void {
    // 初期化
    this.materialOrderDisplayFlg = new MaterialOrderDisplayFlag(false);

    this.itemData = fukuMastaData.item;  // 品番情報
    this.fItemData = fukuMastaData.fkItem;  // フクキタル用品番情報
    this.seasonValue = BusinessUtils.getSeasonValue(this.itemData.subSeasonCode);  // サブシーズンコード名
    this.skuList = fukuMastaData.listScreenSku;  // SKU
    this.generateCompositionsSkuList(this.itemData.compositions);

    this.attentionNameList = fukuMastaData.listAttentionName;  // アテンションネーム
    this.auxiliaryMaterialList = fukuMastaData.listAuxiliaryMaterial;  // 同封副資材
    this.materialFileList = fukuMastaData.listMaterialFile;  // 資材ファイル情報

    this.cnProductCategoryList = fukuMastaData.listCnProductCategory; // 中国内販情報(製品分類)
    this.cnProductTypeList = fukuMastaData.listCnProductType;         // 中国内販情報(製品種別)

    // 資材発注(フクキタル)項目表示フラグを設定する
    this.generateMaterialOrderDisplayFlg();
  }

  /**
   * 資材発注(フクキタル)項目表示フラグを設定する.
   * @param fukuMastaData ScreenSettingFukukiatru
   */
  private generateMaterialOrderDisplayFlg(): void {
    // 中国内販：製品分類リストがある場合は、製品分類を表示する
    if (ListUtils.isNotEmpty(this.cnProductCategoryList)) {
      this.materialOrderDisplayFlg.isDisplayCnProductCategory = true;
    }

    // 中国内販：製品種別リストがある場合は、製品種別を表示する
    if (ListUtils.isNotEmpty(this.cnProductTypeList)) {
      this.materialOrderDisplayFlg.isDisplayCnProductType = true;
    }
  }

  /**
   * フクキタルのバリデーションを設定する
   * 共通バリデーションを追加する場合、this.fkValidatorsに直接追加する。
   * ブランドごとのバリデーションを追加する場合、this.fkValidators.concat(xxx1, xxx2)と追加する。
   */
  private setFukukitaruValidation(brandCode: string): void {
    // ブランド別にバリデーションを設定する
    switch (brandCode) {
      case FukukitaruBrandCode.ROPE_PICNIC:
      case FukukitaruBrandCode.ROPE_PICNIC_PASSAGE:
      case FukukitaruBrandCode.ROPE_PICNIC_KIDS:
        // brand01：ロペピクニック
        this.mainForm.setValidators(this.fkValidators);
        break;
      case FukukitaruBrandCode.VIS:
        // brand02：VIS
        this.mainForm.setValidators(this.fkValidators);
        break;
      case FukukitaruBrandCode.ADAM_ET_ROPE_LADIES:
      case FukukitaruBrandCode.ADAM_ET_ROPE_MENS:
        // brand03：アダム・エ・ロペ
        this.mainForm.setValidators(this.fkValidators);
        break;
      default:
        // common：上記以外のブランドは共通のバリデーションのみ設定
        this.mainForm.setValidators(this.fkValidators);
        break;
    }
  }

  /**
   * 編集不可項目を制御する。
   * @param registStatus 登録ステータス
   */
  private setDisabled(): void {
    // 非活性にする
    this.mainForm.controls.orderNumber.disable();                 // 発注No.
    this.mainForm.controls.productDeliveryAt.disable();           // 生産納期
    this.mainForm.controls.mdfMakerName.disable();                // メーカー(画面表示用)
    this.mainForm.controls.billingCompanyName.disable();          // 請求先会社名
    this.mainForm.controls.deliveryCompanyName.disable();         // 納入先会社名
    this.mainForm.controls.deliveryCompanyAddress.disable();      // 納入先住所
    this.mainForm.controls.deliveryCompanyTel.disable();          // 納入先電話番号
    this.mainForm.controls.deliveryCompanyFax.disable();          // 納入先FAX番号
    this.mainForm.controls.fukukitaruOrderNumber.disable();       // フクキタル発注No.
    this.mainForm.controls.contactOrder.disable();                // 発注先
    this.mainForm.controls.tapeName.disable();                    // テープ種類
    this.mainForm.controls.tapeWidthName.disable();               // テープ巾
    this.mainForm.controls.washPatternName.disable();             // 絵表示
    this.mainForm.controls.printSize.disable();                   // サイズ印字
    this.mainForm.controls.printCoo.disable();                    // 原産国印字
    // アダム・エ・ロペの場合、デリバリを非活性にする
    const partNo: string = this.mainForm.controls.partNo.value;
    const checkPartNo = partNo.slice(0, 2);
    if ((checkPartNo === FukukitaruBrandCode.ADAM_ET_ROPE_LADIES) || (checkPartNo === FukukitaruBrandCode.ADAM_ET_ROPE_MENS)) {
      this.mainForm.controls.deliveryType.disable();
    }
  }

  /**
   * API取得エラーメッセージを表示する。
   * @param error
   */
  private showGetApiErrorMessage(error: any): void {
    this.overallErrorMsgCode = 'ERRORS.ANY_ERROR';
    const apiError = ExceptionUtils.apiErrorHandler(error);

    if (apiError != null) {
      this.overallErrorMsgCode = apiError.viewErrorMessageCode;

      // resourceに値がある場合はapiValidateErrorsMapに詰め込む
      apiError.errors.some(errorDetail => {
        if (StringUtils.isNotEmpty(errorDetail.resource)) {
          this.generateApiValidateErrorsMap(errorDetail);
          this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
        }
        return true; // loop終了
      });

      if ((apiError.viewErrors != null) && (apiError.viewErrors[0] != null) && (apiError.viewErrors[0].code === '400_02')) {
        this.isInitDataSetted = false;
        this.overallErrorMsgCode = '';
        // サーバエラーエリアにメッセージ表示
        ExceptionUtils.displayErrorInfo('fatalErrorMsgArea', apiError.viewErrorMessageCode);
      }
    }
    this.loadingService.loadEnd();
  }

  /**
   * 組成情報の作成
   * @param compositionList 品番の組成情報
   */
  private generateCompositionsSkuList(compositionList: Compositions[]): void {
    this.compositionViewList = [];

    // 組成情報を基に色のリストを作成する。
    if (compositionList !== null) {
      // 抽出した色コード、色名リストから色コードの重複除去
      const uniqueCompositionList = compositionList.filter(
        (value1, idx, array) => (array.findIndex(value2 => value2.colorCode === value1.colorCode) === idx)
      );
      uniqueCompositionList.forEach(uniqueComposition => {
        // 共通の場合はラベルをセットする。
        if (uniqueComposition.colorCode === CompositionsCommon.COLOR_CODE) {
          uniqueComposition.colorName = CompositionsCommon.COLOR_NAME;
        }

        // この色の組成情報を抽出する
        if (compositionList != null) {
          const compositions = compositionList.filter(composition => uniqueComposition.colorCode === composition.colorCode);
          if (compositions.length > 0) {
            const orderViewCompositions = uniqueComposition as OrderViewCompositions;
            orderViewCompositions.compositions = compositions;
            this.compositionViewList.push(orderViewCompositions);
          }
        }
      });
    }
  }

  /**
   * post用に発注SKUリストを作成する.
   * @returns 発注SKUリスト
   */
  private prepareWashListSkuPostData(): FukukitaruOrderSku[] {
    const washList: FukukitaruOrderSku[] = [];

    // 数量入力がある発注SKU取得
    let inputtedOrderSkuList: OrderSkuValue[] = [];
    this.formOrderSkuArray.forEach(orderSku => {
      const sizeListFormArrayValue: OrderSkuValue[] = orderSku.get('sizeList').value;
      const inputtedOrderSku = sizeListFormArrayValue.filter(sizeInfoVal => StringUtils.isNotEmpty(sizeInfoVal.productOrderLot));
      console.debug('inputtedOrderSku:', inputtedOrderSku); // push時はコメントアウトしてください.開発時に使うので消さないでください.
      inputtedOrderSkuList = inputtedOrderSkuList.concat(inputtedOrderSku);
    });
    console.debug('inputtedOrderSkuList:', inputtedOrderSkuList); // push時はコメントアウトしてください.開発時に使うので消さないでください.

    if (inputtedOrderSkuList.length > 0) {
      // 発注SKUごとに設定する
      const materialId = this.mainForm.controls.skuMaterialId.value;
      inputtedOrderSkuList.forEach(inputtedOrderSku => {
        const colorCode = inputtedOrderSku.colorCode;
        const size = inputtedOrderSku.size;
        // 更新前のデータがあればフクキタル発注SKUのIDをセット
        let fkOrderSkuId: number = null;
        this.registeredOrderSkuWash.some(registeredOrderSkuWash => {
          if (registeredOrderSkuWash.materialId === materialId
            && registeredOrderSkuWash.colorCode === colorCode
            && registeredOrderSkuWash.size === size) {
            fkOrderSkuId = registeredOrderSkuWash.id;
            return;
          }
          return false;
        });
        washList.push({
          id: fkOrderSkuId, // フクキタル発注SKU情報ID
          fOrderId: this.mainForm.getRawValue()['id'], // フクキタル発注ID
          colorCode: colorCode, // カラーコード
          size: size, // サイズ
          materialId: materialId, // 資材ID
          orderLot: Number(inputtedOrderSku.productOrderLot), // 資材数量
        } as FukukitaruOrderSku);
      });
    }
    console.debug('washList:', washList); // push時はコメントアウトしてください.開発時に使うので消さないでください.
    return washList;
  }

  /**
   * メインのFormGroupを返す
   * @return FormGroup
   */
  private getFormGroup(): FormGroup {
    // const ovd = new OrderValidatorDirective(); // カスタムバリデータをセット
    return this.formBuilder.group({
      id: [null], // フクキタル発注ID
      orderId: [null], // 発注ID
      orderNumber: [null],  // 発注No
      partNoId: [null], // 品番ID
      partNo: [null], // 品番
      productName: [''], // 品番情報:品名
      productDeliveryAt: [null],  // 生産納期
      mdfMakerName: [''],  // メーカー(画面表示用)
      isApprovalRequired: [null],  // 承認需要フラグ
      billingCompanyId: [null, [Validators.required]],  // 請求先会社名ID
      billingCompanyName: [''],  // 請求先会社名
      billingCompanyAddress: [''],  // 請求先会社住所
      billingCompanyTel: [''],  // 請求先会社電話番号
      billingCompanyFax: [''],  // 請求先会社Fax
      deliveryCompanyId: [null, [Validators.required]], // 納入先会社ID
      deliveryCompanyName: [''],  // 納入先会社名
      deliveryCompanyAddress: [''],  // 納入先会社住所
      deliveryCompanyTel: [''], // 納入先会社電話番号
      deliveryCompanyFax: [''], // 納入先会社FAX番号
      deliveryStaff: [''],  // 部署名・担当者名
      repeatNumber: [null, [Validators.pattern(/^[0-9,]*$/)]],  // リピート数
      deliveryType: [FukukitaruMasterDeliveryType.DOMESTIC],  // デリバリ
      urgent: [null],  // 緊急の出荷希望
      preferredShippingAt: [null, [Validators.required]],  // 希望出荷日
      orderAt: [null, [Validators.required]],  // 発注日
      orderCode: [''],  // オーダー識別コード
      fukukitaruOrderNumber: [''],  // フクキタル発注No.
      contactOrder: [''],  // 発注先
      orderSkus: this.formBuilder.array([]), // 発注SKU情報
      orderSkuWashName: this.formBuilder.array([]), // 発注SKU情報
      reflected: [null],  // 反映済
      tapeName: [''],  // テープ種類
      tapeWidthName: [''],  // テープ巾
      washPatternName: [''],  // 絵表示
      printSize: [null],  // サイズ印字
      printCoo: [null],  // 原産国印字
      orderSkuAttentionName: this.formBuilder.array([]),  // アテンションネーム
      orderSkuWashAuxiliary: this.formBuilder.array([]),  // 同封副資材
      contractNumber: [''],  // 契約No.
      specialReport: [''],  // 特記事項
      remarks: [''],  // 備考
      orderType: [null],  // 発注種別
      isResponsibleOrder: [null],  // 責任発注
      confirmStatus: [FukukitaruMasterConfirmStatusType.ORDER_NOT_CONFIRMED],  // 確定ステータス
      linkingStatus: [null], // 連携ステータス
      lossRate: [null],  // ロス率(表示のみ)
      skuMaterialId: [null],  // 洗濯ネーム発注SKUのmaterialId
      attentionSaveList: this.formBuilder.array([]), // アテンションネーム保存リスト
    });
  }

  /**
   * Formに品番情報を設定する
   * @param settingData フクキタル用情報
   * @param fOrder フクキタル用発注情報
   */
  private setFormValue(settingData: ScreenSettingFukukiatru, fOrder?: FukukitaruOrder): void {
    const order = settingData.order;
    const fItem = settingData.fkItem;

    this.mainForm.patchValue({ id: fOrder ? fOrder.id : null }); // フクキタル発注ID
    this.mainForm.patchValue({ orderId: order.id }); // 発注ID
    this.mainForm.patchValue({ orderNumber: order.orderNumber });  // 発注No
    this.mainForm.patchValue({ partNoId: order.partNoId }); // 品番ID
    this.mainForm.patchValue({ partNo: order.partNo }); // 品番
    this.mainForm.patchValue({ fItemId: fItem ? fItem.id : null }); // フクキタル品番ID
    this.mainForm.patchValue({ productDeliveryAt: order.productDeliveryAt });  // 生産納期
    this.mainForm.patchValue({ mdfMakerName: order.mdfMakerName });  // メーカー(画面表示用)
    this.mainForm.patchValue({ orderType: fOrder ? fOrder.orderType : settingData.orderType });  // 発注種別
    this.mainForm.patchValue({ isResponsibleOrder: fOrder ? fOrder.isResponsibleOrder : null }); // 責任発注
    this.mainForm.patchValue({ isApprovalRequired: fOrder ? fOrder.isApprovalRequired : null }); // 承認需要フラグ

    // 請求先
    const billingCompanyInfo = fOrder
      ? fOrder.billingDestination                                                        // 更新時はt_f_orderのデータを設定
      : this.materialOrderService.getInitBillingCompany(settingData.listBillingAddress); // 登録時はマスタデータの先頭のIDを初期値として設定
    if (billingCompanyInfo != null) {
      this.mainForm.patchValue({ billingCompanyId: billingCompanyInfo.id });
      this.mainForm.patchValue({ billingCompanyName: billingCompanyInfo.companyName });
      this.mainForm.patchValue({ billingCompanyAddress: billingCompanyInfo.address });
      this.mainForm.patchValue({ billingCompanyTel: billingCompanyInfo.tel });
      this.mainForm.patchValue({ billingCompanyFax: billingCompanyInfo.fax });
    }

    if (fOrder) {
      // 納入先
      const deliveryDestination = fOrder.deliveryDestination;
      this.mainForm.patchValue({ deliveryCompanyId: deliveryDestination.id });
      this.mainForm.patchValue({ deliveryCompanyName: deliveryDestination.companyName });
      this.mainForm.patchValue({ deliveryCompanyAddress: deliveryDestination.address });  // postalCodeの結合は不要
      this.mainForm.patchValue({ deliveryCompanyTel: deliveryDestination.tel });
      this.mainForm.patchValue({ deliveryCompanyFax: deliveryDestination.fax });

      this.mainForm.patchValue({ confirmStatus: fOrder.confirmStatus });  // 確定ステータス
      this.mainForm.patchValue({ linkingStatus: fOrder.linkingStatus });  // 連携ステータス
      this.mainForm.patchValue({ deliveryType: fOrder.deliveryType });    // デリバリ
    }

    this.mainForm.patchValue({ deliveryStaff: fOrder ? fOrder.deliveryStaff : '' });  // 部署名・担当者名
    this.mainForm.patchValue({ repeatNumber: fOrder ? fOrder.repeatNumber : null });  // リピート数
    this.mainForm.patchValue({ urgent: fOrder ? fOrder.urgent : null });  // 緊急の出荷希望
    this.mainForm.patchValue({
      preferredShippingAt:
        fOrder ? this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(fOrder.preferredShippingAt)) : null
    });  // 希望出荷日
    this.mainForm.patchValue({
      orderAt:
        fOrder ? this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(fOrder.orderAt)) : null
    });  // 発注日

    // フクキタル発注No.
    let status = '';
    status = FukukitaruMasterConfirmStatusTypeName.ORDER_CONFIRM_MAP[0];  // 未確定
    if (fOrder && fOrder.confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED) {
      status = FukukitaruMasterConfirmStatusTypeName.ORDER_CONFIRM_MAP[1];  // 確定
    }
    this.mainForm.patchValue({ fukukitaruOrderNumber: status });

    this.mainForm.patchValue({ contactOrder: settingData.listSupplierAddress[0].companyName });  // 発注先
    this.mainForm.patchValue({ orderCode: fOrder ? fOrder.orderCode : '' });  // オーダー識別コード
    this.mainForm.patchValue({ tapeName: fItem ? fItem.tapeName : '' });  // テープ種類
    this.mainForm.patchValue({ tapeWidthName: fItem ? fItem.tapeWidthName : '' });  // テープ巾
    this.mainForm.patchValue({ washPatternName: fItem ? fItem.washPatternName : '' });  // 絵表示
    this.mainForm.patchValue({ printSize: fItem ? fItem.printSize : null });  // サイズ印字
    this.mainForm.patchValue({ printCoo: fItem ? fItem.printCoo : null });  // 原産国印字

    const emptySku = []; // データ0件の場合の空フォーム

    // アテンションネーム
    if (fOrder != null && ListUtils.isNotEmpty(fOrder.orderSkuAttentionName)) {
      this.mainForm.setControl('orderSkuAttentionName',
        this.getAttentionNameFormArray(fOrder.orderSkuAttentionName));
    } else {
      this.mainForm.setControl('orderSkuAttentionName',
        this.getAttentionNameFormArray(emptySku));
    }

    // 同封副資材
    if (fOrder != null && ListUtils.isNotEmpty(fOrder.orderSkuWashAuxiliary)) {
      this.mainForm.setControl('orderSkuWashAuxiliary',
        this.getAuxiliaryMaterialFormArray(settingData.listAuxiliaryMaterial, fOrder.orderSkuWashAuxiliary));
    } else {
      this.mainForm.setControl('orderSkuWashAuxiliary',
        this.getAuxiliaryMaterialFormArray(settingData.listAuxiliaryMaterial, null));
    }
    this.mainForm.patchValue({ contractNumber: fOrder ? fOrder.contractNumber : '' }); // 契約No.
    this.mainForm.patchValue({ specialReport: fOrder ? fOrder.specialReport : '' });   // 特記事項
    this.mainForm.patchValue({ remarks: fOrder ? fOrder.remarks : '' });               // 備考
    this.mainForm.patchValue({ skuMaterialId: settingData.listWashName[0].id });       // 洗濯ネーム発注SKUのmateialId

    this.mainForm.setControl('attentionSaveList', this.createAttentionSaveList(fOrder));
  }

  /**
   * アテンション保存リスト作成.
   */
  private createAttentionSaveList(fOrder: FukukitaruOrder): FormArray {
    const saveList = this.formBuilder.array([]);

    this.convertSkuListToColorList().forEach((val) => {
      const list = (fOrder == null) ? [] : fOrder.orderSkuAttentionName.filter((name) => val.colorCode === name.colorCode);

      const resAttentionList = this.formBuilder.array([]);

      // 画面表示用に設定する
      list.forEach((item) => {
        resAttentionList.push(this.setAttentionValueToList(item));
      });

      saveList.push(this.formBuilder.group({
        colorCode: val.colorCode,
        colorName: val.colorName,
        attentionList: resAttentionList
      }));
    });

    return saveList;
  }

  /**
   * sku(色・サイズ)リストから共通含めた全色のリストに変換して返す。
   * @param skuList sku(色・サイズ)リスト
   * @returns 色リスト
   */
  private convertSkuListToColorList(): {colorCode: string, colorName: string}[] {
    // 共通を追加した色リストの作成
    const colorList = [
      {
        colorCode: CompositionsCommon.COLOR_CODE,
        colorName: CompositionsCommon.COLOR_NAME,
      }];

    // sku(色・サイズ)リストから、色コードの重複除去
    this.skuList.forEach((sku) => {
      if (!colorList.some(color => color.colorCode === sku.colorCode)) {
        colorList.push({
          colorCode: sku.colorCode,
          colorName: sku.colorName,
        });
      }
    });

    return colorList;
  }

  /**
   * フクキタル発注SKU情報のフォーム配列を作成して返す
   * @param fOrderSkuList 作成する配列のデータ
   * @returns 組成(混率)のFormGroup
   */
  private getAttentionNameFormArray(fOrderSkuList: FukukitaruOrderSku[]): FormArray {
    const fOrderSkuFormArray = new FormArray([]);

    // データをFormに設定する
    fOrderSkuList.forEach(fOrderSku => {
      fOrderSkuFormArray.push(this.createAttentionNameFormGroupSettedValues(fOrderSku));
    });

    // アテンションネームの場合は残り全行空フォームを作成
    const settedLength = fOrderSkuFormArray.length;
    for (let i = 0; i < this.ATTENTION_NAME_MAX_COUNT - settedLength; i++) {
      fOrderSkuFormArray.push(this.createAttentionNameFormGroupSetted());
    }

    return fOrderSkuFormArray;
  }

  /**
  * データを設定してフクキタル発注SKU情報フォームを１つ作成して返す。
  * @param composition 組成(混率)情報
  * @returns フクキタル発注SKU情報のFormGroup
  */
  private createAttentionNameFormGroupSettedValues(fOrderSku: FukukitaruOrderSku): FormGroup {
    return this.formBuilder.group({
      id: [fOrderSku.id],
      fOrderId: [fOrderSku.fOrderId],
      colorCode: [fOrderSku.colorCode],
      size: [fOrderSku.size],
      materialId: [fOrderSku.materialId],
      orderLot: [fOrderSku.orderLot, [Validators.pattern(new RegExp(ValidatorsPattern.NON_NEGATIVE_INTEGER))]],
      materialType: [fOrderSku.materialType],
      materialTypeName: [fOrderSku.materialTypeName],
      materialCode: [fOrderSku.materialCode],
      materialCodeName: [fOrderSku.materialCodeName],
      moq: [fOrderSku.moq]
    }, { validator: Validators.compose([attentionNameValidator]) }  // リスト選択と資材数量の相関チェック
    );
  }

  /**
   * フクキタル発注SKU情報のFormGroupを1つ作成して返す。
   * @returns フクキタル発注SKU情報のFormGroup
   */
  private createAttentionNameFormGroupSetted(): FormGroup {
    return this.formBuilder.group({
      id: [null],
      fOrderId: [null],
      colorCode: [''],
      size: [''],
      materialId: [null],
      orderLot: [null, [Validators.pattern(new RegExp(ValidatorsPattern.NON_NEGATIVE_INTEGER))]],
      materialType: [null],
      materialTypeName: [''],
      materialCode: [''],
      materialCodeName: [''],
      moq: [null]
    }, { validator: Validators.compose([attentionNameValidator]) }  // リスト選択と資材数量の相関チェック
    );
  }

  /**
   * フクキタルマスタ情報のフォーム配列を作成して返す
   * @param mstDataList 作成する配列のデータ
   * @returns 組成(混率)のFormGroup
   */
  private getAuxiliaryMaterialFormArray(mstDataList: FukukitaruMaster[], fOrderSkuDataList: FukukitaruOrderSku[]): FormArray {
    const mstFormArray = new FormArray([]);

    // データをFormに設定する
    mstDataList.forEach(mstData => {
      const pushData = new FukukitaruOrderSku;
      let isChecked = false;
      // マスタデータをフォームに設定
      pushData.materialId = mstData.id;
      pushData.materialCode = mstData.code;
      pushData.materialCodeName = mstData.codeName;
      pushData.moq = mstData.moq;
      pushData.materialType = FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL;

      if (fOrderSkuDataList != null) {
        fOrderSkuDataList.forEach(fOrderSkuData => {
          if (mstData.id === fOrderSkuData.materialId) {
            // materialIdが一致する場合にフクキタル発注データをフォームに設定
            pushData.id = fOrderSkuData.id;
            pushData.fOrderId = fOrderSkuData.fOrderId;
            pushData.colorCode = fOrderSkuData.colorCode;
            pushData.size = fOrderSkuData.size;
            pushData.orderLot = fOrderSkuData.orderLot;
            pushData.materialTypeName = fOrderSkuData.materialTypeName;
            isChecked = true;
          }
        });
      }

      mstFormArray.push(this.createAuxiliaryMaterialFormGroupSettedValues(pushData, isChecked));
    });
    console.debug('mstFormArray:', mstFormArray); // push時はコメントアウトしてください.開発時に使うので消さないでください.

    return mstFormArray;
  }

  /**
  * データを設定してフクキタル発注SKU情報フォームを１つ作成して返す。
  * @param composition 組成(混率)情報
  * @returns フクキタル発注SKU情報のFormGroup
  */
  private createAuxiliaryMaterialFormGroupSettedValues(fOrderSku: FukukitaruOrderSku, isChecked: boolean): FormGroup {
    return this.formBuilder.group({
      id: [fOrderSku.id],
      fOrderId: [fOrderSku.fOrderId],
      colorCode: [fOrderSku.colorCode],
      size: [fOrderSku.size],
      materialId: [fOrderSku.materialId],
      orderLot: [fOrderSku.orderLot, [Validators.pattern(/^[0-9,]*$/)]],
      materialType: [fOrderSku.materialType],
      materialTypeName: [fOrderSku.materialTypeName],
      materialCode: [fOrderSku.materialCode],
      materialCodeName: [fOrderSku.materialCodeName],
      moq: [fOrderSku.moq],
      checked: [isChecked]
    }, { validator: Validators.compose([auxiliaryMaterialValidator]) }  // チェックと資材数量の相関チェック
    );
  }

  /**
   * サブメニューリンク押下時の処理。
   * 指定した要素へページ内リンクする。
   * @param id リンク先のid
   */
  onScrollEvent(id: string): boolean {
    window.scrollTo(0,
      window.pageYOffset
      + document.getElementById(id).getBoundingClientRect().top
      - document.getElementById('header').getBoundingClientRect().height
    );
    return false;
  }

  /**
   * 請求先と同じ入力値を納入先の項目に反映する。
   * @param value 入力値
   */
  onReflectInfo(value: boolean): void {
    if (value === true) {
      if (this.mainForm.controls.billingCompanyId.value !== this.mainForm.controls.deliveryCompanyId.value) {
        // 請求先会社IDと納入先会社IDが異なる場合のみ、設定する

        // 請求先・納入先入力欄は非活性であるため、this.mainForm.dirtyでの変更感知不可
        // 請求先と同じチェックで値が設定された場合も変更されたと認識し、変更感知フラグをtrueにする
        this.isBillingOrDeliveryAddressTouched = true;

        this.mainForm.patchValue({ deliveryCompanyId: this.mainForm.controls.billingCompanyId.value });
        this.mainForm.patchValue({ deliveryCompanyName: this.mainForm.controls.billingCompanyName.value });
        this.mainForm.patchValue({ deliveryCompanyAddress: this.mainForm.controls.billingCompanyAddress.value });
        this.mainForm.patchValue({ deliveryCompanyTel: this.mainForm.controls.billingCompanyTel.value });
        this.mainForm.patchValue({ deliveryCompanyFax: this.mainForm.controls.billingCompanyFax.value });
      }
    }
  }

  /**
   * 会社名を検索するモーダルを表示する。
   * @param fukukitaruListMasterType マスタタイプリスト
   */
  openSearchCompanyModal(fukukitaruListMasterType: FukukitaruMasterType): void {
    const modalRef = this.modalService.open(SearchCompanyModalComponent, { windowClass: 'company' });

    modalRef.componentInstance.partNoId = this.formConditions.partNoId;
    modalRef.componentInstance.orderId = this.formConditions.orderId;
    modalRef.componentInstance.deliveryType = this.formConditions.deliveryType;
    modalRef.componentInstance.searchCompanyName = this.formConditions.searchCompanyName;

    // モーダルへ渡す値を設定する。
    switch (fukukitaruListMasterType) {
      case FukukitaruMasterType.BILLING_ADDRESS: // 請求先会社名
        modalRef.componentInstance.listMasterType = FukukitaruMasterType.BILLING_ADDRESS;
        modalRef.componentInstance.defaultCompanyId = this.mainForm.controls.billingCompanyId.value; // 宛先情報の会社id
        break;
      case FukukitaruMasterType.DERIVERY_ADDRESS: // 納入先会社名
        modalRef.componentInstance.listMasterType = FukukitaruMasterType.DERIVERY_ADDRESS;
        modalRef.componentInstance.defaultCompanyId = this.mainForm.controls.deliveryCompanyId.value; // 宛先情報の会社id
        break;
      default:
        break;
    }

    // モーダルからの値を設定する。
    modalRef.result.then((result: FukukitaruDestination) => {
      if (result) {
        // 請求先・納入先入力欄は非活性であるため、this.mainForm.dirtyでの変更感知不可
        // モーダルより値が設定された場合を変更されたと認識し、変更感知フラグをtrueにする
        this.isBillingOrDeliveryAddressTouched = true;

        switch (fukukitaruListMasterType) {
          case FukukitaruMasterType.BILLING_ADDRESS:
            this.mainForm.patchValue({ isApprovalRequired: result.isApprovalRequired }); // 承認需要フラグ
            this.mainForm.patchValue({ billingCompanyId: result.id });
            this.mainForm.patchValue({ billingCompanyName: result.companyName });
            this.mainForm.patchValue({ billingCompanyAddress: result.address });  // postalCodeの結合は不要
            this.mainForm.patchValue({ billingCompanyTel: result.tel });
            this.mainForm.patchValue({ billingCompanyFax: result.fax });
            break;
          case FukukitaruMasterType.DERIVERY_ADDRESS:
            this.mainForm.patchValue({ deliveryCompanyId: result.id });
            this.mainForm.patchValue({ deliveryCompanyName: result.companyName });
            this.mainForm.patchValue({ deliveryCompanyAddress: result.address });  // postalCodeの結合は不要
            this.mainForm.patchValue({ deliveryCompanyTel: result.tel });
            this.mainForm.patchValue({ deliveryCompanyFax: result.fax });
            break;
          default:
            break;
        }
      }
    }, () => { });
  }

  /**
   * アテンションネームモーダル表示.
   */
  openAttensionModal(): void {
    const modalRef = this.modalService.open(AttentionModalComponent, { windowClass: 'attention' });
    modalRef.componentInstance.type = FukukitaruMasterType.ATTENTION_NAME;
    modalRef.componentInstance.attentionNameList = this.attentionNameList;
    modalRef.componentInstance.saveList = this.rawAttentionSaveList;
    modalRef.componentInstance.materialFileList = this.materialFileList;  // 資材発注ファイルリスト

    modalRef.result.then((results: FukukitaruAttentionByColor[]) => {
      const func = (): FormArray => {
        const result = this.formBuilder.array([]);

        // 発注SKUを色別に合計する
        const colorOrderFormArray = this.sumColorProductOrderLot();

        this.convertSkuListToColorList().forEach((val1) => {
          const colorAttentionList = results.find((name) => val1.colorCode === name.colorCode);

          const resAttentionList = this.formBuilder.array([]);

          let colorOrderLot = 0;
          // 指定色コードの洗濯ネーム発注数量合計を取得
          if (val1.colorCode !== CompositionsCommon.COLOR_CODE) {
            const colorOrderSkuSumLot = colorOrderFormArray.controls.find(colorOrder =>
              val1.colorCode === colorOrder.value.colorCode);
            if (colorOrderSkuSumLot != null) {
              colorOrderLot = colorOrderSkuSumLot.get('orderLot').value;
            }
          }

          // 戻り値をメイン画面表示用に設定する
          colorAttentionList.attentionList.forEach((item) => {
            if (FormUtils.isEmpty(item.orderLot) || item.orderLot === 0) {
              // アテンションネーム資材数量が空または0の場合はSKU発注数量合計をセット
              item.orderLot = colorOrderLot;
            }
            resAttentionList.push(this.setAttentionValueToList(item));
          });

          result.push(this.formBuilder.group({
            colorCode: colorAttentionList.colorCode,
            colorName: colorAttentionList.colorName,
            attentionList: resAttentionList
          }));
        });
        return result;
      };
      this.mainForm.setControl('attentionSaveList', func());
    }, () => {});  // バツボタンクリック時は何もしない
  }

  /**
   * アテンションネームの値を画面表示用のリストに設定する
   * @param item
   */
  setAttentionValueToList(item: FukukitaruOrderSku): FormGroup {
    const attentionList = this.formBuilder.group({
      /** フクキタル発注SKU情報ID. */
      id: item.id,
      /** フクキタル発注ID. */
      fOrderId: item.fOrderId,
      /** カラーコード. */
      colorCode: item.colorCode,
      /** サイズ. */
      size: null,  // アテンションネームにサイズは無いためnull
      /** 資材ID. */
      materialId: item.materialId,
      /** 資材数量. */
      orderLot: [item.orderLot, [Validators.required, Validators.pattern(new RegExp(ValidatorsPattern.NON_NEGATIVE_INTEGER))]],
      /** 資材種類. */
      materialType: item.materialType,
      /** 資材種類名. */
      materialTypeName: item.materialTypeName,
      /** 資材コード. */
      materialCode: item.materialCode,
      /** 資材コード名. */
      materialCodeName: item.materialCodeName,
      /** 並び順. */
      sortOrder: item.sortOrder,
      /** 出荷単位.(画面表示用) */
      moq: null  // アテンションネームに出荷単位は無いためnull
    });
    return attentionList;
  }

  /**
   * ファイルダウンロードリンク押下処理.
   * @param fukukitaruMasterType 資材種別
   */
  onFileDownLoad(fukukitaruMasterType: FukukitaruMasterType): void {
    this.overallErrorMsgCode = '';
    this.materialFileList.some(materialFile => {
      if (materialFile.masterType === fukukitaruMasterType) {
        this.fileService.fileDownload(materialFile.fileNoId.toString()).subscribe(res => {
          const data = this.fileService.splitBlobAndFileName(res);
          FileUtils.downloadFile(data.blob, data.fileName);
        }, () => this.overallErrorMsgCode = 'ERRORS.FILE_DL_ERROR');
        return true;
      }
    });
  }

  /**
   * 新規登録ボタン押下時の処理.
   * @param url url
   */
  onRouterLinkNew(url: string): void {
    this.router.navigate([url, Path.NEW],
      { queryParams: { 'orderId': this.formConditions.orderId, 'partNoId': this.formConditions.partNoId } });
  }

  /**
   * フクキタル発注情報からSKU情報を取得する.
   * 洗濯ネームの場合、先頭のmaterialIdに該当する情報のみ取得する
   * @param order フクキタル発注情報
   * @return SKU情報
   */
  private ganarateOrderSku(order: FukukitaruOrder): FukukitaruOrderSku[] {
    // 先頭のmaterialIdに該当する情報だけフィルターをかける
    return order.orderSkuWashName.filter((skus, _, self) => self[0].materialId === skus.materialId);
  }

  /**
   * 発注SKU入力値変更時の処理.
   */
  onChangeProductOrderLot(): void {
    let totalOrderLot = this.sumTotalProductOrderLot();
    totalOrderLot = (totalOrderLot === 0 ? null : totalOrderLot);
  }

  /**
   * 発注SKUを合計する.
   * @returns 発注SKU合計
   */
  private sumTotalProductOrderLot(): number {
    let totalOrderLot = 0;
    this.formOrderSkuArray.forEach(orderSku => {
      const sizeList: { id: number, colorCode: string, size: string, productOrderLot: string }[] = orderSku.value.sizeList;
      sizeList.forEach(sizeInfo => {
        const plot = sizeInfo.productOrderLot;
        if (NumberUtils.isNumber(plot)) {
          totalOrderLot += Number(plot);
        }
      });
    });
    return totalOrderLot;
  }

  /**
   * 発注SKUを色別に合計する.
   * @returns 色別の発注SKU合計
   */
  private sumColorProductOrderLot(): FormArray {
    const colorOrderFormArray = new FormArray([]);
    this.formOrderSkuArray.forEach(orderSku => {
      const sizeList: { id: number, colorCode: string, size: string, productOrderLot: string }[] = orderSku.value.sizeList;
      let colorTotalOrderLot = 0;
      sizeList.forEach(sizeInfo => {
        const plot = sizeInfo.productOrderLot;
        if (NumberUtils.isNumber(plot)) {
          colorTotalOrderLot += Number(plot);
        }
      });
      const sizeSetData = orderSku.value.sizeList[0];
      const colorOrderLot = this.formBuilder.group({
        id: sizeSetData.id, // フクキタル発注SKUID
        colorCode: sizeSetData.colorCode,
        size: sizeSetData.size,
        orderLot: colorTotalOrderLot  // 資材数量
      });
      colorOrderFormArray.push(colorOrderLot);
    });
    return colorOrderFormArray;
  }

  /**
   * デリバリラジオボタン変更時の処理.
   * 入力値を保持したままデータを取得し直す.
   * @param deliveryType デリバリ種別
   */
  async onChangeDeliveryType(deliveryType: FukukitaruMasterDeliveryType): Promise<void> {
    this.loadingService.loadStart();

    /**
     * デリバリラジオボタン変更時の値の保持は仕様に記載が無いためコメントアウト。
     * 便利機能として今後要望があれば、その際に検討する。
     */
    // const attentionNameValue: FukukitaruOrderSku[] = this.mainForm.get('orderSkuAttentionName').value;  // アテンションネーム
    // const attentionNameSetData = attentionNameValue.filter(val =>
    //   val.id != null || val.materialId != null || !FormUtils.isEmpty(val.orderLot));  // 入力されたアテンションネームのみ抽出(idがあれば更新データ)

    // const washAuxiliaryMaterialValue: OrderSkuWashAuxiliaryMaterialValue[]
    //   = this.mainForm.get('orderSkuWashAuxiliary').value;  // 洗濯ネーム同封副資材
    // const filteredAuxiliaryMaterial = washAuxiliaryMaterialValue.filter(val =>
    //   val.id != null || val.checked || !FormUtils.isEmpty(val.orderLot));  // 入力された洗濯ネーム同封副資材のみ抽出(idがあれば更新データ)
    // const auxiliaryMaterialSetDataList: FukukitaruOrderSku[] = [];
    // filteredAuxiliaryMaterial.forEach(val => auxiliaryMaterialSetDataList.push({
    //   id: val.id, fOrderId: val.fOrderId, orderLot: val.orderLot, materialId: val.materialId
    // } as FukukitaruOrderSku));

    // マスタ再取得
    this.formConditions.deliveryType = deliveryType;
    const screenSettingFukukiatru = await this.getFukukitaruMasterData().then();

    // メンバ変数にScreenSettingFukukiatru取得データをセット
    this.setDataToMemberVariable(screenSettingFukukiatru);

    /**
     * デリバリラジオボタン変更時の値の保持は仕様に記載が無いためコメントアウト。
     * 便利機能として今後要望があれば、その際に検討する。
     */
    // // FormArray再設定
    // this.mainForm.setControl('orderSkuAttentionName', this.getAttentionNameFormArray(attentionNameSetData)); // アテンションネーム
    // this.mainForm.setControl('orderSkuWashAuxiliary',
    //   this.getAuxiliaryMaterialFormArray(
    //     screenSettingFukukiatru.listAuxiliaryMaterial, auxiliaryMaterialSetDataList)); // 洗濯ネーム同封副資材

    // 再取得したマスタデータを再設定する
    const emptyArray: FukukitaruOrderSku[] = [];
    this.mainForm.setControl('orderSkuWashAuxiliary',
      this.getAuxiliaryMaterialFormArray(
        screenSettingFukukiatru.listAuxiliaryMaterial, emptyArray)); // 洗濯ネーム同封副資材

    this.loadingService.loadEnd();
  }

  /**
   * ロス率入力値変更時の処理.
   * 入力値が正しければエラーメッセージを非表示
   * @param lossRate ロス率入力値
   */
  onChangeLossRate(lossRate: number) {
    // 確定ボタンクリック時の変更検知の対象から除外するため、ロス率変更しても変更なしの状態に戻す
    this.mainForm.controls.lossRate.markAsPristine();
    if (!NumberUtils.isNumber(lossRate) || lossRate < 1) {
      return;
    }
    this.isLossRateInvalid = false;
  }

  /**
   * ロス率反映ボタン押下時の処理.
   * @param ロス率入力値
   */
  onReflectLoss(lossRate: number): void {
    if (!NumberUtils.isNumber(lossRate) || lossRate < 1) {
      this.isLossRateInvalid = true;
      return;
    }
    this.isLossRateInvalid = false;

    this.formOrderSkuArray.forEach(orderSku => {
      const sizeListFormArray = orderSku.get('sizeList') as FormArray;
      sizeListFormArray.controls.forEach(sizeInfo => {
        const pLot = sizeInfo.value.productOrderLot;
        if (NumberUtils.isNumber(pLot)) {
          const lot = Number(pLot);
          sizeInfo.patchValue({ productOrderLot: lot + Math.floor(lot * (lossRate / 100)) });
          // SKUの数量が変更されたことを検知させる
          sizeInfo.markAsDirty();
        }
      });
    });
    this.mainForm.patchValue({ reflected: true });
  }

  /**
   * 責任発注表示判定処理.
   *
   * 資材発注確定済 かつ 優良誤認未承認 の場合のみ表示
   * @returns 優良誤認承認済、または非対象であればtrue
   */
  isShowResponsibleOrder(): boolean {
    return BusinessCheckUtils.isShowResponsibleOrder(this.itemData, this.fOrderData);
  }

  /**
   * ステータスラベルの表示文言を返却
   * @returns ステータスラベル文字
   */
  showStatuslabel(): string {
    const label = this.materialOrderService.getStatusLabel(this.fOrderData.confirmStatus, this.fOrderData.linkingStatus);
    return label;
  }
}
