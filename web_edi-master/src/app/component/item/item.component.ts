import { Component, OnInit, OnDestroy, ElementRef } from '@angular/core';
import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup, FormBuilder, Validators, FormArray, ValidatorFn, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbDateParserFormatter, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';

import { Observable, forkJoin, Subscription, from, of } from 'rxjs';
import { map, tap, filter, finalize, flatMap, catchError, delay, findIndex } from 'rxjs/operators';

import {
  AuthType, FileMode, FileCategory, RegistStatus, SearchTextType, LinkingStatus,
  ViewMode, SupplierType, QualityApprovalStatus, StaffType, ChangeRegistStatusType, APIErrorCode,
  CompositionsCommon, CompositionsRequiredType, PreEventParam, SubmitType, ItemValidationType,
  Path, OrderApprovalStatus, VoiType, FukukitaruMasterType, Const, FukukitaruColorCommon, FukukitaruBrandCode,
  ValidatorsPattern, JanType, ResourceType, CategoryCodeType} from '../../const/const';
import { CodeMaster } from '../../const/code-master';

import { StringUtils } from '../../util/string-utils';
import { CalculationUtils } from '../../util/calculation-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { FileUtils } from '../../util/file-utils';
import { DateUtils } from '../../util/date-utils';
import { ListUtils } from '../../util/list-utils';
import { BusinessCheckUtils } from '../../util/business-check-utils';
import { BusinessUtils } from '../../util/business-utils';
import { FormUtils } from '../../util/form-utils';
import { NumberUtils } from '../../util/number-utils';
import { ObjectUtils } from '../../util/object-utils';

import { CodeService } from '../../service/code.service';
import { ItemService } from '../../service/item.service';
import { FileService } from '../../service/file.service';
import { HeaderService } from '../../service/header.service';
import { JunpcCodmstService } from '../../service/junpc-codmst.service';
import { JunpcSirmstService } from '../../service/junpc-sirmst.service';
import { JunpcSizmstService } from '../../service/junpc-sizmst.service';
import { SessionService } from '../../service/session.service';
import { JunpcKojmstService } from '../../service/junpc-kojmst.service';
import { LoadingService } from '../../service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { ConvertService } from '../../service/convert.service';
import { ScreenSettingService } from '../../service/screen-setting.service';
import { ItemDataService } from '../../service/shared/item-data.service';

import { MessageConfirmModalComponent } from '../message-confirm-modal/message-confirm-modal.component';
import { SearchKojmstModalComponent } from '../search-kojmst-modal/search-kojmst-modal.component';
import { SearchStaffModalComponent } from '../search-staff-modal/search-staff-modal.component';
import { AppendicesTermModalComponent } from '../appendices-term-modal/appendices-term-modal.component';

// PRD_0023 && No_65 mod JFE start
import {
  deploymentWeekLessOrEqualsPendWeekValidator, mdfMakerExistenceValidator,
  mdfMakerFactoryExistenceValidator, plannerExistenceValidator, mdfStaffExistenceValidator, patanerExistenceValidator,
  deploymentWeekNumberCorrectValidator, pendWeekNumberCorrectValidator, NoWhitespaceValidator,
  CategoryCodeRequiredValidator,
  totalCostValidator
} from './validator/item-validator.directive';
// PRD_0023 && No_65 mod JFE end
import { washPatternValidator, tapeCodeAndTapeWidthValidator } from '../../validator/material-order/common-validator.directive';
import { SearchSupplierModalComponent } from '../search-supplier-modal/search-supplier-modal.component';

import { ItemFileInfo } from '../../model/item-file-info';
import { ItemFileInfoRequest } from '../../model/item-file-info-request';
import { MisleadingRepresentationFile } from '../../model/misleading-representation-file';
import { MisleadingRepresentationFileRequest } from '../../model/misleading-representation-file-request';
import { JunpcCodmst } from '../../model/junpc-codmst';
import { JunpcCodmstSearchCondition } from '../../model/junpc-codmst-search-condition';
import { JunpcKojmst } from '../../model/junpc-kojmst';
import { JunpcKojmstSearchCondition } from '../../model/junpc-kojmst-search-condition';
import { JunpcSirmst } from '../../model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from '../../model/junpc-sirmst-search-condition';
import { JunpcSizmst } from '../../model/junpc-sizmst';
import { JunpcSizmstSearchCondition } from '../../model/junpc-sizmst-search-condition';
import { Item } from '../../model/item';
import { Sku } from '../../model/sku';
import { Compositions } from '../../model/compositions';
import { Order } from '../../model/order';
import { ItemPart } from '../../model/item-part';
import { ScreenSettingFukukitaruOrderSearchCondition } from '../../model/screen-setting-fukukitaru-order-search-condition';
import { FukukitaruMaster } from '../../model/fukukitaru-master';
import { ScreenSettingFukukiatru } from '../../model/screen-setting-fukukitaru';
import { MaterialOrderDisplayFlag } from '../../model/material-order-display-flag';
import { FukukitaruItemWashAppendicesTerm } from '../../model/fukukitaru-item-wash-appendices-term';
import { FukukitaruItemAttentionAppendicesTerm } from '../../model/fukukitaru-item-attention-appendices-term';
import { FukukitaruMasterAppendicesTerm } from '../../model/fukukitaru-master-appendices-term';
import { FukukitaruItemWashPattern } from '../../model/fukukitaru-item-wash-pattern';
import { ErrorDetail } from 'src/app/model/error-detail';

import { FukukitaruAppendicesTermByColor } from '../../interface/fukukitaru-appendices-term-by-color';
import { FukukitaruAppendicesTerm } from '../../interface/fukukitaru-appendices-term';
import { FukukitaruWashPatternInterface } from '../../interface/fukukitaru-wash-pattern-interface';
import { AuthUtils } from 'src/app/util/auth-utils';
import { Session } from 'src/app/model/session';
import { OrderSupplier } from 'src/app/model/order-supplier';
import { MaxLength } from 'src/app/const/max-length';
import { ItemService as ItemBoService } from 'src/app/service/bo/item.service';
import { PartNoKind } from 'src/app/interface/part-no-kind';
import { CompotisionFormValue } from 'src/app/interface/composition-form-value';
import { t } from '@angular/core/src/render3';

@Component({
  selector: 'app-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss'],
  providers: [ ItemDataService ]
})
export class ItemComponent implements OnInit, OnDestroy {

  // htmlから参照したい定数を定義
  readonly AUTH_SUPPLIERS: AuthType = AuthType.AUTH_SUPPLIERS;
  readonly AUTH_INTERNAL: AuthType = AuthType.AUTH_INTERNAL;
  readonly DELETED_FILE = FileMode.DELETED_FILE;
  readonly REGIST_STATUS = RegistStatus;
  readonly VIEW_MODE = ViewMode;
  readonly SUPPLIER_TYPE = SupplierType;
  readonly STAFF_TYPE = StaffType;
  readonly LINKING_STATUS = LinkingStatus;
  readonly COMPOSITIONS_REQUIRED_TYPE = CompositionsRequiredType;
  readonly QUALITY_APPROVAL_STATUS = QualityApprovalStatus;
  readonly SUBMIT_TYPE = SubmitType;
  readonly PATH = Path;
  readonly ORDER_APPROVAL_STATUS = OrderApprovalStatus;
  readonly VOI_TYPE = VoiType;
  readonly FUKUKITARU_MASTER_TYPE = FukukitaruMasterType;
  readonly SUB_SEASON_LIST = CodeMaster.subSeason;
  readonly FUKUKITARU_BRAND_CODE = FukukitaruBrandCode;
  readonly JAN_TYPE = JanType;
  readonly RESOURCE_TYPE = ResourceType;

  // フクキタル検索マスタータイプ
  readonly LIST_MASTER_TYPE: FukukitaruMasterType[] = [
    FukukitaruMasterType.TAPE_TYPE, // テープ種類
    FukukitaruMasterType.TAPE_WIDE, // テープ巾
    FukukitaruMasterType.WASH_PATTERN,  // 絵表示
    FukukitaruMasterType.WASH_NAME_APPENDICES_TERM,  // 洗濯ネーム　付記用語
    FukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM, // アテンションタグ　付記用語
    FukukitaruMasterType.ATTENTION_SEAL_TYPE, // アテンションシールのシール種類
    FukukitaruMasterType.RECYCLE, // リサイクルマーク
    FukukitaruMasterType.CN_PRODUCT_CATEGORY, // 製品分類
    FukukitaruMasterType.CN_PRODUCT_TYPE,  // 製品種別
    FukukitaruMasterType.CATEGORY_CODE,     // カテゴリコード
    FukukitaruMasterType.MATERIAL_FILE_INFO, // 資材ファイル情報
    FukukitaruMasterType.SUSTAINABLE_MARK // サスティナブルマーク
  ];
  fukukitaruMaster: ScreenSettingFukukiatru;
  tapeTypeList: FukukitaruMaster[];
  tapeWideList: FukukitaruMaster[];
  washPatternList: FukukitaruMaster[];
  washNameAppendicesTermList: FukukitaruMasterAppendicesTerm[];
  categoryCodeList: FukukitaruMaster[];
  attentionTagAppendicesTermList: FukukitaruMasterAppendicesTerm[];
  attentionSealTypeList: FukukitaruMaster[];
  recycleList: FukukitaruMaster[];
  cnProductCategoryList: FukukitaruMaster[];
  cnProductTypeList: FukukitaruMaster[];
  materialOrderDisplayFlg = new MaterialOrderDisplayFlag(false); // 資材発注(フクキタル)項目表示フラグ

  isInitDataSetted = false; // 画面用データ取得完了フラグ
  isShowFooter = false; // フッター表示フラグ

  private _el: HTMLElement; // domアクセス用の変数
  affiliation: AuthType;    // ログインユーザーの権限
  company = '';             // ログインユーザーの会社名
  private session: Session;         // ユーザの編集権限

  private matlMakerName = '';             // 生地メーカー名
  orderDataList: Order[] = [];           // 発注情報リスト
  currentSupplierIndex = 0;               // 発注先メーカー情報表示インデックス

  viewMode = ViewMode.ITEM_NEW;     // 画面表示モード
  registStatus = RegistStatus.ITEM; // 登録ステータス
  submitted = false;                // submit押下フラグ
  isShowImage = false;              // 画像表示中フラグ
  isBtnLock = false;                // 登録/更新/削除処理中にボタンをロックするためのフラグ
  isCopy = false;                   // コピー新規作成中か
  isAnchorStatisticsCollapsed = false;  // 統計情報表示フラグ
  isArticleNumberCollapsed = true;      // JAN/UPC折り畳みフラグ(true：折り畳む、false：折り畳まない)
  isWashNameTitleCollapsed = false;     // 洗濯ネーム表示フラグ
  isBottomBillTitleCollapsed = false;   // 下札表示フラグ
  isCnTitleCollapsed = false;           // 洗濯ネーム・下札　中国内販情報表示フラグ
  isCollapsed = false;                  // 登録済発注情報表示フラグ
  isMakerCollapsed = false;             // メーカー追加モーダル表示フラグ
  isMdfStaffDisabled = false;           // 製造担当非活性状態
  isMakerCodeModalDisabled = false;     // メーカーコードモーダルボタン非活性フラグ
  isMakerFactoryModalDisabled = false;  // メーカー工場モーダルボタン非活性フラグ

  mainForm: FormGroup;

  // 子Componentの変更感知用の品種
  // 品種変更確定時に値を設定し、その時だけ子Componentのイベントを駆動する
  partNoKindConfirm: string;

  itemData: Item;                             // 品番情報のデータ
  cooMasterList: JunpcCodmst[] = [];          // 原産国マスタリスト
  maruiMasterList: JunpcCodmst[] = [];        // 丸井品番マスタリスト
  voiMasterList: JunpcCodmst[] = [];          // Voi区分マスタリスト
  materialMasterList: JunpcCodmst[] = [];     // 素材マスタリスト
  zoneMasterList: JunpcCodmst[] = [];         // ゾーンマスタリスト
  subBlandMaster: JunpcCodmst[] = [];         // サブブランドマスタリスト
  tasteMasterList: JunpcCodmst[] = [];        // テイストマスタリスト
  sizeMasterList: JunpcSizmst[] = [];         // サイズマスタのデータリスト
  type1List: JunpcCodmst[] = [];              // タイプ1リスト
  type2List: JunpcCodmst[] = [];              // タイプ2リスト
  type3List: JunpcCodmst[] = [];              // タイプ3リスト
  outletMasterList: JunpcCodmst[] = [];       // 展開マスタリスト
  compositionsMasterList: JunpcCodmst[] = []; // 組成(混率)マスタリスト
  partsMaster: ItemPart[] = [];               // パーツマスタリスト
  skusValue: Sku[] = [];                      // SKU(色・サイズ)情報リスト
  private itemFileInfoList: ItemFileInfoRequest[] = []; // 品番添付ファイルの情報リスト
  private viewFileId: number;                 // 表示している画像のId
  estimatesFile: ItemFileInfoRequest[] = [];  // 見積添付ファイルの情報リスト
  linkingStatus = '';                         // 連携ステータス
  misleadingRepresentationFile: MisleadingRepresentationFileRequest[] = [];  // 生地検査結果ファイルの情報リスト

  path = '';

  qualityCompositionStatus: QualityApprovalStatus = null; // 優良誤認承認ステータス(組成)
  qualityCooStatus: QualityApprovalStatus = null;         // 優良誤認承認ステータス(国)
  qualityHarmfulStatus: QualityApprovalStatus = null;     // 優良誤認承認ステータス(有害物質)

  overallErrorMsgCode = '';   // エラーメッセージコード
  overallSuccessMsgCode = ''; // 成功メッセージコード
  estimatesErrorMsgCode = ''; // 見積ファイル上限エラーコード
  tanzakuErrorMsgCode = '';   // タンザク画像ファイル上限エラーコード

  apiValidateErrorsMap: Map<string, ErrorDetail[]> = new Map();   // APIのバリデーションエラーMap

  private attachmentFile: File = null;              // 添付ファイル
  private activefilecount = 0;                      // 添付中ファイル数
  private readonly MAX_FILES = 5;                   // 添付できるファイル数の上限
  private readonly MAX_FILE_SIZE = 10000000;        // 添付できるファイルサイズの上限
  private readonly ZONE_DEFAULT_VALUE = '90';       // ゾーンマスタのデフォルト値
  private readonly MARUI_DEFAULT_VALUE = '000000';  // 丸井品番のデフォルト値
  private readonly OUTLET_DEFAULT_VALUE = '00';     // 展開マスタのデフォルト値
  private readonly COMPOSITIONS_MAX_COUNT = 10;     // 画面に表示する１枠の組成(混率)情報の最大数

  private submitType = '';  // 押された送信ボタンの種類

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

  /** フクキタルのバリデーション */
  private fkValidators = [washPatternValidator, tapeCodeAndTapeWidthValidator]; // 共通バリデーションを追加する場合はここに追加する

  constructor(
    private headerService: HeaderService,
    private formBuilder: FormBuilder,
    private itemService: ItemService,
    private convertService: ConvertService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    private codeService: CodeService,
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private modalService: NgbModal,
    private sessionService: SessionService,
    private fileService: FileService,
    private junpcCodmstService: JunpcCodmstService,
    private junpcSirmstService: JunpcSirmstService,
    private junpcSizmstService: JunpcSizmstService,
    private junpcKojmstService: JunpcKojmstService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private screenSettingService: ScreenSettingService,
    private translate: TranslateService,
    private itemDataService: ItemDataService,
    private itemBo: ItemBoService,
    el: ElementRef
  ) {
    this._el = el.nativeElement;
  }

  /**
   * mainFormの項目の状態を取得する。
   * @return mainForm.controls
   */
  get f(): any {
    return this.mainForm.controls;
  }

  /**
   * mainFormのcompositionsのを取得する。
   * @return this.mainForm.get('compositions') as FormArray
   */
  get compositionsFormArray(): FormArray {
    return this.mainForm.get('compositions') as FormArray;
  }

  /**
   * mainFormのcompositionsの項目の状態を取得する。
   * @return this.mainForm.get('compositions').controls
   */
  get fCtrlCompositions(): AbstractControl[] {
    return this.compositionsFormArray.controls;
  }

  /**
   * mainFormのcompositionsのvalueを返す.
   * @return mainForm.get('compositions').value
   */
  get fValCompositions(): CompotisionFormValue[] {
    return this.compositionsFormArray.getRawValue();
  }

  /**
   * mainFormのorderSuppliersを取得する
   * @returns this.mainForm.get('orderSuppliers') as FormArray
   */
  get suppliersFormArray(): FormArray {
    return this.mainForm.get('orderSuppliers') as FormArray;
  }

  /**
   * mainFormのorderSuppliersを取得する
   * @returns (this.mainForm.get('orderSuppliers') as FormArray).controls
   */
  get fCtrlSuppliers(): AbstractControl[] {
    return this.suppliersFormArray.controls;
  }

  /**
   * mainFormのskusの項目の状態を取得する。
   * @return (<FormArray> this.mainForm.get('skus')).controls
   */
  get fCtrlSkus(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('skus')).controls;
  }

  /**
   * mainFormのcompositionsの項目の状態を取得する。
   * @return mainForm.get('compositions')['controls']
   */
  get formCompositions(): any {
    return this.mainForm.get('compositions')['controls'];
  }

  /**
   * mainFormのorderSuppliersの項目の状態を取得する。
   * @return mainForm.get('compositions')['controls']
   */
  get formOrderSuppliers(): any {
    return this.mainForm.get('orderSuppliers')['controls'];
  }

  /**
   * @return this.mainForm.get('fkItem').get('washPatterns') as FormArray
   */
  get formWashPatterns(): FormArray {
    return this.mainForm.get('fkItem').get('washPatterns') as FormArray;
  }

  /**
   * @return this.formWashPatterns.controls
   */
  get fCtrlWashPatterns(): AbstractControl[] {
    return this.formWashPatterns.controls;
  }

  /**
   * @return washAppendicesTermByColorList
   */
  get formWashAppendicesTermByColorList(): FukukitaruAppendicesTermByColor[] {
    return (<FukukitaruAppendicesTermByColor[]>(<FormGroup> this.mainForm.controls.fkItem).controls.washAppendicesTermByColorList.value);
  }

  /**
   * @return attentionTagAppendicesTermByColorList
   */
  get formAttentionTagAppendicesTermByColorList(): FukukitaruAppendicesTermByColor[] {
    return (<FukukitaruAppendicesTermByColor[]>(<FormGroup> this.mainForm.controls.fkItem)
      .controls.attentionTagAppendicesTermByColorList.value);
  }

  /**
   * mainFormのcategoryCodeの項目の状態を取得する。
   * @return categoryCode
   */
  get formCategoryCode(): any {
    return (<FormGroup> this.mainForm.controls.fkItem).controls.categoryCode;
  }

  ngOnInit() {
    this.loadingService.clear();
    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.isBtnLock = isLoading);
    this.isInitDataSetted = false;

    // ローディング開始
    const loadingToken = this.loadingService.loadStart();

    this.session = this.sessionService.getSaveSession(); // ログインユーザの情報を取得する
    this.affiliation = this.session.affiliation;
    this.company = this.session.company;
    this.headerService.show();
    // フッター表示条件: ROLE_EDIまたはROLE_MAKER
    this.isShowFooter = AuthUtils.isEdi(this.session) || AuthUtils.isMaker(this.session);
    this.createForm(); // 画面起動時にフォームを作成する

    // パラメータ取得
    const id = this.route.snapshot.params['id'];
    const path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
    const queryParamViewMode = Number(this.route.snapshot.queryParamMap.get('viewMode'));

    const preEvent: number = Number(this.route.snapshot.queryParamMap.get('preEvent'));
    if (preEvent === PreEventParam.CREATE) {
      this.location.replaceState((this.router.serializeUrl(this.router.createUrlTree(['items', id, Path.EDIT]))));
      this.overallSuccessMsgCode = 'SUCSESS.ITEM_ENTRY';  // 新規登録後のメッセージ設定
    }

    this.setInitViewMode(path, queryParamViewMode);
    this.getMasterData();

    // コピー新規で遷移してきた場合は、copy対象のIdを取得し、コピー対象のデータをDBから取得してformに設定する
    const copyId = this.route.snapshot.queryParamMap.get('copyId');
    this.isCopy = copyId == null ? false : true;
    if (this.viewMode === ViewMode.ITEM_EDIT || this.viewMode === ViewMode.PART_EDIT || this.isCopy) {
      // 品番情報を取得
      this.getInputData(this.isCopy ? copyId : id, this.isCopy).then(
        () => {
          this.isInitDataSetted = true;
          // ローディング停止
          this.loadingService.stop(loadingToken);
        },
        (error: HttpErrorResponse) => this.getItemErrorHandler(error));
    } else {
      this.setSuppliersInfoByAuth();
      this.isInitDataSetted = true;
      // ローディング停止
      this.loadingService.stop(loadingToken);
    }
  }

  ngOnDestroy() {
    this.loadingSubscription.unsubscribe();
  }

  /**
   * 初期表示時のviewModeを設定する。
   * @param path URLのパス
   * @param queryParamViewMode パラメータのViewMode
   */
  private setInitViewMode(path: string, queryParamViewMode: number): void {
    switch (path) {
      case Path.NEW: // 商品登録
        this.viewMode = ViewMode.ITEM_NEW;
        break;
      case Path.EDIT: // 商品編集
        this.viewMode = queryParamViewMode;
        if (!queryParamViewMode) {
          this.viewMode = ViewMode.ITEM_EDIT;
        }
        break;
      default:
        break;
    }
    this.mainForm.patchValue({ formViewMode: this.viewMode });
  }

  /**
   * 権限により取引先情報を設定する。
   * 取引先権限の場合、自分の所属会社で生産メーカーを固定にする為、
   * 取引先情報を取得する。
   */
  private setSuppliersInfoByAuth(): void {
    if (this.affiliation === AuthType.AUTH_SUPPLIERS) {
      this.junpcSirmstService.getSirmst({
        sirkbn: SupplierType.MDF_MAKER,
        searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH, // Ph2で完全一致検索対応
        searchText: this.company
      } as JunpcSirmstSearchCondition).subscribe(x => {
        // マスタの配列に設定
        const supplierFormGroup = this.formBuilder.group({
          supplierCode: x.items[0].sire,
          supplierName: x.items[0].name,
          supplierFactoryCode: '',
          supplierFactoryName: '',
          consignmentFactory: '',
        });

        this.mainForm.setControl('orderSuppliers', this.formBuilder.array([supplierFormGroup]));  // 発注先メーカー情報
      });
    }
  }

  /**
   * 仕入れ先を検索するモーダルを表示する。
   * @param supplier 仕入先区分
   */
  openSearchSupplierModal(supplier: SupplierType, supplierFormGroup: FormGroup): void {
    const modalRef = this.modalService.open(SearchSupplierModalComponent);
    let paramSearchText = '';
    let paramSire = '';
    let paramName = '';

    // モーダルへ渡す値を設定する。
    switch (supplier) {
      // TODO 生地附属機能追加時に処理見直し
      // case SupplierType.MALT_MAKER:
      //   paramSearchText = this.mainForm.controls.matlMakerCode.value;
      //   paramSire = this.mainForm.controls.matlMakerCode.value;
      //   paramName = this.matlMakerName;
      //   break;
      case SupplierType.MDF_MAKER:
        paramSearchText = supplierFormGroup.get('supplierCode').value;
        paramSire = supplierFormGroup.get('supplierCode').value;
        paramName = supplierFormGroup.get('supplierName').value;
        break;
      default:
        break;
    }
    modalRef.componentInstance.searchCondition = {
      sirkbn: supplier,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: paramSearchText
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: paramSire, name: paramName } as JunpcSirmst;

    // モーダルからの値を設定する。
    modalRef.result.then((result: JunpcSirmst) => {
      if (result != null) {
        switch (supplier) {
          // TODO 生地附属機能追加時に処理見直し
          // case SupplierType.MALT_MAKER:
          //   this.f.matlMakerCode.setValue(result.sire);
          //   this.matlMakerName = result.name;
          //   break;
          case SupplierType.MDF_MAKER:
            supplierFormGroup.patchValue({
              supplierCode: result.sire,
              supplierName: result.name
            });
            break;
          default:
            break;
        }
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * メーカー追加ボタン押下時の処理.
   * 仕入先検索モーダルを表示する.
   */
  onAddSupplier(supplier: SupplierType): void {
    const orderSuppliers = (this.mainForm.get('orderSuppliers') as FormArray);
    // 追加可能件数超えチェック
    if (MaxLength.ORDER_SUPPRIERS < orderSuppliers.length) {
      this.messageConfirmModalService.translateAndOpenConfirmModal('ERRORS.VALIDATE.NO_MORE_ADD').subscribe();
      return;
    }

    const modalRef = this.modalService.open(SearchSupplierModalComponent);
    modalRef.componentInstance.searchCondition = {
      sirkbn: supplier,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: ''
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = { sire: '', name: '' } as JunpcSirmst;

    let nonDupulicate = true; // 追加済みではないフラグ
    from(modalRef.result).pipe(
      filter(result => result != null),
      filter(result => nonDupulicate = !orderSuppliers.value.some(code => code.supplierCode === result.sire)),
      tap(this.pushToSupplierFormArray),
      tap(() => {
        this.currentSupplierIndex = this.suppliersFormArray.length - 1;
        this.displayMakerInfo(this.currentSupplierIndex);
      }),
      catchError(() => of(null)), // バツボタンクリック時は何もしない
      finalize(() => {
        // 選択したコードが追加済の場合はエラーモーダルを表示
        if (!nonDupulicate) {
          this.messageConfirmModalService.translateAndOpenConfirmModal('ERRORS.ALREADY_REGISTERED').subscribe();
        }}
      )
    ).subscribe();
  }

  /**
   * 発注先メーカーのFormArrayにレコード追加.
   * @param supplier メーカー情報
   */
  private pushToSupplierFormArray = (supplier: JunpcSirmst): void =>
    this.suppliersFormArray.push(this.formBuilder.group({
      id: '',
      partNoId: this.itemData.id,
      supplierCode: supplier.sire,
      supplierName: supplier.name,
      supplierFactoryCode: '',
      supplierFactoryName: '',
      consignmentFactory: '',
    }))

  /**
   * 担当者を検索するモーダルを表示する。
   * @param staffType 担当区分
   */
  openSearchStaffModel(staffType: StaffType): void {
    const modalRef = this.modalService.open(SearchStaffModalComponent);

    modalRef.componentInstance.staffType = staffType;
    modalRef.componentInstance.brandCode = BusinessUtils.splitPartNoKind(this.mainForm.controls.partNoKind.value).blandCode;

    // モーダルへ渡す値を設定する。
    switch (staffType) {
      case StaffType.PLANNING:
        modalRef.componentInstance.defaultStaffCode = this.mainForm.controls.plannerCode.value;
        modalRef.componentInstance.defaultStaffName = this.mainForm.controls.plannerName.value;
        break;
      case StaffType.PRODUCTION:
        modalRef.componentInstance.defaultStaffCode = this.mainForm.controls.mdfStaffCode.value;
        modalRef.componentInstance.defaultStaffName = this.mainForm.controls.mdfStaffName.value;
        break;
      case StaffType.PATANER:
        modalRef.componentInstance.defaultStaffCode = this.mainForm.controls.patanerCode.value;
        modalRef.componentInstance.defaultStaffName = this.mainForm.controls.patanerName.value;
        break;
      default:
        break;
    }

    // モーダルからの値を設定する。
    modalRef.result.then((result: JunpcCodmst) => {
      if (result != null) {
        switch (staffType) {
          case StaffType.PLANNING:
            this.mainForm.patchValue({ plannerCode: result.code1 });
            this.mainForm.patchValue({ plannerName: result.item2 });
            break;
          case StaffType.PRODUCTION:
            this.mainForm.patchValue({ mdfStaffCode: result.code1 });
            this.mainForm.patchValue({ mdfStaffName: result.item2 });
            break;
          case StaffType.PATANER:
            this.mainForm.patchValue({ patanerCode: result.code1 });
            this.mainForm.patchValue({ patanerName: result.item2 });
            break;
          default:
            break;
        }
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 工場を検索するモーダルを表示する。
   * @param supplier 仕入先区分
   */
  openSearchFactoryModal(supplier: SupplierType, supplierFormGroup: FormGroup): void {
    const modalRef = this.modalService.open(SearchKojmstModalComponent);

    modalRef.componentInstance.searchCondition = {
      sire: supplierFormGroup.get('supplierCode').value,
      sirkbn: supplier,
      brand: this.mainForm.controls.brandCode.value,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: supplierFormGroup.get('supplierFactoryCode').value
    } as JunpcKojmstSearchCondition;

    modalRef.componentInstance.default = {
      kojcd: supplierFormGroup.get('supplierFactoryCode').value,
      name: supplierFormGroup.get('supplierFactoryName').value,
    } as JunpcKojmst;

    modalRef.result.then((result: JunpcKojmst) => {
      if (result) {
        supplierFormGroup.patchValue({
          supplierFactoryCode: result.kojcd,
          supplierFactoryName: result.name,
        });
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 付記用語を検索するモーダルを表示する。
   * @param fukukitaruMasterType マスタID
   */
  openSearchAppendicesTermModal(fukukitaruMasterType: FukukitaruMasterType): void {
    const modalRef = this.modalService.open(AppendicesTermModalComponent, { windowClass: 'appendices-term' });

    let target = '';
    if (fukukitaruMasterType === FukukitaruMasterType.WASH_NAME_APPENDICES_TERM) {
      target = 'washAppendicesTermByColorList';
    } else {
      target = 'attentionTagAppendicesTermByColorList';
    }

    let appendicesTerm = this.mainForm.getRawValue()['fkItem'][target];
    if (appendicesTerm === null) {
      appendicesTerm = this.convertFukukitaruAppendicesTermByColorList(appendicesTerm, this.skusValue);
    }

    // マスタID
    modalRef.componentInstance.listMasterType = fukukitaruMasterType;
    // フクキタルマスタデータ
    modalRef.componentInstance.fukukitaruMaster = this.fukukitaruMaster;
    // 色別の洗濯ネーム付記用語リスト
    modalRef.componentInstance.appendicesTermByColorList = appendicesTerm;

    modalRef.result.then((results: FukukitaruAppendicesTermByColor[]) => {
      this.mainForm.patchValue({ fkItem: { [target]: results } });
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * フォームの外側を設定する。
   * FormArrayにする場合は、さらに作成用のFunctionを定義する。
   */
  private createForm(): void {
    // メインのFormGroupを作成する。
    this.mainForm = this.getFormGroup();
    // バリデーションの設定切り替え
    if (this.viewMode === ViewMode.ITEM_NEW || this.viewMode === ViewMode.ITEM_EDIT) {
      this.resetItemModeValidationError();
    }
  }

  /**
   * メインのFormGroupを返す。
   * データ取得時にも使うので外出しにしておく。
   * 品番登録をベースに作成し、商品登録時のバリデーションは後で書き換えを行う。
   */
  private getFormGroup(): FormGroup {
    return this.formBuilder.group({
      id: [''], // 品番Id
      partNo: [null], // 品番
      partNoKind: [null, [Validators.required, Validators.pattern(/^[a-zA-Z]{3}$/)]], // 品番_品種
      partNoSerialNo: [null, [Validators.required, Validators.pattern(/^[0-9]{4}[0]$/)]], // 品種_通番
      sample: [null], // サンプル
      // --- Changed 2021/06/04 (Fri) by JFE-Comservice ---
      // productName: [null, [Validators.required, NoWhitespaceValidator]], // 品名 (Changed 2021/06/04 (Fri) JFE-COM)
      productName: [null, [Validators.required, Validators.pattern(ValidatorsPattern.PRODUCT_NAME), NoWhitespaceValidator]], // 品名 (Changed 2021/06/04 (Fri) JFE-COM)
      // --- Changed 2021/06/04 (Fri) by JFE-Comservice ---
      productNameKana:
        [null, [Validators.pattern(ValidatorsPattern.PRODUCT_NAME_KANA),
        Validators.required, NoWhitespaceValidator]],  // 品名カナ
      preferredDeliveryDate: [null],  // 希望納品日
      proviOrderDate: [null], // 仮発注日
      deploymentDate: [null, [Validators.required]],  // 投入日
      deploymentWeek: [null, [Validators.required]],  // 投入週
      pendDate: [null, [Validators.required]],  // P終了日
      pendWeek: [null, [Validators.required]],  // P終了週
      year: [null, [Validators.pattern(/^2[0-9]{3}$/), Validators.required]], // 年度
      seasonCode: [null, [Validators.required]], // シーズン
      subSeasonCode: [null, [Validators.required]], // サブシーズン
      retailPrice: [null, [Validators.pattern(/^[0-9,]*$/), Validators.required]],  // 上代
      otherCost: [null, [Validators.pattern(/^[0-9,]*$/), Validators.required]],  // 原価
      //PRD_0118-01 mod JFE Start
      //// PRD_0084 add JFE start
      //matlCost: [null], // 生地原価
      //processingCost : [null],  // 加工原価
      //accessoriesCost: [null], // 付属原価
      //// PRD_0084 add JFE end
      matlCost: [null, [Validators.pattern(/^[0-9,]*$/)]], // 生地原価
      processingCost : [null, [Validators.pattern(/^[0-9,]*$/)]], // 加工原価
      accessoriesCost: [null, [Validators.pattern(/^[0-9,]*$/)]], // 付属原価
      //PRD_0118-01 mod JFE END
      // PRD_0023 add JFE start
      totalCost: [null], // 原価合計
      // PRD_0023 add JFE end
      matlMakerCode: [null, [Validators.pattern(/^[0-9]{5}$/)]],  // 生地メーカー
      orderSuppliers: this.formBuilder.array([this.setInitOrderSuppliersForm()]),  // 発注先メーカー情報
      cooName: [null], // 原産国名称
      cooCode: [null], // 原産国コード
      plannerCode: [null, [Validators.pattern(/^[0-9]{6}$/), Validators.required]], // 企画担当コード
      plannerName: [null],  // 企画担当名
      mdfStaffCode: [null, [Validators.pattern(/^[0-9]{6}$/), Validators.required]],  // 製造担当コード
      mdfStaffName: [null], // 製造担当名
      patanerCode: [null, [Validators.pattern(/^[0-9]{6}$/)]],  // パターンナーコード
      patanerName: [null],  // パターンナー名
      patternNo: [null, [Validators.pattern(/^[a-zA-Z0-9-?]*$/)]],  // パターンNo
      maruiGarmentNo: [null], // 丸井品番
      voiCode: [null],  // Voi区分
      materialCode: [null], // 素材
      zoneCode: [null], // ゾーン
      brandCode: [null],  // ブランド
      subBrandCode: [null], // サブブランド
      itemCode: [null], // アイテム
      tasteCode: [null],  // テイスト
      type1Code: [null],  // タイプ1
      type2Code: [null],  // タイプ2
      type3Code: [null],  // タイプ3
      grabBag: [null],  // 福袋:
      inventoryManagementType: [true],  // 在庫管理区分
      devaluationType: [true],  // 評価減区分
      reducedTaxRateFlg: [false], // 軽減税率対象フラグ
      digestionCommissionType: [false], // 消化委託区分
      outletCode: [null], // アウトレット区分
      makerGarmentNo: [null, [Validators.pattern(/^[a-zA-Z0-9]*$/)]], // メーカー品番
      memo: [null], // メモ
      itemMassageDisplay: [null], // 管理メモ
      itemMassage: [null],  // 管理メモフラグ
      registStatus: [RegistStatus.ITEM],  // 登録ステータス
      skus: this.formBuilder.array([]), // SKU(色・サイズ)
      janType: [JanType.IN_HOUSE_JAN], // JAN区分(初期値に自社JANを設定)
      articleNumbers: this.formBuilder.array([]), // JAN/UPCコード
      compositions: this.formBuilder.array([]), // 組成(混率)
      formViewMode: [this.viewMode],  // 画面表示モード
      compositionRequireCode: [''], // 組成必須コード
      fkAvailable: [false],
      fkItem: this.generateMaterialOrderFormGroup() // フクキタル項目のFormGroup
    }, { /* PRD_0023 && No_65 mod JFE start*/
      validator: Validators.compose([
        deploymentWeekLessOrEqualsPendWeekValidator, mdfMakerExistenceValidator, mdfMakerFactoryExistenceValidator,
        plannerExistenceValidator, mdfStaffExistenceValidator, patanerExistenceValidator,
        deploymentWeekNumberCorrectValidator, pendWeekNumberCorrectValidator, totalCostValidator
      ]) /* PRD_0023 && No_65 mod JFE end*/
    });
  }

  /**
   * フクキタルのFormGroupを返す。
   * 品種変更時にも使うので外出しにしておく。
   *
   * @returns フクキタルのFormGroup
   */
  private generateMaterialOrderFormGroup(): FormGroup {
    return this.formBuilder.group({
      id: [null], // フクキタル品番ID
      partNoId: [null], // 品番ID
      categoryCode: [null], // カテゴリコード
      nergyBillCode1: [''], // NERGY用メリット下札コード1
      nergyBillCode2: [''], // NERGY用メリット下札コード2
      nergyBillCode3: [''], // NERGY用メリット下札コード3
      nergyBillCode4: [''], // NERGY用メリット下札コード4
      nergyBillCode5: [''], // NERGY用メリット下札コード5
      nergyBillCode6: [''], // NERGY用メリット下札コード6
      printAppendicesTerm: [false], // 付記用語(シールへの印字)
      printCoo: [true],  // 原産国印字(初期値チェックあり)
      printParts: [false],  // 品質
      printQrcode: [false], // QRコード印字
      printSize: [false], // サイズ印字
      printWashPattern: [false],  // 絵表示(シールへの印字)
      printSustainableMark: [false],  // サスティナブルマーク印字
      recycleMark: [null],  // リサイクルマーク
      reefurPrivateBrandCode: [''], // REEFUR用ブランド
      saturdaysPrivateNyPartNo: [''], // サタデーズサーフ用NY品番
      stickerTypeCode: [null],  // アテンションシールのシール種類
      tapeCode: [null], // テープ種類
      tapeName: [''],   // テープ種類(コード名)
      tapeWidthCode: [null],  // テープ巾
      tapeWidthName: [''],    // テープ巾(コード名)
      washAppendicesTermByColorList: [[{
        colorCode: CompositionsCommon.COLOR_CODE,
        colorName: CompositionsCommon.COLOR_NAME,
        appendicesTermList: []
      }]],  // 色別の洗濯ネーム付記用語リスト
      listItemWashAppendicesTerm: [[]],
      attentionTagAppendicesTermByColorList: [[{
        colorCode: CompositionsCommon.COLOR_CODE,
        colorName: CompositionsCommon.COLOR_NAME,
        appendicesTermList: []
      }]], // 色別のアテンションタグ付記用語リスト
      listItemAttentionAppendicesTerm: [[]],
      washPatterns: this.getItemWashPatternFormArray([], []),  // 絵表示(洗濯ネーム)
      attentionTags: this.formBuilder.array([]), // アテンションタグ 付記用語
      cnProductCategory: [null],  // 製品分類
      cnProductType: [null], // 製品種別
      cnProductCooName: [''] // 産地
    });
  }

  /**
   * 原産国名称を入力した時の処理。
   * 原産国リストに存在する名称であればformの原産国コードに設定する。
   * @param cooName 原産国名称
   */
  onInputCooName(cooName: string): void {
    const isExist = this.cooMasterList.some(coo => {
      if (coo.item2.trim() === cooName.trim()) {
        this.mainForm.patchValue({ cooCode: coo.code1 });
        this.onChangeCnProduct(); // 中国内販情報 産地に原産国を設定
        return true;
      }
    });

    if (!isExist) {
      this.mainForm.patchValue({ cooCode: null });
      this.onChangeCnProduct(); // nullの場合も中国内販情報 産地に設定
    }
  }

  /**
   * 品名入力欄フォーカスアウト時の処理.
   * 品名をカナ変換して品名カナの入力欄に設定する.
   * @param prodName 品名
   */
  onBlurProdNameInput(prodName: string): void {
    if (StringUtils.isEmpty(prodName)) { return; }
    this.convertService.convertToKana(prodName).subscribe(text => this.mainForm.patchValue({ productNameKana: text.text }));
  }

  /**
   * 組成FormArrayにpatchValueする
   * @param idx index
   * @param detailIdx 詳細index
   * @param itemName 項目名
   * @param data patchするデータ
   */
  private patchValueInCompositions(idx: number, detailIdx: number, itemName: string, data: any) {
    this.fCtrlCompositions[idx].get('compositionDetailList')['controls'][detailIdx].patchValue({ [itemName]: data });
  }

  /**
   * パーツ名称を入力した時の処理。
   * パーツリストに存在する名称であればformの組成明細のパーツidに設定する。
   * @param partsName パーツ名称
   * @param compositionIndex 組成index
   * @param compositionDetailIndex 組成明細index
   */
  onInputPartsName(partsName: string, compositionIndex: number, compositionDetailIndex: number): void {
    const isExist = this.partsMaster.some(parts => {
      if (parts.partsName.trim() === partsName.trim()) {
        this.patchValueInCompositions(compositionIndex, compositionDetailIndex, 'partsCode', parts.id);
        return true;
      }
    });

    if (!isExist) {
      this.patchValueInCompositions(compositionIndex, compositionDetailIndex, 'partsCode', null);
    }
  }

  /**
   * 組成名称を入力した時の処理。
   * 組成リストに存在する名称であればformの組成明細の組成コードに設定する。
   * @param compositionName 組成名称
   * @param compositionIndex 組成index
   * @param compositionDetailIndex 組成明細index
   */
  onInputCompositionName(compositionName: string, compositionIndex: number, compositionDetailIndex: number): void {
    const isExist = this.compositionsMasterList.some(composition => {
      if (composition.item1.trim() === compositionName.trim()) {
        this.patchValueInCompositions(compositionIndex, compositionDetailIndex, 'compositionCode', composition.code1);
        return true;
      }
    });

    if (!isExist) {
      this.patchValueInCompositions(compositionIndex, compositionDetailIndex, 'compositionCode', null);
    }
  }

  /**
   * シーズン入力値変更時の処理.
   * @param シーズン入力値
   */
  onChangeSeason(value: string): void {
    this.mainForm.patchValue({ seasonCode: this.itemService.extractSeasonCode(value) });  // シーズンコードに格納
  }

  /**
   * 選択したメーカーをformに表示する
   * @param idx 選択したメーカーのindex
   */
  displayMakerInfo(idx: number): void {
    this.currentSupplierIndex = idx;
    // 活性・非活性
    this.disableMakerForm(idx);
  }

  /**
   * 取得データをformに設定する。
   * @param item 品番情報
   * @param registStatus 登録ステータス
   * @param isCopy コピー新規フラグ
   */
  private patchValuesToMainForm(item: Item, registStatus: RegistStatus, isCopy: boolean): void {
    const partNo = item.partNo;
    const splitPartNo = BusinessUtils.splitPartNo(partNo);
    this.mainForm.patchValue({ id: isCopy ? null : item.id });  // 品番Id。コピー新規の場合は新規採番する為null
    this.mainForm.patchValue({ partNo: partNo }); // 品番
    const partNoKind = splitPartNo.partNoKind;
    this.mainForm.patchValue({ partNoKind: partNoKind }); // 品番_品種
    this.mainForm.patchValue({ partNoSerialNo: isCopy ? null : splitPartNo.partNoSerialNo }); // 品種_通番。コピー新規の場合は重複させないようにnull
    this.mainForm.patchValue({ sample: item.sample });  // サンプル
    this.mainForm.patchValue({ productName: item.productName });  // 品名
    this.mainForm.patchValue({ productNameKana: item.productNameKana });  // 品名カナ
    this.mainForm.patchValue({  // 希望納品日
      preferredDeliveryDate:
        this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(item.preferredDeliveryDate))
    });
    this.mainForm.patchValue({  // 仮発注日
      proviOrderDate:
        this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(item.proviOrderDate))
    });
    this.mainForm.patchValue({  // 投入日
      deploymentDate: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(item.deploymentDate))
    });
    this.mainForm.patchValue({ deploymentWeek: item.deploymentWeek });  // 投入週
    this.mainForm.patchValue({  // P終了日
      pendDate: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(item.pendDate))
    });
    this.mainForm.patchValue({ pendWeek: item.pendWeek });  // P終了週
    this.mainForm.patchValue({ year: isCopy ? null : item.year });  // 年度。コピー新規の場合は品種_通番をnullする為年もnull
    this.mainForm.patchValue({ seasonCode: item.seasonCode });  // シーズン
    this.mainForm.patchValue({ subSeasonCode: item.subSeasonCode });  // サブシーズン
    this.mainForm.patchValue({ retailPrice: item.retailPrice });  // 上代
    this.mainForm.patchValue({ otherCost: item.otherCost });  // 原価

    //  PRD_0084 add JFE start
    this.mainForm.patchValue({ matlCost: item.matlCost }); // 生地原価
    this.mainForm.patchValue({ processingCost: item.processingCost }); // 加工賃
    this.mainForm.patchValue({ accessoriesCost: item.accessoriesCost }); // 付属品
    // PRD_0084 add JFE end
    //  PRD_0023 add JFE start
    this.mainForm.patchValue({ totalCost: null }); // 原価合計
    // PRD_0023 add JFE end
    this.mainForm.patchValue({ janType: isCopy ? JanType.IN_HOUSE_JAN : item.janType }); // JAN区分。コピー新規の場合は自社JANで登録
    // Service経由でJAN区分渡す
    this.itemDataService.janType$.next(this.f.janType.value);

    this.mainForm.patchValue({ matlMakerCode: item.matlMakerCode });  // 生地メーカー

    this.setCurrentSupplierIndex(item);
    this.mainForm.setControl('orderSuppliers', this.generateOrderSupplierFA(item.orderSuppliers, isCopy));  // 発注先メーカー情報

    this.cooMasterList.some(coo => {
      if (coo.code1 === item.cooCode) {
        this.mainForm.patchValue({ cooName: coo.item2 });  // 原産国名称
        return true;
      }
    });
    this.mainForm.patchValue({ cooCode: item.cooCode });  // 原産国
    this.mainForm.patchValue({ plannerCode: item.plannerCode });  // 企画担当コード
    this.mainForm.patchValue({ plannerName: item.plannerName });  // 企画担当名
    this.mainForm.patchValue({ mdfStaffCode: item.mdfStaffCode });  // 製造担当コード
    this.mainForm.patchValue({ mdfStaffName: item.mdfStaffName });  // 製造担当名
    this.mainForm.patchValue({ patanerCode: item.patanerCode });  // パターンナーコード
    this.mainForm.patchValue({ patanerName: item.patanerName });  // パターンナー名
    this.mainForm.patchValue({ patternNo: item.patternNo });  // パターンNo
    this.mainForm.patchValue({ brandCode: item.brandCode });  // ブランド
    this.mainForm.patchValue({ itemCode: item.itemCode });  // アイテム
    this.mainForm.patchValue({ grabBag: item.grabBag });  // 福袋
    this.mainForm.patchValue({ inventoryManagementType: item.inventoryManagementType });  // 在庫管理区分
    this.mainForm.patchValue({ devaluationType: item.devaluationType });  // 評価減区分
    this.mainForm.patchValue({ reducedTaxRateFlg: item.reducedTaxRateFlg });  // 軽減税率対象フラグ
    this.mainForm.patchValue({ digestionCommissionType: item.digestionCommissionType });  // 消化委託区分
    this.mainForm.patchValue({ outletCode: item.outletCode });  // アウトレット区分
    this.mainForm.patchValue({ makerGarmentNo: item.makerGarmentNo });  // メーカー品番
    this.mainForm.patchValue({ memo: item.memo });  // メモ
    this.mainForm.patchValue({ itemMassageDisplay: item.itemMassageDisplay });  // 管理メモ
    this.mainForm.patchValue({ itemMassage: item.itemMassage });  // 管理メモフラグ
    this.mainForm.patchValue({ registStatus: registStatus });  // 登録ステータス

    const fkItem = item.fkItem;
    this.mainForm.patchValue({
      fkItem: {
        id: fkItem.id,  // フクキタル品番ID
        partNoId: fkItem.partNoId,  // 品番ID
        categoryCode: fkItem.categoryCode,  // カテゴリコード
        nergyBillCode1: fkItem.nergyBillCode1,  // NERGY用メリット下札コード1
        nergyBillCode2: fkItem.nergyBillCode2,  // NERGY用メリット下札コード2
        nergyBillCode3: fkItem.nergyBillCode3,  // NERGY用メリット下札コード3
        nergyBillCode4: fkItem.nergyBillCode4,  // NERGY用メリット下札コード4
        nergyBillCode5: fkItem.nergyBillCode5,  // NERGY用メリット下札コード5
        nergyBillCode6: fkItem.nergyBillCode6,  // NERGY用メリット下札コード6
        printAppendicesTerm: fkItem.printAppendicesTerm,  // 付記用語(シールへの印字)
        printCoo: fkItem.printCoo,  // サイズ印字
        printParts: fkItem.printParts,  // 品質
        printQrcode: fkItem.printQrcode,  // QRコード印字
        printSize: fkItem.printSize,  // 原産国印字
        printWashPattern: fkItem.printWashPattern,  // 絵表示(シールへの印字)
        printSustainableMark: fkItem.printSustainableMark,  // サスティナブルマーク印字
        recycleMark: fkItem.recycleMark,  // リサイクルマーク
        reefurPrivateBrandCode: fkItem.reefurPrivateBrandCode,  // REEFUR用ブランド
        saturdaysPrivateNyPartNo: fkItem.saturdaysPrivateNyPartNo,  // サタデーズサーフ用NY品番
        stickerTypeCode: fkItem.stickerTypeCode,  // アテンションシールのシール種類
        tapeCode: fkItem.tapeCode,  // テープ種類
        tapeName: fkItem.tapeName,  // テープ種類(コード名)
        tapeWidthCode: fkItem.tapeWidthCode,  // テープ巾
        tapeWidthName: fkItem.tapeWidthName,  // テープ巾(コード名)
        washAppendicesTermByColorList: this.convertFukukitaruAppendicesTermByColorList(
          fkItem.listItemWashAppendicesTerm, item.skus, isCopy),  // 色別の洗濯ネーム 付記用語リスト
        attentionTagAppendicesTermByColorList: this.convertFukukitaruAppendicesTermByColorList(
          fkItem.listItemAttentionAppendicesTerm, item.skus, isCopy),
        cnProductCategory: fkItem.cnProductCategory,  // 製品分類
        cnProductType: fkItem.cnProductType, // 製品種別
      }
    });
    // 製品分類または製品種別がある場合は中国内販情報の産地に原産国名称を設定
    if (fkItem.cnProductCategory != null || fkItem.cnProductType != null) {
      this.f.fkItem.patchValue({ cnProductCooName: this.mainForm.getRawValue().cooName });
    }

    // 品番情報の統計情報がDB保存されていない場合、フォームデータとして設定しない
    if (StringUtils.isNotEmpty(item.materialCode)) { this.mainForm.patchValue({ materialCode: item.materialCode }); } // 素材
    if (StringUtils.isNotEmpty(item.zoneCode)) { this.mainForm.patchValue({ zoneCode: item.zoneCode }); }            // ゾーン
    if (StringUtils.isNotEmpty(item.subBrandCode)) { this.mainForm.patchValue({ subBrandCode: item.subBrandCode }); } // サブブランド
    if (StringUtils.isNotEmpty(item.tasteCode)) { this.mainForm.patchValue({ tasteCode: item.tasteCode }); }         // テイスト
    if (StringUtils.isNotEmpty(item.type1Code)) { this.mainForm.patchValue({ type1Code: item.type1Code }); }         // タイプ1
    if (StringUtils.isNotEmpty(item.type2Code)) { this.mainForm.patchValue({ type2Code: item.type2Code }); }          // タイプ2
    if (StringUtils.isNotEmpty(item.type3Code)) { this.mainForm.patchValue({ type3Code: item.type3Code }); }         // タイプ3
    // 丸井品番 Null/'000000'(スペース扱い)/のときはセットしない
    if (StringUtils.isNotEmpty(item.maruiGarmentNo) && item.maruiGarmentNo.trim() !== this.MARUI_DEFAULT_VALUE) {
      this.mainForm.patchValue({ maruiGarmentNo: item.maruiGarmentNo });
    }
    if (StringUtils.isNotEmpty(item.voiCode)) { this.mainForm.patchValue({ voiCode: item.voiCode }); } // Voi区分
    if (splitPartNo.partNoKind && splitPartNo.partNoKind.length >= 2) { // 品種を取得している場合、組成(混率)Formを設定する。
      this.mainForm.setControl('compositions', this.getCompositionsFormArray(item.compositions, item.skus, isCopy));
    }
    (<FormGroup> this.mainForm.controls.fkItem)
      .setControl('washPatterns', this.getItemWashPatternFormArray(fkItem.listItemWashPattern, item.skus, isCopy));  // 絵表示(洗濯ネーム));
  }

  /**
   * 添付ファイルを表示する。
   */
  private showAttachmentFile(): void {
    // 見積添付ファイルのリストを作成
    this.estimatesFile = this.itemFileInfoList.filter(fileInfo => fileInfo.fileCategory === FileCategory.TYPE_ESTIMATES);
    // 追加で添付できる数をカウントするために現在の添付ファイル数をセット
    this.activefilecount = this.estimatesFile.length;
    // タンザク添付ファイルのリストを作成
    const tanzakuList = this.itemFileInfoList.filter(fileInfo => fileInfo.fileCategory === FileCategory.TYPE_TANZAKU);

    if (ListUtils.isEmpty(tanzakuList)) {
      return;
    }
    const tanzakuImage = tanzakuList[tanzakuList.length - 1]; // 最新の画像
    // ファイルのダウンロード
    this.fileService.fileDownload(tanzakuImage.fileNoId.toString()).subscribe(res => {
      const data = this.fileService.splitBlobAndFileName(res);
      // アップロードが成功したら、配列に保持しておく。
      const file = new File([data.blob], tanzakuImage.fileName); // ファイル名もセット
      this.viewFileId = tanzakuImage.fileNoId;
      this.onFileSelect([file], false, data.blob.type); // 参照モード
    });
  }

  /**
   * 編集不可項目を制御する。
   * SKU(カラーコード)については品番登録後も編集は不可だが追加は可能※別コンポーネントで制御
   * 見積ファイルはhtml側で制御
   * @param registStatus 登録ステータス
   * @param item 品番情報
   */
  private setDisabled(registStatus: number, item: Item): void {
    if (item.registStatus === RegistStatus.PART) {
      // 品番登録済みの場合、JAN区分を非活性にする
      this.disableJanTypeForm();
    }

    if (registStatus === RegistStatus.ITEM) {
      // 登録ステータスが商品の場合
      if (item.registeredOrder) {
        // 登録済みの発注情報がある場合は、品種を非活性にする
        this.f.partNoKind.disable();   // 品種
      }

      // 処理を終了する
      return;
    }

    // 品番情報の項目を非活性にする
    this.disableItemInfoForm(item);

    // メーカーの項目を非活性にする
    this.disableMakerForm(this.currentSupplierIndex);

    if (item.approvedDelivery || item.completedAllOrder) {
      // 承認済みの納品依頼あり または 全発注が完納の場合
      // 組成(混率)、原産国、丸井、その他の項目を非活性にする + 品名、品名（カナ）、納品日・週、投入日・週を非活性にする
      this.disableWhenOrderApprovedOrComplete();
    }

    // 優良誤認承認済の組成は非活性
    const isApprovedColorFormFn = this.itemBo.isApprovedColorForm(item.approvedColors);
    this.fCtrlCompositions.filter(isApprovedColorFormFn).forEach(f => f.disable());

    // 共通の組成非活性
    if (this.itemBo.isDisableCommonComposition(item)) {
      this.fCtrlCompositions.find(this.itemBo.isCommonCompositionForm).disable();
    }

    if (AuthUtils.isMasterMaintenance(this.session)) {
      // マスタメンテ権限の場合
      // シーズン、年度、品名、品名（カナ）、投入日・週、P終了日・週、上代、原価を活性にする
      this.enableWhenSsAuth(item);
    }
  }

  /**
   * 品番情報の項目を非活性にする.
   * @param item 品番情報
   */
  private disableItemInfoForm(item: Item): void {
    // 品番
    this.f.partNoKind.disable();            // 品種
    this.f.partNoSerialNo.disable();        // 通番

    if (item.approvedOrder || item.approvedMisleadingRepresentation) {
      // 「発注承認済み、または優良誤認承認済み」の場合、シーズン、年度を非活性
      this.f.subSeasonCode.disable();
      this.f.year.disable();
    }

    this.f.sample.disable();                // サンプルとして登録する
    this.f.proviOrderDate.disable();        // 仮発注日
    this.f.preferredDeliveryDate.disable(); // 納品日

    if (item.registeredOrder || item.approvedMisleadingRepresentation) {
      // 「発注登録済み、または優良誤認承認済み」の場合
      this.f.retailPrice.disable();         // 上代(税無)
      this.f.otherCost.disable();           // 原価
      this.f.mdfStaffCode.disable();        // 製造担当
      this.isMdfStaffDisabled = true;       // 製造担当
      //PRD_0118-02 add JFE Start
      this.f.matlCost.disable();           // 生地原価
      this.f.processingCost.disable();           // 加工原価
      this.f.accessoriesCost.disable();           // 附属原価
      //PRD_0118-02 add JFE END
    }

    if (item.completedAllOrder) {
      // 「全発注が完納」の場合、活性にする
      this.f.retailPrice.enable();         // 上代(税無)
      this.f.otherCost.enable();           // 原価
      //PRD_0118-03 add JFE Start
      this.f.matlCost.enable();           // 生地原価
      this.f.processingCost.enable();           // 加工原価
      this.f.accessoriesCost.enable();           // 附属原価
      //PRD_0118-03 add JFE END
    }
  }

  /**
   * JAN区分の項目を非活性にする.
   */
  private disableJanTypeForm(): void {
    this.f.janType.disable();        // JAN区分
  }

  /**
   * メーカーの項目を非活性にする。
   * @param index 活性/非活性を変更する発注先メーカー情報のIndex
   */
  private disableMakerForm(index: number): void {

    const orderSupplier = this.suppliersFormArray.at(index);

    if (StringUtils.isEmpty(orderSupplier.value.id)) {
        // 未登録の場合はメーカーコードは非活性 生産工場と委託先工場は活性
        orderSupplier.get('supplierCode').disable();
        this.isMakerCodeModalDisabled = true;
        this.isMakerFactoryModalDisabled = false;

        return;
    }
    // 登録済みのときは、全て非活性
    orderSupplier.disable();        // 生産メーカー / 生産工場 / 委託先工場
    this.isMakerCodeModalDisabled = true;
    this.isMakerFactoryModalDisabled = true;
  }

  /**
   * 承認済みの納品依頼あり または 全発注が完納の場合の非活性項目を設定する
   */
  private disableWhenOrderApprovedOrComplete(): void {
    this.disableItemInfo();         // 品番の一部
    this.f.compositions.disable();  // 組成(混率)
    this.disableCooForm();          // 原産国
    this.disableMaruiForm();        // 丸井
    this.disableOtherForm();        // その他
  }

  /**
   * 品番情報の一部を非活性にする
   * 品名、品名カナ
   * 投入日・週、P終了日・週
   */
  private disableItemInfo(): void {
    this.f.productName.disable();           // 品名
    this.f.productNameKana.disable();       // 品名(全角カナ)
    this.f.deploymentDate.disable();        // 投入日
    this.f.deploymentWeek.disable();        // 投入週
    this.f.pendDate.disable();              // P終了日
    this.f.pendWeek.disable();              // P終了週
  }

  /**
   * 原産国を非活性にする。
   */
  private disableCooForm(): void {
    this.f.cooName.disable(); // 原産国名
    this.f.cooCode.disable(); // 原産国コード
  }

  /**
   * 丸井の項目を非活性にする。
   */
  private disableMaruiForm(): void {
    this.f.maruiGarmentNo.disable();  // 丸井品番
    this.f.voiCode.disable();         // Voi区分
  }

  /**
   * その他の項目を非活性にする。
   */
  private disableOtherForm(): void {
    this.f.inventoryManagementType.disable(); // 在庫管理
    this.f.devaluationType.disable();         // 評価減
    this.f.digestionCommissionType.disable(); // 消化委託商品
    this.f.reducedTaxRateFlg.disable();       // 軽減税率対象
    // 見積ファイルはhtml側で制御
  }

  /**
   * Admin権限のとき項目を活性にする
   * @param item 品番情報
   */
  private enableWhenSsAuth(item: Item): void {
    this.f.subSeasonCode.enable();         // サブシーズン
    this.f.year.enable();                  // 年度
    this.f.productName.enable();           // 品名
    this.f.productNameKana.enable();       // 品名(全角カナ)
    this.f.deploymentDate.enable();        // 投入日
    this.f.deploymentWeek.enable();        // 投入週
    this.f.pendDate.enable();              // P終了日
    this.f.pendWeek.enable();              // P終了週

    if (item.approvedDelivery) {
      // 納品依頼承認後の場合
      this.f.retailPrice.enable();         // 上代(税無)
      this.f.otherCost.enable();           // 原価
      //PRD_0118-04 add JFE Start
      this.f.matlCost.enable();           // 生地原価
      this.f.processingCost.enable();           // 加工原価
      this.f.accessoriesCost.enable();           // 附属原価
      //PRD_0118-04 add JFE END
    }

    this.f.devaluationType.enable();         // 評価減
  }

  /**
   * 品種コードで検索するマスタデータを取得する。
   * @param viewMode 画面表示モード
   * @param splitPartNoKind 分割した品種
   * @returns Promise<void>
   */
  private async getMasterDataByPartNoKind(viewMode: number, splitPartNoKind: PartNoKind): Promise<void> {
    if (splitPartNoKind.blandCode == null || splitPartNoKind.blandCode.length < 2) {
      // ブランドコードが2桁未満
      // サイズマスタ取得不可。品種の変更を感知させる為、初期化する
      this.partNoKindConfirm = null;
      // 検索しない
      return;
    }

    const partNoKind = splitPartNoKind.itemCode != null ?
        splitPartNoKind.blandCode + splitPartNoKind.itemCode : splitPartNoKind.blandCode;

    await forkJoin(
      this.getPartsMaster(splitPartNoKind),     // パーツ
      this.getFukukitaruMaster(splitPartNoKind), // フクキタル
      this.generateSizeMaster(partNoKind) // サイズマスタ
    ).toPromise();

    // 品番更新時のみ必要
    if (viewMode === ViewMode.PART_EDIT) {
      await forkJoin(
        this.getVoi(splitPartNoKind.blandCode) // Voi区分取得
        , this.getTasteMaster(splitPartNoKind.blandCode) // テイスト取得
        , this.getMaruiPartNo(splitPartNoKind) // 丸井品番
        , this.getSubBlandMaster(splitPartNoKind.blandCode)  // サブブランド
        , this.getoutletMaster(splitPartNoKind.blandCode)  // 展開
        , this.getType(partNoKind) // タイプ
        , this.getZone(splitPartNoKind.blandCode)  // ゾーン
        , this.setCompositionRequire(splitPartNoKind)  // 組成(混率)必須設定
      ).toPromise();
    }
  }

  /**
   * 品種フォーカスアウト時処理
   * @param partNoKind 品種入力値
   */
  onBlurPartNoKind(partNoKind: string): void {
    this.mainForm.patchValue({ partNoKind: partNoKind }); // ここでpatchValueしないと画面で無理やり大文字にしたものが戻ってしまう
    // クリア
    this.mainForm.patchValue({
      // 担当者クリア
      mdfStaffCode: null, // 製造担当者コード
      mdfStaffName: null, // 製造担当者名
      plannerCode: null,  // 企画担当者コード
      plannerName: null,  // 企画担当者名
      patanerCode: null,  // パターンナーコード
      patanerName: null,  // パターンナー名
      // 統計情報クリア
      type1Code: null,    // タイプ1コード
      type2Code: null,    // タイプ2コード
      type3Code: null,    // タイプ3コード
      subBrandCode: null, // サブブランドコード
      tasteCode: null,    // テイストコード
      zoneCode: null,     // ゾーンコード
      fkAvailable: false  // フクキタル利用可能をfalseへ
    });
    // フクキタル項目フォームクリア
    this.resetMaterialOrderForm();

    // 丸井品番、Voi区分設定処理
    this.setFormMaruiAndVoiByPartNoKindChange(this.viewMode);
    // 品種桁数による分岐処理
    switch (partNoKind.length) {
      case 0:
      case 1:
        this.clearSkuAndArticleNumberAndComposition();  // 品種が0桁または1桁の場合、色・サイズとJANと混率をクリア
        this.mainForm.patchValue({ compositionRequireCode: '' }); // 組成必須コード初期化
        // 品種がないときは丸井品番のバリデーションを外す
        this.f.maruiGarmentNo.setErrors(null);
        this.setValidation(<FormGroup> this.f.maruiGarmentNo);
        this.partNoKindConfirm = null; // 子Componentのイベントを駆動させ、サイズを取得してSKUFormを設定する
        break;
      case 2:
      case 3:
        // formを設定する。
        this.setFormByPartNoKindChange(this.viewMode, partNoKind);
        this.setFukukitaruValidation(partNoKind.slice(0, 2));  // 品種変更のタイミングでフクキタルのバリデーションを設定
        break;
      default:
        // その他の桁数の時は何もしない
        break;
    }
  }

  /**
   * フクキタル項目のフォームクリア.
   * ※IDと品番IDは保持すること
   */
  private resetMaterialOrderForm(): void {
    this.mainForm.setControl('fkItem', this.generateMaterialOrderFormGroup());
    // IDは保持すること(初期登録時はnull)
    this.mainForm.patchValue({
      fkItem: {
        id: (this.itemData == null || this.itemData.fkItem == null) ? null : this.itemData.fkItem.id,  // フクキタル品番ID
        partNoId: this.itemData == null ? null : this.itemData.id,  // 品番ID
      }
    });
  }

  /**
   * 品番情報画面での品種変更時のFormの丸井品番、Voi区分設定処理を行う。
   * @param viewMode 画面表示モード
   * @param partNoKind 変更後の品種
   */
  private setFormMaruiAndVoiByPartNoKindChange(viewMode: number): void {
    if (viewMode !== ViewMode.PART_EDIT) {
      return;
    }
    this.mainForm.patchValue({
      maruiGarmentNo: null,   // 丸井品番
      voiCode: null           // Voi区分
    });
    // 丸井品番マスタリストクリア
    this.maruiMasterList = [];
    // Voi区分マスタリストクリア
    this.voiMasterList = [];
  }

  /**
   * 品種変更時のForm設定処理を行う。
   * @param viewMode 画面表示モード
   * @param partNoKind 変更後の品種
   */
  private setFormByPartNoKindChange(viewMode: number, partNoKind: string): void {
    // 品種に紐づく各種マスタデータ再取得
    const splitPartNoKind = BusinessUtils.splitPartNoKind(partNoKind);
    this.getMasterDataByPartNoKind(viewMode, splitPartNoKind);

    // 品種の値をFormGroupにセットする。
    this.mainForm.patchValue({
      brandCode: splitPartNoKind.blandCode,
      itemCode: splitPartNoKind.itemCode
    });

    // 組成(混率)Form未設定であれば共通を設定する。
    if (ListUtils.isEmpty(this.compositionsFormArray)) {
      this.setCommonCompositionForm();
    }

    // 絵表示Form未設定であれば共通を設定する。
    if (ListUtils.isEmpty(this.fCtrlWashPatterns)) {
      this.setCommonWashPatternForm();
    }
  }

  /**
   * 組成(混率)Formに共通を設定する。
   */
  private setCommonCompositionForm(): void {
    // 組成(混率)に共通入力値設定
    const colorInfo = {
      colorCode: CompositionsCommon.COLOR_CODE,
      colorName: CompositionsCommon.COLOR_NAME,
    } as Sku;
    this.compositionsFormArray.push(this.createCompositionDefaultForm(colorInfo));
  }

  /**
   * 絵表示Formに共通を設定する。
   */
  private setCommonWashPatternForm(): void {
    // 絵表示に共通入力値設定
    const colorInfo = {
      colorCode: FukukitaruColorCommon.COLOR_CODE,
      colorName: FukukitaruColorCommon.COLOR_NAME,
    } as Sku;
    this.formWashPatterns.push(this.createWashPatternDefaultForm(colorInfo));
  }

  /**
   * 色・サイズ、JAN/UPCコード及び組成(混率)の入力値と入力欄表示をクリアする。
   */
  private clearSkuAndArticleNumberAndComposition(): void {
    // 色・サイズ表示中の場合はクリアする。
    // 品番から自動でセットした値をクリアする。
    this.mainForm.setControl('skus', this.formBuilder.array([]));         // SKU(色・サイズ)リストクリア
    this.skusValue = [];                                                  // SKU(色・サイズ)リストクリア
    this.mainForm.patchValue({  // ブランドコード、アイテムコードクリア
      brandCode: null,
      itemCode: null
    });
    this.mainForm.setControl('articleNumbers', this.formBuilder.array([])); // JAN/UPCコードリストクリア
    this.mainForm.setControl('compositions', this.formBuilder.array([])); // 組成(混率)リストクリア
  }

  /**
   * 混率(組成)フォーム配列を作成して返す。(共通含めた全色の組成(混率))。
   * 混率(組成)リストになくてもsku(色・サイズ)リストにあれば、その色の混率(組成)フォームを作成する。※混率(組成)明細の入力値は空)
   * 混率(組成)リスト、sku(色・サイズ)リストともにデータがない場合は組成(混率)Formに共通を設定する。
   * @param compositionsList 組成(混率)リスト
   * @param skusList sku(色・サイズ)リスト
   * @param isCopy コピー新規フラグ(任意)
   * @returns 組成(混率)のFormGroup
   */
  private getCompositionsFormArray(compositionsList: Compositions[], skusList: Sku[], isCopy: boolean = false): FormArray {
    // 組成(混率)リストとsku(色・サイズ)リストから色コード、色名を全て抽出
    const colorCodeNameList: { colorCode: string, colorName: string }[] = [];
    let isExistsCommonComposition = false;
    compositionsList.forEach(value => {
      if (value.colorCode === CompositionsCommon.COLOR_CODE) {
        isExistsCommonComposition = true;
        // 共通はリストの先頭に格納
        colorCodeNameList.unshift({ colorCode: value.colorCode, colorName: value.colorName });
      } else {
        colorCodeNameList.push({ colorCode: value.colorCode, colorName: value.colorName });
      }
    });

    // 共通がない場合は先頭に追加
    if (!isExistsCommonComposition) {
      colorCodeNameList.unshift({
        colorCode: CompositionsCommon.COLOR_CODE,
        colorName: CompositionsCommon.COLOR_NAME
      });
    }
    skusList.forEach(value => colorCodeNameList.push({ colorCode: value.colorCode, colorName: value.colorName }));

    // 抽出した色コード、色名リストから色コードの重複除去
    const uniqueColorCodeNameList = colorCodeNameList.filter(
      (value1, idx, array) => (array.findIndex(value2 => value2.colorCode === value1.colorCode) === idx)
    );

    const compositionFormArray = new FormArray([]); // 共通含めた全色の組成(混率)

    // 色コードの種類分、混率(組成)フォーム配列を作成する。
    uniqueColorCodeNameList.forEach(uniqueColorCodeName => {
      const compositionDetailFormArray = new FormArray([]); // 各色の組成(混率)全行

      // 組成(混率)に登録済み情報を1つの色の組成(混率)明細Formに設定する
      compositionsList.forEach(compositions => {
        if (compositions.colorCode === uniqueColorCodeName.colorCode) {
          compositionDetailFormArray.push(this.createCompositionFormGroupSettedValues(compositions, isCopy));
        }
      });

      // 1つの色の組成(混率)残り全行作成(色コードだけ設定した組成(混率)詳細Form)
      const settedLength = compositionDetailFormArray.length;
      for (let i = 0; i < this.COMPOSITIONS_MAX_COUNT - settedLength; i++) {
        compositionDetailFormArray.push(this.createCompositionFormGroupSettedColorCode(uniqueColorCodeName.colorCode));
      }

      const colorName =
        uniqueColorCodeName.colorCode === CompositionsCommon.COLOR_CODE ?
          CompositionsCommon.COLOR_NAME : uniqueColorCodeName.colorName;
      // 各色の組成(混率)全情報フォームグループ(＝組成(混率)全行に色コード、色名、indexを加えたフォームグループ)
      const colorFormGroup = this.formBuilder.group({
        colorCode: uniqueColorCodeName.colorCode,
        colorName: colorName,
        showCompositions: settedLength > 0, // 組成(混率)明細に入力値があればチェックボックスon
        compositionDetailList: compositionDetailFormArray
      });
      compositionFormArray.push(colorFormGroup);
    });

    return compositionFormArray;
  }


  /**
   * 絵表示フォーム配列を作成して返す。(共通含めた全色の絵表示)。
   * 絵表示リストになくてもsku(色・サイズ)リストにあれば、その色の絵表示チェックボックスを作成する。
   * 絵表示リスト、sku(色・サイズ)リストともにデータがない場合は絵表示Formに共通を設定する。
   * @param itemWashPatternList 絵表示リスト
   * @param skusList sku(色・サイズ)リスト
   * @returns 絵表示のFormGroup
   */
  private getItemWashPatternFormArray(
    itemWashPatternList: FukukitaruItemWashPattern[],
    skuList: Sku[],
    isCopy: boolean = false
  ): FormArray {
    /** 返却する配列 */
    const itemWashPatternForm = new FormArray([]);
    /** 一時保持用配列 */
    const tempArr = new FormArray([]);

    // 抽出した色コード、色名リストから色コードの重複除去
    const uniqueColorCodeNameList = skuList.filter(
      (value1, idx, array) => (array.findIndex(value2 => value2.colorCode === value1.colorCode) === idx)
    );

    // 1. 00登録
    const isShowCommon = true;
    const commonColorArray = {
      colorCode: FukukitaruColorCommon.COLOR_CODE,
      colorName: FukukitaruColorCommon.COLOR_NAME,
    };
    tempArr.push(this.createInitWashPatternFormArray(commonColorArray, isShowCommon)); // APIから得た情報があった場合に必要であるため設定

    // 2. SKUの情報をマージ
    const isShowUniqueColor = false;
    uniqueColorCodeNameList.forEach((val) => {
      const uniqueColorArray = {
        colorCode: val.colorCode,
        colorName: val.colorName,
      };
      tempArr.push(this.createInitWashPatternFormArray(uniqueColorArray, isShowUniqueColor)); // APIから得た情報があった場合に必要であるため設定
    });

    // カラーコード、カラー名を設定した一時保持用配列をもとに、返却する配列にAPIから得た情報を設定する
    tempArr.controls.forEach(val => {
      // APIから得た情報があれば、該当するカラーコードの情報を取得
      const itemWashPatternData = itemWashPatternList.find((value) => value.colorCode === val.get('colorCode').value);

      if (itemWashPatternData) {
        if (val.get('colorCode').value === FukukitaruColorCommon.COLOR_CODE) {
          // 3. APIから得た情報を元に00の情報を更新
          itemWashPatternForm.push(this.createMapWashPatternFormArray(commonColorArray, itemWashPatternData, isCopy));
        } else {
          if (itemWashPatternList.length > 0) {
            // 4. APIから得た情報をマージ
            const color = uniqueColorCodeNameList.find((value) => value.colorCode === itemWashPatternData.colorCode);  // 色名を取得

            const apiColorArray = {
              colorCode: itemWashPatternData.colorCode,
              colorName: color.colorName,
            };
            itemWashPatternForm.push(this.createMapWashPatternFormArray(apiColorArray, itemWashPatternData, isCopy));
          }
        }
      } else {
        // 5. APIから得た情報に該当するカラーコードの情報が無い場合、
        //    チェックボックスを表示する必要があるため、カラーコードとカラー名を設定
        const isShowByColor = (val.get('colorCode').value === FukukitaruColorCommon.COLOR_CODE) ? true : false; // 00共通は必ずtrue
        const uniqueColorArray = {
          colorCode: val.get('colorCode').value,
          colorName: val.get('colorName').value,
        };
        itemWashPatternForm.push(this.createInitWashPatternFormArray(uniqueColorArray, isShowByColor));
      }
    });

    return itemWashPatternForm;
  }

  /**
   * データを設定して絵表示(洗濯ネーム)フォームを１つ作成して返す。
   * @param colorArray 色情報
   * @param isShow 表示フラグ
   * @returns 絵表示(洗濯ネーム)のFormGroup
   */
  private createInitWashPatternFormArray(colorArray: { colorCode, colorName }, isShow: boolean): FormGroup {
    return this.formBuilder.group({
      id: null,
      washPatternId: null,
      colorCode: colorArray.colorCode,
      colorName: colorArray.colorName,
      showWashPattern: isShow,
      washPatternCode: null,
      washPatternName: null
    });
  }

  /**
   * データを設定して絵表示(洗濯ネーム)フォームを１つ作成して返す。
   * @param colorArray 色情報
   * @param washPatternData APIから返ってきたデータ
   * @returns 絵表示(洗濯ネーム)のFormGroup
   */
  private createMapWashPatternFormArray(
    colorArray: { colorCode, colorName },
    washPatternData: FukukitaruItemWashPattern,
    isCopy: boolean = false
  ): FormGroup {
    return this.formBuilder.group({
      id: isCopy ? null : washPatternData.id,
      washPatternId: washPatternData.washPatternId,
      colorCode: colorArray.colorCode,
      colorName: colorArray.colorName,
      showWashPattern: true,
      washPatternCode: washPatternData.washPatternCode,
      washPatternName: washPatternData.washPatternName
    });
  }

  /**
   * 共通含めた全色の洗濯ネーム 付記用語のリストを作成して返す。(共通含めた全色の絵表示)。
   * 洗濯ネーム 付記用語になくてもsku(色・サイズ)リストにあれば、その色の洗濯ネーム 付記用語を作成する。※各色の洗濯ネーム 付記用語の入力値は空
   * 洗濯ネーム 付記用語、sku(色・サイズ)リストともにデータがない場合は洗濯ネーム 付記用語に共通を設定する。
   * @param list 洗濯ネーム 付記用語リスト
   * @param skuList sku(色・サイズ)リスト
   * @param isCopy コピー新規フラグ(任意)
   * @returns 色別の洗濯ネーム 付記用語リスト
   */
  private convertFukukitaruAppendicesTermByColorList(
    list: FukukitaruItemWashAppendicesTerm[] | FukukitaruItemAttentionAppendicesTerm[],
    skuList: Sku[],
    isCopy: boolean = false
  ): FukukitaruAppendicesTermByColor[] {
    // 配列がnullの場合、空の配列を返却する
    const washAppendicesTermList = ListUtils.isEmpty(list) ? [] : list;

    // 色コードのリストから、全色の洗濯ネーム 付記用語のリストを取得
    return this.convertSkuListToColorList(skuList).map(color => {
      return {
        colorCode: color.colorCode,
        colorName: color.colorName,
        appendicesTermList: washAppendicesTermList
          .filter((appendicesTerm) => (appendicesTerm.colorCode === color.colorCode))
          .map((appendicesTerm) => {
            return {
              id: isCopy ? null : appendicesTerm.id,
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
  private convertSkuListToColorList(skuList: Sku[]): any {
    // 共通を追加した色リストの作成
    const colorList = [
      {
        colorCode: CompositionsCommon.COLOR_CODE,
        colorName: CompositionsCommon.COLOR_NAME,
      }];

    // sku(色・サイズ)リストから、色コードの重複除去
    skuList.forEach((sku) => {
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
   * データを設定して組成(混率)フォームを１つ作成して返す。
   * @param composition 組成(混率)情報
   * @param isCopy コピー新規フラグ(任意)
   * @returns 組成(混率)のFormGroup
   */
  private createCompositionFormGroupSettedValues(composition: Compositions, isCopy: boolean = false): FormGroup {
    return this.formBuilder.group({
      id: [isCopy ? null : composition.id], // コピー新規の場合は新規採番する為null
      colorCode: [composition.colorCode],
      partsCode: [composition.partsCode],
      partsName: [composition.partsName],
      compositionCode: [composition.compositionCode],
      compositionName: [composition.compositionName],
      percent: [composition.percent, [Validators.pattern(ValidatorsPattern.PERCENT)]]
    });
  }

  /**
   * 組成(混率)チェックボックス入力時の処理
   * @param checkedColorCode チェックされた色のコード
   * @param checked チェックon/off
   */
  onCheckComposition(checkedColorCode: string, checked: boolean): void {
    if (checked) {
      // onの場合は組成(混率)明細の入力値に共通の入力値をコピーペースト
      this.copyCommonCompositionsDetailInputValue(checkedColorCode);
      return;
    }
    // offの場合は組成(混率)明細の入力値を初期化
    this.initCompositionsDetailInputValue(checkedColorCode);
  }

  /**
   * 指定した色コードの組成(混率)明細入力値に、共通の入力値をコピーペーストしてFormを表示する。
   * @param targetColorCode 初期化対象の色コード
   */
  private copyCommonCompositionsDetailInputValue(targetColorCode: string): void {

    // 共通の組成(混率)入力値を抽出
    const commonCompositonDetailValues =
      this.fValCompositions.find(composition => composition.colorCode === CompositionsCommon.COLOR_CODE).compositionDetailList;

    // チェックした色の組成(混率)入力値に共通の組成(混率)入力値をペースト
    this.fCtrlCompositions.some(composition => {
      if (composition.get('colorCode').value === targetColorCode) {
        const compositionDetailFormArray = composition.get('compositionDetailList') as FormArray;
        compositionDetailFormArray.controls.forEach((compositionDetail, index) => {
          compositionDetail.patchValue({
            partsCode: commonCompositonDetailValues[index].partsCode,
            partsName: commonCompositonDetailValues[index].partsName,
            compositionCode: commonCompositonDetailValues[index].compositionCode,
            compositionName: commonCompositonDetailValues[index].compositionName,
            percent: commonCompositonDetailValues[index].percent
          });
        });
        // 組成(混率)明細Formを表示
        composition.patchValue({ showCompositions: true });
        return true;
      }
    });
  }

  /**
   * 指定した色コードの組成(混率)明細入力値を初期化してFormを非表示にする。
   * @param targetColorCode 初期化対象の色コード
   */
  private initCompositionsDetailInputValue(targetColorCode: string): void {
    this.fCtrlCompositions.some(composition => {
      if (composition.get('colorCode').value === targetColorCode) {
        const compositionDetailFormArray = composition.get('compositionDetailList') as FormArray;
        // 明細全行初期化
        compositionDetailFormArray.controls.forEach(compositionDetail => {
          compositionDetail.patchValue({
            partsCode: '',
            partsName: '',
            compositionCode: '',
            compositionName: '',
            percent: ''
          });
        });
        // 組成(混率)明細Formを非表示
        composition.patchValue({ showCompositions: false });
        return true;
      }
    });
  }

  /**
   * 絵表示プルダウン変更時の処理
   */
  onChangeWashPattern(): void {
    // 絵表示の入力値を取得
    const washPatternList = this.mainForm.controls.fkItem.get('washPatterns')['controls'];

    washPatternList.forEach(val => {
      // フクキタルのマスタ情報を取得
      const masterInfo = this.fukukitaruMaster.listWashPattern.find(masterVal => masterVal.id === Number(val.value.washPatternId));
      // 空白を選択するとマスタ情報取得不可のため、空白を選択した場合は''を設定。
      // コードを選択した場合はマスタ情報のコードを設定。
      const washPatternCode = ObjectUtils.isNullOrUndefined(masterInfo) ? '' : masterInfo.code;
      val.patchValue({ washPatternCode: washPatternCode });
    });
  }

  /**
   * 絵表示チェックボックス入力時の処理
   * @param checkedColorCode チェックされた色のコード
   * @param checked チェックon/off
   */
  onCheckWashPattern(checkedColorCode: string, checked: boolean): void {
    if (checked) {
      // onの場合は絵表示の入力値に共通の入力値をコピーペースト
      this.copyCommonWashPatternsDetailInputValue(checkedColorCode);
      return;
    }
    // offの場合は絵表示の入力値を初期化
    this.initWashPatternInputValue(checkedColorCode);
  }

  /**
   * 指定した色コードのFormを表示する。
   * @param targetColorCode 初期化対象の色コード
   */
  private copyCommonWashPatternsDetailInputValue(targetColorCode: string): void {
    // 共通の絵表示入力値を抽出
    let washPatternValues = [];
    this.fCtrlWashPatterns.some(washPattern => {
      if (washPattern.get('colorCode').value === FukukitaruColorCommon.COLOR_CODE) {
        washPatternValues = [{
          washPatternCode: washPattern.get('washPatternCode').value,
          washPatternId: washPattern.get('washPatternId').value,
          washPatternName: washPattern.get('washPatternId').value,
        }];
        return true;
      }
    });

    this.fCtrlWashPatterns.some((washPattern: FormGroup) => {
      if (washPattern.getRawValue()['colorCode'] === targetColorCode) {
        // 表示する色に共通を設定
        washPattern.patchValue({ washPatternCode: washPatternValues[0].washPatternCode });
        washPattern.patchValue({ showWashPattern: true });  // 絵表示Formを表示する
        washPattern.patchValue({ washPatternId: washPatternValues[0].washPatternId });
        washPattern.patchValue({ washPatternName: washPatternValues[0].washPatternName });
        return true;
      }
    });
  }

  /**
   * 指定した色コードの絵表示入力値を初期化してFormを非表示にする。
   * @param targetColorCode 初期化対象の色コード
   */
  private initWashPatternInputValue(targetColorCode: string): void {
    this.fCtrlWashPatterns.some((washPattern: FormGroup) => {
      if (washPattern.getRawValue()['colorCode'] === targetColorCode) {
        // 表示する色に共通の絵表示入力値を設定
        washPattern.patchValue({ washPatternCode: '' });
        washPattern.patchValue({ showWashPattern: false });  // 絵表示Formを非表示する
        washPattern.patchValue({ washPatternId: null });
        washPattern.patchValue({ washPatternName: '' });
        return true;
      }
    });
  }

  /**
   * 色コードのみ設定して組成(混率)のFormGroupを1つ作成して返す。
   * @param colorCode 色コード
   * @returns 組成(混率)のFormGroup
   */
  private createCompositionFormGroupSettedColorCode(colorCode: string): FormGroup {
    return this.formBuilder.group({
      id: [''],
      colorCode: [colorCode],
      partsCode: [''],
      partsName: [''],
      compositionCode: [''],
      compositionName: [''],
      percent: ['', [Validators.pattern(ValidatorsPattern.PERCENT)]],
    });
  }

  /**
   * テープ種類プルダウン変更時の処理
   */
  onChangeTape(): void {
    const tapeCode = this.mainForm.controls.fkItem.get('tapeCode').value; // テープ種類のテーブルマスタid取得
    const masterInfo = this.fukukitaruMaster.listTapeType.find(masterVal => masterVal.id === Number(tapeCode)); // マスタ情報取得
    // コードを設定
    this.mainForm.controls.fkItem.patchValue({
      tapeName: ObjectUtils.isNotNullAndNotUndefined(masterInfo) ? masterInfo.code : ''
    });
  }

  /**
   * テープ巾プルダウン変更時の処理
   */
  onChangeTapeWidth(): void {
    const tapeWidthCode = this.mainForm.controls.fkItem.get('tapeWidthCode').value; // テープ巾のテーブルマスタid取得
    const masterInfo = this.fukukitaruMaster.listTapeWidth.find(masterVal => masterVal.id === Number(tapeWidthCode)); // マスタ情報取得
    // コードを設定
    this.mainForm.controls.fkItem.patchValue({
      tapeWidthName: ObjectUtils.isNotNullAndNotUndefined(masterInfo) ? masterInfo.code : ''
    });
  }

  /**
   * 組成(混率)のフォーム(組成詳細の値はデフォルト値)を作成して返す。
   * @param colorInfo 色情報
   * @returns 組成(混率)のFormGroup
   */
  private createCompositionDefaultForm(colorInfo: Sku): FormGroup {
    const compositionDetailFormArray = new FormArray([]);

    for (let i = 0; i < this.COMPOSITIONS_MAX_COUNT; i++) {
      compositionDetailFormArray.push(this.createCompositionFormGroupSettedColorCode(colorInfo.colorCode));
    }
    const colorForm = this.formBuilder.group({
      colorCode: [colorInfo.colorCode],
      colorName: [colorInfo.colorName],
      showCompositions: false,
      compositionDetailList: compositionDetailFormArray
    });
    return colorForm;
  }


  /**
   * 絵表示のフォームを作成して返す。
   * @param colorInfo 色情報
   * @returns 絵表示のFormGroup
   */
  private createWashPatternDefaultForm(colorInfo: Sku): FormGroup {
    const colorForm = this.formBuilder.group({
      colorCode: [colorInfo.colorCode],
      colorName: [colorInfo.colorName],
      showWashPattern: false,
      washPatternCode: null,
      washPatternId: null
    });
    return colorForm;
  }

  /**
   * 組成(混率)、フクキタル絵表示(洗濯ネーム)Formを変更する。
   * @param colorInfo 新たに入力された色情報
   */
  changeCompositon(colorInfo: Sku): void {
    const isSameColor = this.fCtrlCompositions.some(composition => {
      if (composition.get('colorCode').value === colorInfo.colorCode) {
        // 同じcodeのものがあったら書き換え
        composition.patchValue({ colorCode: colorInfo.colorCode });
        composition.patchValue({ colorName: colorInfo.colorName });
        // 組成(混率)リストの中のカラーコードも全て書き換え
        const compositionDetailFormArray = composition.get('compositionDetailList') as FormArray;
        compositionDetailFormArray.controls.forEach(compositionDetail => compositionDetail.patchValue({ colorCode: colorInfo.colorCode }));
        return true;
      }
    });

    if (!isSameColor) {
      // 新しい色情報が追加される場合、1要素分追加
      this.compositionsFormArray.push(this.createCompositionDefaultForm(colorInfo));
    }

    // sku(色・コード)のFormにない色の組成(混率)Formを削除する。
    this.deleteCompositonNotExistsSkuForm();

    // 絵表示(洗濯ネーム)
    const skuWashPattern = this.fCtrlWashPatterns.some(washPattern => {
      if (washPattern.get('colorCode').value === colorInfo.colorCode) {
        // 同じcodeのものがあったら書き換え
        washPattern.patchValue({ colorCode: colorInfo.colorCode });
        washPattern.patchValue({ colorName: colorInfo.colorName });
        return true;
      }
    });

    if (!skuWashPattern) {
      // 新しい色情報が追加される場合、1要素分追加
      this.formWashPatterns.push(this.createWashPatternDefaultForm(colorInfo));
    }
    // sku(色・コード)のFormにない色の絵表示Formを削除する。
    this.deleteWashPatternNotExistsSkuForm();

    this.createAppendicesTerm(colorInfo, 'washAppendicesTermByColorList');
    this.deleteAppendicesTerm('washAppendicesTermByColorList');

    this.createAppendicesTerm(colorInfo, 'attentionTagAppendicesTermByColorList');
    this.deleteAppendicesTerm('attentionTagAppendicesTermByColorList');
  }

  /**
   * SKUのFormにない色の組成(混率)Formを削除する。
   * ※共通は消さない。
   */
  deleteComposition(): void {
    this.deleteCompositonNotExistsSkuForm();
    this.deleteWashPatternNotExistsSkuForm();
    this.deleteAppendicesTerm('washAppendicesTermByColorList');
    this.deleteAppendicesTerm('attentionTagAppendicesTermByColorList');
  }

  /**
   * SKUのFormにない色の組成(混率)Formを作成する。
   * ※共通は消さない。
   */
  createAppendicesTerm(colorInfo: Sku, target: string): void {
    const list = this.mainForm.getRawValue()['fkItem'][target];
    const appendicesTerm = list.find((value) => value.colorCode === colorInfo.colorCode);
    if (appendicesTerm != null) {
      appendicesTerm.colorName = colorInfo.colorName;
    } else {
      list.push({
        colorCode: colorInfo.colorCode,
        colorName: colorInfo.colorName,
        appendicesTermList: []
      });
    }
  }

  /**
   * SKUのFormにない色の組成(混率)Formを削除する。
   * ※共通は消さない。
   */
  deleteCompositonNotExistsSkuForm(): void {
    this.fCtrlCompositions.some((compositon, index, array) => {
      const compositonColorCode: string = compositon.get('colorCode').value;
      if (compositonColorCode === CompositionsCommon.COLOR_CODE) {
        return false; // 共通は消さない。skip
      }

      if (!this.fCtrlSkus.some(sku => compositonColorCode === sku.get('colorCode').value)) {
        array.splice(index, 1);
        return true;  // loop end
      }
    });
  }

  /**
   * sku(色・コード)のFormにない色の絵表示Formを削除する。
   * ※共通は消さない。
   */
  deleteWashPatternNotExistsSkuForm(): void {
    this.fCtrlWashPatterns.some((washPattern, index, array) => {
      const washPatternColorCode = washPattern.get('colorCode').value;
      if (washPatternColorCode === FukukitaruColorCommon.COLOR_CODE) {
        return false; // 共通は消さない。skip
      }
      const existsColorCode = this.fCtrlSkus.some(sku => {
        return washPatternColorCode === sku.get('colorCode').value;
      });
      if (!existsColorCode) {
        array.splice(index, 1);
        return true;  // loop end
      }
    });
  }

  /**
   * sku(色・コード)のFormにない色の絵表示Formを削除する。
   * ※共通は消さない。
   */
  deleteAppendicesTerm(target: string): void {
    const appendicesTerm = this.mainForm.getRawValue()['fkItem'][target];
    appendicesTerm.some((term, index, array) => {
      if (term.colorCode === FukukitaruColorCommon.COLOR_CODE) {
        return false;
      }
      if (!this.fCtrlSkus.some((sku) => term.colorCode === sku.get('colorCode').value)) {
        array.splice(index, 1);
        return true;
      }
    });
  }

  /**
   * 画面表示モード切り替え処理
   * @param viewMode 切り替え後の画面表示モード
   */
  onViewChange(viewMode: number): void {
    // 商品の新規登録、または現在のViewModeと同じViweModeへの切替が発生する時は切り替えを行わない。
    if (this.viewMode === ViewMode.ITEM_NEW || this.viewMode === viewMode) { return; }

    this.viewMode = viewMode;
    this.submitted = false;

    // 品種に紐づく各種マスタデータ取得
    this.getMasterDataByPartNoKind(viewMode, BusinessUtils.splitPartNoKind(this.mainForm.controls.partNoKind.value));
    this.getMaterial(); // 素材取得

    switch (viewMode) {
      case ViewMode.PART_EDIT:
        // 品番入力モードのみのバリデーションリセット
        this.resetPartNoModeValidationError();
        break;
      case ViewMode.ITEM_NEW:
      case ViewMode.ITEM_EDIT:
        // 商品入力モードのみのバリデーションリセット
        this.resetItemModeValidationError();
        break;
      default:
        break;
    }
    this.mainForm.patchValue({ formViewMode: viewMode });
  }

  /**
   * 品番入力モード限定のバリデーションエラーリセット&必須のバリデーションを再セット処理.
   * 残存しているエラーは切り替え時に再検査される.
   */
  private resetPartNoModeValidationError(): void {
    this.clearErrorMessage();
    this.clearValidationError();

    // 上代
    this.setValidation(<FormGroup> this.f.retailPrice, [Validators.required, Validators.pattern(/^[0-9,]*$/)]);
    // 原価
    this.setValidation(<FormGroup> this.f.otherCost, [Validators.required, Validators.pattern(/^[0-9,]*$/)]);
    //PRD_0118-05 add JFE Start
    // 生地原価
    this.setValidation(<FormGroup> this.f.matlCost, [Validators.pattern(/^[0-9,]*$/)]);
    // 加工原価
    this.setValidation(<FormGroup> this.f.processingCost, [Validators.pattern(/^[0-9,]*$/)]);
    // 附属原価
    this.setValidation(<FormGroup> this.f.accessoriesCost, [Validators.pattern(/^[0-9,]*$/)]);
    //PRD_0118-05 add JFE END
    // 投入日
    this.setValidation(<FormGroup> this.f.deploymentDate, [Validators.required]);
    // 投入週
    this.setValidation(<FormGroup> this.f.deploymentWeek, [Validators.required]);
    // P終了日
    this.setValidation(<FormGroup> this.f.pendDate, [Validators.required]);
    // P終了週
    this.setValidation(<FormGroup> this.f.pendWeek, [Validators.required]);
    // 原産国コード
    this.setValidation(<FormGroup> this.f.cooCode, [Validators.required]);
    // 原産国名
    this.setValidation(<FormGroup> this.f.cooName, [Validators.required]);
    // パターンNo
    this.setValidation(<FormGroup> this.f.patternNo, [Validators.pattern(/^[a-zA-Z0-9-?]*$/)]);
    // メーカー品番
    this.setValidation(<FormGroup> this.f.makerGarmentNo, [Validators.pattern(/^[a-zA-Z0-9]*$/)]);
  }

  /**
   * 商品情報モード(商品更新 or 品番更新)の時のエラー初期化とバリデーションの再セット
   */
  private resetItemModeValidationError(): void {
    this.clearErrorMessage();
    this.clearValidationError();

    // 丸井品番
    this.setValidation(<FormGroup> this.f.maruiGarmentNo);

    // 投入日、投入週、P終了日、P終了週
    // 商品情報登録時は表示しない為、入力エラーがある場合は全て入力値をクリアする
    const errors = this.mainForm.errors;
    const isDeploymentWeekLessOrEqualsPendWeekErrors = errors ? errors.deploymentWeekLessOrEqualsPendWeek : null;
    const isDeploymentWeekNumberCorrectErrors = errors ? errors.deploymentWeekNumberCorrect : null;
    const isPendWeekNumberCorrectErrors = errors ? errors.pendWeekNumberCorrect : null;
    if (this.f.deploymentDate.invalid
      || this.f.deploymentWeek.invalid
      || this.f.pendDate.invalid
      || this.f.pendWeek.invalid
      || isDeploymentWeekLessOrEqualsPendWeekErrors
      || isDeploymentWeekNumberCorrectErrors
      || isPendWeekNumberCorrectErrors
    ) {
      this.mainForm.patchValue({ deploymentDate: null, deploymentWeek: null, pendDate: null, pendWeek: null });
    }
    this.setValidation(<FormGroup> this.f.deploymentDate);
    this.setValidation(<FormGroup> this.f.deploymentWeek);
    this.setValidation(<FormGroup> this.f.pendDate);
    this.setValidation(<FormGroup> this.f.pendWeek);

    // 登録ステータスが未設定または商品登録の場合は、必須を外す。
    if (this.mainForm.controls.registStatus.value === null || this.mainForm.controls.registStatus.value === RegistStatus.ITEM) {
      // 上代
      this.setValidation(<FormGroup> this.f.retailPrice, [Validators.pattern(/^[0-9,]*$/)]);
      // 原価
      this.setValidation(<FormGroup> this.f.otherCost, [Validators.pattern(/^[0-9,]*$/)]);
      // 原産国コード
      this.setValidation(<FormGroup> this.f.cooCode, null);
      // 原産国名
      this.setValidation(<FormGroup> this.f.cooName, null);
    }
  }

  /**
   * バリデーションエラーをクリアする.
   */
  private clearValidationError(): void {
    this.f.retailPrice.setErrors(null);     // 上代
    this.f.otherCost.setErrors(null);       // 原価
    //PRD_0118-06 add JFE Start
    this.f.matlCost.setErrors(null);       // 生地原価
    this.f.processingCost.setErrors(null);       // 加工原価
    this.f.accessoriesCost.setErrors(null);       // 附属原価
    //PRD_0118-06 add JFE END
    this.f.deploymentDate.setErrors(null);  // 投入日
    this.f.deploymentWeek.setErrors(null);  // 投入週
    this.f.pendDate.setErrors(null);        // P終了日
    this.f.pendWeek.setErrors(null);        // P終了週
    this.f.cooCode.setErrors(null);         // 原産国コード
    this.f.cooName.setErrors(null);         // 原産国名
    this.f.patternNo.setErrors(null);       // パターンNo
    this.f.makerGarmentNo.setErrors(null);  // メーカー品番
    this.f.maruiGarmentNo.setErrors(null);  // 丸井品番
  }

  /**
   * 必須と入力パターンのバリデーションを設定する。
   * @param control FormGroup
   * @param validators ValidatorFn[]
   */
  private setValidation(control: FormGroup, validators?: ValidatorFn[]): void {
    control.clearValidators();
    if (validators) { control.setValidators(validators); }
  }

  /**
   * 登録処理
   * @param nextMode 登録後の画面表示モード
   */
  private onSubmitRegister(nextMode: number): void {
    let loadingToken = null;
    this.apiValidateErrorsMap.clear(); // APIバリデーションエラーのクリア

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearAllMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid(this.viewMode)),
      // 登録データの取得
      map(() => {
        // mainformの値を取得する
        const formValue = this.mainForm.getRawValue();

        // フクキタル用 絵表示  画面表示用の配列からmodelの形に変換
        formValue.fkItem.listItemWashPattern = this.formatingWashPattern(formValue.fkItem.washPatterns);
        // フクキタル用 付記用語 二次元配列から一次元に変換
        formValue.fkItem.listItemWashAppendicesTerm = this.reduceDimension(formValue.fkItem.washAppendicesTermByColorList);
        formValue.fkItem.listItemAttentionAppendicesTerm = this.reduceDimension(formValue.fkItem.attentionTagAppendicesTermByColorList);

        // モードを登録(商品で登録)
        formValue.registStatus = RegistStatus.ITEM;

        // ファイル情報を追加する
        formValue.itemFileInfos = this.itemFileInfoList.concat(this.estimatesFile);
        return formValue;
      }),
      // 商品登録
      flatMap((formValue) => this.itemService.postItem(formValue)),
      tap((response) => {
        this.setSubmitSuccessMessage('SUCSESS.ITEM_ENTRY');
        this.f.id.setValue(response.id); // 品番IDを保持しておく
        if (nextMode === ViewMode.ITEM_EDIT || nextMode === ViewMode.PART_EDIT) { this.viewMode = nextMode; }
        // 新規登録後の更新画面表示
        this.router.navigate(['items', response.id, Path.EDIT], { queryParams: { preEvent: PreEventParam.CREATE, viewMode: nextMode } });
      }),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.handleCatchedError(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * フクキタル用 絵表示  画面表示用の配列からmodelの形に変換
   * @param itemWashPatternList 画面表示用のリスト
   */
  private formatingWashPattern(itemWashPatternList: FukukitaruWashPatternInterface[]) {
    let submitWashPatterns: FukukitaruWashPatternInterface[] = [];
    const result: FukukitaruItemWashPattern[] = [];

    submitWashPatterns = itemWashPatternList.filter(item => (item.showWashPattern === true) && (FormUtils.isNotEmpty(item.washPatternId)));

    submitWashPatterns.forEach(val => {
      result.push({
        id: val.id,
        washPatternId: NumberUtils.toInteger(val.washPatternId),
        colorCode: val.colorCode,
        washPatternCode: val.washPatternCode,
        washPatternName: val.washPatternName
      });
    });

    return result;
  }

  /**
   * フクキタル用 付記用語 二次元配列から一次元に変換
   * @param colorList 二次元配列色リスト
   */
  private reduceDimension(colorList: FukukitaruAppendicesTermByColor[]):
    FukukitaruItemWashAppendicesTerm[] | FukukitaruItemAttentionAppendicesTerm[] {

    const result: FukukitaruItemWashAppendicesTerm[] | FukukitaruItemAttentionAppendicesTerm[] = [];

    colorList.forEach((color) => {
      color.appendicesTermList.forEach((term) => {
        result.push({
          id: term.id,
          appendicesTermId: term.appendicesTermId,
          colorCode: color.colorCode,
          appendicesTermCode: term.appendicesTermCode,
          appendicesTermCodeName: term.appendicesTermCodeName
        });
      });
    });

    return result;
  }

  /**
   * API登録・更新・削除エラー処理
   *
   * ※削除処理などでまだ使用しているため、リファクタリングしない。いったん放置。
   * @param error エラー情報
   * @param id 品番ID(更新時・削除時のみ必要)
   */
  private handleApiError(error: HttpErrorResponse, id?: number): void {
    this.overallErrorMsgCode = 'ERRORS.ANY_ERROR';
    // httpエラーをErrorModelに変換
    const apiError = ExceptionUtils.generateError(error);
    if (apiError == null) {
      return;
    }
    apiError.errors.some(errorDetail => {
      switch (errorDetail.code) {
        case APIErrorCode.INVALID:  // バリデーションエラー
          return true; // loop終了
        case APIErrorCode.PART_NO_DUP_JUNOT:
        case APIErrorCode.PART_NO_DUP_EXISTING:
          // 品番重複チェックエラーは品番エラーエリアにメッセージ表示
          ExceptionUtils.displayErrorInfo('partNoErrorInfo', ('ERRORS.' + errorDetail.code));
          break;
        case APIErrorCode.NO_COO_CODE_EXISTING:
          // 原産国コード存在チェックエラーは原産国エラーエリアにメッセージ表示
          this.f.cooCode.setErrors({ 'required': true });
          break;
        case APIErrorCode.MAKER_EXISTENCE:
          // 仕入先コード存在チェックエラー
          this.mainForm.setErrors({ 'makerExistence': true });
          break;
        case APIErrorCode.MARUI_EXISTENCE:
          // 丸井品番存在チェックエラー
          this.mainForm.setErrors({ 'maruiGarmentNoExistence': true });
          break;
        case APIErrorCode.COO_CODE_REQUIRED:
          // 原産国コード必須エラー
          this.f.cooCode.setErrors({ 'required': true });
          this.f.cooName.setErrors({ 'required': true });
          break;
        case APIErrorCode.MARUI_REQUIRED:
          // 丸井品番必須エラー
          this.f.maruiGarmentNo.setErrors({ 'required': true });
          break;
        // 以下4つは更新時のみ
        case APIErrorCode.NOT_CHANGE_TO_PART:       // 品番昇格済
        case APIErrorCode.REGIST_STATUS_UNMATCH:    // ステータスアンマッチ
        case APIErrorCode.ACTIVE_ORDER_REGISTERED:  // 発注中
        case APIErrorCode.NO_CHANGE_ITEM:           // 変更不可項目エラー
        // 削除時のみ
        case APIErrorCode.NO_DELITED:                  // 削除不可エラー
          ExceptionUtils.displayErrorInfo('defaultErrorInfo', ('ERRORS.' + errorDetail.code));
          // 情報再取得
          this.getInputData(id).then(() => {
            this.isInitDataSetted = true;
            this.loadingService.loadEnd();
          }, (itemError: HttpErrorResponse) => this.getItemErrorHandler(itemError));
          return true; // loop終了
        case APIErrorCode.REGISTERED_ORDER_NO_DELITED: // 受注登録済の為品番削除不可
          // 情報再取得
          this.getInputData(id).then(() => {
            this.isInitDataSetted = true;
            this.loadingService.loadEnd();
          }, (itemError: HttpErrorResponse) => this.getItemErrorHandler(itemError));
          return true; // loop終了
        default:
          // それ以外、resourceに値がある場合はapiValidateErrorsMapに詰め込む
          if (StringUtils.isNotEmpty(errorDetail.resource)) {
            this.generateApiValidateErrorsMap(errorDetail);
            this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
          } else {
            // それ以外はサーバエラーエリアにメッセージ表示
            ExceptionUtils.displayErrorInfo('defaultErrorInfo', ('ERRORS.' + errorDetail.code));
            this.isInitDataSetted = false;
            this.loadingService.loadEnd();
            return true; // loop終了
          }
          break;
      }
    });
    this.loadingService.loadEnd();
    this.isInitDataSetted = true;
    this.isBtnLock = false;
  }

  /**
   * Submit処理(登録・更新)でcatchError時の処理
   * @param error エラー情報
   * @returns 処理結果Observable<boolean>
   */
  private handleCatchedError(error: HttpErrorResponse): Observable<boolean> {
    // httpエラーをErrorModelに変換
    const apiError = ExceptionUtils.generateError(error);
    if (apiError == null) {
      return of(false);
    }
    if (apiError.errors == null) {
      // errorsがない場合はエラーモーダル表示
      return this.messageConfirmModalService.openErrorModal(error);
    }

    // APIバリデーション系エラー
    const apiValidationErrors = apiError.errors.filter(errorDetail => StringUtils.isNotEmpty(errorDetail.resource));
    // APIバリデーション系エラーが1件でもある場合は、画面上にエラーメッセージを表示
    if (ListUtils.isNotEmpty(apiValidationErrors)) {
      apiValidationErrors.forEach(errorDetail => {
        this.generateApiValidateErrorsMap(errorDetail);  // エラー詳細をapiValidateErrorsMapに詰め込む
        this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      });
      return of(true);
    } else {
      // APIバリデーション系エラーがない場合はエラーモーダル表示
      return this.messageConfirmModalService.openErrorModal(error);
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
   * バリデーションチェックを行う。
   * @param viewMode 画面表示モード
   * @return isValid バリデーションエラーあり: false、なし：true
   */
  private isValid(viewMode: number): boolean {
    let isValid = true;
    this.submitted = true;

    // バリデーションエラーの時に画面に戻す
    if (this.mainForm.invalid || this.mainForm.controls.fkItem.invalid) {
      console.debug('バリデーションエラー:', this.mainForm);
      // カテゴリーコードがエラーの場合、下札を表示する
      this.isBottomBillTitleCollapsed = this.formCategoryCode.invalid;
      this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      isValid = false;
    }

    // カスタムバリデーション:色・サイズ
    let allSkuMessageCode = '';
    const skuErrorMessageCodeList = this.itemService.validSkus(this.mainForm.getRawValue(), viewMode);

    if (ListUtils.isNotEmpty(skuErrorMessageCodeList)) {
      this.overallErrorMsgCode = 'ERRORS.VALID_ERROR';
      skuErrorMessageCodeList.forEach(skuMessageCode => allSkuMessageCode += skuMessageCode + '<br>');
      ExceptionUtils.displayErrorInfo('skuErrorInfo', allSkuMessageCode);
      isValid = false;
    }

    return isValid;
  }

  /**
   * 品番データ更新処理
   * @param registStatus 登録ステータス変更区分
   */
  private onSubmitUpdate(changeRegistStatus?: number): void {
    let loadingToken = null;
    this.apiValidateErrorsMap.clear(); // APIバリデーションエラーのクリア

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearAllMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid(this.viewMode)),
      // 更新データの整形
      map(() => this.setFormValueInUpdate(changeRegistStatus)),
      // 商品更新/品番登録/品番更新
      flatMap((formValue) => this.itemService.putItem(formValue)),
      tap(() => this.setUpdateSuccessMessage(changeRegistStatus)),
      // 更新後のURL書き換え
      tap(response => this.router.navigate(['items', response.id, Path.EDIT],
        { queryParams: { preEvent: PreEventParam.UPDATE, viewMode: this.viewMode, t: new Date().valueOf() } })
      ),
      // 画面再表示
      tap(() => this.submitted = false),
      // 再描画時に品種の変更を感知させる為、初期化する
      tap(() => this.partNoKindConfirm = null),
      // Formの初期化
      tap(() => this.createForm()),
      // 再描画で必要なデータ取得
      flatMap(response =>
        from(this.getInputData(response.id)).pipe(
          catchError(error => of(this.getItemErrorHandler(error))),
        )
      ),
      // SKU、JANの遅延ロードに対応するため、delayでローディング停止する
      delay(new Date(new Date().getTime() + 1000)),
      // loading消す
      tap(() => this.loadingService.stop(loadingToken)),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError(error => this.handleCatchedError(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 更新処理時、formの値を設定して返す。
   * @param changeRegistStatus 登録ステータス変更区分
   * @return formValue
   */
  private setFormValueInUpdate(changeRegistStatus?: number): any {
    const formValue = this.mainForm.getRawValue();  // disable項目の値も取得する
    formValue.orderSupplierInfos = this.mainForm.getRawValue()['orderSuppliers'];
    formValue.itemFileInfos = this.itemFileInfoList.concat(this.estimatesFile); // 品番ファイル情報を追加する(短冊＋見積)
    formValue.misleadingRepresentationFiles = this.misleadingRepresentationFile;  // 生地検査結果ファイル情報を追加する

    // バリデーション区分をセット
    if (this.viewMode === ViewMode.PART_EDIT || formValue.registStatus === RegistStatus.PART) {
      // 画面表示モードが品番編集 または 品番登録済の場合、品番バリデーションをセット
      formValue.validationType = ItemValidationType.PART;
    } else {
      // その他の場合、商品バリデーションをセット
      formValue.validationType = ItemValidationType.ITEM;
    }

    // 登録モードの編集
    if (changeRegistStatus !== null && changeRegistStatus === ChangeRegistStatusType.PART) {
      formValue.registStatus = RegistStatus.PART;
      formValue.changeRegistStatusType = ChangeRegistStatusType.PART;
      formValue.sample = false;
    }

    // フクキタル用 絵表示  画面表示用の配列からmodelの形に変換
    formValue.fkItem.listItemWashPattern = this.formatingWashPattern(formValue.fkItem.washPatterns);
    // フクキタル用 付記用語 二次元配列から一次元に変換
    formValue.fkItem.listItemWashAppendicesTerm = this.reduceDimension(formValue.fkItem.washAppendicesTermByColorList);
    formValue.fkItem.listItemAttentionAppendicesTerm = this.reduceDimension(formValue.fkItem.attentionTagAppendicesTermByColorList);

    return formValue;
  }

  /**
   * 更新成功メッセージ設定
   * @param changeRegistStatus 登録ステータス変更区分
   */
  private setUpdateSuccessMessage(changeRegistStatus?: number): void {
    // 渡す処理成功メッセージの分岐
    if (changeRegistStatus !== null && changeRegistStatus === ChangeRegistStatusType.PART) {
      // 品番登録
      this.setSubmitSuccessMessage('SUCSESS.PART_ENTRY');
      return;
    }

    if (this.mainForm.controls.registStatus.value === RegistStatus.PART) {
      // 品番更新
      this.setSubmitSuccessMessage('SUCSESS.PART_UPDATE');
      return;
    }

    // 商品更新
    this.setSubmitSuccessMessage('SUCSESS.ITEM_UPDATE');
  }

  /**
   * 商品情報を削除する。
   */
  onSubmitDelete(): void {
    this.clearAllMessage();
    this.apiValidateErrorsMap.clear(); // APIバリデーションエラーのクリア

    // 確認モーダルを表示
    const modalRef = this.modalService.open(MessageConfirmModalComponent);
    this.translate.get('INFO.ITEM_DELETE_CONFIRM_MESSAGE').subscribe((msg: string) => modalRef.componentInstance.message = msg);

    // 確認モーダルのメッセージの戻り値を確認して、削除処理を行う。
    modalRef.result.then((result: string) => {
      if (result === 'OK') {
        this.isBtnLock = true;
        this.loadingService.loadStart();
        const id = this.mainForm.get('id').value as number;
        this.itemService.deleteItem(id).subscribe(item => {
          console.debug('deleteItem:', item);
          this.setSubmitSuccessMessage('SUCSESS.ITEM_DELTE');
          this.loadingService.loadEnd();
          this.isBtnLock = false;
          // フォームの初期化
          this.router.navigate(['items']);
        }, (error: HttpErrorResponse) => this.handleApiError(error, id));
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * トランザクションデータを取得する。
   * @param id 品番ID
   * @param isCopy コピー新規フラグ(任意)
   * @returns 取得データ。失敗時はエラー情報
   */
  private async getInputData(id: number, isCopy: boolean = false): Promise<void> {
    // コピー新規の場合はコピー元データを取得する
    const item = await this.itemService.getItemForId(id).toPromise();

    if (isCopy) {
      // コピー新規の場合、コピーする項目以外を初期化する
      this.copyItemOtherThanInitialize(item);
    }

    this.itemData = item;
    this.skusValue = item.skus;
    this.orderDataList = item.orders;
    this.setFileAndQuality(item, isCopy);

    this.registStatus = isCopy ? RegistStatus.ITEM : item.registStatus;

    this.patchValuesToMainForm(item, this.registStatus, isCopy);
    this.setDisabled(this.registStatus, item);

    const splitPartNo = BusinessUtils.splitPartNo(item.partNo);
    const splitPartNoKind = BusinessUtils.splitPartNoKind(splitPartNo.partNoKind);

    // 品種に紐づく各種マスタデータ取得
    await this.getMasterDataByPartNoKind(this.viewMode, splitPartNoKind);

    this.setFukukitaruValidation(splitPartNoKind.blandCode); // フクキタルのバリデーションを設定
    this.getMasterDataByPartNoKind(this.viewMode, splitPartNoKind); // 品種に紐づく各種マスタデータ取得
    if (ListUtils.isNotEmpty(this.itemFileInfoList)) { await this.showAttachmentFile(); }  // 品番添付ファイルが存在すれば表示処理

    // 品番添付ファイルが存在すれば表示処理
    if (ListUtils.isNotEmpty(this.itemFileInfoList)) {
      this.showAttachmentFile();
    }
  }

  /**
   * コピーする項目以外を初期化する。
   * 後続処理でisCopyを使って初期化しているが、基本的には本処理で初期化すること。
   *
   * @param item 品番情報
   */
  private copyItemOtherThanInitialize(item: Item): void {
    // IDは、後続処理で初期化しているため本処理では初期化しない
    // item.id = null;
    // 品番は、後続処理で初期化しているため本処理では初期化しない
    // item.partNo = item.partNo.substring(0, 3);
    // 年度は、後続処理で初期化しているため本処理では初期化しない
    // item.year = null;
    // 発注先メーカーID(最新製品)は、後続処理でIDを使用しているため本処理では初期化しない
    // item.currentProductOrderSupplierId = null;
    // 登録ステータス
    item.registStatus = RegistStatus.ITEM;
    // 優良誤認区分
    item.misleadingRepresentation = null;
    // 優良誤認承認区分（組成）
    item.qualityCompositionStatus = null;
    // 優良誤認承認区分（国）
    item.qualityCooStatus = null;
    // 優良誤認承認区分（有害物質）
    item.qualityHarmfulStatus = null;
    // JAN区分は、後続処理で初期化しているため本処理では初期化しない
    // item.janType = JanType.IN_HOUSE_JAN;
    // 連携ステータス 変数定義時に''で初期化しているため、''で初期化する
    item.linkingStatus = '';
    // 連携日時
    item.linkedAt = null;
    // 登録日時
    item.createdAt = null;
    // 登録ユーザID
    item.createdUserId = null;
    // 更新日時
    item.updatedAt = null;
    // 登録ユーザID
    item.updatedUserId = null;
    // 発注先メーカー情報のリストは、後続処理でIDを使用しているため本処理では初期化しない
    // item.orderSuppliers.forEach(x => {
    //   // ID
    //   x.id = null;
    //   // 品番ID
    //   x.partNoId = null;
    // });
    // SKU情報は、後続処理でIDを初期化しているため本処理では初期化しない
    // item.skus.forEach(x => {
    //   // ID
    //   x.id = null;
    //   // 品番ID
    //   x.partNoId = null;
    //   // 品番
    //   x.partNo = null;
    //   // 代表JANフラグ
    //   x.representationJanFlg = false;
    // });
    // 組成情報は、後続処理でIDを初期化しているため本処理では初期化しない
    // item.compositions.forEach(x => {
    //   // ID
    //   x.id = null;
    //   // 品番ID
    //   x.partNoId = null;
    //   // 品番
    //   x.partNo = null;
    // });
    // 品番ファイル情報リスト
    item.itemFileInfos = [];
    // 発注情報のリスト
    item.orders = [];
    // 優良誤認検査ファイル情報リスト
    item.misleadingRepresentationFiles = [];
    // 登録ステータス変更区分
    item.changeRegistStatusType = null;
    // バリデーション区分
    item.validationType = null;
    // フクキタル品番情報は、後続処理でIDを初期化しているため本処理では初期化しない
    // item.fkItem
    // 読み取り専用
    item.readOnly = false;
    // 受注・発注登録済み
    item.registeredOrder = false;
    // 発注承認済み
    item.approvedOrder = false;
    // 全ての発注が完納
    item.completedAllOrder = false;
    // 納品依頼承認済み
    item.approvedDelivery = false;
    // 優良誤認承認済み
    item.approvedMisleadingRepresentation = false;
    // 優良誤認（組成）承認済みのカラーのリスト
    item.approvedColors = [];
  }

  /**
   * ファイルと優良誤認のステータスを設定
   * @param item 品番情報
   * @param isCopy コピー新規作成フラグ
   */
  private setFileAndQuality(item: Item, isCopy: boolean): void {
    // コピー新規作成時は設定しない
    if (isCopy) { return; }

    this.setItemFileInfos(item.itemFileInfos);
    this.setMisleadingRepresentationFiles(item.misleadingRepresentationFiles);
    this.linkingStatus = item.linkingStatus;
    this.qualityCompositionStatus = item.qualityCompositionStatus;
    this.qualityCooStatus = item.qualityCooStatus;
    this.qualityHarmfulStatus = item.qualityHarmfulStatus;
  }

  /**
   * 発注先メーカー情報を設定する
   * @param orderSuppliers 発注先メーカー情報
   */
  private setInitOrderSuppliersForm(): FormGroup {
    const supplierFormGroup = this.formBuilder.group({
      id: '',
      partNoId: '',
      supplierCode: '',
      supplierName: '',
      supplierFactoryCode: '',
      supplierFactoryName: '',
      consignmentFactory: '',
    });
    return supplierFormGroup;
  }

  /**
   * 発注先メーカーID(最新製品)を表示するformに設定する.
   * ※更新、またはコピー新規の場合のみ呼ばれる関数
   * @param item 品番情報
   */
  private setCurrentSupplierIndex(item: Item): void {
    if (this.affiliation === AuthType.AUTH_INTERNAL) {
      // JUN権限の場合、発注先メーカーID(最新製品)が一致するインデックスを取得
      this.currentSupplierIndex =
        item.orderSuppliers.findIndex(
          value => item.currentProductOrderSupplierId === value.id);
    } else {
      // メーカー権限の場合、会社コードが一致するインデックスを取得
      this.currentSupplierIndex =
        item.orderSuppliers.findIndex(
          value => this.company === value.supplierCode);
    }
  }
  //PRD_0122 #7364 add JFE start
  /**
   * 発注先メーカーID(最新製品)を表示するformに設定する.
   * コピー新規の場合のみ呼ばれる関数
   * @param orderSuppliers 最新の発注先メーカー情報
   */
   private setSupplierIndexToMakerCode(orderSuppliers: OrderSupplier[]): void {
    let foundMakerCode = false;
    if (this.affiliation === AuthType.AUTH_INTERNAL) {
      // JUN権限の場合、発注先メーカーコード(最新製品)が一致するインデックスを取得
      orderSuppliers.forEach((value, index) => {
        if (value.id === this.itemData.currentProductOrderSupplierId) {
          this.currentSupplierIndex = index;
          foundMakerCode = true;
        }
      });
    } else {
      // メーカー権限の場合、会社コードが一致するインデックスを取得
      orderSuppliers.forEach((value, index) => {
        if (this.company === value.supplierCode) {
          this.currentSupplierIndex = index;
          foundMakerCode = true;
        }
      });
    }
    //見つけられなかったらIndexを0にする。
    if (foundMakerCode == false) {
      this.currentSupplierIndex = 0;
    }
  }
  //PRD_0122 #7364 add JFE end

  /**
   * ※更新、またはコピー新規の場合のみ呼ばれる関数
   * @param orderSuppliers 発注先生産メーカーリスト
   * @param isCopy コピー新規フラグ
   * @returns 発注先生産メーカーFormArray
   */

  private generateOrderSupplierFA(orderSuppliers: OrderSupplier[], isCopy = false): FormArray {
    /** PRD_0116 #7364 add JFE start */
    if (isCopy == true) {
      orderSuppliers = orderSuppliers.slice(-1)
      //PRD_0122  #7364 add JFE start　
      this.setSupplierIndexToMakerCode(orderSuppliers)
      //PRD_0122  #7364 add JFE end
    }
    /** PRD_0116 #7364 add JFE end */
    return this.formBuilder.array(
      orderSuppliers.map(orderSupplier =>
        this.formBuilder.group({
          id: isCopy ? null : orderSupplier.id,
          partNoId: orderSupplier.partNoId,
          supplierCode: orderSupplier.supplierCode,
          supplierName: orderSupplier.supplierName,
          supplierFactoryCode: orderSupplier.supplierFactoryCode,
          supplierFactoryName: orderSupplier.supplierFactoryName,
          consignmentFactory: orderSupplier.consignmentFactory,
        })
      ));
  }

  /**
   * 品番ファイル情報を設定する。
   * @param itemFileInfos 品番ファイル情報
   */
  private setItemFileInfos(itemFileInfos: ItemFileInfo[]): void {
    this.itemFileInfoList = [];
    if (itemFileInfos == null) { return; }
    itemFileInfos.forEach(fileInfo => {
      const fileInfoReq = new ItemFileInfoRequest();
      fileInfoReq.id = fileInfo.id;
      fileInfoReq.fileNoId = fileInfo.fileNoId;
      fileInfoReq.fileName = fileInfo.fileName;
      fileInfoReq.partNoId = fileInfo.partNoId;
      fileInfoReq.fileCategory = fileInfo.fileCategory;
      this.itemFileInfoList.push(fileInfoReq);
    });
  }

  /**
   * 優良誤認検査ファイル情報を設定する。
   * @param misleadingRepresentationFiles 優良誤認検査ファイル情報
   */
  private setMisleadingRepresentationFiles(misleadingRepresentationFiles: MisleadingRepresentationFile[]): void {
    this.misleadingRepresentationFile = [];
    if (misleadingRepresentationFiles == null) { return; }
    misleadingRepresentationFiles.forEach(misleadingRepresentationFile => {
      const misleadingRepresentationFileReq = new MisleadingRepresentationFileRequest();
      misleadingRepresentationFileReq.id = misleadingRepresentationFile.id;
      misleadingRepresentationFileReq.partNoId = misleadingRepresentationFile.partNoId;
      misleadingRepresentationFileReq.file = misleadingRepresentationFile.file;
      this.misleadingRepresentationFile.push(misleadingRepresentationFileReq);
    });
  }

  /**
   * 押された送信ボタンの種類を保持.
   * @param type 押された送信ボタンの種類
   */
  setSubmitType(type: string): void {
    this.submitType = type;
  }

  /**
   * FormのSubmitイベントをハンドリングする
   * 押されたボタンのnameによって、実行するFuctionの切り分けを行う。
   */
  submitHandler(): void {
    switch (this.submitType) {
      case SubmitType.ENTRY:            // 登録
        this.onSubmitRegister(ViewMode.ITEM_EDIT);
        break;
      case SubmitType.CONTINUE_TO_PART: // 続けて品番も登録
        this.onSubmitRegister(ViewMode.PART_EDIT);
        break;
      case SubmitType.UPDATE:           // 更新
        this.onSubmitUpdate();
        break;
      case SubmitType.UPDATE_TO_PART:   // 品番として登録
        this.onSubmitUpdate(ChangeRegistStatusType.PART);
        break;
    }
  }

  /**
   * 登録更新成功した時のメッセージを設定する。
   * @param messageCode メッセージコード
   */
  private setSubmitSuccessMessage(messageCode?: string): void {
    // nullの時は、デフォルトのメッセージを表示する
    this.overallSuccessMsgCode = messageCode != null ? messageCode : 'SUCSESS.ITEM_ENTRY';
  }

  /**
   * マスタ情報を取得する。(1度取得するだけで良いマスタ)
   */
  private getMasterData(): void {
    this.getOriginCountriesMaster();  // 原産国マスタを取得
    this.getCompMaster(); // 組成(混率)を取得
    // 品番更新時のみ必要
    if (this.viewMode === ViewMode.PART_EDIT) {
      this.getMaterial(); // 素材取得
    }
  }

  /**
   * フクキタルマスタを取得する。
   * @param splitPartNoKind 品種
   * @returns Observable<void>
   */
  private getFukukitaruMaster(splitPartNoKind: PartNoKind): Observable<void> {
    // 初期化
    this.fukukitaruMaster = null;
    this.materialOrderDisplayFlg = new MaterialOrderDisplayFlag(false);

    return this.screenSettingService.getFukukitaruItemList({
      listMasterType: this.LIST_MASTER_TYPE,
      partNoKind: splitPartNoKind.blandCode + splitPartNoKind.itemCode
    } as ScreenSettingFukukitaruOrderSearchCondition).pipe(map(
      data => {
        const fukukitaruMasterList = data.items;
        if (ListUtils.isEmpty(fukukitaruMasterList)) {
          // フクキタル対象外の場合は、false
          this.mainForm.patchValue({ fkAvailable: false });
          return;
        }

        // フクキタル対象の場合は、true
        this.mainForm.patchValue({ fkAvailable: true });

        this.fukukitaruMaster = fukukitaruMasterList[0];
        this.tapeTypeList = this.fukukitaruMaster.listTapeType;
        this.tapeWideList = this.fukukitaruMaster.listTapeWidth;
        this.washPatternList = this.fukukitaruMaster.listWashPattern;
        this.washNameAppendicesTermList = this.fukukitaruMaster.listWashNameAppendicesTerm;
        this.categoryCodeList = this.fukukitaruMaster.listCategoryCode;
        this.attentionTagAppendicesTermList = this.fukukitaruMaster.listAttentionTagAppendicesTerm;
        this.attentionSealTypeList = this.fukukitaruMaster.listAttentionSealType;
        this.recycleList = this.fukukitaruMaster.listRecycle;
        this.cnProductCategoryList = this.fukukitaruMaster.listCnProductCategory;
        this.cnProductTypeList = this.fukukitaruMaster.listCnProductType;

        // 資材発注(フクキタル)項目表示フラグを設定する
        this.generateMaterialOrderDisplayFlg();
        // フクキタル項目：カテゴリコード初期値の設定
        this.resetCategoryCodeForm();
      }, (error: HttpErrorResponse) => this.handleApiError(error)
    ));
  }

  /**
   * 資材発注(フクキタル)項目表示フラグを設定する.
   */
  private generateMaterialOrderDisplayFlg(): void {
    // カテゴリコードリストがある場合は、カテゴリコードを表示する
    if (ListUtils.isNotEmpty(this.categoryCodeList)) {
      this.materialOrderDisplayFlg.isDisplayCategoryCode = true;
    }

    // サスティナブルマーク印字
    this.materialOrderDisplayFlg.isDisplaySustainableMark = this.fukukitaruMaster.sustainableMarkDisplayFlg;

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
   * カテゴリコードのフォームを再設定する.
   * ・必須バリデーションの設定
   * ・初期値の設定
   */
  private resetCategoryCodeForm(): void {
    // フクキタル項目：カテゴリコードを表示する場合
    if (this.materialOrderDisplayFlg.isDisplayCategoryCode) {
      // カテゴリコード必須バリデーションセット
      const fkItemFormGroup = <FormGroup> this.f.fkItem;
      const categoryCodeFormGroup = <FormGroup> fkItemFormGroup.controls.categoryCode;
      this.setValidation(categoryCodeFormGroup, [CategoryCodeRequiredValidator]);

      // 「カテゴリコードなし」の選択肢がある かつ formに値がなければ初期値に「カテゴリコードなし」をセット
      const isNoCategoryCodeExist = this.categoryCodeList.some(catrgoryCode => catrgoryCode.id === CategoryCodeType.NO_CATEGORY_CODE);
      const categoryCode = this.f.fkItem.getRawValue()['categoryCode'];
      if (isNoCategoryCodeExist && FormUtils.isEmpty(categoryCode)) {
        this.f.fkItem.patchValue({ categoryCode: CategoryCodeType.NO_CATEGORY_CODE });
      }
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
        this.mainForm.controls.fkItem.setValidators(this.fkValidators);
        break;
      case FukukitaruBrandCode.VIS:
        // brand02：VIS
        this.mainForm.controls.fkItem.setValidators(this.fkValidators);
        break;
      case FukukitaruBrandCode.ADAM_ET_ROPE_LADIES:
      case FukukitaruBrandCode.ADAM_ET_ROPE_MENS:
        // brand03：アダム・エ・ロペ
        this.mainForm.controls.fkItem.setValidators(this.fkValidators);
        break;
      default:
        // common：上記以外のブランドは共通のバリデーションのみ設定
        this.mainForm.controls.fkItem.setValidators(this.fkValidators);
        break;
    }
  }

  /**
   * 原産国マスタを取得する.
   */
  private getOriginCountriesMaster(): void {
    if (ListUtils.isNotEmpty(this.cooMasterList)) { return; }
    // マスタの配列に設定
    this.junpcCodmstService.getOriginCountriesFromCache().subscribe(list => this.cooMasterList = list.items);
  }

  /**
   * 品種をキーに、サイズマスタを取得する。
   * @param partNoKind 品種
   */
  private generateSizeMaster(partNoKind: string): Observable<void> {
    const hscdParam = partNoKind.length === 2 ? partNoKind + 'M' : partNoKind;  // 品種2桁の場合はアイテムコード'M'で検索する
    return this.junpcSizmstService.getSizmst({ hscd: hscdParam } as JunpcSizmstSearchCondition).pipe(map(list => {
      const sizeMaster = list.items;
      if (ListUtils.isEmpty(sizeMaster)) {
        // サイズマスタが取得できなかった場合、品種の変更を感知させる為、初期化する
        this.partNoKindConfirm = null;
      } else {
        this.sizeMasterList = sizeMaster;
        // Service経由でサイズマスタ渡す
        this.itemDataService.sizeMasterList = sizeMaster;
        this.partNoKindConfirm = partNoKind; // 子Componentのイベントを駆動させ、サイズを取得してSKUFormを設定する
      }
    }));
  }

  /**
   * Voi区分を取得する。
   * @param brand ブランドコード
   * @retuns Observable<void>
   */
  private getVoi(brand: string): Observable<void> {
    this.voiMasterList = [];
    return this.junpcCodmstService.getVoiSections({ brand: brand } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getVoi:', list);
      this.voiMasterList = list.items;  // マスタの配列に設定
      // formControlの初期値に設定
      if (ListUtils.isEmpty(this.voiMasterList)) {
        this.mainForm.patchValue({ voiCode: null });
        return;
      }

      // 初期値設定判定
      if (this.isInitValueByCode1(this.voiMasterList, this.mainForm.controls.voiCode.value)) {
        // 初期値設定
        this.mainForm.patchValue({ voiCode: list.items[0].code1 });
      }
    }));
  }

  /**
   * 素材を取得する.
   */
  private getMaterial(): void {
    this.junpcCodmstService.getMaterialsFromCache().subscribe(list => {
      console.debug('getMaterial:', list);
      this.materialMasterList = list.items; // マスタの配列に設定
      // formControlの初期値に設定
      if (ListUtils.isEmpty(this.materialMasterList)) {
        this.mainForm.patchValue({ materialCode: null });
        return;
      }

      // 初期値設定判定
      if (this.isInitValueByCode1(this.materialMasterList, this.mainForm.controls.materialCode.value)) {
        // 初期値設定
        const initalIndex = this.itemService.getInitIndex(this.materialMasterList);
        this.mainForm.patchValue({
          materialCode: initalIndex == null ? this.materialMasterList[0].code1 : this.materialMasterList[initalIndex].code1
        });
      }
    });
  }

  /**
   * ゾーンを取得する。
   * @param brand ブランドコード
   * @returns Observable<void>
   */
  private getZone(brand: string): Observable<void> {
    this.zoneMasterList = [];
    return this.junpcCodmstService.getZones({ brand: brand } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getZone:', list);
      this.zoneMasterList = list.items; // マスタの配列に設定

      // 初期値設定判定
      if (this.isInitValueByCode1(this.zoneMasterList, this.mainForm.controls.zoneCode.value)) {
        // 初期値を設定する
        const initalIndex = this.itemService.getInitIndex(this.zoneMasterList);
        // 初期値に設定無ければ空文字を初期選択にする。
        this.mainForm.patchValue({
          zoneCode: initalIndex == null ? this.ZONE_DEFAULT_VALUE : this.zoneMasterList[initalIndex].code1
        });
      }
    }));
  }

  /**
   * サブブランドを取得する。
   * @param brand ブランドコード
   * @returns Observable<void>
   */
  private getSubBlandMaster(brand: string): Observable<void> {
    this.subBlandMaster = [];
    return this.junpcCodmstService.getSubBrands({ brand: brand } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getSubBlandMaster:', list);
      this.subBlandMaster = list.items; // マスタの配列に設定
      this.subBlandMaster.sort((val1, val2) => Number(val1.code2) < Number(val2.code2) ? -1 : 1);

      // 初期値設定判定
      if (this.isInitValueByCode2(this.subBlandMaster, this.mainForm.controls.subBrandCode.value)) {
        // 初期値設定
        const initalIndex = this.itemService.getInitIndex(this.subBlandMaster);
        this.mainForm.patchValue({
          subBrandCode: initalIndex == null ? this.subBlandMaster[0].code2 : this.subBlandMaster[initalIndex].code2
        });
      }
    }));
  }

  /**
   * テイストマスタを取得する。
   * @param brand ブランドコード
   * @returns Observable<void>
   */
  private getTasteMaster(brand: string): Observable<void> {
    this.tasteMasterList = [];
    return this.junpcCodmstService.getTastes({ brand: brand } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getTasteMaster:', list);
      this.tasteMasterList = list.items;  // マスタの配列に設定

      // 初期値設定判定
      if (this.isInitValueByCode2(this.tasteMasterList, this.mainForm.controls.tasteCode.value)) {
        // 初期値設定
        const initalIndex = this.itemService.getInitIndex(this.tasteMasterList);
        this.mainForm.patchValue({
          tasteCode: initalIndex == null ? this.tasteMasterList[0].code2 : this.tasteMasterList[initalIndex].code2
        });
      }
    }));
  }

  /**
   * 展開マスタを取得する。
   * @param brand ブランドコード
   * @returns Observable<void>
   */
  private getoutletMaster(brand: string): Observable<void> {
    this.outletMasterList = [];
    return this.junpcCodmstService.getOutlets({ brand: brand } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getoutletMaster:', list);
      this.outletMasterList = list.items; // マスタの配列に設定
      this.outletMasterList.unshift({ code1: this.OUTLET_DEFAULT_VALUE, item1: '' } as JunpcCodmst);

      // 初期値設定判定
      if (this.isInitValueByCode1(this.outletMasterList, this.mainForm.controls.outletCode.value)) {
        // 初期値を設定する場合、空文字(区分='00')を初期選択にする。
        this.mainForm.patchValue({ outletCode: this.OUTLET_DEFAULT_VALUE }); // 空文字(区分='00')を初期選択にする。
      }
    }));
  }

  /**
   * 丸井品番を取得する。
   * @param splitPartNoKind 分割済み品種コード
   * @returns Observable<void>
   */
  private getMaruiPartNo(splitPartNoKind: PartNoKind): Observable<void> {
    this.maruiMasterList = [];
    return this.junpcCodmstService.getMaruiItems({
      brand: splitPartNoKind.blandCode,
      item: splitPartNoKind.itemCode
    } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getMaruiPartNo:', list.items);
      this.maruiMasterList = list.items;  // マスタの配列に設定
      // formControlの初期値に設定
      // 丸井品番リストがない場合：
      if (ListUtils.isEmpty(this.maruiMasterList)) {
        // 丸井品番のバリデーションを外す
        this.f.maruiGarmentNo.setErrors(null);
        this.setValidation(<FormGroup> this.f.maruiGarmentNo);
        // 丸井品番初期選択値：空白(「000000」)
        this.mainForm.patchValue({ maruiGarmentNo: this.MARUI_DEFAULT_VALUE });
        return;
      }

      // 丸井品番リストがある場合:
      // 丸井品番のバリデーションをセット(必須、「000000」不可)
      this.f.maruiGarmentNo.setErrors(null);
      this.setValidation(<FormGroup> this.f.maruiGarmentNo,
        // 0埋め6桁の数値を除く6桁の数値に一致する正規表現
        [Validators.required, Validators.pattern(/^((?![0]{6}))([0-9]{6})$/)]);

      // 初期値設定をするか判定する：
      if (this.isInitValueByCode3(this.maruiMasterList, this.itemData.maruiGarmentNo)) {
        // 初期値設定
        let initalIndex = null;

        // 品種変更後のブランドのアイテムコードに合致する丸井品番があるかを判断：
        this.maruiMasterList.some((item, index) => {
          if (item.code2 === splitPartNoKind.itemCode) {
            // 初期値フラグが設定されている場合は、初期値設定されている丸井品番を初期値にする。
            if (item.item30 === Const.M_CODMST_INITIAL_FLAG) {
              initalIndex = index;
              return true;
            }
            // アイテムコードに合致する最初の丸井品番の場合は、indexを控えておく。
            if (initalIndex == null) {
              initalIndex = index;
            }
          }
          return false;
        });
        if (initalIndex != null) {
          // 変更した品種のブランドにDBに登録されている丸井品番と同じものはないが、品種変更後のブランドのアイテムコードに合致する丸井品番がある
          // →丸井品番初期選択値：品種変更後のブランドのアイテムに合致する丸井品番
          this.mainForm.patchValue({ maruiGarmentNo: this.maruiMasterList[initalIndex].code3 });
        } else {
          // 変更した品種のブランドにDBに登録されている丸井品番と同じものがないが、品種変更後のブランドのアイテムコードに合致する丸井品番もない
          // →丸井品番初期選択値：空白(「000000」)
          this.mainForm.patchValue({ maruiGarmentNo: this.MARUI_DEFAULT_VALUE });
        }
      }
    }));
  }

  /**
   * タイプ1～3を取得
   * @param partNoKind 品種
   * @retruns Observable<void>
   */
  private getType(partNoKind: string): Observable<void> {
    this.type1List = [];
    this.type2List = [];
    this.type3List = [];
    return this.junpcCodmstService.getTypes({ searchText: partNoKind } as JunpcCodmstSearchCondition).pipe(map(list => {
      console.debug('getType:', list);
      // タイプ1の配列
      this.type1List = list.items[0].type1s;
      // 初期値設定判定
      if (this.isInitValueByCode1(this.type1List, this.mainForm.controls.type1Code.value)) {
        // 初期値設定
        const initalIndexType1 = this.itemService.getInitIndex(this.type1List);
        this.mainForm.patchValue({
          type1Code: initalIndexType1 == null ? this.type1List[0].code1 : this.type1List[initalIndexType1].code1
        });
      }

      // タイプ2の配列
      this.type2List = list.items[0].type2s;
      // 初期値設定判定
      if (this.isInitValueByCode1(this.type2List, this.mainForm.controls.type2Code.value)) {
        // 初期値設定
        const initalIndexType2 = this.itemService.getInitIndex(this.type2List);
        // 選択肢に99(その他)があれば初期値に設定
        this.mainForm.patchValue({
          type2Code: initalIndexType2 == null ? this.type2List[0].code1 : this.type2List[initalIndexType2].code1
        });
      }

      // タイプ3の配列
      this.type3List = list.items[0].type3s;
      // 初期値設定判定
      if (this.isInitValueByCode1(this.type3List, this.mainForm.controls.type3Code.value)) {
        const initalIndexType3 = this.itemService.getInitIndex(this.type3List);
        // 選択肢に999(その他)があれば初期値に設定
        this.mainForm.patchValue({
          type3Code: initalIndexType3 == null ? this.type3List[0].code1 : this.type3List[initalIndexType3].code1
        });
      }
    }));
  }

  /**
   * パーツ情報マスタを取得する。
   * @param itemCodeParam 分割済み品種コード
   * @returns Observable<void>
   */
  private getPartsMaster(splitPartNoKind: PartNoKind): Observable<void> {
    const blandCodeParam = splitPartNoKind.blandCode;
    let itemCodeParam = splitPartNoKind.itemCode;
    if (blandCodeParam != null && blandCodeParam.length === 2 && itemCodeParam == null) {
      itemCodeParam = 'M';  // ブランドコードのみの場合はアイテムコード'M'で検索する。
    }

    return this.codeService.getPartsMaster(itemCodeParam).pipe(map(list => {
      console.debug('getPartsMaster:', list);
      this.partsMaster = list.items;
    }));
  }

  /**
   * 組成(混率)情報マスタを取得する。
   */
  private getCompMaster(): void {
    this.junpcCodmstService.getCompositionsFromCache().subscribe(list => {
      console.debug('getCompMaster:', list);
      this.compositionsMasterList = list.items;
    });
  }

  /**
   * アイテムを検索し、組成(混率)の必須を設定する。
   * @param splitPartNoKind 分割した品種
   * @returns Observable<void>
   */
  private setCompositionRequire(splitPartNoKind: PartNoKind): Observable<void> {
    this.mainForm.patchValue({ compositionRequireCode: '' });

    // アイテムを検索
    return this.junpcCodmstService.getItems({
      brand: splitPartNoKind.blandCode, item: splitPartNoKind.itemCode
    } as JunpcCodmstSearchCondition).pipe(map(list => {
      if (ListUtils.isEmpty(list.items)) {
        this.mainForm.patchValue({ compositionRequireCode: null });
        return;
      }
      // 組成(混率)必須を設定
      this.mainForm.patchValue({ compositionRequireCode: list.items[0].item7 });
    }));
  }

  /**
   * 画像・PDFの操作(タンザクの画像)
   * ファイル選択した時のイベント
   * @param files ファイルリスト
   * @param isEdit 編集モードか(false:参照モード)
   * @param mimeType mimeType
   */
  onFileSelect(files: FileList | File[], isEdit: boolean, mimeType?: string): void {
    this.tanzakuErrorMsgCode = ''; // 画像のエラーメッセージ初期化
    const file = files[0];

    this.attachmentFile = file;
    if (this.MAX_FILE_SIZE <= this.attachmentFile.size) { // ファイルサイズが大きいのでアップロードしない
      this.tanzakuErrorMsgCode = 'ERRORS.ESTIMATES_FILE_SIZE_ERROR';
      return;
    }
    if (!this.attachmentFile.name.match(/\.(png|jpg|jpeg|pdf|gif)$/i)) {  // 指定の拡張子以外のファイルが添付された
      this.tanzakuErrorMsgCode = 'ERRORS.FILE_UNMATCH_EXTENSION';
      return;
    }

    const fileInfo = new ItemFileInfoRequest();
    fileInfo.fileCategory = FileCategory.TYPE_TANZAKU;
    fileInfo.mode = FileMode.NEW_FILE;
    fileInfo.file = file;

    const type = isEdit ? this.attachmentFile.type : mimeType; // 参照モードであれば引数のtypeを設定

    // ファイルを表示するDOMを取得
    const node = this._el.querySelectorAll('#output');
    const output = node[0];

    switch (type) {
      case 'application/pdf': // PDFの時
        this.outputPdf(this.attachmentFile, output);
        break;
      default:                // PDF以外のとき
        this.outputImage(this.attachmentFile, output);
        break;
    }

    if (isEdit) {
      // 編集モードであればファイルをアップロードする。
      this.fileService.fileUpload(fileInfo.file).subscribe(x => {
        // アップロードが成功したら、配列に保持しておく。
        fileInfo.fileNoId = x.id;
        this.viewFileId = fileInfo.fileNoId;
        this.itemFileInfoList.push(JSON.parse(JSON.stringify(fileInfo)));
      });
    }
  }

  /**
   * PDFを表示するイベント
   * @param blob
   * @param output 出力先htmlのDOM
   */
  private outputPdf(blob: File, output: Element): void {
    // object要素の生成
    const objectElement = document.createElement('object');

    // File/BlobオブジェクトにアクセスできるURLを生成
    const file = new Blob([blob], { type: 'application/pdf' });
    const blobURL = URL.createObjectURL(file);

    objectElement.setAttribute('data', blobURL);

    // File/BlobオブジェクトにアクセスできるURLを開放
    URL.revokeObjectURL(blobURL + '#page=1');

    // 属性追加
    objectElement.setAttribute('type', 'application/pdf');
    objectElement.setAttribute('width', '100%');
    objectElement.setAttribute('height', '250px');

    // 現在表示されている画像を削除
    while (output.childNodes.length > 0) {
      output.removeChild(output.firstChild);
    }
    // #output へ出力
    output.appendChild(objectElement);

    this.isShowImage = true;
  }

  /**
   * 画像を表示するイベント
   * @param blob
   * @param output 出力先htmlのDOM
   */
  private outputImage(blob: File, output: Element): void {
    const image = new Image();  // 画像要素の生成
    const blobURL = URL.createObjectURL(blob);  // File/BlobオブジェクトにアクセスできるURLを生成
    image.src = blobURL;  // src にURLを入れる

    // 画像読み込み完了後
    image.addEventListener('load', () => {
      URL.revokeObjectURL(blobURL); // File/BlobオブジェクトにアクセスできるURLを開放
      while (output.childNodes.length > 0) {
        output.removeChild(output.firstChild);  // 現在表示されている画像を削除
      }
      output.appendChild(image);  // #output へ出力
    });
    this.isShowImage = true;
  }

  /**
   * 画像を削除する.
   */
  onDeleteImage(): void {
    this.tanzakuErrorMsgCode = '';
    const output = this._el.querySelectorAll('#output')[0];
    while (output.childNodes.length > 0) {
      output.removeChild(output.firstChild);
    }
    this.itemFileInfoList.some(itemFileInfo => {
      const fileInfo = itemFileInfo;
      if (fileInfo.fileNoId === this.viewFileId) {
        // ファイルのステータスを削除にする。
        fileInfo.mode = FileMode.DELETED_FILE;
        this.viewFileId = null;
        this.attachmentFile = null;
        return true;
      }
    });
    this.isShowImage = false;
  }

  /**
   * 画像ドロップのエリアでクリックした際のイベント
   */
  onKickFileEvent(): void {
    document.getElementById('file').click();  // input type='file'タグのclickイベントを起動
  }

  /**
   * 見積添付ファイル選択ボタン押下時の処理
   * @param files 見積添付ファイルリスト
   */
  onEstimatesFileSelect(files: FileList): void {
    this.estimatesErrorMsgCode = '';
    const attachbleCnt = this.MAX_FILES - this.activefilecount;

    if (files.length > attachbleCnt) {  // 添付可能数を超えている場合、エラーメッセージを出す
      this.estimatesErrorMsgCode = 'ERRORS.ESTIMATES_FILE_COUNT_ERROR';
      return;
    }

    // エラーチェック
    const existsFileError: boolean = Array.prototype.some.call(files, file => {
      if (this.MAX_FILE_SIZE <= file.size) {  // ファイルサイズが大きいのでアップロードしない
        this.estimatesErrorMsgCode = 'ERRORS.ESTIMATES_FILE_SIZE_ERROR';
        return true;
      }

      if (!file.name.match(/\.(txt|csv|png|jpg|jpeg|gif|pdf|doc|docm|docx|xls|xlsm|xlsx|pptx|pptm|ppt|zip)$/i)) {
        // 指定の拡張子以外のファイルが添付された
        this.estimatesErrorMsgCode = 'ERRORS.FILE_UNMATCH_EXTENSION';
        return true;
      }
    });

    if (existsFileError) {
      return; // エラーあり
    }

    // エラーなし
    Array.prototype.forEach.call(files, file => {
      // ファイルをアップロード
      this.fileService.fileUpload(file).subscribe(x => {
        // ファイル情報を作成
        const fileInfo = new ItemFileInfoRequest();
        fileInfo.fileNoId = x.id;
        fileInfo.fileName = file.name;
        fileInfo.fileCategory = FileCategory.TYPE_ESTIMATES;
        fileInfo.mode = FileMode.NEW_FILE;
        fileInfo.file = file;
        this.estimatesFile.push(fileInfo);
        this.activefilecount++;
      });
    });
  }

  /**
   * 見積添付ファイルリンク押下時の処理。
   * @param index ファイルのindex
   */
  onEstimatesFileDownload(index: number): void {
    if (this.estimatesFile[index].fileNoId && this.estimatesFile[index].file == null) {
      this.fileService.fileDownload(this.estimatesFile[index].fileNoId.toString()).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.estimatesFile[index].file = data.blob;
        FileUtils.downloadFile(data.blob, this.estimatesFile[index].fileName);
      });
      return;
    }

    // ローカルで参照している or 一度ファイルをダウンロードしている
    FileUtils.downloadFile(this.estimatesFile[index].file, this.estimatesFile[index].fileName);
  }

  /**
   * 生地検査結果添付ファイルリンク押下時の処理。
   * @param index ファイルのindex
   */
  onMisleadingRepresentationFileDownlad(index: number): void {
    if (this.misleadingRepresentationFile[index].fileBlob == null) {
      this.fileService.fileDownload(this.misleadingRepresentationFile[index].file.id.toString()).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.misleadingRepresentationFile[index].fileBlob = data.blob;
        FileUtils.downloadFile(data.blob, this.misleadingRepresentationFile[index].file.fileName);
      });
      return;
    }

    // 一度ファイルをダウンロードしている
    FileUtils.downloadFile(this.misleadingRepresentationFile[index].fileBlob, this.misleadingRepresentationFile[index].file.fileName);
  }

  /**
   * 見積添付削除ボタン押下時の処理。
   * @param index ファイルのindex
   */
  onEstimatesFileDeleted(index: number): void {
    const fileInfo = this.estimatesFile[index];
    // ファイルのモードを削除にする。
    fileInfo.mode = FileMode.DELETED_FILE;
    this.activefilecount--;
    this.estimatesErrorMsgCode = '';
  }

  /**
   * 生地検査結果添付削除ボタン押下時の処理。
   * @param index ファイルのindx
   */
  onMisleadingRepresentationFileDeleted(index: number): void {
    const file = this.misleadingRepresentationFile[index];
    file.mode = FileMode.DELETED_FILE;  // ファイルのモードを削除にする。
  }

  /**
   * 登録済発注情報プルダウンに発注Noか生産発注日を表示する。
   * 受注確定済は発注Noを表示
   * 受注未確定は生産発注日を表示
   * @param orderData 発注情報
   * @returns 表示する値
   */
  showOrderNoOrProductOrderAt(orderData: Order): string {
    // 受注確定済か判定
    if (BusinessCheckUtils.isConfirmOrderOk(orderData)) {
      return orderData.orderNumber.toString();
    }
    return orderData.productOrderAt.toString() + '(未確定)';
  }

  /**
   * サブメニューリンク押下時の処理。
   * 指定した要素へページ内リンクする。
   * @param id リンク先のid
   * @returns false
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
   * 原価率計算.
   * @returns 原価率
   */
  caluculateCostRate(): number {
    return CalculationUtils.calcRate(this.mainForm.controls.otherCost.value,
      this.mainForm.controls.retailPrice.value);
  }

  // PRD_0023 && No_65 mod JFE start
  /**
   * 原価合計.
   * @returns 原価合計
   */
    calculateTotal(): number {
      const matlCost: number = this.mainForm.controls.matlCost.value || 0;
      const processingCost: number = this.mainForm.controls.processingCost.value || 0;
      const assrCost: number = this.mainForm.controls.accessoriesCost.value || 0;
      const otherCost: number = this.mainForm.controls.otherCost.value || 0;
      const totalCost: number = +matlCost + +processingCost + +assrCost + +otherCost;
      return totalCost;
    }
  // PRD_0023 && No_65 mod JFE start
  /**
   * 原価率.
   * @returns 原価率
   */
  caluculateTotalCostCostRate(): number {
      return CalculationUtils.calcRate(
        this.calculateTotal().toString(),
        this.mainForm.controls.retailPrice.value
      );
    }

  // PRD_0023 add JFE end

  /**
   * 投入日を自動で計算し、formに設定する。
   * 投入日＝納品日
   */
  setDeploymentDate(): void {
    const deploymentDate = this.mainForm.controls.deploymentDate.value;
    // 投入日が空でない場合は計算しない
    if (deploymentDate != null) { return; }

    // 納品日 JSON型をstring型に変換
    const preferredDeliveryDate = this.ngbDateParserFormatter.format(this.mainForm.controls.preferredDeliveryDate.value);

    // 投入日を算出
    this.f.deploymentDate.setValue(
      this.ngbDateParserFormatter.parse(CalculationUtils.calcDeploymentDate(preferredDeliveryDate))
    );

    // 投入の自動計算は投入日のinputイベントを拾っているため、ここで週計算も行う
    this.setWeek('deployment');
  }

  /**
   * P終了日を自動で計算し、formに設定する。
   * P終了日=投入日+加算日
   */
  setPendDate(): void {
    const pendDate = this.mainForm.controls.pendDate.value;
    // P終了日が空でない場合は計算しない
    if (pendDate != null) { return; }

    // 品番よりブランドコードとアイテムコードを取得する
    const partNoKind = BusinessUtils.splitPartNoKind(this.mainForm.controls.partNoKind.value);
    // ブランドコードとアイテムコードで加算日を取得する
    const addDay = this.itemService.getAddDays(partNoKind.blandCode, partNoKind.itemCode);
    // 投入日 JSON型をstring型に変換
    const deploymentDate = this.ngbDateParserFormatter.format(this.mainForm.controls.deploymentDate.value);
    // P終了日を算出
    this.f.pendDate.setValue(
      this.ngbDateParserFormatter.parse(CalculationUtils.calcPendDate(deploymentDate, addDay))
    );

    // P終了週の自動計算は投入日のinputイベントを拾っているため、ここで週計算も行う
    this.setWeek('pend');
  }

  /**
   * 週計算処理を行いformに設定する。
   * @param type pend/deployment
   */
  setWeek(type: string): void {
    switch (type) {
      case 'pend':
        this.f.pendWeek.setValue(
          CalculationUtils.calcWeek(this.mainForm.controls.pendDate.value));
        break;
      case 'deployment':
        this.f.deploymentWeek.setValue(
          CalculationUtils.calcWeek(this.mainForm.controls.deploymentDate.value));
        break;
      default:
        break;
    }
  }

  /**
   * 逆・週計算処理を行いformに設定する。
   * @param type pend/deployment
   */
  setWeekDate(type: string): void {
    switch (type) {
      case 'pend':
        // json形式に変更
        this.f.pendDate.setValue(
          this.ngbDateParserFormatter.parse(CalculationUtils.calcWeekDate(
            this.mainForm.controls.pendWeek.value, this.mainForm.controls.year.value)
          )
        );
        break;
      case 'deployment':
        this.f.deploymentDate.setValue(
          this.ngbDateParserFormatter.parse(CalculationUtils.calcWeekDate(
            this.mainForm.controls.deploymentWeek.value, this.mainForm.controls.year.value)
          )
        );
        break;
      default:
        break;
    }
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // patchValueしないとpipeの変換値がformにセットされない
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.mainForm.patchValue({ [type]: ngbDate }); }
  }

  /**
   * 担当者ID変更時の処理。
   * @param staffType 担当区分
   */
  onChangeStaff(staffType: StaffType): void {
    let code = '';
    // 入力項目毎のコード取得
    switch (staffType) {
      case StaffType.PLANNING:
        code = this.mainForm.controls.plannerCode.value;
        break;
      case StaffType.PRODUCTION:
        code = this.mainForm.controls.mdfStaffCode.value;
        break;
      case StaffType.PATANER:
        code = this.mainForm.controls.patanerCode.value;
        break;
      default:
        break;
    }

    // 最大長まで入力されていない場合は名称を削除して検索しない
    if (code.length !== 6) {
      switch (staffType) {
        case StaffType.PLANNING:
          this.mainForm.patchValue({ plannerName: null });
          break;
        case StaffType.PRODUCTION:
          this.mainForm.patchValue({ mdfStaffName: null });
          break;
        case StaffType.PATANER:
          this.mainForm.patchValue({ patanerName: null });
          break;
        default:
          break;
      }
      return;
    }
    // コードマスタ(スタッフ)取得APIコール
    this.junpcCodmstService.getStaffs({
      staffType: staffType,
      brand: BusinessUtils.splitPartNoKind(this.mainForm.controls.partNoKind.value).blandCode,
      searchType: SearchTextType.CODE_PERFECT_MATCH,
      searchText: code
    } as JunpcCodmstSearchCondition).subscribe(x => {
      let setValue = '';  // 結果が取得できない場合は初期化
      if (x != null && ListUtils.isNotEmpty(x.items)) {
        setValue = x.items[0].item2; // 結果が取得出来たらそれぞれのName項目にセット
      }
      switch (staffType) {
        case StaffType.PLANNING:
          this.mainForm.patchValue({ plannerName: setValue });
          break;
        case StaffType.PRODUCTION:
          this.mainForm.patchValue({ mdfStaffName: setValue });
          break;
        case StaffType.PATANER:
          this.mainForm.patchValue({ patanerName: setValue });
          break;
        default:
          break;
      }
    });
  }

  /**
   * 担当者削除ボタン押下時の処理。
   * @param staffType 担当区分
   */
  onStaffDeleted(staffType: StaffType): void {
    switch (staffType) {
      case StaffType.PLANNING:
        this.mainForm.patchValue({ plannerName: null });
        this.mainForm.patchValue({ plannerCode: null });
        break;
      case StaffType.PRODUCTION:
        this.mainForm.patchValue({ mdfStaffName: null });
        this.mainForm.patchValue({ mdfStaffCode: null });
        break;
      case StaffType.PATANER:
        this.mainForm.patchValue({ patanerName: null });
        this.mainForm.patchValue({ patanerCode: null });
        break;
      default:
        break;
    }
  }

  /**
   * メーカーコード変更時の処理。
   * @param supplierType 仕入先区分
   */
  onChangeMaker(supplierType: SupplierType, supplierFormGroup: FormGroup): void {
    let code;
    // 入力項目毎のコード取得
    switch (supplierType) {
      case SupplierType.MDF_MAKER:
        code = supplierFormGroup.get('supplierCode').value;
        break;
      case SupplierType.MALT_MAKER:
        code = this.mainForm.controls.matlMakerCode.value;
        break;
      default:
        break;
    }

    // 最大長まで入力されていない場合は検索しない
    if (code.length !== 5) {
      switch (supplierType) {
        case SupplierType.MDF_MAKER:
          supplierFormGroup.patchValue({
            supplierName: null,
          });
          // 生産メーカーの時は、生産工場もクリアする
          supplierFormGroup.patchValue({
            supplierFactoryCode: null,
            supplierFactoryName: null,
          });
          break;
        case SupplierType.MALT_MAKER:
          this.matlMakerName = null;
          break;
        default:
          break;
      }
      return;
    }

    // 仕入れマスタ取得APIコール
    this.junpcSirmstService.getSirmst({
      sirkbn: supplierType,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: code
    } as JunpcSirmstSearchCondition).subscribe(x => {
      let setValue = '';  // 結果が取得できない場合は初期化
      if (x != null && ListUtils.isNotEmpty(x.items)) {
        setValue = x.items[0].name; // 結果が取得出来たらそれぞれのName項目にセット
      }
      switch (supplierType) {
        case SupplierType.MDF_MAKER:
          supplierFormGroup.patchValue({
            supplierName: setValue,
          });
          break;
        case SupplierType.MALT_MAKER:
          this.matlMakerName = setValue;
          break;
        default:
          break;
      }
    });
  }

  /**
   * 工場コード変更時の処理。
   * @param supplierType 仕入先区分
   */
  onChangeFactory(supplierType: SupplierType, supplierFormGroup: FormGroup): void {
    const code = supplierFormGroup.get('supplierFactoryCode').value;

    // 最大長まで入力されていない場合は検索しない
    if (code.length !== 6) {
      // 工場名をクリアする
      supplierFormGroup.patchValue({ supplierFactoryName: null });
      return;
    }

    // 工場マスタ取得APIコール
    this.junpcKojmstService.getKojmst({
      sire: supplierFormGroup.get('supplierCode').value,
      sirkbn: supplierType,
      brand: this.mainForm.controls.brandCode.value,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: code
    } as JunpcKojmstSearchCondition).subscribe(x => {
      if (x != null && ListUtils.isNotEmpty(x.items)) {
        // 結果が取得出来たらName項目にセット
        supplierFormGroup.patchValue({ supplierFactoryName: x.items[0].name });
        return;
      }
      supplierFormGroup.patchValue({ supplierFactoryName: null });
    });
  }

  /**
   * 通番を元に年度を算出してFormにセットする
   * @param partNoSerialNo 通番
   */
  setYear(partNoSerialNo: string): void {
    this.mainForm.patchValue({ year: CalculationUtils.calFiscalYear(partNoSerialNo) });
  }

  /**
   * 初期値設定をするか判定する。
   * コードマスタのCODE1と比較
   * @param codmstlist コードマスタのリスト
   * @param formValue フォームの値
   * @return 初期値を設定する場合、trueを返す。
   */
  private isInitValueByCode1(codmstlist: JunpcCodmst[], formValue: string): boolean {
    if (formValue == null) { return true; }
    return !codmstlist.some(item => item.code1 === formValue);
  }

  /**
   * 初期値設定をするか判定する。
   * コードマスタのCODE2と比較
   * @param codmstlist コードマスタのリスト
   * @param formValue フォームの値
   * @return 初期値を設定する場合、trueを返す。
   */
  private isInitValueByCode2(codmstlist: JunpcCodmst[], formValue: string): boolean {
    if (formValue == null) { return true; }
    return !codmstlist.some(item => item.code2 === formValue);
  }

  /**
   * 初期値設定をするか判定する。
   * コードマスタのCODE3と比較
   * @param codmstlist コードマスタのリスト
   * @param maruiGarmentNo DBに登録されている丸井品番
   * @return 初期値を設定する場合、trueを返す。
   */
  private isInitValueByCode3(codmstlist: JunpcCodmst[], maruiGarmentNo: string): boolean {
    // DBに登録されている丸井品番がない、または空白値「000000」の場合は初期値を設定する
    if (maruiGarmentNo == null || maruiGarmentNo === this.MARUI_DEFAULT_VALUE) {
      return true;
    }
    // DBに登録されている丸井品番がある(「000000」以外)場合:
    // 変更した品種のブランドにDBに登録されている丸井品番と同じものがある場合は、初期値を設定しない
    const isIncluded = codmstlist.some(item => {
      if (item.code3 === maruiGarmentNo) {
        // 品種を変更するたびに丸井品番を初期化しているため、formに丸井品番をセットする
        this.mainForm.patchValue({ maruiGarmentNo: maruiGarmentNo });
        return true;
      }
    });
    return !isIncluded;
  }

  /**
   * 品番情報取得エラー
   * @param error エラー情報
   */
  private getItemErrorHandler(error: HttpErrorResponse): void {
    // データの取得に失敗
    this.isInitDataSetted = false;
    this.loadingService.loadEnd();
    this.overallErrorMsgCode = 'ERRORS.ANY_ERROR';
    // サーバエラーエリアにメッセージ表示
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      ExceptionUtils.displayErrorInfo('defaultErrorInfo', apiError.viewErrorMessageCode);
    }
  }

  /**
   * エラーメッセージをクリアする.
   */
  private clearErrorMessage(): void {
    this.estimatesErrorMsgCode = '';  // 添付ファイルのエラーメッセージ初期化
    this.tanzakuErrorMsgCode = '';    // 画像のエラーメッセージ初期化
    this.overallErrorMsgCode = '';    // エラーメッセージ用
    ExceptionUtils.clearErrorInfo();  // カスタムエラーメッセージクリア
  }

  /**
   * メッセージをクリアする.
   */
  private clearAllMessage(): void {
    this.overallSuccessMsgCode = '';  // 成功メッセージ用
    this.clearErrorMessage();
  }

  /**
   * ラジオボタンを選択したときの処理.
   * @param janType 選択したJAN区分
   */
  onChangeJanTypeRadio(janType: number): void {
    // JAN区分セット
    this.mainForm.patchValue({ janType: janType});
    // Service経由でJAN区分の変更感知
    this.itemDataService.janType$.next(this.f.janType.value);
  }

  /**
   * 中国内販情報の「製品分類」または「製品種別」変更時の処理
   * 中国内販情報 産地に原産国を設定する
   */
  onChangeCnProduct(): void {
    const cnProductCategory = this.f.fkItem.getRawValue()['cnProductCategory'];  // 製品分類
    const cnProductType = this.f.fkItem.getRawValue()['cnProductType'];          // 製品種別

    if ((StringUtils.isNotEmpty(cnProductCategory)) || (StringUtils.isNotEmpty(cnProductType))) {
      // 製品分類または製品種別が設定されている
      const cooName = this.f.cooName.value; // 原産国
      this.f.fkItem.patchValue({ cnProductCooName: cooName }); // 原産国を中国内販情報の産地に設定
    } else {
      this.f.fkItem.patchValue({ cnProductCooName: '' });
    }
  }
}
