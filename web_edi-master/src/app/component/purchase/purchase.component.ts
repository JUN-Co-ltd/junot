import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, AbstractControl, Validators } from '@angular/forms';

import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { TranslateService } from '@ngx-translate/core';

import { Subscription, Observable, of, forkJoin, combineLatest } from 'rxjs';
import { tap, flatMap, catchError, finalize, map, filter } from 'rxjs/operators';

import { SupplierType, SearchTextType, Path, ValidatorsPattern, PreEventParam, Const , CarryType} from 'src/app/const/const';
import { PurchaseType } from 'src/app/const/purchase-type';
import { PurchaseDataType } from 'src/app/const/purchase-data-type';
import { Dictionaries } from 'src/app/const/dictionaries';

import { SeasonService } from 'src/app/service/bo/season.service';
import { FormUtilsService } from 'src/app/service/bo/form-utils.service';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { PurchaseService } from '../../service/bo/purchase.service';
import { HeaderService } from 'src/app/service/header.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { PurchaseHttpService } from 'src/app/service/purchase-http.service';
import { DeliveryRequestService } from 'src/app/service/delivery-request.service';
import { OrderService } from 'src/app/service/order.service';
import { ItemService } from 'src/app/service/item.service';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { SkuService } from 'src/app/service/bo/sku.service';
import { AllocationService } from 'src/app/service/bo/allocation.service';
import { DeliveryService } from 'src/app/service/bo/delivery.service';
import { NumberUtilsService } from 'src/app/service/bo/number-utils.service';

import { PurchaseTransactionData } from './interface/purchase-transaction-data';
import { Division } from './interface/division';
import { Purchase } from './interface/purchase';

import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { Delivery } from 'src/app/model/delivery';
import { Order } from 'src/app/model/order';
import { Item } from 'src/app/model/item';
import { JunpcCodmstSearchCondition } from 'src/app/model/junpc-codmst-search-condition';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { DeliveryDetail } from 'src/app/model/delivery-detail';
import { ColorSize } from 'src/app/model/color-size';
import { LgSendType } from 'src/app/const/lg-send-type';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';
import { BusinessError } from 'src/app/model/bussiness-error';

@Component({
  selector: 'app-purchase',
  templateUrl: './purchase.component.html',
  styleUrls: ['./purchase.component.scss']
})
export class PurchaseComponent implements OnInit, OnDestroy {

  PATH = Path;

  /** パス */
  path: string;

  /** 入荷場所リスト. */
  stores: JunpcTnpmst[];

  /** 仕入先名称. */
  mdfMakerName: string;

  /** ブランド名称. */
  brandName: string;

  /** 費目名称. */
  expenseItemName: string;

  /** サブシーズン名 */
  subSeasonName: string;

  /** 配分課リスト */
  divisionList: Division[];

  /** 納品依頼情報. */
  delivery: Delivery;

  /** 発注情報 */
  order: Order;

  /** 品番情報 */
  item: Item;

  /** フォーム */
  mainForm: FormGroup;

  /** ローディング表示フラグ */
  isLoading: boolean;

  /** 画面表示フラグ */
  showScreen = false;

  /** submitted押下済フラグ */
  private submitted = false;

  /** LG送信未指示がある */
  existsLgNoInstruction = false;

  /** arrivalCount必須エラーフラグ */
  isArrivalCountRequiredError = false;

  /** arrivalCoun形式tエラーフラグ */
  isArrivalCountPatternError = false;

  /** arrivalCount最大値エラーフラグ */
  isArrivalCountMaxError = false;

  /** 現在日 */
  currentDate: NgbDateStruct;

  /** 画面に表示するメッセージ */
  message = {
    /** フッター */
    footer: {
      /** 正常系 */
      success: { code: '', param: null },
      /** 異常系 */
      error: { code: '', param: null }
    }
  };

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

  /**
   * @returns this.mainForm.controls
   */
  get fCtrl(): { [key: string]: AbstractControl } {
    return this.mainForm.controls;
  }

  constructor(
    private router: Router,
    private headerService: HeaderService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private translateService: TranslateService,
    private purchaseHttpService: PurchaseHttpService,
    private deliveryRequestService: DeliveryRequestService,
    private orderService: OrderService,
    private itemService: ItemService,
    private junpcCodmstService: JunpcCodmstService,
    private junpcSirmstService: JunpcSirmstService,
    private junpcTnpmstService: JunpcTnpmstHttpService,
    private skuService: SkuService,
    private allocationService: AllocationService,
    private deliveryService: DeliveryService,
    private dateUtils: DateUtilsService,
    private purchaseService: PurchaseService,
    private seasonService: SeasonService,
    private formUtils: FormUtilsService,
    private numberUtils: NumberUtilsService,
    private deliveryHttpService: DeliveryRequestService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(isLoading => this.isLoading = isLoading);
    let loadingToken = null;

    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap
    ]).pipe(
      tap(() => this.clearMessage()),
      flatMap(([paramMap, queryParamMap]) => forkJoin(
        of(Number(paramMap.get('id'))),
        this.loadingService.start(),
        of(queryParamMap.get('arrivalShop')),
        this.translateInitialText(this.numberUtils.defaultNull(queryParamMap.get('preEvent'))),
      )),
      tap(([_, token]) => {
        loadingToken = token;
        this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
        this.currentDate = this.dateUtils.generateCurrentDate();
      }),
      filter(([deliveryId]) => deliveryId != null),
      flatMap(([deliveryId, _, arrivalShop]) => forkJoin(of(arrivalShop), this.fetchData(deliveryId, this.path))),
      tap(([arrivalShop, data]) => {
        this.createForm(arrivalShop, data);
        this.showScreen = true;
      })
    ).subscribe(
      () => this.loadingService.stop(loadingToken),
      err => {
        this.loadingService.stop(loadingToken);
        this.showErrorModal(err);
      }
    );
  }

  ngOnDestroy(): void {
    this.loadingSubscription.unsubscribe();
  }

  /**
   * @param deliveryId 納品ID
   * @param path パス
   * @returns Observable<PurchaseTransactionData>
   */
  private fetchData(deliveryId: number, path: Path): Observable<PurchaseTransactionData> {
    return forkJoin(
      this.fetchBaseTransactionData(deliveryId),
      this.fetchPurchase(deliveryId, path),
      this.fetchColors(),
      this.fetchStores(),
      this.checkDirectDelivery(deliveryId)
    ).pipe(
      map(([tranData, purchase, colors]) =>
        Object.assign(tranData, { purchase }, { colors })
      )
    );
  }

  /**
   * テキストを翻訳する.
   * @param preEvent 遷移前イベント
   * @returns Observable<string>
   */
  private translateInitialText(preEvent: number): Observable<string> {
    return this.translateService.get('TITLE.PURCHASE').pipe(
      tap(title => this.setSubmitMessage(preEvent, title))
    );
  }

  /**
   * Submit完了メッセージを設定する.
   * @param preEvent 遷移前イベント
   * @param title Submit処理のメッセージタイトル
   */
  private setSubmitMessage(preEvent: number, title: string): void {
    const code = Dictionaries.SUBMIT_MSG_CODE[preEvent];
    this.message.footer.success = {
      code: code == null ? '' : code,
      param: { value: title }
    };
  }

  /**
   * トランザクションデータを取得する.
   * @param path パス
   * @param deliveryId 納品ID
   * @returns Observable<PurchaseTransactionData>
   */
  private fetchBaseTransactionData(deliveryId: number): Observable<PurchaseTransactionData> {
    return this.deliveryRequestService.getDeliveryRequestForId(deliveryId).pipe(
      tap(delivery => this.delivery = delivery),
      flatMap(delivery =>
        forkJoin(
          of(delivery),
          this.fetchOrder(delivery.orderId),
          this.fetchItem(delivery.partNoId)
        )
      ),
      flatMap(([delivery, order, item]) =>
        forkJoin(
          of(order),
          of(delivery),
          this.fetchAllocations(delivery.deliveryDetails, item.brandCode),
          this.fetchMdfMakerName(order.mdfMakerCode),
          this.fetchBrandName(item.brandCode),
          this.fetchExpenseItemName(order.expenseItem)
        )
      ),
      map(([order, delivery, allocations]: [Order, Delivery, JunpcCodmst[]]) =>
        ({ order, delivery, allocations } as PurchaseTransactionData)
      )
    );
  }

  /**
   * @param orderId 発注ID
   * @returns 発注情報
   */
  private fetchOrder(orderId: number): Observable<Order> {
    return this.orderService.getOrderForId(orderId).pipe(
      map(order => this.order = order)
    );
  }

  /**
   * @param partNoId 品番ID
   * @returns 品番情報
   */
  private fetchItem(partNoId: number): Observable<Item> {
    return this.itemService.getItemForId(partNoId).pipe(
      tap(item => this.subSeasonName = this.seasonService.findSubSeasonValue(item.subSeasonCode)),
      map(item => this.item = item)
    );
  }

  /**
   * @returns 入荷場所リスト
   */
  private fetchStores(): Observable<JunpcTnpmst[]> {
    return this.junpcTnpmstService.search({ shpcdAhead: Const.PURCHASE_SHPCD_PREFIX } as JunpcTnpmstSearchCondition).pipe(
      map(res => this.stores = res.items)
    );
  }

  /**
   * 直送の場合、エラーにする.
   * @param deliveryId 納品ID
   * @return Observable<void>
   */
  private checkDirectDelivery(deliveryId: number): Observable<void> {
    return this.deliveryHttpService.getDeliveryRequestForId(deliveryId).pipe(
      tap(res => {
        if (res.deliveryDetails.some(dd => CarryType.DIRECT === dd.carryType)) {
          throw new BusinessError('ERRORS.400_PC_08');
        }
      }),
      map(() => null));
  }

  /**
   * @returns カラーリスト
   */
  private fetchColors(): Observable<JunpcCodmst[]> {
    return this.junpcCodmstService.getColors({} as JunpcCodmstSearchCondition).pipe(
      map(res => res.items)
    );
  }

  /**
   * @param mdfMakerCode 生産メーカーコード
   * @returns 生産メーカー名
   */
  private fetchMdfMakerName(mdfMakerCode: string): Observable<string> {
    return this.junpcSirmstService.getSirmst({
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_PERFECT_MATCH,
      searchText: mdfMakerCode
    } as JunpcSirmstSearchCondition).pipe(
      map(res => this.mdfMakerName = res.items[0].name)
    );
  }

  /**
   * @param brandCode ブランドコード
   * @returns ブランド名
   */
  private fetchBrandName(brandCode: string): Observable<string> {
    return this.junpcCodmstService.searchBrands({
      code1: brandCode
    } as JunpcCodmstSearchCondition).pipe(
      map(res => this.brandName = res.items[0].item1)
    );
  }

  /**
   * @param expenseCode 費目コード
   * @returns 費目名
   */
  private fetchExpenseItemName(expenseCode: string): Observable<string> {
    return this.junpcCodmstService.searchExpenseItems({
      code1: expenseCode
    } as JunpcCodmstSearchCondition).pipe(
      map(res => this.expenseItemName = res.items[0].item1)
    );
  }

  /**
   * @param deliveryDetails 納品明細情報
   * @param brandCode ブランドコード
   * @returns 配分課マスタリスト
   */
  private fetchAllocations(deliveryDetails: DeliveryDetail[], brandCode: string): Observable<JunpcCodmst[]> {
    const code1s = deliveryDetails.map(dd => brandCode + dd.divisionCode);
    return this.junpcCodmstService.getAllocations({
      code1s: code1s,
      searchType: '1'
    } as JunpcCodmstSearchCondition).pipe(
      map(res => res.items)
    );
  }

  /**
   * @param deliveryId 納品ID
   * @param path パス
   * @returns 仕入情報
   */
  private fetchPurchase(deliveryId: number, path: Path): Observable<Purchase> {
    if (path === Path.NEW) { return of(null); }
    return this.purchaseHttpService.getByDeliveryId(deliveryId).pipe(
      tap(res => this.existsLgNoInstruction = this.purchaseService.existsLgNoInstruction(res)));
  }

  /**
   * フォームを作成する.
   * @param arrivalShop クエリパラメータの店舗コード
   * @param data 取得データ
   */
  private createForm(arrivalShop: string, data: PurchaseTransactionData): void {
    const order = data.order;
    const delivery = data.delivery;

    this.mainForm = this.formBuilder.group({
      dataType: [PurchaseDataType.SR],
      purchaseType: [PurchaseType.ADDITIONAL_PURCHASE],
      arrivalPlace: [null],
      arrivalShop: [arrivalShop, [Validators.required]],
      supplierCode: [order.mdfMakerCode],
      mdfMakerFactoryCode: [order.mdfMakerFactoryCode],
      arrivalAt: [this.currentDate, [Validators.required]],
      makerVoucherNumber: [null, [
        Validators.maxLength(6),
        Validators.minLength(6),
        Validators.pattern(new RegExp(ValidatorsPattern.NO_MINUS_HALF_WIDTH_ALPHANUMERIC))
      ]],
      purchaseVoucherNumber: [null],
      purchaseVoucherLine: [null],
      partNoId: [order.partNoId],
      partNo: [order.partNo],
      purchaseSkus: this.generateSkuFormArray(data),
      nonConformingProductType: [false],
      instructNumber: ['000000'],
      instructNumberLine: [0],
      orderId: [delivery.orderId],
      orderNumber: [delivery.orderNumber],
      purchaseCount: [delivery.deliveryCount],
      purchaseUnitPrice: [delivery.nonConformingProductType === true ? delivery.nonConformingProductUnitPrice : order.unitPrice],
      deliveryId: [delivery.id],
      totalLot: [0]
    });

    this.divisionList = this.generateDivisionList(data);
    this.patchPurchasesToMainForm(this.mainForm, data.purchase, this.divisionList);
    this.setDisable(data, this.mainForm);
  }

  /**
   * 非活性制御
   * @param data 取得データ
   * @param mainForm フォーム
   */
  private setDisable(data: PurchaseTransactionData, mainForm: FormGroup): void {
    if (this.purchaseService.noExistsLgInstruction(data)) { return; }
    // LG送信指示済みが1つでもあれば非活性

    mainForm.controls.arrivalShop.disable();
    mainForm.controls.arrivalAt.disable();
    mainForm.controls.makerVoucherNumber.disable();
  }

  /**
   * 配分課リストへを作成する.
   * @param data 取得データ
   * @returns 配分課リスト
   */
  private generateDivisionList(data: PurchaseTransactionData): Division[] {
    const calculateFn = data.purchase == null ?
      this.deliveryService.calculateDivisionTotalLot : this.purchaseService.calculateDivisionTotalLot(data.purchase);
    return data.delivery.deliveryDetails.map(detail => (
      {
        code: detail.divisionCode,
        name: this.allocationService.findAllocationName(data.allocations, detail.divisionCode),
        totalLot: calculateFn(detail)
      })
    );
  }

  /**
   * フォームに仕入の値を設定する.
   * @param mainForm フォーム
   * @param purchase 仕入情報
   * @param divisions 配分課リスト
   */
  private patchPurchasesToMainForm(mainForm: FormGroup, purchase: Purchase, divisions: Division[]): void {

    mainForm.patchValue({ totalLot: this.purchaseService.calculateTotalLot(divisions) });

    if (purchase == null) { return; }

    mainForm.patchValue({
      dataType: purchase.dataType,
      purchaseType: purchase.purchaseType,
      arrivalPlace: purchase.arrivalPlace,
      arrivalShop: purchase.arrivalShop,
      arrivalAt: this.dateUtils.parse(purchase.arrivalAt as Date),
      makerVoucherNumber: purchase.makerVoucherNumber,
      purchaseVoucherNumber: purchase.purchaseVoucherNumber,
      purchaseVoucherLine: purchase.purchaseVoucherLine,
      nonConformingProductType: purchase.nonConformingProductType
    });
  }

  /**
   * @param data 取得データ
   * @returns SkuFormArray
   */
  private generateSkuFormArray(data: PurchaseTransactionData): FormArray {
    const deliveryDetails = data.delivery.deliveryDetails;
    const distinct = this.deliveryService.distinctColorSize(deliveryDetails);
    const colorSizeList = this.deliveryService.sortByAsc(distinct);

    const caluculateFn = data.purchase == null ?
      this.deliveryService.calculateSkuTotalLot(data.delivery.deliveryDetails) : this.purchaseService.calculateSkuTotalLot(data.purchase);

    return this.formBuilder.array(
      colorSizeList.map((sku, idx) =>
        this.formBuilder.group({
          colorCode: [sku.colorCode],
          colorName: [this.skuService.findColorName(data.colors, sku.colorCode)],
          size: [sku.size],
          purchaseDivisions: this.generateDivisionFormArray(data, sku),
          totalLot: [caluculateFn(sku)],
          isFirstColor: [this.skuService.isFirstColor(colorSizeList, sku, idx)]
        })
      )
    );
  }

  /**
   * @param data 取得データ
   * @param sku 処理中のSKU
   * @returns DivisionFormArray
   */
  private generateDivisionFormArray(data: PurchaseTransactionData, sku: ColorSize): FormArray {
    const generateFn = this.generateDivisionFormGroup(sku);
    const fa = this.formBuilder.array(data.delivery.deliveryDetails.map(generateFn));

    this.patchPurchasesToDivisionFormArray(data.purchase, fa, sku);
    return fa;
  }

  /**
   * @param sku 処理中のSKU
   * @param detail 処理中の納品明細
   * @retunr DivisionFormGroup
   */
  private generateDivisionFormGroup = (sku: ColorSize) => (detail: DeliveryDetail): FormGroup => {
    const targetSku = this.deliveryService.findSku(detail.deliverySkus, sku);
    const initialSet = targetSku == null ? { value: null, disabled: true } : { value: targetSku.deliveryLot, disabled: false };
    return this.formBuilder.group({
      id: [null],
      arrivalCount: [
        {
          value: initialSet.value,
          disabled: initialSet.disabled
        },
        [
          Validators.required,
          Validators.max(this.purchaseService.getMaxArrivalCountVaue(targetSku)),
          Validators.pattern(ValidatorsPattern.NUMERIC)
        ]
      ],
      fixArrivalCount: [null],
      divisionCode: [detail.divisionCode],
      lgSendType: [null]
    });
  }

  /**
   * DivisionFormArrayに仕入の値をpatchする.
   * @param purchase 仕入情報
   * @param divisionFA DivisionFormArray
   * @param sku 処理中のSKU
   */
  private patchPurchasesToDivisionFormArray(purchase: Purchase, divisionFA: FormArray, sku: ColorSize): void {
    if (purchase == null) { return; }

    const targetSku = purchase.purchaseSkus.find(ss => this.skuService.isMatchSku(ss, sku));
    if (targetSku == null) { return; }

    divisionFA.controls.forEach(fc => {
      const targetDivision = targetSku.purchaseDivisions.find(sd => sd.divisionCode === fc.get('divisionCode').value);
      if (targetDivision != null) {
        fc.patchValue({
          id: targetDivision.id,
          arrivalCount: targetDivision.arrivalCount,
          fixArrivalCount: targetDivision.fixArrivalCount,
          lgSendType: targetDivision.lgSendType
        });
        // LG送信指示済みの場合、非活性
        if (LgSendType.INSTRUCTION === targetDivision.lgSendType) {
          fc.disable();
        }
      }
    });
  }

  /**
   * 登録ボタン押下時の処理.
   */
  onCreate(): void {
    this.loadingAtSubmit(
      this.purchaseHttpService.create(this.mainForm),
      PreEventParam.CREATE
    );
  }

  /**
   * 更新ボタン押下時の処理.
   */
  onUpdate(): void {
    this.loadingAtSubmit(
      this.purchaseHttpService.update(this.mainForm),
      PreEventParam.UPDATE
    );
  }

  /**
   * Submit時のローディング処理.
   * @param httpFn http通信関数
   * @param event Submitイベント
   */
  private loadingAtSubmit(httpFn: () => Observable<any>, event: PreEventParam): void {
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      filter(() => this.isValid()),
      flatMap(() => httpFn()),
      tap(() => this.navigateAftetSubmit(event)),
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid = (): boolean => {
    this.submitted = true;

    // PRD_0041 add SIT start
    if (this.checkDivisionDista())
    {
      this.message.footer.error = { code: 'ERRORS.DIVISION18_HQ_NOTALLOWED', param: null };
      return false;
    }
    // PRD_0041 add SIT end
    if (this.mainForm.invalid) {
      // デバッグしたいときはコメント外してください. push時はコメントアウトしてください.
      // this.formUtils.logValidationErrors(this.mainForm);

      // 入荷数量エラーメッセージ表示設定
      this.setArrivalCountErrorMessage(this.mainForm.get('purchaseSkus') as FormArray);

      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      this.formUtils.markAsTouchedAllFields(this.mainForm);
      return false;
    }
    return true;
  }

  /**
   * Submit完了後の遷移処理.
   * @param event イベント区分
   */
  private navigateAftetSubmit(event: PreEventParam): void {
    this.submitted = false;
    this.router.navigate(
      ['purchases', this.delivery.id, Path.EDIT],
      { queryParams: { preEvent: event, t: new Date().valueOf() } });
  }

  /**
   * 仕入数Blur時の処理.
   * @param skuFG SKUフォームグループ
   * @param divisionFG 配分課フォームグループ
   */
  onBlurArrivalCount(skuFG: FormGroup, divisionFG: FormGroup): void {
    const formVal: Purchase = this.mainForm.getRawValue();

    // 合計仕入数を再計算する
    this.patchSkuTotalLot(skuFG, formVal);
    this.patchDivisionTotalLot(divisionFG, formVal);
    this.patchTotalLot(this.divisionList);

    // 入荷数量エラーメッセージ表示設定
    this.setArrivalCountErrorMessage(this.mainForm.get('purchaseSkus') as FormArray);
  }

  /**
   * SKU別の仕入数合計値patch
   * @param skuFG SKUFormGroup
   * @param formVal フォーム値
   */
  private patchSkuTotalLot(skuFG: FormGroup, formVal: Purchase): void {
    const targetSkuTotalLot = this.purchaseService.calculateSkuFormTotalLot(formVal.purchaseSkus, skuFG);
    skuFG.patchValue({ totalLot: targetSkuTotalLot });
  }

  /**
   * 課別の仕入数合計値patch
   * @param divisionFG 課別FormGroup
   * @param formVal フォーム値
   */
  private patchDivisionTotalLot(divisionFG: FormGroup, formVal: Purchase): void {
    const targetDivisionTotalLot =
      this.purchaseService.calculateDivisionFormTotalLot(formVal.purchaseSkus, divisionFG.get('divisionCode').value);
    this.divisionList
      .find(division => division.code === divisionFG.get('divisionCode').value)
      .totalLot = targetDivisionTotalLot;
  }

  /**
   * 全数量の仕入数合計値patch
   * @param divisionList SKUFormGroup
   */
  private patchTotalLot(divisionList: Division[]): void {
    const totalLot = this.purchaseService.calculateTotalLot(divisionList);
    this.mainForm.patchValue({ totalLot: totalLot });
  }

  /**
   * 入荷数量エラー表示フラグを設定する.
   * @param purchaseSkuFA 仕入SKUFormArray
   */
  private setArrivalCountErrorMessage(purchaseSkuFA: FormArray): void {
    this.initArrivalCountError();
    if (!this.submitted || purchaseSkuFA.valid) { return; }

    // フラグ全てtrueになったらループ終了
    purchaseSkuFA.controls.some(sfg =>
      (sfg.get('purchaseDivisions') as FormArray).controls.some(this.setArrivalCountErrorMessages));
  }

  /**
   * 入荷数量エラーフラグ初期化.
   */
  private initArrivalCountError() {
    this.isArrivalCountMaxError = false;
    this.isArrivalCountRequiredError = false;
    this.isArrivalCountPatternError = false;
  }

  /**
   * 入荷数量エラー表示フラグを設定する.
   * @param dfg purchaseDivisionFormGroup
   * @return true:全ての入荷数量エラー表示フラグがtrue
   */
  private setArrivalCountErrorMessages = (dfg: AbstractControl): boolean => {
    const errors = dfg.get('arrivalCount').errors;
    if (errors == null) { return false; }

    this.isArrivalCountMaxError = errors.max ? true : this.isArrivalCountMaxError;
    this.isArrivalCountRequiredError = errors.required ? true : this.isArrivalCountRequiredError;
    this.isArrivalCountPatternError = errors.pattern ? true : this.isArrivalCountPatternError;
    return this.isArrivalCountMaxError
      && this.isArrivalCountRequiredError
      && this.isArrivalCountPatternError;
  }

  /**
   * @param field フォーム項目名
   * @returns true:エラー表示
   */
  displayError(field: string): boolean {
    return this.submitted
      && this.fCtrl[field].invalid
      && (this.fCtrl[field].dirty || this.fCtrl[field].touched);
  }

  /**
   * @param fg FormGroup
   * @param field フォーム項目名
   * @returns true:エラー表示
   */
  displayChildFormGroupError(fg: FormGroup, field: string): boolean {
    return this.submitted
      && fg.controls[field].invalid
      && (fg.controls[field].dirty || fg.controls[field].touched);
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
   * 日付入力フォーカスアウト時処理.
   * 整形した値をpatchValueする.
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    const ngbDate = this.dateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.mainForm.patchValue({ [type]: ngbDate }); }
  }

  /**
   * メッセージをクリアする.
   */
  private clearMessage = (): void => {
    this.message = {
      footer: {
        success: { code: '', param: null },
        error: { code: '', param: null }
      }
    };
  }

  // PRD_0041 add SIT start
  /**
   * 課コードとディスタの組み合わせチェック
   * 18課で本部が選択されてる場合はtrue
   */
   private checkDivisionDista = (): boolean => {
    return this.divisionList.some(dl => dl.code === '18')
      && (this.stores.find(s => s.shpcd === this.mainForm.controls.arrivalShop.value).warekind === 1);
  }
  // PRD_0041 add SIT end
}
