import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, FormArray } from '@angular/forms';
import { NgbDateParserFormatter, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
  FukukitaruMasterType, Path, PreEventParam, FukukitaruMasterDeliveryType, CompositionsCommon,
  FukukitaruMasterOrderType, FukukitaruMasterConfirmStatusType, SubmitType, FukukitaruMasterOrderTypeName,
  FukukitaruMasterConfirmStatusTypeName, ValidatorsPattern, FukukitaruBrandCode, FukukitaruMasterMaterialType, OccupationType, ResourceType
} from '../../const/const';
import { ExceptionUtils } from '../../util/exception-utils';
import { DateUtils } from '../../util/date-utils';
import { BusinessUtils } from '../../util/business-utils';
import { StringUtils } from '../../util/string-utils';
import { NumberUtils } from '../../util/number-utils';
import { FileUtils } from '../../util/file-utils';
import { FormUtils } from '../../util/form-utils';
import { ListUtils } from '../../util/list-utils';
import { BusinessCheckUtils } from 'src/app/util/business-check-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

import {
  auxiliaryMaterialValidator, attentionTagValidator
} from './validator/fukukitaru-order01-hang-tag-validator.directive';
// import { shoeSealValidator } from '../../validator/material-order/common-validator.directive';

import { SearchCompanyModalComponent } from '../search-company-modal/search-company-modal.component';
import { FukukitaruInputAssistModalComponent } from '../fukukitaru-input-assist-modal/fukukitaru-input-assist-modal.component';
import { AttentionModalComponent } from '../attention-modal/attention-modal.component';
import {
  MaterialOrderSubmitConfirmModalComponent
} from '../material-order/material-order-submit-confirm-modal/material-order-submit-confirm-modal.component';

import { FukukitaruOrder01Service } from '../../service/fukukitaru-order01.service';
import { LoadingService } from '../../service/loading.service';
import { FileService } from '../../service/file.service';
import { HeaderService } from '../../service/header.service';
import { SessionService } from '../../service/session.service';
import { MaterialOrderService } from '../../service/material-order/material-order.service';

import { Item } from '../../model/item';
import { Compositions } from '../../model/compositions';
import { OrderViewCompositions } from '../../model/order-view-compositions';
import { Order } from '../../model/order';
import { FukukitaruItem } from '../../model/fukukitaru-item';
import { FukukitaruOrder } from '../../model/fukukitaru-order';
import { FukukitaruOrderSku } from '../../model/fukukitaru-order-sku';
import { ScreenSettingFukukiatru } from '../../model/screen-setting-fukukitaru';
import { ScreenSettingFukukitaruSku } from '../../model/screen-setting-fukukitaru-sku';
import { ScreenSettingFukukitaruOrderSearchCondition } from '../../model/screen-setting-fukukitaru-order-search-condition';
import { FukukitaruMaster } from '../../model/fukukitaru-master';
import { FukukitaruDestination } from '../../model/fukukitaru-destination';
import { FukukitaruOrderSearchCondition } from '../../model/fukukitaru-order-search-condition';
import { FukukitaruMasterAppendicesTerm } from '../../model/fukukitaru-master-appendices-term';
import { FukukitaruMaterialAttentionTag } from '../../model/fukukitaru-material-attention-tag';
import { FukukitaruItemAttentionAppendicesTerm } from 'src/app/model/fukukitaru-item-attention-appendices-term';
import { FukukitaruInputAssistSet, FukukitaruInputAssistSetDetails } from '../../model/fukukitaru-input-assist-set';
import { MaterialFileInfo } from '../../model/material-file-info';
import { MaterialOrderDisplayFlag } from 'src/app/model/material-order-display-flag';
import { ErrorDetail } from '../../model/error-detail';

import { OrderSkuBottomBillAttention } from '../../interface/order-sku-bottom-bill-attention';
import { FukukitaruAppendicesTermByColor } from '../../interface/fukukitaru-appendices-term-by-color';
import { FukukitaruAppendicesTerm } from '../../interface/fukukitaru-appendices-term';
import { FukukitaruAttentionByColor } from '../../interface/fukukitaru-attention-by-color';
import { OrderSkuAttentionTagValue } from 'src/app/interface/order-sku-attention-tag-value';
import { OrderSkuBottomBillAuxiliaryMaterialValue } from 'src/app/interface/order-sku-bottom-bill-auxiliary-material-value';
import { OrderSkuBottomBillValue } from 'src/app/interface/order-sku-bottom-bill-value';
import { OrderSkuValue } from 'src/app/interface/order-sku-value';

@Component({
  selector: 'app-fukukitaru-order01-hang-tag',
  templateUrl: './fukukitaru-order01-hang-tag.component.html',
  styleUrls: ['./fukukitaru-order01-hang-tag.component.scss']
})
export class FukukitaruOrder01HangTagComponent implements OnInit {

  // htmlから参照したい定数を定義
  readonly FUKUKITARU_MASTER_TYPE = FukukitaruMasterType;
  readonly FUKUKITARU_DELIVERY_TYPE = FukukitaruMasterDeliveryType;
  readonly FUKUKITARU_MASTER_CONFIRM_STATUS_TYPE = FukukitaruMasterConfirmStatusType;
  readonly ORDER_CONFIRMED: FukukitaruMasterConfirmStatusType = FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED;
  readonly ORDER_TYPE = FukukitaruMasterOrderType;
  readonly F_ORDER_TYPE_NAME = FukukitaruMasterOrderTypeName;
  readonly F_CONFIRM_STATUS_TYPE_NAME = FukukitaruMasterConfirmStatusTypeName;
  readonly PATH = Path;
  readonly SUBMIT_TYPE = SubmitType;
  readonly FUKUKITARU_BRAND_CODE = FukukitaruBrandCode;
  readonly OCCUPATION_TYPE = OccupationType;
  readonly RESOURCE_TYPE = ResourceType;
  readonly COMPOSITIONS_COMMON = CompositionsCommon;

  // フクキタル検索項目
  private readonly SEARCH_ITEMS: FukukitaruMasterType[] = [
    FukukitaruMasterType.ITEM,  // 品番情報
    FukukitaruMasterType.ORDER,  // 発注情報
    FukukitaruMasterType.SKU,  // SKU
    FukukitaruMasterType.FUKUKITARU_ITEM,  // フクキタル品番情報

    FukukitaruMasterType.BILLING_ADDRESS, // 請求先
    FukukitaruMasterType.SUPPLIER_ADDRESS,  // 発注先

    FukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM,  // アテンションタグ付記用語
    FukukitaruMasterType.ATTENTION_SEAL_TYPE, // アテンションシールのシール種類
    FukukitaruMasterType.RECYCLE, // リサイクルマーク

    FukukitaruMasterType.BOTTOM_BILL, // 下札類
    FukukitaruMasterType.ATTENTION_TAG, // アテンションタグ
    FukukitaruMasterType.AUXILIARY_MATERIAL,  // 同封副資材

    FukukitaruMasterType.ORDER_TYPE,        // 発注種別
    FukukitaruMasterType.MATERIAL_FILE_INFO, // 資材ファイル情報

    FukukitaruMasterType.ATTENTION_BOTTOM_BILL, // アテンション下札

    FukukitaruMasterType.CN_PRODUCT_CATEGORY, // 中国内販情報(製品分類)
    FukukitaruMasterType.CN_PRODUCT_TYPE,      // 中国内販情報(製品種別)
    FukukitaruMasterType.CATEGORY_CODE,  // カテゴリコード
    FukukitaruMasterType.INPUT_ASSIST_SET, // 入力補助セット
    FukukitaruMasterType.SUSTAINABLE_MARK // サスティナブルマーク
  ];

  materialOrderDisplayFlg = new MaterialOrderDisplayFlag(false); // 資材発注(フクキタル)項目表示フラグ

  readonly URL_WASH = 'fukukitaruOrder01Wash';
  readonly URL_HANG_TAG = 'fukukitaruOrder01HangTag';

  itemData: Item; // 品番情報
  orderData: Order; // 発注情報
  fkItemData: FukukitaruItem;  // フクキタル用品番情報
  fOrderData: FukukitaruOrder;  // フクキタル用発注情報のデータ
  skuList: ScreenSettingFukukitaruSku[] = []; // SKU(カラーサイズ)リスト
  orderSkuValue: FukukitaruOrderSku[] = []; // フクキタル発注登録済のSKU(カラーサイズ)情報リスト
  compositionViewList: OrderViewCompositions[] = [];  // 組成情報のデータリスト
  fOrderPastList: FukukitaruOrder[] = []; // 資材発注過去データ

  attentionTagAppendicesTermList: FukukitaruMasterAppendicesTerm[] = []; // アテンションタグ 付記用語リストマスタデータ
  attentionSealTypeList: FukukitaruMaster[] = []; // アテンションシールの種類リスト
  recycleMarkList: FukukitaruMaster[] = []; // リサイクルマークリスト
  attentionTagAppendicesTermByColorList: FukukitaruAppendicesTermByColor[] = [];  // 色別のアテンションタグ付記用語リスト
  attentionTagAppendicesTermSelectedColorList: string[] = [];  // アテンションタグ 付記用語で指定されている色コードリスト

  bottomBillList: FukukitaruMaster[] = []; // 下札リスト
  attentionTagList: FukukitaruMaterialAttentionTag[] = []; // アテンションタグリスト
  auxiliaryMaterialList: FukukitaruMaster[] = []; // 同封副資材マスタリスト

  private listMaterialFile: MaterialFileInfo[] = [];  // 資材ファイル情報リスト
  private registeredOrderSkuBottomBill: FukukitaruOrderSku[] = []; // 登録済下げ札類SKU

  billingDestination: FukukitaruDestination;   // 請求先
  deliveryDestination: FukukitaruDestination;  // 納入先

  cnProductCategoryList: FukukitaruMaster[] = []; // 中国内販情報(製品分類)
  cnProductTypeList: FukukitaruMaster[] = [];     // 中国内販情報(製品種別)

  categoryCodeList: FukukitaruMaster[] = [];
  materialFileList: MaterialFileInfo[] = [];  // 資材発注ファイルリスト
  inputAssistSetList: FukukitaruInputAssistSet[] = []; // 入力補助セットリスト

  occupationType = ''; // ログインユーザーの職種

  confirmStatus = '未確定'; // 発注No(確定／未確定を表示)
  contactOrder = '';  // 発注先

  seasonValue: string;  // サブシーズンのvalue値

  path = '';  // new,view,edit
  private queryParamsPartNoId: number; // 品番Id(queryParams)
  private queryParamsOrderId: number; // 発注Id(queryParams)

  isShowFooter = false;     // フッター表示フラグ
  isDirty = false;          // 画面変更検知フラグ
  isBillingOrDeliveryAddressTouched = false; // 請求先・納入先変更検知フラグ
  isBtnLock = false;        // 登録/更新/確定処理中にボタンをロックするためのフラグ
  isInitDataSetted = false; // 画面用データ取得完了フラグ
  submitted = false;        // submit押下フラグ
  private submitType = '';  // 押された送信ボタンの種類

  mainForm: FormGroup;

  isLossRateInvalid = false;  // ロス率バリデーションエラーフラグ

  overallSuccessMsgCode = ''; // 画面全体にかかる正常系メッセージ
  overallErrorMsgCode = '';  // 画面全体にかかる異常系メッセージ

  /** フクキタルのバリデーション */
  private fkValidators = [
    // shoeSealValidator  // アイテムコードAのバリデーション(必要に応じてコメント解除する)
  ]; // 共通バリデーションを追加する場合はここに追加する

  apiValidateErrorsMap: Map<string, ErrorDetail[]> = new Map();   // APIのバリデーションエラーMap

  constructor(
    private headerService: HeaderService,
    private router: Router,
    private loadingService: LoadingService,
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private formBuilder: FormBuilder,
    private modalService: NgbModal,
    private route: ActivatedRoute,
    private fukukitaruOrder01Service: FukukitaruOrder01Service,
    private fileService: FileService,
    private sessionService: SessionService,
    private materialOrderService: MaterialOrderService
  ) { }

  /**
   * mainFormのcontrolを返す.
   * @returns this.mainForm.controls
   */
  get fCtrl(): any { return this.mainForm.controls; }

  /**
   * mainFormのorderSkusのcontrolを返す.
   * @return mainForm.get('orderSkus').controls
   */
  get fCtrlOrderSkus(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkus')).controls;
  }

  /**
   * mainFormのorderSkuBottomBillのcontrolを返す.
   * @return mainForm.get('orderSkuBottomBill').controls
   */
  get fCtrlOrderSkuBottomBill(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkuBottomBill')).controls;
  }

  /**
   * mainFormのorderSkuBottomBillのvalueを返す.
   * @return mainForm.get('orderSkuBottomBill').value
   */
  get fValOrderSkuBottomBill(): OrderSkuBottomBillValue[] {
    return this.mainForm.getRawValue()['orderSkuBottomBill'];
  }

  /**
   * mainFormのorderSkuAttentionTagのcontrolを返す.
   * @return mainForm.get('orderSkuAttentionTag').controls
   */
  get fCtrlOrderSkuAttentionTag(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkuAttentionTag')).controls;
  }

  /**
   * mainFormのorderSkuAttentionTagのvalueを返す.
   * @return mainForm.get('orderSkuAttentionTag').value
   */
  get fValOrderSkuAttentionTag(): OrderSkuAttentionTagValue[] {
    return this.mainForm.getRawValue()['orderSkuAttentionTag'];
  }

  /**
   * mainFormのorderSkuBottomBillAuxiliaryMaterialのcontrolを返す.
   * @return mainForm.get('orderSkuBottomBillAuxiliaryMaterial').controls
   */
  get fCtrlOrderSkuBottomBillAuxiliaryMaterial(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkuBottomBillAuxiliaryMaterial')).controls;
  }

  /**
   * mainFormのorderSkuBottomBillAuxiliaryMaterialのvalueを返す.
   * @return mainForm.get('orderSkuBottomBillAuxiliaryMaterial').value
   */
  get fValOrderSkuBottomBillAuxiliaryMaterial(): OrderSkuBottomBillAuxiliaryMaterialValue[] {
    return this.mainForm.getRawValue()['orderSkuBottomBillAuxiliaryMaterial'];
  }

  /**
   * mainFormのorderSkuBottomBillAttentionのcontrolを返す.
   * @return mainForm.get('orderSkuBottomBillAttention').controls
   */
  get fCtrlOrderSkuAttentionBottomBill(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('orderSkuBottomBillAttention')).controls;
  }

  /**
   * mainFormのorderSkuBottomBillAttentionのvalueを返す.
   * @return mainForm.get('orderSkuBottomBillAttention').value
   */
  get fValOrderSkuAttentionBottomBill(): OrderSkuBottomBillAttention[] {
    return this.mainForm.getRawValue()['orderSkuBottomBillAttention'];
  }

  /**
   * mainFormのcategoryCodeの項目の状態を取得する。
   * @return categoryCode
   */
  get formCategoryCode(): any {
    return (<FormGroup> this.mainForm.controls.fkItem).controls.categoryCode;
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

  ngOnInit() {
    this.headerService.show();

    const session = this.sessionService.getSaveSession(); // ログインユーザの情報を取得する
    this.occupationType = session.occupationType;
    // フッター表示条件: ROLE_EDIまたはROLE_MAKER
    this.isShowFooter = AuthUtils.isEdi(session) || AuthUtils.isMaker(session);

    // 過去資材発注参照時にidだけが異なる同一URLでも遷移可能にする為、subscribeで取得する。
    this.route.paramMap.subscribe(paramsMap => {
      this.isInitDataSetted = false;

      this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      const preEvent: number = Number(this.route.snapshot.queryParamMap.get('preEvent'));
      this.queryParamsPartNoId = Number(this.route.snapshot.queryParams['partNoId']);  // 品番Id
      this.queryParamsOrderId = Number(this.route.snapshot.queryParams['orderId']);    // 発注Id

      this.createForm();  // フォーム作成
      this.initializeDisplay(this.path, preEvent, this.queryParamsPartNoId, this.queryParamsOrderId, Number(paramsMap.get('id')));
    });
  }

  /**
   * 初期表示処理.
   * @param path URLパス
   * @param preEvent 遷移前の処理
   * @param partNoId 品番ID
   * @param orderId 発注ID
   * @param fukitaruOrderId フクキタル発注ID
   * @returns Promise
   */
  private async initializeDisplay(path: string, preEvent: number, partNoId: number, orderId: number, fukitaruOrderId: number)
    : Promise<void> {
    this.isInitDataSetted = false;  // フォームを非表示
    // 初期化
    this.isDirty = false;
    this.isBillingOrDeliveryAddressTouched = false;
    this.clearErrorMessage();
    this.mainForm.reset();  // 更新後もthis.mainForm.dirtyがtrueとなってしまうためフォームの値を一度リセットする
    this.createForm();  // フォーム作成

    this.showPreEventMessage(preEvent);
    this.isLossRateInvalid = false;

    this.getFukukitaruOrderList(partNoId, orderId);

    let screenSettingFukukiatru: ScreenSettingFukukiatru = null;
    let fukukitaruOrder: FukukitaruOrder = null;
    switch (path) {
      case Path.NEW:
        screenSettingFukukiatru = await this.getFukukitaruOrderBottomBillMaster(
          partNoId, orderId, FukukitaruMasterDeliveryType.DOMESTIC).toPromise();
        break;
      case Path.EDIT:
      case Path.VIEW:
        fukukitaruOrder = await this.getFukukitaruOrder(fukitaruOrderId).toPromise();
        screenSettingFukukiatru = await this.getFukukitaruOrderBottomBillMaster(
          partNoId, orderId, fukukitaruOrder.deliveryType).toPromise();
        this.orderSkuValue = this.ganarateOrderSku(fukukitaruOrder);
        this.setFukukitaruOrderDataToMemberVariable(fukukitaruOrder);
        break;
      default:
        break;
    }

    this.setDataToMemberVariable(screenSettingFukukiatru);  // メンバ変数にScreenSettingFukukiatru取得データをセット
    this.setDataToForm(screenSettingFukukiatru, fukukitaruOrder, this.orderSkuValue);  // formに取得データをセット
    this.setFukukitaruValidation(screenSettingFukukiatru.item.brandCode);  // フクキタルのバリデーションを設定
    this.convertByColorList(screenSettingFukukiatru);

    const partNo: string = this.mainForm.controls.partNo.value;
    const checkPartNo = partNo.slice(0, 2);
    if ((checkPartNo === FukukitaruBrandCode.ADAM_ET_ROPE_LADIES) || (checkPartNo === FukukitaruBrandCode.ADAM_ET_ROPE_MENS)) {
      this.mainForm.controls.deliveryType.disable();
    }

    this.isInitDataSetted = true;  // フォームを表示
  }

  /**
   * 一次元配列のデータを色ごとに二次元配列に変換する
   */
  private convertByColorList(settingData: ScreenSettingFukukiatru): void {
    // アテンションタグ付記用語
    this.attentionTagAppendicesTermByColorList =
      this.convertAppendicesTermByColorList(settingData.fkItem.listItemAttentionAppendicesTerm);

    // アテンションタグ 付記用語で指定されている色コードリスト取得(00共通含まない)
    this.generateAppendicesTermSelectedColorList();
  }

  /**
   * アテンションタグ付記用語のデータを二次元配列に変換する
   * @param list
   */
  private convertAppendicesTermByColorList(list: FukukitaruItemAttentionAppendicesTerm[]): FukukitaruAppendicesTermByColor[] {
    // 配列がnullの場合、空の配列を返却する
    const attentionTagAppendicesTermList = ListUtils.isEmpty(list) ? [] : list;

    // APIから受け取ったデータが0件の場合は空の配列を返却する
    if (ListUtils.isEmpty(attentionTagAppendicesTermList)) {
      const emptyArray: FukukitaruAppendicesTermByColor[] = [];
      return emptyArray;
    }

    // 色コードのリストから、全色のアテンションタグ 付記用語のリストを取得
    return this.convertSkuListToColorList().map(color => {
      return {
        colorCode: color.colorCode,
        colorName: color.colorName,
        appendicesTermList: attentionTagAppendicesTermList
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
   * アテンションタグ 付記用語で指定されている色コードリスト取得(00共通含まない).
   * ※00共通が指定されている場合は、SKUの全色コードが指定されている
   */
  private generateAppendicesTermSelectedColorList(): void {
    // アテンションタグ 付記用語が選択されている色コードを抽出
    const selectedColors = this.attentionTagAppendicesTermByColorList
      .filter(colorAppendicesTerm => ListUtils.isNotEmpty(colorAppendicesTerm.appendicesTermList))
      .map(colorAppendicesTerm => colorAppendicesTerm.colorCode);

    // 付記用語で00共通が指定されているか
    const isCommonColorSelected = selectedColors.some(color => color === CompositionsCommon.COLOR_CODE);

    if (isCommonColorSelected) {
      // 付記用語で00共通が指定されている場合は、SKUの全色コードが指定されている(00共通含まない)
      this.attentionTagAppendicesTermSelectedColorList = this.attentionTagAppendicesTermByColorList
        .filter(colorAppendicesTerm => colorAppendicesTerm.colorCode !== CompositionsCommon.COLOR_CODE)
        .map(colorAppendicesTerm => colorAppendicesTerm.colorCode);
    } else {
      // 付記用語で00共通が指定されていない場合
      this.attentionTagAppendicesTermSelectedColorList = selectedColors;
    }
  }

  /**
   * 遷移前の処理結果のメッセージを表示する.
   * @param preEvent 遷移前の処理
   */
  private showPreEventMessage(preEvent: number): void {
    switch (preEvent) {
      case PreEventParam.CREATE:
        // 登録後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_ENTRY';
        break;
      case PreEventParam.UPDATE:
        // 更新後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_UPDATE';
        break;
      case PreEventParam.CONFIRM:
        // 確定後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_CONFIRMED';
        break;
      case PreEventParam.APPROVE:
        // 承認後のメッセージを設定
        this.overallSuccessMsgCode = 'SUCSESS.FUKUKITARU_ORDER_APPROVED';
        break;
      default:
        break;
    }
  }

  /**
   * メインのFormGroupを作成する.
   */
  private createForm(): void {
    this.mainForm = this.formBuilder.group({
      id: [null], // ID(フクキタル発注ID)
      fItemId: [null],  // フクキタル品番ID
      partNoId: [null], // 品番ID
      partNo: [null], // 品番
      orderId: [null], // 発注ID
      orderCode: [null],  // オーダー識別コード
      orderAt: [null, [Validators.required]],  // 発注日
      orderUserId: [null], // 発注者ユーザID
      isApprovalRequired: [null],  // 承認需要フラグ
      billingCompanyId: [null, [Validators.required]],  // 請求先ID
      deliveryCompanyId: [null, [Validators.required]], // 納入先ID
      deliveryStaff: [''],  // 部署名・担当者名(納入先担当者)
      urgent: [false],  // 緊急
      preferredShippingAt: [null, [Validators.required]],  // 希望出荷日
      contractNumber: [''],  // 契約No.
      specialReport: [''],  // 特記事項
      deliveryType: [FukukitaruMasterDeliveryType.DOMESTIC],  // 手配先(デリバリ種別)
      repeatNumber: [null, [Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]],  // リピート数
      mdfMakerFactoryCode: [''],  // 工場No
      confirmStatus: [null], // 確定ステータス
      linkingStatus: [null], // 連携ステータス
      orderSendAt: [null],  // 発注送信日
      orderType: [null],  // 発注種別
      isResponsibleOrder: [null],  // 責任発注
      orderSkus: this.formBuilder.array([]), // 発注SKU情報
      orderSkuBottomBill: this.formBuilder.array([]), // 下札
      orderSkuAttentionTag: this.formBuilder.array([]), // アテンションタグ
      orderSkuBottomBillAuxiliaryMaterial: this.formBuilder.array([]),  // 下札同封副資材
      orderSkuBottomBillAttention: this.formBuilder.array([]),  // アテンション下札
      remarks: [''],  // 備考
      lossRate: [null],  // ロス率(表示のみ)

      reflected: [false],  // 反映済
      attentionSaveList: this.formBuilder.array([]), // アテンションタグ保存リスト
      fkItem: this.formBuilder.group({
        categoryCode: [null], // カテゴリコード
        printSustainableMark: [false],  // サスティナブルマーク印字
        listItemAttentionAppendicesTerm: [null] // アテンションタグ付記用語
      })
    });
  }

  /**
   * フクキタル下札発注画面情報取得.
   * @param partNoId 品番ID
   * @param orderId 発注ID
   * @param deliveryType フクキタルデリバリ種別
   * @returns フクキタル下札発注画面情報
   */
  private getFukukitaruOrderBottomBillMaster(partNoId: number, orderId: number, deliveryType: FukukitaruMasterDeliveryType)
    : Observable<ScreenSettingFukukiatru> {

    const conditions = {
      partNoId: partNoId,
      orderId: orderId,
      listMasterType: this.SEARCH_ITEMS,
      deliveryType: deliveryType
    } as ScreenSettingFukukitaruOrderSearchCondition;

    return this.fukukitaruOrder01Service.listFukukitaruOrderBottomBillMaster(conditions).pipe(map(
      data => {
        console.debug('フクキタル下札発注画面情報:', data);
        return data.items[0];
      }, error => this.handleApiError(error)
    ));
  }

  /**
   * フクキタル発注情報取得.
   * @param id フクキタル発注ID
   * @returns フクキタル発注情報
   */
  private getFukukitaruOrder(id: number): Observable<FukukitaruOrder> {
    return this.fukukitaruOrder01Service.getFukukitaruOrderForId(id).pipe(map(
      fukuOrder => {
        console.debug('フクキタル発注情報:', fukuOrder);
        this.fOrderData = fukuOrder;
        return fukuOrder;
      }, error => this.handleApiError(error)
    ));
  }

  /**
   * フクキタル過去資材発注情報取得処理.
   * @param partNoId 品番注ID
   * @param orderId 発注ID
   */
  private getFukukitaruOrderList(partNoId: number, orderId: number): void {
    const conditions = { partNoId: partNoId, orderId: orderId } as FukukitaruOrderSearchCondition;
    this.fukukitaruOrder01Service.listFukukitaruOrders(conditions).subscribe(
      data => {
        console.debug('資材発注リスト:', data);
        this.fOrderPastList = data.items;
      }, error => this.handleApiError(error));
  }

  /**
   * フクキタル発注データのメンバ変数にFukukitaruOrder取得データをセットする.
   * @param fukukitaruOrder FukukitaruOrder
   */
  private setFukukitaruOrderDataToMemberVariable(fukukitaruOrder: FukukitaruOrder): void {
    this.billingDestination = fukukitaruOrder.billingDestination;    // 請求先
    this.deliveryDestination = fukukitaruOrder.deliveryDestination;  // 納入先
    this.confirmStatus = fukukitaruOrder.confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED ? '確定' : '未確定';
    this.registeredOrderSkuBottomBill = fukukitaruOrder.orderSkuBottomBill; // 登録済下げ札類SKU
  }

  /**
   * メンバ変数にScreenSettingFukukiatru取得データをセットする.※フクキタル発注データは除く
   * @param fukuMastaData ScreenSettingFukukiatru
   */
  private setDataToMemberVariable(fukuMastaData: ScreenSettingFukukiatru): void {
    // 初期化
    this.materialOrderDisplayFlg = new MaterialOrderDisplayFlag(false);

    this.itemData = fukuMastaData.item;
    this.seasonValue = BusinessUtils.getSeasonValue(this.itemData.subSeasonCode);
    this.skuList = fukuMastaData.listScreenSku;
    this.generateCompositionsSkuList(this.itemData.compositions);
    this.orderData = fukuMastaData.order;
    this.fkItemData = fukuMastaData.fkItem;

    this.contactOrder = fukuMastaData.listSupplierAddress[0].companyName; // 発注先

    this.attentionTagAppendicesTermList = fukuMastaData.listAttentionTagAppendicesTerm; // アテンションタグ 付記用語
    this.attentionSealTypeList = fukuMastaData.listAttentionSealType; // アテンションシールの種類
    this.recycleMarkList = fukuMastaData.listRecycle; // リサイクルマーク

    this.bottomBillList = fukuMastaData.listBottomBill;  // 下札類
    this.attentionTagList = fukuMastaData.listAttentionTag;  // アテンションタグ
    this.auxiliaryMaterialList = fukuMastaData.listAuxiliaryMaterial;  // 同封副資材

    this.listMaterialFile = fukuMastaData.listMaterialFile; // 資材ファイル情報

    this.cnProductCategoryList = fukuMastaData.listCnProductCategory; // 中国内販情報(製品分類)
    this.cnProductTypeList = fukuMastaData.listCnProductType;         // 中国内販情報(製品種別)

    this.categoryCodeList = fukuMastaData.listCategoryCode;
    this.materialFileList = fukuMastaData.listMaterialFile;  // 資材ファイル情報
    this.inputAssistSetList = fukuMastaData.listInputAssistSet; // 入力補助セットリスト

    // 資材発注(フクキタル)項目表示フラグを設定する
    this.generateMaterialOrderDisplayFlg(fukuMastaData);
  }

  /**
   * 資材発注(フクキタル)項目表示フラグを設定する.
   * @param fukuMastaData ScreenSettingFukukiatru
   */
  private generateMaterialOrderDisplayFlg(fukuMastaData: ScreenSettingFukukiatru): void {
    // カテゴリコードリストがある場合は、カテゴリコードを表示する
    if (ListUtils.isNotEmpty(this.categoryCodeList)) {
      this.materialOrderDisplayFlg.isDisplayCategoryCode = true;
    }

    // サスティナブルマーク印字
    this.materialOrderDisplayFlg.isDisplaySustainableMark = fukuMastaData.sustainableMarkDisplayFlg;

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
   * Formにデータを設定する.
   * @param fukuMastaData フクキタルマスタ情報
   * @param fkOrder フクキタル用発注情報
   * @param orderSkuValue 発注SKU
   */
  private setDataToForm(fukuMastaData: ScreenSettingFukukiatru, fkOrder?: FukukitaruOrder, orderSkuValue?: FukukitaruOrderSku[]): void {
    this.mainForm.setControl('attentionSaveList', this.createAttentionSaveList(fkOrder));
    const billingCompanyInfo = this.materialOrderService.getInitBillingCompany(fukuMastaData.listBillingAddress);

    // フクキタル用発注情報未取得時(新規登録時)
    if (fkOrder == null) {
      this.mainForm.patchValue({
        partNoId: fukuMastaData.item.id,  // 品番ID
        partNo: fukuMastaData.item.partNo, // 品番
        orderId: fukuMastaData.order.id,  // 発注ID
        fItemId: fukuMastaData.fkItem.id, // フクキタル品番ID
        billingCompanyId: billingCompanyInfo != null ? billingCompanyInfo.id : null,  // 請求先ID
        orderType: fukuMastaData.orderType,  // 発注種別
        isResponsibleOrder: null, // 責任発注
        fkItem: {
          categoryCode: fukuMastaData.fkItem.categoryCode, // カテゴリコード
          printSustainableMark: fukuMastaData.fkItem.printSustainableMark,  // サスティナブルマーク印字
          listItemAttentionAppendicesTerm: fukuMastaData.fkItem.listItemAttentionAppendicesTerm // アテンションタグ付記用語
        }
      });
      this.mainForm.setControl('orderSkuBottomBill', this.generateBottomBillFormArray(fukuMastaData.listBottomBill)); // 下札
      this.mainForm.setControl('orderSkuAttentionTag', this.generateAttentionTagFormArray()); // アテンションタグ
      this.mainForm.setControl('orderSkuBottomBillAuxiliaryMaterial',
        this.generateBottomBillAuxiliaryMaterialFormArray(fukuMastaData.listAuxiliaryMaterial)); // 下札同封副資材
      this.mainForm.setControl('orderSkuBottomBillAttention',
        this.generateAttentionBottomBillFormArray(fukuMastaData.listAttentionBottomBill, fukuMastaData.listScreenSku)); // アテンション下札
      this.billingDestination = billingCompanyInfo != null ? billingCompanyInfo : null; // 請求先
      return;
    }

    // フクキタル用発注情報取得時(更新時)
    this.mainForm.patchValue({
      id: fkOrder.id, // ID(フクキタル発注ID)
      fItemId: fkOrder.fItemId,  // フクキタル品番ID
      partNoId: fkOrder.partNoId, // 品番ID
      partNo: fkOrder.partNo, // 品番
      orderId: fkOrder.orderId, // 発注ID
      orderCode: fkOrder.orderCode,  // オーダー識別コード
      orderAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(fkOrder.orderAt)),   // 発注日
      orderUserId: fkOrder.orderUserId, // 発注者ユーザID
      isApprovalRequired: fkOrder.isApprovalRequired,  // 承認需要フラグ
      billingCompanyId: fkOrder.billingCompanyId,  // 請求先ID
      deliveryCompanyId: fkOrder.deliveryCompanyId, // 納入先ID
      deliveryStaff: fkOrder.deliveryStaff,  // 部署名・担当者名(納入先担当者)
      urgent: fkOrder.urgent,  // 緊急
      preferredShippingAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(fkOrder.preferredShippingAt)),  // 希望出荷日
      contractNumber: fkOrder.contractNumber,  // 契約No.
      specialReport: fkOrder.specialReport,  // 特記事項
      deliveryType: fkOrder.deliveryType,  // 手配先(デリバリ種別)
      repeatNumber: fkOrder.repeatNumber, // リピート数
      mdfMakerFactoryCode: fkOrder.mdfMakerFactoryCode,  // 工場No
      confirmStatus: fkOrder.confirmStatus, // 確定ステータス
      linkingStatus: fkOrder.linkingStatus, // 連携ステータス
      orderSendAt: fkOrder.orderSendAt,  // 発注送信日
      orderType: fkOrder.orderType, // 発注種別
      isResponsibleOrder: fkOrder.isResponsibleOrder, // 責任発注
      remarks:  fkOrder.remarks,    // 備考
      fkItem: {
        categoryCode: fukuMastaData.fkItem.categoryCode, // カテゴリコード
        printSustainableMark: fukuMastaData.fkItem.printSustainableMark,  // サスティナブルマーク印字
        listItemAttentionAppendicesTerm: fukuMastaData.fkItem.listItemAttentionAppendicesTerm // アテンションタグ付記用語
      }
    });
    const totalOrderLot = orderSkuValue.reduce((acc, val) => acc + val.orderLot, 0);
    this.mainForm.setControl('orderSkuBottomBill', this.generateBottomBillFormArray(
      fukuMastaData.listBottomBill, fkOrder.orderSkuBottomBill, totalOrderLot)); // 下札
    this.mainForm.setControl('orderSkuAttentionTag', this.generateAttentionTagFormArray(fkOrder.orderSkuAttentionTag)); // アテンションタグ
    this.mainForm.setControl('orderSkuBottomBillAuxiliaryMaterial',
      this.generateBottomBillAuxiliaryMaterialFormArray(
        fukuMastaData.listAuxiliaryMaterial, fkOrder.orderSkuBottomBillAuxiliaryMaterial)); // 下札同封副資材
    this.mainForm.setControl('orderSkuBottomBillAttention',
      this.generateAttentionBottomBillFormArray(
        fukuMastaData.listAttentionBottomBill, fukuMastaData.listScreenSku, fkOrder.orderSkuBottomBillAttention)); // アテンション下札
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
   * アテンション保存リスト作成.
   */
  private createAttentionSaveList(fOrder: FukukitaruOrder): FormArray {
    const saveList = this.formBuilder.array([]);

    this.convertSkuListToColorList().forEach((val) => {
      const list = (fOrder == null) ? [] : fOrder.orderSkuAttentionTag.filter((tag) => val.colorCode === tag.colorCode);

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
   * アテンションネームの値を画面表示用のリストに設定する
   * @param item
   */
  setAttentionValueToList(item: FukukitaruOrderSku): FormGroup {
    return this.formBuilder.group({
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
  }

  /**
   * 組成情報の作成.
   * @param compositionList 品番の組成情報
   */
  private generateCompositionsSkuList(compositionList: Compositions[]): void {
    if (ListUtils.isEmpty(compositionList)) {
      return;
    }

    // 組成情報を基に色のリストを作成する
    this.compositionViewList = [];  // 初期化

    compositionList
      .filter((value1, idx, array) => array.findIndex(value2 => value2.colorCode === value1.colorCode) === idx) // 色コードの重複除去
      .forEach(uniqueComposition => {
        // 共通の場合はラベルをセットする
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

  /**
   * フクキタル発注情報からSKU情報を取得する.
   * 下札の場合、先頭のmaterialIdに該当する情報のみ取得する.
   * @param order フクキタル発注情報
   * @return SKU情報
   */
  private ganarateOrderSku(order: FukukitaruOrder): FukukitaruOrderSku[] {
    // 先頭のmaterialIdに該当する情報だけフィルターをかける
    return order.orderSkuBottomBill.filter((skus, _, self) => self[0].materialId === skus.materialId);
  }

  /**
   * 下札FormArrayを作成して返す.
   * @param bottomBillList 下札リスト(マスタ)
   * @param bottomBillSetData 値を設定する下札リスト
   * @param totalOrderLot 発注SKU入力値合計
   * @returns 下札FormArray
   */
  private generateBottomBillFormArray(bottomBillList: FukukitaruMaster[],
    bottomBillSetData: FukukitaruOrderSku[] = null, totalOrderLot: number = null): FormArray {
    const bottomBillFormArray = new FormArray([]);

    // 下札マスタリストごとに作成
    bottomBillList.forEach(bottomBill => {
      const materialId = bottomBill.id;
      const isRegisted = bottomBillSetData != null && bottomBillSetData.some(setData => {
        if (setData.materialId === materialId) {
          return true;
        }
      });

      const bottomBillForm = this.formBuilder.group({
        checked: [isRegisted], // チェックボックス
        code: [bottomBill.code],  // 資材コード
        name: [bottomBill.codeName],  // 名称
        materialId: [materialId],  // 資材ID
        totalOrderLot: [{ value: isRegisted ? totalOrderLot : null, disabled: true }], // 資材数量合計
        materialCode: [bottomBill.code]
      });
      bottomBillFormArray.push(bottomBillForm);
    });
    return bottomBillFormArray;
  }

  /**
   * アテンションタグFormArrayを作成して返す.
   * @param orderSkuAttentionTagList 値を設定するアテンションタグリスト
   * @returns アテンションタグFormArray
   */
  private generateAttentionTagFormArray(orderSkuAttentionTagList: FukukitaruOrderSku[] = []): FormArray {
    const attentionTagFormArray = new FormArray([]);

    // 3つ作成
    [0, 1, 2].forEach(idx => {
      const orderSkuAttentionTag = orderSkuAttentionTagList.length > idx ? orderSkuAttentionTagList[idx] : null;
      const attentionTagForm = this.formBuilder.group({
        id: [orderSkuAttentionTag != null ? orderSkuAttentionTag.id : null], // フクキタル発注SKUID
        fOrderId: [orderSkuAttentionTag != null ? orderSkuAttentionTag.fOrderId : null], // フクキタル発注ID
        materialId: [orderSkuAttentionTag != null ? orderSkuAttentionTag.materialId : null], // 資材ID
        orderLot: [orderSkuAttentionTag != null ? orderSkuAttentionTag.orderLot : null, [
          Validators.pattern(new RegExp(ValidatorsPattern.NON_NEGATIVE_INTEGER))]] // 資材数量
      }, { validator: Validators.compose([attentionTagValidator]) }  // リスト選択と資材数量の相関チェック
      );
      attentionTagFormArray.push(attentionTagForm);
    });
    return attentionTagFormArray;
  }

  /**
   * 下札同封副資材FormArrayを作成して返す.
   * @param orderSkuBottomBillAuxiliaryMaterialList 同封副資材リスト(マスタ)
   * @param orderSkuBottomBillAuxiliaryMaterial 値を設定する下札同封副資材リスト
   * @returns 下札同封副資材FormArray
   */
  private generateBottomBillAuxiliaryMaterialFormArray(auxiliaryMaterialList: FukukitaruMaster[],
    orderSkuBottomBillAuxiliaryMaterialList: FukukitaruOrderSku[] = []): FormArray {
    const bottomBillAuxiliaryMaterialFormArray = new FormArray([]);

    // 下札同封副資材マスタリストごとに作成
    auxiliaryMaterialList.forEach(auxiliaryMaterial => {
      const materialId = auxiliaryMaterial.id;
      let fkOrderSkuId: number = null;
      let fOrderId: number = null;
      let orderLot: number = null;
      let isRegisted = orderSkuBottomBillAuxiliaryMaterialList.some(osAuxiliaryMaterial => {
        if (osAuxiliaryMaterial.materialId === materialId) {
          fkOrderSkuId = osAuxiliaryMaterial.id;
          fOrderId = osAuxiliaryMaterial.fOrderId;
          orderLot = osAuxiliaryMaterial.orderLot;
          isRegisted = true;
          return true;
        }
      });
      const auxiliaryMaterialForm = this.formBuilder.group({
        id: fkOrderSkuId, // フクキタル発注SKUID
        fOrderId: fOrderId, // フクキタル発注ID
        checked: [isRegisted], // チェックボックス
        code: [auxiliaryMaterial.code], // 資材コード
        name: [auxiliaryMaterial.codeName], // 名称
        materialId: [materialId], // 資材ID
        orderLot: [orderLot, [Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]],  // 資材数量
        moq: [auxiliaryMaterial.moq]  // 出荷単位
      }, { validator: Validators.compose([auxiliaryMaterialValidator]) }  // チェックと資材数量の相関チェック
      );
      bottomBillAuxiliaryMaterialFormArray.push(auxiliaryMaterialForm);
    });
    return bottomBillAuxiliaryMaterialFormArray;
  }

  /**
   * アテンション下札FormArrayを作成して返す.
   * @param orderSkuAttentionTagList 値を設定するアテンション下札リスト
   * @returns アテンション下札FormArray
   */
  private generateAttentionBottomBillFormArray(attentionBottomBillList: FukukitaruMaster[], screenSkuList: ScreenSettingFukukitaruSku[],
    orderSkuAttentionBottomBillList: FukukitaruOrderSku[] = []): FormArray {
    const attentionBottomBillFormArray = new FormArray([]);
    const materialId = attentionBottomBillList[0].id;  // マスタデータのmaterialId

    // 枠を作成
    const enabledSkuList = screenSkuList.filter(val => val.enabled);
    // 抽出した色コード、色名リストから色コードの重複除去
    enabledSkuList.filter(
      (value1, idx, array) => (array.findIndex(value2 => value2.colorCode === value1.colorCode) === idx)
    ).forEach(screenSku => {
      let fkOrderSkuId: number = null;
      let fOrderId: number = null;
      let orderLot: number = null;

      // 一致するカラーコードのデータのみ設定
      orderSkuAttentionBottomBillList.forEach(osAttentionBottomBill => {
        if (osAttentionBottomBill.colorCode === screenSku.colorCode) {
          fkOrderSkuId = osAttentionBottomBill.id;
          fOrderId = osAttentionBottomBill.fOrderId;
          orderLot = osAttentionBottomBill.orderLot;
        }
      });

      const attentionBottomBillForm = this.formBuilder.group({
        colorCode: [screenSku.colorCode],
        id: fkOrderSkuId, // フクキタル発注SKUID
        fOrderId: fOrderId, // フクキタル発注ID
        name: [screenSku.colorName], // 名称
        materialId: materialId, // 資材ID
        orderLot: [orderLot, [Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]]  // 資材数量
      });
      attentionBottomBillFormArray.push(attentionBottomBillForm);
    });

    return attentionBottomBillFormArray;
  }

  /**
   * APIエラーハンドリング
   * @param error エラー情報
   */
  private handleApiError(error: any): void {
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

      if ((apiError.viewErrors[0] != null) && (apiError.viewErrors[0].code === '400_02')) {
        this.isInitDataSetted = false;
        this.overallErrorMsgCode = '';
        ExceptionUtils.displayErrorInfo('fatalErrorMsgArea', apiError.viewErrorMessageCode);
      }
    }
    this.isBtnLock = false;
  }

  /**
   * 請求先を検索するモーダルを表示する。
   * @param fukukitaruListMasterType マスタタイプリスト
   */
  openSearchCompanyModal(fukukitaruListMasterType: FukukitaruMasterType): void {
    const modalRef = this.modalService.open(SearchCompanyModalComponent, { windowClass: 'company' });
    modalRef.componentInstance.partNoId = this.fCtrl.partNoId.value;
    modalRef.componentInstance.orderId = this.fCtrl.orderId.value;
    modalRef.componentInstance.deliveryType = this.fCtrl.deliveryType.value;

    // モーダルへ渡す値を設定する。
    switch (fukukitaruListMasterType) {
      case FukukitaruMasterType.BILLING_ADDRESS: // 請求先会社名
        modalRef.componentInstance.listMasterType = fukukitaruListMasterType;
        if (this.billingDestination != null) {
          modalRef.componentInstance.defaultCompanyId = this.billingDestination.id;
          modalRef.componentInstance.searchCompanyName = this.billingDestination.companyName;
        }
        break;
      case FukukitaruMasterType.DERIVERY_ADDRESS: // 納入先会社名
        modalRef.componentInstance.listMasterType = fukukitaruListMasterType;
        if (this.deliveryDestination != null) {
          modalRef.componentInstance.defaultCompanyId = this.deliveryDestination.id;
          modalRef.componentInstance.searchCompanyName = this.deliveryDestination.companyName;
        }
        break;
      default:
        break;
    }

    // モーダルからの値を設定する。
    modalRef.result.then((result: FukukitaruDestination) => {
      if (result != null) {
        // 請求先・納入先入力欄は非活性であるため、this.mainForm.dirtyでの変更感知不可
        // モーダルより値が設定された場合を変更されたと認識し、変更感知フラグをtrueにする
        this.isBillingOrDeliveryAddressTouched = true;

        switch (fukukitaruListMasterType) {
          case FukukitaruMasterType.BILLING_ADDRESS:
            this.billingDestination = result;
            this.mainForm.patchValue({ billingCompanyId: result.id });
            this.mainForm.patchValue({ isApprovalRequired: result.isApprovalRequired });
            break;
          case FukukitaruMasterType.DERIVERY_ADDRESS:
            this.deliveryDestination = result;
            this.mainForm.patchValue({ deliveryCompanyId: result.id });
            break;
          default:
            break;
        }
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * アテンションタグモーダル表示.
   */
  openAttensionModal(): void {
    const modalRef = this.modalService.open(AttentionModalComponent, { windowClass: 'attention' });
    modalRef.componentInstance.type = FukukitaruMasterType.ATTENTION_TAG;
    modalRef.componentInstance.attentionTagList = this.attentionTagList;
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
          // 指定色コードの下札発注数量合計を取得
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
              // アテンションタグ資材数量が空または0の場合はSKU発注数量合計をセット
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
   * セット選択・登録モーダル表示.
   */
  openInputAssistModal(): void {
    const modalRef = this.modalService.open(FukukitaruInputAssistModalComponent, { windowClass: 'fukukitaru-input-assist' });
    modalRef.componentInstance.listInputAssistSet = this.inputAssistSetList;

    modalRef.result.then((results: FukukitaruInputAssistSetDetails[]) => {
      // 発注SKU合計を取得する
      const totalProductOrderLot = this.sumTotalProductOrderLot();

      const listButtomBill: OrderSkuBottomBillValue[] = this.fValOrderSkuBottomBill;
      const listButtomBillAuxiliary: OrderSkuBottomBillAuxiliaryMaterialValue[] = this.fValOrderSkuBottomBillAuxiliaryMaterial;
      const newListButtomBill = this.formBuilder.array([]);
      const newListButtomBillAuxiliary = this.formBuilder.array([]);

      listButtomBill.forEach((val) => {
        if (results.some((res) => (res.id === val.materialId && res.materialType === FukukitaruMasterMaterialType.BOTTOM_BILL ))) {
          val.checked = true;
        }
        // 下札類のtotalOrderLotは、チェックが付いている場合は必ずSKU合計を設定する
        newListButtomBill.push(this.formBuilder.group({
          checked: [val.checked], // チェックボックス
          name: [val.name],  // 名称
          materialId: [val.materialId],  // 資材ID
          totalOrderLot: [
            val.checked ? { value: totalProductOrderLot, disabled: true } : { value: null, disabled: true }
          ], // 資材数量合計
          materialCode: [val.materialCode]
        }));
      });

      listButtomBillAuxiliary.forEach((val) => {
        if (results.some((res) =>
          (res.id === val.materialId && res.materialType === FukukitaruMasterMaterialType.BOTTOM_BILL_AUXILIARY_MATERIAL))) {
          val.checked = true;
          val.orderLot = totalProductOrderLot; // 資材数量合計
        }

        const auxiliaryMaterialForm = this.formBuilder.group({
          id: val.id, // フクキタル発注SKUID
          fOrderId: val.fOrderId, // フクキタル発注ID
          checked: [val.checked], // チェックボックス
          name: [val.name], // 名称
          materialId: [val.materialId], // 資材ID
          orderLot: [val.orderLot, [Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]],  // 資材数量
          moq: [val.moq]  // 出荷単位
        }, { validator: Validators.compose([auxiliaryMaterialValidator]) }  // チェックと資材数量の相関チェック
        );
        newListButtomBillAuxiliary.push(auxiliaryMaterialForm);
      });

      // 同封副資材の数量は数値切り上げを行う
      newListButtomBillAuxiliary.controls.forEach(val => {
        this.onBlurAuxiliaryMaterial(val as FormGroup);
      });

      this.mainForm.setControl('orderSkuBottomBill', newListButtomBill);
      // 下札類が変更されたことを検知させる
      this.mainForm.controls.orderSkuBottomBill.markAsDirty();
      this.mainForm.setControl('orderSkuBottomBillAuxiliaryMaterial', newListButtomBillAuxiliary);
      // 同封副資材が変更されたことを検知させる
      this.mainForm.controls.orderSkuBottomBillAuxiliaryMaterial.markAsDirty();
    }, () => {});  // バツボタンクリック時は何もしない
  }

  /**
   * デリバリラジオボタン変更時の処理.
   * 入力値を保持したままデータを取得し直す.
   * @param deliveryType デリバリ種別
   */
  async onChangeDeliveryType(deliveryType: FukukitaruMasterDeliveryType): Promise<void> {
    this.loadingService.loadStart();

    /**
     * デリバリラジオボタン変更時の入力値の保持は仕様書に記載が無い。不要とするためコメントアウト。
     * 今後、要望があった際に検討する。
     */
    // 入力値退避
    // const totalOrderLot = this.sumTotalProductOrderLot(); // SKU合計値
    // const bottomBillSetDataList: FukukitaruOrderSku[] = [];
    // this.fValOrderSkuBottomBill
    //   .filter(val => val.checked)  // チェックのついた下札類のみ抽出
    //   .forEach(val => bottomBillSetDataList.push({ materialId: val.materialId } as FukukitaruOrderSku));

    // const attentionTagSetData: FukukitaruOrderSku[] = [];
    // this.fValOrderSkuAttentionTag
    //   .filter(val => val.id != null || val.materialId != null || !FormUtils.isEmpty(val.orderLot))  // 入力されたアテンションタグのみ抽出(idがあれば更新データ)
    //   .forEach(val => attentionTagSetData.push({
    //     id: val.id, fOrderId: val.fOrderId, orderLot: val.orderLot, materialId: val.materialId
    //   } as FukukitaruOrderSku));

    // const auxiliaryMaterialSetDataList: FukukitaruOrderSku[] = [];
    // this.fValOrderSkuBottomBillAuxiliaryMaterial
    //   .filter(val => val.id != null || val.checked || !FormUtils.isEmpty(val.orderLot)) // 入力された下札同封副資材のみ抽出(idがあれば更新データ)
    //   .forEach(val => auxiliaryMaterialSetDataList.push({
    //     id: val.id, fOrderId: val.fOrderId, orderLot: val.orderLot, materialId: val.materialId
    //   } as FukukitaruOrderSku));

    // const attentionBottomBillDataList: FukukitaruOrderSku[] = [];
    // this.fValOrderSkuAttentionBottomBill
    //   .filter(val => val.id != null || !FormUtils.isEmpty(val.orderLot)) // 入力されたアテンション下札のみ抽出(idがあれば更新データ)
    //   .forEach(val => attentionBottomBillDataList.push({
    //     id: val.id, fOrderId: val.fOrderId, colorCode: val.colorCode, orderLot: val.orderLot
    //   } as FukukitaruOrderSku));

    // マスタ再取得
    const screenSettingFukukiatru = await this.getFukukitaruOrderBottomBillMaster(
      this.queryParamsPartNoId, this.queryParamsOrderId, deliveryType).toPromise();

    // メンバ変数にScreenSettingFukukiatru取得データをセット
    this.setDataToMemberVariable(screenSettingFukukiatru);

    /**
     * デリバリラジオボタン変更時の入力値の保持は仕様書に記載が無い。不要とするためコメントアウト。
     * 今後、要望があった際に検討する。
     */
    // FormArray再設定
    // this.mainForm.setControl('orderSkuBottomBill',
    //   this.generateBottomBillFormArray(screenSettingFukukiatru.listBottomBill, bottomBillSetDataList, totalOrderLot)); // 下札
    // this.mainForm.setControl('orderSkuAttentionTag', this.generateAttentionTagFormArray(attentionTagSetData)); // アテンションタグ
    // this.mainForm.setControl('orderSkuBottomBillAuxiliaryMaterial',
    //   this.generateBottomBillAuxiliaryMaterialFormArray(
    //     screenSettingFukukiatru.listAuxiliaryMaterial, auxiliaryMaterialSetDataList)); // 下札同封副資材
    // this.mainForm.setControl('orderSkuBottomBillAttention',
    //   this.generateAttentionBottomBillFormArray(screenSettingFukukiatru.listAttentionBottomBill,
    //     screenSettingFukukiatru.listScreenSku, attentionBottomBillDataList)); // アテンション下札

    // 取得したマスタデータを再設定
    // 下札
    const emptyFkOrderSku: FukukitaruOrderSku[] = [];
    this.mainForm.setControl('orderSkuBottomBill',
      this.generateBottomBillFormArray(screenSettingFukukiatru.listBottomBill, emptyFkOrderSku, null));
    // 下札同封副資材
    this.mainForm.setControl('orderSkuBottomBillAuxiliaryMaterial',
      this.generateBottomBillAuxiliaryMaterialFormArray(
        screenSettingFukukiatru.listAuxiliaryMaterial, emptyFkOrderSku));
    // アテンション下札
    this.mainForm.setControl('orderSkuBottomBillAttention',
      this.generateAttentionBottomBillFormArray(screenSettingFukukiatru.listAttentionBottomBill,
        screenSettingFukukiatru.listScreenSku, emptyFkOrderSku));

    this.loadingService.loadEnd();
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
   * 「請求先と同じ」チェックボックス押下時.
   * @param checked チェック状態
   */
  onSameBillingCompanyId(checked: boolean): void {
    if (checked) {
      if (this.mainForm.controls.billingCompanyId.value !== this.mainForm.controls.deliveryCompanyId.value) {
        // 請求先会社IDと納入先会社IDが異なる場合のみ、設定する

        // 請求先・納入先入力欄は非活性であるため、this.mainForm.dirtyでの変更感知不可
        // 請求先と同じチェックで値が設定された場合も変更されたと認識し、変更感知フラグをtrueにする
        this.isBillingOrDeliveryAddressTouched = true;

        this.deliveryDestination = this.billingDestination;
        this.mainForm.patchValue({ deliveryCompanyId: this.fCtrl.billingCompanyId.value });
      }
    }
  }

  /**
   * 発注SKU入力値変更時の処理.
   * 発注SKU入力値を合計し直して下札類テキストボックスにセット.
   */
  onChangeProductOrderLot(): void {
    let totalOrderLot = this.sumTotalProductOrderLot();
    totalOrderLot = (totalOrderLot === 0 ? null : totalOrderLot);

    // チェックがついている下札にセット
    this.fCtrlOrderSkuBottomBill
      .filter(bottomBill => bottomBill.get('checked').value === true)
      .forEach(bottomBill => bottomBill.patchValue({ totalOrderLot: totalOrderLot }));

    // 発注SKUを色別に合計する
    const colorOrderFormArray = this.sumColorProductOrderLot();

    // アテンション下札にセット
    // ※色コードがアテンションタグ 付記用語で指定されている場合のみセット
    this.fCtrlOrderSkuAttentionBottomBill
      .filter(attentionBottomBill =>
        this.attentionTagAppendicesTermSelectedColorList.some(color => color === attentionBottomBill.value.colorCode))
      .forEach(attentionBottomBill => {
        const colorOrderForm = colorOrderFormArray.controls.find(colorOrder =>
          colorOrder.value.colorCode === attentionBottomBill.value.colorCode);
        if (colorOrderForm != null) {
          const colorTotalOrderLot = (colorOrderForm.value.orderLot === 0 ? null : colorOrderForm.value.orderLot);
          attentionBottomBill.patchValue({ orderLot: colorTotalOrderLot });
        }
      });
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
   * @param lossRate ロス率入力値
   */
  onReflectLoss(lossRate: number): void {
    if (!NumberUtils.isNumber(lossRate) || lossRate < 1) {
      this.isLossRateInvalid = true;
      return;
    }
    this.isLossRateInvalid = false;

    this.fCtrlOrderSkus.forEach(orderSku => {
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
    this.onChangeProductOrderLot(); // 下札類にも反映
  }

  /**
   * 下札チェックボックス押下時の処理.
   * 発注SKU入力値を合計してテキストボックスにセット.
   * @param rowIdx index
   * @param checked チェック状態
   */
  onCheckBottomBill(rowIdx: number, checked: boolean): void {
    if (!checked) {
      this.fCtrlOrderSkuBottomBill[rowIdx].patchValue({ totalOrderLot: null });
      return;
    }

    const totalOrderLot = this.sumTotalProductOrderLot();
    this.fCtrlOrderSkuBottomBill[rowIdx].patchValue({ totalOrderLot: (totalOrderLot === 0 ? null : totalOrderLot) });
  }

  /**
   * 発注SKUを合計する.
   * @returns 発注SKU合計
   */
  private sumTotalProductOrderLot(): number {
    let totalOrderLot = 0;
    this.fCtrlOrderSkus.forEach(orderSku => {
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
   * 発注SKUを色別に合計する
   */
  private sumColorProductOrderLot(): FormArray {
    const colorOrderFormArray = new FormArray([]);

    this.fCtrlOrderSkus.forEach(orderSku => {
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
   * 同封副資材の数量入力後の処置.
   * 単位で割り切れない場合は単位分を加算する.
   * @param auxiliaryMaterial 入力行の同封副資材情報
   */
  onBlurAuxiliaryMaterial(auxiliaryMaterial: FormGroup): void {
    const orderLot = auxiliaryMaterial.value.orderLot;
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
   * ファイルダウンロードリンク押下処理.
   * @param fukukitaruMasterType 資材種別
   */
  onFileDownLoad(fukukitaruMasterType: FukukitaruMasterType): void {
    this.overallErrorMsgCode = '';
    this.listMaterialFile.some(materialFile => {
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
   * 押された送信ボタンの種類を保持
   *
   * @param type 押された送信ボタンの種類
   */
  setSubmitType(type: string) {
    this.submitType = type;
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
   * Submit時の処理(登録・更新・承認・削除).
   */
  onSubmit(): void {
    // 初期化
    this.clearErrorMessage();
    this.submitted = true;

    // 確定・承認時の変更感知
    // 入力フォームに変更があるか判断する。変更がある場合確定・承認ボタンをロックし、確定・承認前に更新するメッセージを表示
    if (this.submitType === SubmitType.APPROVE || this.submitType === SubmitType.CONFIRM) {
      if (this.mainForm.dirty || this.isBillingOrDeliveryAddressTouched) {
        this.disableApproveOrConfirm(this.submitType);
        return;
      }
    }

    // バリデーションチェック
    if (this.mainForm.invalid) {
      console.debug('バリデーションエラー:', this.mainForm);
      this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    // Submitハンドリング
    if (this.submitType === SubmitType.CONFIRM) {
      // 確定の場合、確認モーダル表示
      this.openConfirmModal();
    } else {
      this.submitHandler();
    }
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
   * 確定モーダルを表示する.
   */
  private openConfirmModal(): void {
    const modalRef = this.modalService.open(MaterialOrderSubmitConfirmModalComponent);
    modalRef.componentInstance.isQualityApprovalOk = BusinessCheckUtils.isQualityApprovalOk(this.itemData);
    modalRef.result.then((isOrderResponsible: boolean) => {
      this.mainForm.patchValue({ isResponsibleOrder : isOrderResponsible});
      this.submitHandler();
    }, () => {});  // バツボタンクリック時は何もしない
  }

  /**
   * FormのSubmitイベントをハンドリングする.
   * 押されたボタンのnameによって、実行するServiceの切り分けを行う.
   */
  private submitHandler() {
    this.isBtnLock = true;
    this.loadingService.loadStart();

    const formValue = this.mainForm.getRawValue();
    formValue.orderSkuBottomBill = this.prepareBottomBillListPostData();
    formValue.orderSkuAttentionTag = this.prepareAttentionTagListPostData();
    formValue.orderSkuBottomBillAuxiliaryMaterial = this.prepareBottomBillAuxiliaryMaterialListPostData();
    formValue.orderSkuBottomBillAttention = this.prepareAttentionBottomBillPostData();
    formValue.orderSkuAttentionTag = this.reduceDimension(formValue.attentionSaveList);
    console.debug('送信用のデータ作成後', formValue);

    switch (this.submitType) {
      case SubmitType.ENTRY:    // 登録
        this.postProcessSubmit(this.fukukitaruOrder01Service.postFukukitaruOrder(formValue), PreEventParam.CREATE);
        break;
      case SubmitType.UPDATE:   // 更新
        this.postProcessSubmit(this.fukukitaruOrder01Service.putFukukitaruOrder(formValue), PreEventParam.UPDATE);
        break;
      case SubmitType.CONFIRM:  // 確定
        this.postProcessSubmit(this.fukukitaruOrder01Service.confirmFukukitaruOrder(formValue), PreEventParam.CONFIRM);
        break;
      case SubmitType.APPROVE:  // 承認
        this.postProcessSubmit(this.fukukitaruOrder01Service.approveFukukitaruOrder(formValue), PreEventParam.APPROVE);
        break;
    }
  }

  /**
   * 承認・確定機能を不可にする
   *
   * ・承認・確定ボタンのロック
   * ・エラーメッセージの設定
   * @param submitType SUBMIT種類
   */
  private disableApproveOrConfirm(submitType: string) {
    this.isDirty = true;
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
   * post用に下札SKUリストを作成する.
   * @returns 下札SKUリスト
   */
  private prepareBottomBillListPostData(): FukukitaruOrderSku[] {
    const bottomBillList: FukukitaruOrderSku[] = [];

    // 数量入力がある発注SKU取得
    let inputtedOrderSkuList: OrderSkuValue[] = [];
    this.fCtrlOrderSkus.forEach(orderSku => {
      const sizeListFormArrayValue: OrderSkuValue[] = orderSku.get('sizeList').value;
      const inputtedOrderSku = sizeListFormArrayValue.filter(sizeInfoVal => StringUtils.isNotEmpty(sizeInfoVal.productOrderLot));
      inputtedOrderSkuList = inputtedOrderSkuList.concat(inputtedOrderSku);
    });

    // チェックがついている下札を数量入力されている発注SKUごとに設定する
    this.fValOrderSkuBottomBill
      .filter(bottomBill => bottomBill.checked)
      .forEach(bottomBill => {
        const materialId = bottomBill.materialId;
        inputtedOrderSkuList.forEach(inputtedOrderSku => {
          const colorCode = inputtedOrderSku.colorCode;
          const size = inputtedOrderSku.size;
          // 更新前のデータがあればフクキタル発注SKUのIDをセット
          let fkOrderSkuId: number = null;
          this.registeredOrderSkuBottomBill.some(registeredOrderSkuBottom => {
            if (registeredOrderSkuBottom.materialId === materialId
              && registeredOrderSkuBottom.colorCode === colorCode
              && registeredOrderSkuBottom.size === size) {
              fkOrderSkuId = registeredOrderSkuBottom.id;
              return;
            }
            return false;
          });
          bottomBillList.push({
            id: fkOrderSkuId, // フクキタル発注SKU情報ID
            fOrderId: this.mainForm.getRawValue()['id'], // フクキタル発注ID
            colorCode: colorCode, // カラーコード
            size: size, // サイズ
            materialId: materialId, // 資材ID
            orderLot: Number(inputtedOrderSku.productOrderLot), // 資材数量
          } as FukukitaruOrderSku);
        });
      });
    return bottomBillList;
  }

  /**
   * post用にアテンションタグSKUリストを作成する.
   * @returns アテンションタグSKUリスト
   */
  private prepareAttentionTagListPostData(): OrderSkuAttentionTagValue[] {
    // 数量が入力されているアテンションタグを抽出
    return this.fValOrderSkuAttentionTag.filter(val => !FormUtils.isEmpty(val.orderLot));
  }

  /**
   * post用に下札同封副資材SKUリストを作成する.
   * @returns 下札同封副資材SKUリスト
   */
  private prepareBottomBillAuxiliaryMaterialListPostData(): OrderSkuBottomBillAuxiliaryMaterialValue[] {
    // 数量が入力されている下札同封副資材を抽出
    return this.fValOrderSkuBottomBillAuxiliaryMaterial.filter(val => !FormUtils.isEmpty(val.orderLot));
  }

  /**
   * post用にアテンション下札SKUリストを作成する.
   * @returns アテンション下札SKUリスト
   */
  private prepareAttentionBottomBillPostData(): OrderSkuBottomBillAttention[] {
    // 数量が入力されているアテンション下札を抽出
    return this.fValOrderSkuAttentionBottomBill.filter(val => !FormUtils.isEmpty(val.orderLot));
  }

  /**
   * Submit後の処理.
   * @param observable Observable<FukukitaruOrder>
   * @param event イベント
   */
  private postProcessSubmit(observable: Observable<FukukitaruOrder>, event: number) {
    observable.subscribe(
      result => {
        this.loadingService.loadEnd();
        this.isBtnLock = false;

        // 登録・更新・確定・承認後の編集画面表示
        this.router.navigate([this.URL_HANG_TAG, result.id, Path.EDIT],
          { queryParams: { preEvent: event, 'orderId': result.orderId, 'partNoId': result.partNoId } });
        // 同じパスで遷移になるため、フォーム再描画を実施
        this.initializeDisplay(this.path, event, result.partNoId, result.orderId, result.id);
      },
      error => {
        this.loadingService.loadEnd();
        this.handleApiError(error);
      }
    );
  }

  /**
   * サブメニューリンク押下時の処理。
   * 指定した要素へページ内リンクする。
   * @param id リンク先のid
   */
  onScrollEvent(id: string): boolean {
    const pageY = window.pageYOffset
      + document.getElementById(id).getBoundingClientRect().top
      - document.getElementById('header').getBoundingClientRect().height;
    window.scrollTo(0, pageY);
    return false;
  }

  /**
   * 新規登録ボタン押下時の処理.
   * @param url url
   */
  onRouterLinkNew(url: string): void {
    this.router.navigate([url, Path.NEW],
      { queryParams: { 'orderId': this.queryParamsOrderId, 'partNoId': this.queryParamsPartNoId } });
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
