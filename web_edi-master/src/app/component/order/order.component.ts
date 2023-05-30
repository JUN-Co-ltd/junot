import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbDateParserFormatter, NgbDateStruct, NgbModal } from '@ng-bootstrap/ng-bootstrap';

import {
  AuthType, FileCategory, PreEventParam, CompositionsCommon,
  Path, RegistStatus, OrderApprovalStatus, APIErrorTypeParam, SupplierType, SearchTextType, StaffType, ViewMode
} from '../../const/const';
import { CodeMaster } from '../../const/code-master';
import { CalculationUtils } from '../../util/calculation-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { StringUtils } from '../../util/string-utils';
import { FileUtils } from '../../util/file-utils';
import { DateUtils } from '../../util/date-utils';
import { ListUtils } from 'src/app/util/list-utils';
import { BusinessCheckUtils } from 'src/app/util/business-check-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

import { Order } from '../../model/order';
import { Item } from '../../model/item';
import { OrderSku } from '../../model/order-sku';
import { Sku } from '../../model/sku';
import { ItemFileInfo } from '../../model/item-file-info';
import { Compositions } from '../../model/compositions';
import { OrderViewCompositions } from '../../model/order-view-compositions';

import { HeaderService } from '../../service/header.service';
import { ItemService } from '../../service/item.service';
import { OrderService } from '../../service/order.service';
import { DeliveryRequestService } from '../../service/delivery-request.service';
import { FileService } from '../../service/file.service';
import { MKanmstService } from '../../service/m-kanmst.service';
import { SessionService } from '../../service/session.service';
import { LoadingService } from '../../service/loading.service';

import { SkuInputComponent } from '../sku-input/sku-input.component';
import { MessageConfirmModalComponent } from '../message-confirm-modal/message-confirm-modal.component';

// PRD_0023 && No_65 mod JFE start
import { costRateValidator, OrderValidatorDirective, totalCostValidator } from './validator/order-validator.directive';
// PRD_0023 && No_65 mod JFE end
import { VOrder } from 'src/app/model/v-order';
import { MKanmst } from 'src/app/model/m-kanmst';
import { DeliveryRequestSearchConditions } from 'src/app/model/search-conditions';
import { Delivery } from 'src/app/model/delivery';

import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { JunpcCodmstSearchCondition } from 'src/app/model/junpc-codmst-search-condition';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { SearchStaffModalComponent } from '../search-staff-modal/search-staff-modal.component';
import { Observable, from, of } from 'rxjs';
import { tap, filter, map, catchError } from 'rxjs/operators';
import { GenericList } from 'src/app/model/generic-list';
import { FormUtilsService } from 'src/app/service/bo/form-utils.service';
import { Session } from 'src/app/model/session';
import { e } from '@angular/core/src/render3';

// PRD_0144 #10776 add JFE start
interface List {
  label: string;
  value: string;
}
// PRD_0144 #10776 add JFE end

@Component({
  selector: 'app-order',
  templateUrl: './order.component.html',
  styleUrls: ['./order.component.scss']
})
export class OrderComponent implements OnInit {
  @ViewChild(SkuInputComponent)
  protected skuInputComponent: SkuInputComponent;

  // htmlから参照したい定数を定義
  readonly AUTH_INTERNAL: AuthType = AuthType.AUTH_INTERNAL;      // JUN権限(社内)
  readonly AUTH_SUPPLIERS: AuthType = AuthType.AUTH_SUPPLIERS;    // メーカー権限(取引先)
  readonly PATH = Path;
  readonly REGIST_STATUS = RegistStatus;
  readonly ORDER_APPROVAL_STATUS = OrderApprovalStatus;
  readonly SUPPLIER_TYPE = SupplierType;                // 仕入先区分
  readonly STAFF_TYPE = StaffType;                      // 担当区分
  readonly VIEW_MODE = ViewMode;                        // 画面表示モード

  affiliation: AuthType;
  private session: Session;
  mainForm: FormGroup;  // メインのフォーム

  overall_susses_msg_code = ''; // 画面全体にかかる正常系メッセージ
  overall_error_msg_code = '';  // 画面全体にかかる異常系メッセージ
  orderFileDLErrorMessageCode: string = null;     // 発注書ファイルダウンロードエラーメッセージコード
  estimatesFileDLErrorMessageCode: string = null; // 見積ファイルダウンロードエラーメッセージコード

  seasonValue: string;  // サブシーズンのvalue値
  cooMasterList: JunpcCodmst[] = [];  // 原産国マスタリスト
  itemData: Item;             // 品番情報のデータ
  orderData: Order;           // 発注情報のデータ
  skusValue: Sku[];           // 品番のSKU情報のデータ
  orderSkuValue: OrderSku[];  // 発注SKU情報のデータ
  deliveryList: Delivery[] = [];  // 納品依頼情報のデータ
  // PRD_0144 #10776 add JFE start
  // 費目リスト
  expenseItemList: List[] = [
    {
      label: '製品発注',
      value: '01'
    },
    {
      label: '縫製発注',
      value: '04'
    }
  ];
  // PRD_0144 #10776 add JFE end

  endDeliveryAt: NgbDateStruct; // 納期 END
  endOrderAt: NgbDateStruct;    // 発注日 END

  estimatesFile = []; // 見積添付ファイルの情報リスト
  compViewList = [];  // 組成情報のデータリスト

  path = '';              // new,view,edit,delete

  isShowFooter = false; // フッター表示フラグ
  isDirty = false;          // 画面変更検知フラグ
  isInitDataSetted = false; // 画面用データ取得完了フラグ
  submitted = false;        // submit可否フラグ
  isBtnLock = false;        // 登録/更新/削除処理中にボタンをロックするためのフラグ
  isAbleToDLFile = false;   // 発注書ファイルダウンロードアイコン表示可否フラグ
  isConfirmed = false;      // 発注確定済み判断フラグ
  isOrderComplete = false;  // 完納済み判断フラグ
  // PRD_0112 #7710 JFE add start
  isSeihin = false;         //発注区分(製品)判断フラグ
  // PRD_0112 #7710 JFE add end

  viewMode = ViewMode.ITEM_NEW; // 画面表示モード
  registStatus = RegistStatus.ITEM;  // 登録ステータス
  isOtherCollapsed = false;

  private orderFileList: { id: number, fileName: string, fileBlob: Blob }[] = [];  // 発注ファイルの実態リスト

  orderAmount = 0;  // 金額
  statusLabel = ''; // 確定・承認ステータスラベル 発注情報がある時のみ表示

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private headerService: HeaderService,
    private itemService: ItemService,
    private orderService: OrderService,
    private deliveryRequestService: DeliveryRequestService,
    private fileService: FileService,
    private mKanmstService: MKanmstService,
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private loadingService: LoadingService,
    private sessionService: SessionService,
    private modalService: NgbModal,
    private junpcCodmstService: JunpcCodmstService,
    private formUtils: FormUtilsService
  ) { }

  /**
   * mainFormのcontrolを返す.
   * @returns this.mainForm.controls
   */
  get f(): any { return this.mainForm.controls; }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession(); // ログインユーザの情報を取得する
    this.affiliation = this.session.affiliation; // ログインユーザの権限取得
    this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;

    // URLからパラメータを取得する
    const orderId: number = Number(this.route.snapshot.params['id']);    // 発注Id
    const preEvent: number = Number(this.route.snapshot.queryParamMap.get('preEvent'));
    const errorType: number = Number(this.route.snapshot.queryParamMap.get('errorType'));
    // フッター表示条件: ROLE_EDIまたはROLE_MAKER
    this.isShowFooter = AuthUtils.isEdi(this.session) || AuthUtils.isMaker(this.session);

    // 原産国の取得
    this.getOriginCountriesMaster().subscribe(() =>
      // pathによる分岐表示
      this.initializeDisplayByRouting(this.path, preEvent, orderId, errorType)
    );
  }

  /**
   * 発注情報SKUと単価のデータ変更監視
   */
  private onSkusOrUnitPriceValueChanges(): void {
    // 発注情報SKUの変更監視
    this.mainForm.get('orderSkus').valueChanges.subscribe(() => {
      // 発注SKUが変更されたら金額の再集計を行う。
      this.orderAmount = this.calculateOrderAmount();
    });

    // 単価の状態監視
    this.mainForm.get('unitPrice').valueChanges.subscribe(() => {
      // 単価が変更されたら金額の再集計を行う。
      this.orderAmount = this.calculateOrderAmount();
    });
  }

  /**
   * pathによる分岐表示
   * @param path URLパス
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   * @param orderId 当画面で処理する発注ID
   * @param errorType URLクエリパラメータerrorType(APIエラーの種類)
   */
  private initializeDisplayByRouting(path: string, preEvent: number, orderId?: number, errorType?: number): void {
    switch (path) {
      case Path.NEW:  // 新規登録
        this.initializeNewDisplay(preEvent);
        break;
      case Path.VIEW: // 参照
        this.initializeViewDisplay(preEvent, orderId, errorType);
        break;
      case Path.EDIT: // 編集
        this.initializeEditDisplay(preEvent, orderId);
        break;
      default:
        break;
    }
  }

  /**
   * pathがNEW時の表示処理。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private initializeNewDisplay(preEvent: number): void {
    switch (preEvent) {
      case PreEventParam.DELETE:
        // 発注削除後のメッセージを設定
        this.overall_susses_msg_code = 'SUCSESS.ORDER_DELTE';
        break;
      case PreEventParam.CONFIRM_ERROR:
        // (確定時)発注削除済みのエラーメッセージを設定
        this.overall_error_msg_code = 'ERRORS.400_O_01';
        break;
      case PreEventParam.UPDATE_ERROR:
        // (更新時)発注削除済みのエラーメッセージを設定
        this.overall_error_msg_code = 'ERRORS.400_O_04';
        break;
      default:
        break;
    }

    // 日付範囲取得
    this.setDateRange().then(() => {
      const partNoId = Number(this.route.snapshot.queryParamMap.get('partNoId'));
      // 品番情報を取得する
      this.getItemData(partNoId).then(item => {
        this.createForm(item);
        this.isInitDataSetted = true; // フォームを表示
      }).catch(reason => { this.showGetApiErrorMessage(reason); });
    }).catch(reason => this.showGetApiErrorMessage(reason));
  }

  /**
   * pathがVIEW時の表示処理。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   * @param orderId 当画面で処理する発注ID
   * @param errorType URLクエリパラメータerrorType(APIエラーの種類)
   */
  private initializeViewDisplay(preEvent: number, orderId: number, errorType?: number): void {
    switch (preEvent) {
      case PreEventParam.SUBMIT_ERROR:
        // 発注確定済みのエラーメッセージを設定
        this.overall_error_msg_code = 'ERRORS.400_O_02';
        break;
      case PreEventParam.ITEM_READONLY_ERROR:
        this.overall_error_msg_code = 'ERRORS.400_12';
        break;
      case PreEventParam.UPDATE_ERROR:
        // 完納のエラーメッセージを設定
        if (errorType === APIErrorTypeParam.ORDER_COMPLETE) {
          this.overall_error_msg_code = 'ERRORS.400_O_07';
        }
        // 納品依頼登録済みのエラーメッセージを設定
        if (errorType === APIErrorTypeParam.DELIVERY_REGISTED) {
          this.overall_error_msg_code = 'ERRORS.400_O_08';
        }
        break;
      default:
        break;
    }

    // 発注情報取得
    this.getOrderData(orderId).then(order => {
      // 品番情報取得
      this.getItemData(order.partNoId).then(() => {
        this.setFormValue(order);  // formに発注情報セット
        this.mainForm.disable();  // 入力フォームをすべて非活性
        // 完納フラグを確認
        this.isCompleteOrder(this.orderData);
        this.isInitDataSetted = true; // フォームを表示
      }).catch(reason => { this.showGetApiErrorMessage(reason); });
    }).catch(reason => { this.showGetApiErrorMessage(reason); });
  }

  /**
   * pathがEDIT時の表示処理。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   * @param orderId 当画面で処理する発注ID
   */
  private initializeEditDisplay(preEvent: number, orderId: number): void {
    switch (preEvent) {
      case PreEventParam.CREATE:
        // 発注登録後のメッセージを設定
        this.overall_susses_msg_code = 'SUCSESS.ORDER_ENTRY';
        break;
      case PreEventParam.UPDATE:
        // 発注更新後のメッセージを設定
        this.overall_susses_msg_code = 'SUCSESS.ORDER_UPDATE';
        break;
      case PreEventParam.CONFIRM:
        // 発注確定後のメッセージを設定
        this.overall_susses_msg_code = 'SUCSESS.ORDER_CONFIRMED';
        break;
      default:
        break;
    }

    // 納品依頼リスト取得
    this.getDeliveryList(orderId).then(() => {
      // 発注情報取得
      this.getOrderData(orderId).then(order => {
        // 完納フラグを確認
        this.isCompleteOrder(this.orderData);
        // 日付範囲取得
        this.setDateRange().then(() => {
          // 品番情報取得
          this.getItemData(order.partNoId).then(item => {
            if (order != null) {
              this.setFormValue(order);  // formに発注情報セット
            }
            this.isInitDataSetted = true; // フォームを表示
          }).catch(reason => { this.showGetApiErrorMessage(reason); });
        }).catch(reason => { this.showGetApiErrorMessage(reason); });
      }).catch(reason => { this.showGetApiErrorMessage(reason); });
    }).catch(reason => { this.showGetApiErrorMessage(reason); });
  }

  /**
   * 各種カレンダーの日付範囲設定
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async setDateRange(): Promise<MKanmst> {
    // 管理マスタを取得
    return await this.mKanmstService.getKanmst().toPromise().then(
      kanriMstData => {
        // 納期の終了を算出。開始は生産発注日になる為算出不要
        this.endDeliveryAt = this.ngbDateParserFormatter.parse(CalculationUtils.calcEndDeliveryAt(kanriMstData.simymd));

        // 生産発注日の終了を算出
        this.endOrderAt = this.ngbDateParserFormatter.parse(CalculationUtils.calcEndOrderAt(kanriMstData.simymd));

        return Promise.resolve(kanriMstData);
      }, error => Promise.reject(error));
  }

  /**
   * 原産国マスタを取得する.
   * @returns Observable
   */
  private getOriginCountriesMaster(): Observable<GenericList<JunpcCodmst>> {
    return this.junpcCodmstService.getOriginCountriesFromCache()
      .pipe(
        tap(list => this.cooMasterList = list.items)
      );
  }

  /**
   * フォームを作成する.
   * @param item 品番情報
   */
  private createForm(item: Item): void {
    this.mainForm = this.getFromGroup();  // メインのFormGroupを作成する。
    this.onSkusOrUnitPriceValueChanges(); // 発注SKUと単価の変更監視
    const coo = this.cooMasterList.find(x => x.code1 === item.cooCode);
    const orderSupplier = item.orderSuppliers[0]; // 新規登録時は先頭の発注先生産メーカーを初期値とする.

    this.mainForm.patchValue({
      partNoId: item.id, // 品番ID
      partNo: item.partNo, // 品番
      productOrderAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(item.proviOrderDate)), // 生産発注日
      productCorrectionDeliveryAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(item.preferredDeliveryDate)), // 生産納期
      unitPrice: item.otherCost, // 単価
      retailPrice: item.retailPrice, // 上代
      otherCost: item.otherCost, // 原価
      // PRD_0023 add JFE start
      matlCost: item.matlCost,  // 生地原価
      processingCost: item.processingCost, // 加工賃
      // PRD_0023 && No_65 mod JFE start
      attachedCost: item.accessoriesCost,  // 付属品
      // PRD_0023 && No_65 mod JFE end
      totalCost: null, // 原価合計
      // PRD_0023 add JFE start
      mdfStaffCode: item.mdfStaffCode, // 製造担当者コード
      mdfStaffName: item.mdfStaffName, // 製造担当者名
      cooCode: item.cooCode, // 原産国コード
      cooName: coo == null ? null : coo.item2, // 原産国名称
      mdfMakerCode: orderSupplier.supplierCode, // 生産メーカーコード
      mdfMakerName: orderSupplier.supplierName, // 生産メーカー名
      mdfMakerFactoryCode: orderSupplier.supplierFactoryCode, // 生産工場コード
      mdfMakerFactoryName: orderSupplier.supplierFactoryName, // 生産工場名
      consignmentFactory: orderSupplier.consignmentFactory // 委託先工場名
    });
  }

  /**
   * メインのFormGroupを返す
   * @return FormGroup
   */
  private getFromGroup(): FormGroup {
    const ovd = new OrderValidatorDirective(); // カスタムバリデータをセット
    return this.formBuilder.group({
      id: [null], // 発注Id
      orderNumber: [null],  // 発注No
      partNoId: [null], // 品番ID
      partNo: [null], // 品番
      expenseItem: ['01'],  // 費目
      cutAutoType: [null],  // 自動裁断区分
      matlMakerCode: [null],  // 生地メーカー
      matlPartNo: [null], // 生地品番
      matlProductName: [null],  // 生地品名
      matlDeliveryAt: [null], // 生地納期
      matlMeter: [null],  // 生地メーター数
      matlUnitPrice: [null],  // 生地単価
      clothNumber: [null],  // 反番
      necessaryLengthActual: [null],  // 実用尺
      necessaryLengthUnit: [null],  // 用尺単位
      matlCost: [null], // 生地原価
      mdfMakerCode: [null], // 生産メーカー
      mdfMakerName: [null], // 生産メーカー名
      mdfMakerFactoryCode: [null],  // 生産工場コード
      mdfMakerFactoryName: [null],  // 生産工場名
      consignmentFactory: [null],   // 委託先工場
      brandCode: [null],  // ブランドコード
      productOrderAt: [null], // 製品発注日
      productCorrectionDeliveryAt: [null],  // 製品修正納期
      mdfStaffCode: [null, [Validators.required]], // 製造担当コード
      mdfStaffName: [null], // 製造担当者名
      productCompleteOrder: [null], // 製品完納区分
      quantity: [null, ovd.greaterThanZero], // 数量
      //  PRD_0114 単価０NG対応#7820 --mod JFE Start//
      //unitPrice: [null, [Validators.pattern(/^[0-9]+$/)]],  // 単価
      unitPrice: [null, ovd.equalZero],  // 単価
      //  PRD_0114 単価０NG対応#7820 --mod JFE END--//
      retailPrice: [null, [Validators.pattern(/^[0-9]+$/)]],  // 上代
      productCost: [null],  // 製品原価
      processingCost: [null], // 加工賃
      attachedCost: [null], // 附属代
      otherCost: [null, [Validators.pattern(/^[0-9]+$/)]],  // その他原価
      // PRD_0023 add JFE start
      accessoriesCost: [null],  // 付属品
      // PRD_0023 && No_65 mod JFE start
      totalCost: [null], // 原価合計
      /// PRD_0023 && No_65 mod JFE end
      importCode: [null], // 輸入区分
      cooCode: [null, [Validators.required]],  // 原産国コード
      cooName: [null], // 原産国名称
      application: [null],  // 摘要
      orderApproveStatus: [null],  // 発注承認ステータス
      orderSkus: this.formBuilder.array([]),  // 発注SKU情報
      costRate: [null], // 原価率(バリデーションで必要)
      orderFileInfo: [null], // 発注ファイル情報
      // PRD_0144 #10776 add JFE start
      relationNumber: [null, [Validators.pattern(/^0[0-9]{5}$/)]], // 生地発注番号(関連番号)
      // PRD_0144 #10776 add JFE end
    // PRD_0023 && No_65 mod JFE start
    }, { validator: Validators.compose([totalCostValidator]) }
    // PRD_0023 && No_65 mod JFE end
    );
  }

  /**
   * 組成情報の作成
   * @param compositionList 品番の組成情報
   */
  private creatCompositionsSkuList(compositionList: Compositions[]): void {
    this.compViewList = [];

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
            this.compViewList.push(orderViewCompositions);
          }
        }
      });
    }
  }

  /**
   * 見積り添付ファイルのリストの作成
   * @param itemFileInfos 品番ファイル情報リスト
   */
  private createEstimatesFiles(itemFileInfos: ItemFileInfo[]): void {
    // 見積り添付ファイルのリストを作成
    this.estimatesFile = itemFileInfos.filter(itemFileInfo => itemFileInfo.fileCategory === FileCategory.TYPE_ESTIMATES);
  }

  /**
   * Formに発注情報を設定する.
   * @param orderData 発注情報データ
   */
  private setFormValue(orderData: Order): void {
    this.mainForm = this.getFromGroup();
    this.onSkusOrUnitPriceValueChanges();  // 発注SKUと単価の変更監視
    const coo = this.cooMasterList.find(x => x.code1 === orderData.cooCode);

    this.mainForm.patchValue({
      id: orderData.id, // 発注Id
      orderNumber: orderData.orderNumber, // 発注No
      partNoId: orderData.partNoId, // 品番ID
      partNo: orderData.partNo, // 品番
      expenseItem: orderData.expenseItem, // 費目
      cutAutoType: orderData.cutAutoType, // 自動裁断区分
      matlMakerCode: orderData.matlMakerCode, // 生地メーカー
      matlPartNo: orderData.matlPartNo, // 生地品番
      matlProductName: orderData.matlProductName, // 生地品名
      matlDeliveryAt: orderData.matlDeliveryAt, // 生地納期
      matlMeter: orderData.matlMeter, // 生地メーター数
      // PRD_0023 && No_65 mod JFE start
      matlUnitPrice: orderData.matlUnitPrice, // 生地単価
      // PRD_0023 && No_65 mod JFE end
      clothNumber: orderData.clothNumber, // 反番
      necessaryLengthActual: orderData.necessaryLengthActual, // 実用尺
      necessaryLengthUnit: orderData.necessaryLengthUnit, // 用尺単位
      // PRD_0023 && No_65 del JFE start
      // matlCost: orderData.matlCost, // 生地原価
      // PRD_0023 && No_65 del JFE start
      cooCode: orderData.cooCode, // 原産国コード
      cooName: coo.item2, // 原産国名称
      productOrderAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(orderData.productOrderAt)), // 製品発注日
      productCorrectionDeliveryAt:
        this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(orderData.productCorrectionDeliveryAt)), // 製品修正納期
      mdfStaffCode: orderData.mdfStaffCode, // 製造担当コード
      mdfMakerCode: orderData.mdfMakerCode, // 生産メーカーコード
      mdfMakerName: orderData.mdfMakerName, // 生産メーカー名
      mdfMakerFactoryCode: orderData.mdfMakerFactoryCode, // 生産工場コード
      mdfMakerFactoryName: orderData.mdfMakerFactoryName, // 生産工場名
      consignmentFactory: orderData.consignmentFactory, // 委託先工場名
      productCompleteOrder: orderData.productCompleteOrder, // 製品完納区分
      quantity: orderData.quantity, // 数量
      unitPrice: orderData.unitPrice, // 単価
      retailPrice: orderData.retailPrice, // 上代
      productCost: orderData.productCost, // 製品原価
      // PRD_0023 && No_65 mod JFE start
      attachedCost: orderData.attachedCost, // 附属代
      // PRD_0023 && No_65 mod JFE end

      // PRD_0023 && No_65 del JFE start
      // processingCost: orderData.processingCost, // 加工賃
      // matlCost: this.itemData.matlCost,
      // accessoriesCost: this.itemData.accessoriesCost,
      // processingCost: this.itemData.processingCost,
      // accessoriesCost: orderData.attachedCost,
      // PRD_0023 && No_65 del JFE end

      // PRD_0023 && No_65 mod JFE start
      matlCost: orderData.matlCost,
      processingCost: orderData.processingCost,
      // PRD_0023 && No_65 mod JFE end
      otherCost: orderData.otherCost, // その他原価
      importCode: orderData.importCode, // 輸入区分
      application: orderData.application, // 摘要
      // PRD_0144 #10776 add JFE start
      relationNumber: (orderData.expenseItem == '04')
        ? orderData.relationNumber.toString().padStart(6, '0')
        : null, // 生地発注番号(関連番号)
      // PRD_0144 #10776 add JFE end
      orderApproveStatus: orderData.orderApproveStatus // 発注承認ステータス
    });

    // 製造担当名設定
    this.onChangeStaff(orderData.mdfStaffCode);
  }

  /**
   * 発注総数を合計して返す
   * @returns 発注総数
   */
  private sumAllOrderQuantity(): number {
    const orderSkus = this.orderService.convertSku(this.mainForm.getRawValue().orderSkus);
    const qantity = this.orderService.sumAllOrderQuantity(orderSkus);
    this.mainForm.patchValue({ quantity: qantity });  // formに数量を登録
    return qantity;
  }

  /**
   * 発注金額を合計して返す.
   * 単価 × 数量
   * @returns 発注金額
   */
  calculateOrderAmount(): number {
    const unitPrice = this.mainForm.controls.unitPrice.value;
    const quantity = this.sumAllOrderQuantity();

    let orderAmount = 0;
    if (StringUtils.isNotEmpty(unitPrice) && quantity != null) {
      orderAmount = unitPrice * quantity;
    }
    return orderAmount;
  }

  /**
   * 発注情報取得処理
   * @param orderId 発注ID
   */
  private async getOrderData(orderId: number): Promise<Order> {
    return await this.orderService.getOrderForId(orderId).toPromise().then(
      order => {
        this.orderData = order;
        this.orderSkuValue = order.orderSkus;
        this.setStatusLabel(order);

        // ファイルダウンロードアイコンを表示可能かチェック
        this.isAbleToDLFile = order.orderFileInfo != null
          && DateUtils.isWithinPeriod(new Date(), order.orderFileInfo.publishedAt, order.orderFileInfo.publishedEndAt);

        // 発注確定済みかチェック
        this.isOrderConfirmed(order);
        return Promise.resolve(order);
      },
      error => {
        return Promise.reject(error);
      }
    );
  }

  /**
   * 品番情報取得処理
   * @param partNoId 品番ID
   * @param orderData 発注情報
   */
  private async getItemData(partNoId: number): Promise<Item> {
    return await this.itemService.getItemForId(partNoId).toPromise().then(
      item => {
        this.itemData = item;
        this.skusValue = item.skus;
        this.creatCompositionsSkuList(item.compositions); // 組成情報の作成
        this.createEstimatesFiles(item.itemFileInfos);  // 見積りファイル情報の作成
        this.setSeasonValue(item.subSeasonCode); // サブシーズンのvalue値を設定
        return Promise.resolve(item);
      }, error => {
        return Promise.reject(error);
      }
    );
  }

  /**
   * 納品依頼リスト取得処理
   * @param orderId 発注ID
   */
  private async getDeliveryList(orderId: number): Promise<Delivery[]> {
    const requestParam = { orderId: orderId, idSortDesc: true } as DeliveryRequestSearchConditions;
    return await this.deliveryRequestService.getDeliveryRequestList(requestParam).toPromise().then(
      deliveryList => {
        this.deliveryList = deliveryList.items;
        return Promise.resolve(this.deliveryList);
      },
      error => {
        return Promise.reject(error);
      }
    );
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // patchValueしないとpipeの変換値がformにセットされない
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) {
      this.mainForm.patchValue({ [type]: ngbDate });
    }
  }

  /**
   * 発注情報登録処理
   */
  onSubmit(): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;
    // メッセージクリア
    this.clearErrorMessageWhenSubmit();

    // バリデーション
    if (this.isValidationError()) {
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    const formValue = this.mainForm.value; // mainformの値を取得する
    this.orderService.postOrder(formValue).toPromise().then(
      result => {
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 新規登録後の編集画面表示
        this.router.navigate(['orders', result['id'], 'edit'], { queryParams: { preEvent: PreEventParam.CREATE } });
      },
      error => this.handleSubmitError(error)
    );
  }

  /**
   * 発注情報更新処理
   */
  onUpdate(): void {
    this.loadingService.loadStart();
    this.isBtnLock = true;

    // メッセージクリア
    this.clearErrorMessageWhenSubmit();

    // バリデーションチェック
    if (this.isValidationError()) {
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    const formValue = this.mainForm.getRawValue();  // disable項目の値も取得する
    this.orderService.updateOrder(formValue).toPromise().then(
      () => {
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 更新後のURL書き換え(EDIT)
        this.router.navigate(['orders', formValue.id, 'edit'], { queryParams: { preEvent: PreEventParam.UPDATE } });
        this.isInitDataSetted = false;
        this.isDirty = false;
        // 同じパスで遷移になるため、フォーム再描画を実施
        this.initializeEditDisplay(PreEventParam.UPDATE, formValue.id);
      },
      error => this.handleSubmitError(error)
    );
  }

  /**
   * 発注情報確定処理
   */
  onConfirm(): void {
    // メッセージクリア
    this.clearErrorMessageWhenSubmit();

    // 入力フォームに変更があるか判断する。変更がある場合確定ボタンをロックし、確定前に更新するメッセージを表示
    if (this.mainForm.dirty) {
      this.isDirty = true;
      this.overall_error_msg_code = 'ERRORS.VALIDATE.UPDATE_BEFORE_CONFIRM';
      return;
    }
    this.loadingService.loadStart();
    this.isBtnLock = true;

    // バリデーションチェック
    if (this.isValidationError()) {
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }
    this.loadingService.loadStart();
    // 品番未登録の場合は処理しない
    if (this.itemData.registStatus !== RegistStatus.PART) {
      return;
    }

    const formValue = this.mainForm.getRawValue();  // disable項目の値も取得する
    this.orderService.confirmOrder(formValue).toPromise().then(
      () => {
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        // 発注確定後の画面遷移：
        if (this.affiliation === AuthType.AUTH_SUPPLIERS) {
          // メーカー権限：発注参照画面
          this.router.navigate(['orders', formValue.id, 'view'], { queryParams: { preEvent: PreEventParam.CONFIRM } });
        } else {
          // JUN権限：発注編集画面
          // 更新後のURL書き換え(EDIT)
          this.router.navigate(['orders', formValue.id, 'edit'], { queryParams: { preEvent: PreEventParam.CONFIRM } });
          this.isInitDataSetted = false;
          this.isDirty = false;
          // 同じパスで遷移になるため、フォーム再描画を実施
          this.initializeEditDisplay(PreEventParam.CONFIRM, formValue.id);
        }
      },
      error => this.handleSubmitError(error)
    );
  }

  /**
   * 削除時の確認モーダルを表示する
   */
  onDeleteConfirmModal(): void {
    // メッセージクリア
    this.clearErrorMessageWhenSubmit();
    // 確認モーダルを表示し、発注情報を削除する
    this.setConfirmModalAndDelete('受注情報を削除します。よろしいですか。');
  }

  /**
   * 確認モーダルを表示し、削除処理を行う
   * @param message モーダルに表示するメッセージ
   */
  private setConfirmModalAndDelete(message: string): void {
    // 確認モーダルを表示
    const modalRef = this.modalService.open(MessageConfirmModalComponent);
    modalRef.componentInstance.message = message;

    // 確認モーダルのメッセージの戻り値を確認して、削除処理を行う。
    modalRef.result.then(result => {
      if (result === 'OK') {
        this.deleteOrder();
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 発注情報を削除する
   */
  private deleteOrder(): void {
    this.isBtnLock = true;
    this.loadingService.loadStart();
    const orderId = this.mainForm.get('id').value as number; // 発注ID

    this.orderService.deleteOrder(orderId).toPromise().then(
      () => {
        // 削除後の画面表示：受注登録画面
        this.loadingService.loadEnd();
        this.isBtnLock = false;
        const partNoId = this.mainForm.get('partNoId').value as number; // 品番ID
        this.router.navigate(['orders', Path.NEW], { queryParams: { 'partNoId': partNoId, preEvent: PreEventParam.DELETE } });
      },
      error => this.handleSubmitError(error)
    );
  }


  /**
   * submit時にエラーメッセージをクリアする
   */
  private clearErrorMessageWhenSubmit(): void {
    this.overall_error_msg_code = '';
    this.overall_susses_msg_code = '';
    ExceptionUtils.clearErrorInfo();
    this.estimatesFileDLErrorMessageCode = null;
    this.orderFileDLErrorMessageCode = null;
    // PRD_0112 #7710 JFE add start
    this.isSeihin = false;
    // PRD_0112 #7710 JFE add end
  }

  /**
   * バリデーションチェックを行う。
   * @return true:エラーあり
   */
  private isValidationError(): boolean {
    this.submitted = true;
    this.skuInputComponent.submitted = true;  // 子コンポーネントのsumittedもtrueにする

    // バリデーションエラーの時に画面に戻す
    if (this.mainForm.invalid) {
      // デバッグしたいときはコメント外してください. push時はコメントアウトしてください.
      // this.formUtils.logValidationErrors(this.mainForm);

      this.isOtherCollapsed = true;
      this.overall_error_msg_code = 'ERRORS.VALID_ERROR';
      return true;
    }

    return false;
  }

  /**
   * API取得エラーメッセージを表示する。
   * @param error エラー情報
   */
  private showGetApiErrorMessage(error: any): void {
    this.isInitDataSetted = false;
    this.overall_error_msg_code = 'ERRORS.ANY_ERROR';
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      // サーバエラーエリアにメッセージ表示
      ExceptionUtils.displayErrorInfo('noDataErrorInfo', apiError.viewErrorMessageCode);
    }
    this.loadingService.loadEnd();
  }

  /**
   * Submit時のAPIエラー処理
   * @param error APIレスポンスエラー情報
   */
  private handleSubmitError(error: any): void {
    const apiError = ExceptionUtils.apiErrorHandler(error);
    let orderId: number; // 発注ID
    if (apiError != null && apiError.viewErrors != null) {
      const firstError = apiError.viewErrors[0];
      if (firstError != null) {
        switch (firstError.code) {
          case '400_O_01':   // (確定時)発注が既に削除されている場合は、新規登録画面へ遷移し、エラーを表示
            this.router.navigate(['orders', Path.NEW],
              { queryParams: { 'partNoId': this.orderData.partNoId, preEvent: PreEventParam.CONFIRM_ERROR } });
            break;
          case '400_O_02': // (確定/更新/削除時)発注が既に確定されている場合は、参照画面へ遷移し、エラーを表示
            orderId = this.mainForm.get('id').value as number;
            this.router.navigate(['orders', orderId, Path.VIEW], { queryParams: { preEvent: PreEventParam.SUBMIT_ERROR } });
            break;
          case '400_12': // 品番の外部連携区分:JUNoT登録以外の場合、参照画面へ遷移し、エラーを表示
            // (新規時)エラーメッセージ表示して終了
            if (this.path === Path.NEW) {
              this.overall_error_msg_code = 'ERRORS.400_12';
              break;
            }
            // (確定/更新/削除時)
            orderId = this.mainForm.get('id').value as number;
            this.router.navigate(['orders', orderId, Path.VIEW], { queryParams: { preEvent: PreEventParam.ITEM_READONLY_ERROR } });
            break;
          case '400_O_04': // (更新時)発注が既に削除されている場合は、新規登録画面へ遷移し、エラーを表示
            this.router.navigate(['orders', Path.NEW],
              { queryParams: { 'partNoId': this.orderData.partNoId, preEvent: PreEventParam.UPDATE_ERROR } });
            break;
          case '400_O_05': // (削除時)発注が既に削除されている場合は、新規登録画面へ遷移(エラーは表示しない)
            this.router.navigate(['orders', Path.NEW],
              { queryParams: { 'partNoId': this.orderData.partNoId, preEvent: PreEventParam.DELETE } });
            break;
          case '400_O_07': // (更新時)完納の場合は、参照画面へ遷移し、エラーを表示
            orderId = this.mainForm.get('id').value as number;
            this.router.navigate(['orders', orderId, Path.VIEW],
              { queryParams: { preEvent: PreEventParam.UPDATE_ERROR, errorType: APIErrorTypeParam.ORDER_COMPLETE } });
            break;
          case '400_O_08': // (更新時)納品依頼がある場合は、参照画面へ遷移し、エラーを表示
            orderId = this.mainForm.get('id').value as number;
            this.router.navigate(['orders', orderId, Path.VIEW],
              { queryParams: { preEvent: PreEventParam.UPDATE_ERROR, errorType: APIErrorTypeParam.DELIVERY_REGISTED } });
            break;
          case '400_02': // (更新時)商品(品番)が削除されている場合は一覧へ遷移
            orderId = this.mainForm.get('id').value as number;
            this.router.navigate(['orders']);
            break;
          case '400_O_06': // (登録時)消化委託商品だった場合は白画面にエラーメッセージ表示し
            ExceptionUtils.displayErrorInfo('noDataErrorInfo', 'ERRORS.400_O_06');
            this.isInitDataSetted = false;
            break;
          // PRD_0112 #7710 JFE add start
          case '400_I_32': //サブミット時、製品メーカーの発注区分が「０」の場合はエラー表示する
              this.isSeihin = true;
            break;
          // PRD_0112 #7710 JFE add end
          default:
            break;
        }
      }
    }
    // PRD_0112 #7710 JFE mod start
    //発注区分エラーは、バリデーションチェックをしないがメッセージはバリエーションエラーと同じ内容を表示させるため、条件分の追加
    // this.overall_error_msg_code = 'ERRORS.ANY_ERROR';
    if (apiError.viewErrors[0].code = '400_I_32') {
      this.overall_error_msg_code = 'ERRORS.VALID_ERROR';
    }else {
      this.overall_error_msg_code = 'ERRORS.ANY_ERROR';
    }
    // PRD_0112 #7710 JFE mod end
    this.loadingService.loadEnd();
    this.isBtnLock = false;
  }

  // PRD_0023 mod JFE start
  /**
   * 原価率計算
   * @returns 原価率
   */
  calculateCostRate(): number {
    const rate = CalculationUtils.calcRate(this.calculateTotal().toString(), this.mainForm.controls.retailPrice.value);
    this.mainForm.patchValue({ costRate: rate });
    return rate;
  }
// PRD_0023 && No_65 mod JFE start
    /**
   * 原価合計.
   * @returns 原価合計
   */
    calculateTotal(): number {
      if (this.mainForm.controls) {
        const matlCost: number = this.mainForm.controls.matlCost.value || 0;
        const processingCost: number = this.mainForm.controls.processingCost.value || 0;
        const assrCost: number = this.mainForm.controls.attachedCost.value || 0;
        const otherCost: number = this.mainForm.controls.otherCost.value || 0;
        const totalCost: number = +matlCost + +processingCost + +assrCost + +otherCost;
        return totalCost;
      }
      return 0;
    }
// PRD_0023 && No_65 mod JFE end
  /**
   * 見積ファイル操作処理
   * @param index ファイルリストのindex
   */
  onEstimatesFileDownlad(index: number): void {
    this.estimatesFileDLErrorMessageCode = null;
    if (this.estimatesFile[index].fileNoId && this.estimatesFile[index].file == null) {
      this.fileService.fileDownload(this.estimatesFile[index].fileNoId).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.estimatesFile[index].file = data.blob;
        FileUtils.downloadFile(data.blob, this.estimatesFile[index].fileName);
      }, () => this.estimatesFileDLErrorMessageCode = 'ERRORS.FILE_DL_ERROR');
    } else {
      // ローカルで参照している or 一度ファイルをダウンロードしている
      FileUtils.downloadFile(this.estimatesFile[index].file, this.estimatesFile[index].fileName);
    }
  }

  /**
   * 発注書ファイルダウンロードアイコン押下処理
   * @param orderFileNoId ファイルID
   */
  onFileDownload(orderFileNoId: number): void {
    this.orderFileDLErrorMessageCode = null;

    const idExists = this.orderFileList.some(orderFile => {
      if (orderFile.id === orderFileNoId) { // 一度ファイルをダウンロードしている
        FileUtils.downloadFile(orderFile.fileBlob, orderFile.fileName);
        return true;
      }
    });

    // キャッシュにない場合はAPIから取得
    if (!idExists) {
      this.fileService.fileDownload(orderFileNoId.toString()).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.orderFileList.push({ id: orderFileNoId, fileName: data.fileName, fileBlob: data.blob });
        FileUtils.downloadFile(data.blob, data.fileName);
      }, () => this.orderFileDLErrorMessageCode = 'ERRORS.FILE_DL_ERROR');
    }
  }

  /**
   * サブシーズンのコード名を設定する.
   * @param subSeasonCode サブシーズンコード
   */
  private setSeasonValue(subSeasonCodeStr: string): void {
    const subSeasonCode = Number(subSeasonCodeStr);
    const seasonCodeItem = CodeMaster.subSeason.find(codeItem => codeItem.id === subSeasonCode);
    this.seasonValue = seasonCodeItem.value;
  }

  /**
   * サブメニューリンククリック時の処理.
   * ページ内遷移.
   * @param id HTML要素のID
   * @returns false
   */
  scrollEvent(id: string): boolean {
    window.scrollTo(0,
      window.pageYOffset
      + document.getElementById(id).getBoundingClientRect().top
      - document.getElementById('header').getBoundingClientRect().height
    );
    return false;
  }

  /**
   * 発注確定済みか判断する。
   * @param orderData 発注情報
   */
  private isOrderConfirmed(orderData: Order): void {
    const vOrder = { orderApproveStatus: orderData.orderApproveStatus } as VOrder;
    if (!BusinessCheckUtils.isConfirmOrderOk(vOrder)) {
      // 発注未確定の場合はreturn。
      return;
    }
    // 発注確定済みの場合は発注確定フラグをtrueにする
    this.isConfirmed = true;
  }

  /**
   * 完納または自動完納か判定する。
   * @param orderData 発注情報(Order)
   * @returns 完納または自動完納であればtrue
   */
  private isCompleteOrder(orderData: Order): boolean {
    const vOrder = {
      allCompletionType: orderData.allCompletionType,
      productCompleteOrder: orderData.productCompleteOrder
    } as VOrder;

    if (BusinessCheckUtils.isCompleteOrder(vOrder)) {
      // 完納の場合は完納フラグをtrueにし、trueを返す
      this.isOrderComplete = true;
      return true;
    }
    // 未完納の場合はfalseを返す
    return false;
  }

  /**
   * ステータスラベルの表示文言を設定
   * @param orderData 発注情報
   */
  private setStatusLabel(orderData: Order): void {
    switch (orderData.orderApproveStatus) {
      case OrderApprovalStatus.ACCEPT:
        this.statusLabel = '発注承認済';
        break;
      case OrderApprovalStatus.CONFIRM:
      case OrderApprovalStatus.REJECT:
        this.statusLabel = '発注確定済';
        break;
      default:
        this.statusLabel = '発注確定待ち';
        break;
    }
  }

  /**
   * 生産メーカー変更時の処理.
   * @param makerCode 入力値
   */
  onChangeMaker(makerCode: string): void {
    const orderSupplier = this.itemData.orderSuppliers.find(os => os.supplierCode === makerCode);
    this.mainForm.patchValue({
      mdfMakerCode: orderSupplier.supplierCode, // 生産メーカーコード
      mdfMakerName: orderSupplier.supplierName, // 生産メーカー名
      mdfMakerFactoryCode: orderSupplier.supplierFactoryCode, // 生産工場コード
      mdfMakerFactoryName: orderSupplier.supplierFactoryName, // 生産工場名
      consignmentFactory: orderSupplier.consignmentFactory // 委託先工場名
    });
  }

  /**
   * 担当者ID変更時の処理.
   * @param mdfStaffCode 入力値
   */
  onChangeStaff(mdfStaffCode: string): void {
    // 最大長まで入力されていない場合は名称を削除して終了
    if (mdfStaffCode == null || mdfStaffCode.length !== 6) {
      this.mainForm.patchValue({ mdfStaffName: null });
      return;
    }

    // コードマスタ(スタッフ)取得APIコール
    this.junpcCodmstService.getStaffs({
      staffType: StaffType.PRODUCTION.toString(),
      brand: this.mainForm.controls.brandCode.value,
      searchType: SearchTextType.CODE_PERFECT_MATCH,
      searchText: mdfStaffCode
    } as JunpcCodmstSearchCondition).pipe(
      map(x => x != null && ListUtils.isNotEmpty(x.items) ? x.items[0].item2 : null),
      tap(setValue => this.mainForm.patchValue({ mdfStaffName: setValue }))
    ).subscribe();
  }

  // PRD_0144 #10776 add JFE start
  /**
   * 費目切り替え時の処理.
   * @param value 入力値
   */
  onChangeExpenseItem(value: string): void {
    this.mainForm.patchValue({ expenseItem: value });
    const formControls = this.f;
    // disable設定
    // 費目01
    if (formControls.expenseItem.value === '01') {
      this.mainForm.patchValue({ relationNumber: null });
      formControls.relationNumber.disable();
      //PRD_0206 && TEAM_ALBUS-16 add start
      this.mainForm.patchValue({ matlUnitPrice: null });
      formControls.matlUnitPrice.disable();
      //PRD_0206 && TEAM_ALBUS-16 add end
    }
    // 費目04
    if (formControls.expenseItem.value === '04') {
      this.mainForm.patchValue({ relationNumber: null });
      formControls.relationNumber.enable();
      //PRD_0206 && TEAM_ALBUS-16 add start
      this.mainForm.patchValue({ matlUnitPrice: null });
      formControls.matlUnitPrice.enable();
      //PRD_0206 && TEAM_ALBUS-16 add end
    }
  }
  // PRD_0144 #10776 add JFE end

  /**
   * 担当者を検索するモーダルを表示する。
   */
  openSearchStaffModel(): void {
    const modalRef = this.modalService.open(SearchStaffModalComponent);

    modalRef.componentInstance.staffType = StaffType.PRODUCTION;
    const controls = this.mainForm.controls;
    modalRef.componentInstance.brandCode = controls.brandCode.value;
    modalRef.componentInstance.defaultStaffCode = controls.mdfStaffCode.value;
    modalRef.componentInstance.defaultStaffName = controls.mdfStaffName.value;

    from(modalRef.result).pipe(
      filter(result => result != null),
      tap((result: JunpcCodmst) =>
        this.mainForm.patchValue({
          mdfStaffCode: result.code1,
          mdfStaffName: result.item2
        })
      ),
      catchError(() => of(null)) // バツボタンクリック時は何もしない
    ).subscribe();
  }

  /**
   * 原産国名称を入力した時の処理。
   * 原産国リストに存在する名称であればformの原産国コードに設定する。
   * @param cooName 原産国名称
   */
  onInputCooName(cooName: string): void {
    const coo = this.cooMasterList.find(x => x.item2.trim() === cooName.trim());
    const cooCode = coo == null ? null : coo.code1;
    this.mainForm.patchValue({ cooCode: cooCode });
  }

  /**
   * 発注確定済または完納済の場合、非活性と判定する(trueを返す).
   * @returns true:非活性
   */
  isConfirmedOrCompleted(): boolean {
    return this.isConfirmed || this.isOrderComplete;
  }

  /**
   * 発注承認済か判定する。
   * @param orderData 発注情報
   * @returns 発注承認済であればtrue
   */
  isApprovalOk(): boolean {
    return this.orderData == null ? false : BusinessCheckUtils.isApprovalOk(this.orderData);
  }

  /**
   * 発注承認済または完納済の場合、非活性と判定する(trueを返す).
   * ※発注承認済の場合は発注確定済とみなす
   * @returns true:非活性
   */
  isApprovalOkOrCompleted(): boolean {
    return this.isApprovalOk() || this.isOrderComplete;
  }

  /**
   * 納品依頼済または完納済の場合、非活性と判定する(trueを返す).
   * ※納品依頼済の場合は発注確定済、発注承認済とみなす
   * @returns true:非活性
   */
  isDeliveriedOrCompleted(): boolean {
    return ListUtils.isNotEmpty(this.deliveryList) || this.isOrderComplete;
  }

  /**
   * 納品依頼済または完納済またはメーカー権限の場合、
   * 非活性と判定する(trueを返す).
   * @returns true:非活性
   */
  isDeliveriedOrCompletedOrAuthSuppriers(): boolean {
    return this.isDeliveriedOrCompleted()
      || this.affiliation === AuthType.AUTH_SUPPLIERS;
  }

  // PRD_0144 #10776 add JFE start
  /**
   * 費目が製品発注(01)の場合、非活性と判定する(trueを返す).
   * @returns true:非活性
   */
  isProduct(): boolean {
    const formControls = this.f;
    return formControls.expenseItem.value === '01';
  }
  // PRD_0144 #10776 add JFE end
}
