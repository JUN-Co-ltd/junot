import { Component, OnDestroy, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { combineLatest, forkJoin, from, Observable, of, Subscription } from 'rxjs';
import { catchError, filter, finalize, flatMap, map, tap } from 'rxjs/operators';
import { Path, PreEventParam, SearchTextType, SupplierType, ValidatorsPattern, Const, StaffType } from 'src/app/const/const';
import { Dictionaries } from 'src/app/const/dictionaries';
import { ShopKind } from 'src/app/const/shop-kind';
import { GenericList } from 'src/app/model/generic-list';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';
import { MakerReturn } from 'src/app/model/maker-return';
import { MakerReturnProductComposite } from 'src/app/model/maker-return-product-composite';
import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';
import { NumberUtilsService } from 'src/app/service/bo/number-utils.service';
import { FileService } from 'src/app/service/file.service';
import { HeaderService } from 'src/app/service/header.service';
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MakerReturnsHttpService } from 'src/app/service/maker-returns-http.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { OrderService } from 'src/app/service/order.service';
import { MessageConfirmModalComponent } from '../message-confirm-modal/message-confirm-modal.component';
import { SearchMakerReturnProductModalComponent } from '../search-maker-return-product-modal/search-maker-return-product-modal.component';
import { SearchSupplierModalComponent } from '../search-supplier-modal/search-supplier-modal.component';
import { MakerReturnService } from './service/maker-return.service';
import { MakerReturnProductSearchConditions } from 'src/app/model/maker-return-product-search-conditions';
import { ItemService } from 'src/app/service/bo/item.service';
import { MakerReturnProductsHttpService } from 'src/app/service/maker-return-products-http.service';
import { MaxLength } from 'src/app/const/max-length';
import { Order } from 'src/app/model/order';
import { FormUtilsService } from 'src/app/service/bo/form-utils.service';
import { ShopService } from 'src/app/service/bo/shop.service';
import { SearchShopModalComponent } from '../search-shop-modal/search-shop-modal.component';
import { SearchStaffModalComponent } from '../search-staff-modal/search-staff-modal.component';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { JunpcCodmstSearchCondition } from 'src/app/model/junpc-codmst-search-condition';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { AuthUtils } from 'src/app/util/auth-utils';
import { SessionService } from 'src/app/service/session.service';
import { Session } from 'src/app/model/session';

@Component({
  selector: 'app-maker-return',
  templateUrl: './maker-return.component.html',
  styleUrls: ['./maker-return.component.scss']
})
export class MakerReturnComponent implements OnInit, OnDestroy {

  /** ログインユーザーのセッション */
  private session: Session;

  /** HTML参照用パス定数 */
  readonly PATH = Path;

  /** メーカー返品ファイルID */
  fileId: number = null;

  /** パス */
  path: string;

  /** ディスタリスト. */
  distas: JunpcTnpmst[];

  /** 合計 */
  total = {
    /** 返品数 */
    returnLot: 0,
    /** 金額 */
    amount: 0
  };

  /** フォーム */
  mainForm: FormGroup;

  /** ローディング表示フラグ */
  isLoading: boolean;

  /** Submit押下フラグ */
  submitted = false;

  /** 画面表示フラグ */
  showScreen = false;

  /** 発注ID(queryParam) */
  private orderId: number;

  /** 返品日最小値 */
  readonly minReturnAtValue = this.dateUtils.generateCurrentSubstractDayDate(30);

  /** 返品日最大値 */
  readonly maxReturnAtValue = this.dateUtils.generateCurrentAddDayDate(30);

  /** 削除確認メッセージ */
  private deleteConformMessage: string;

  /** 追加不可メッセージ */
  private addErrorMessage: string;

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

  /**
   * @returns this.mainForm.get('makerReturnProducts')
   */
  get fArrayMakerReturnProducts(): FormArray {
    return this.mainForm.get('makerReturnProducts') as FormArray;
  }

  /**
   * @returns true:非活性
   */
  get isProductCodeDisable(): boolean {
    return this.path !== Path.NEW
      || this.mainForm.get('supplierName').value == null
      || this.mainForm.get('distaCode').value == null;
  }

  /**
   * true:警告メッセージ表示
   */
  get showAlertMessage(): boolean {
    return this.path === Path.NEW
      && this.fArrayMakerReturnProducts.value.some(val => val.partNoId != null);
  }

  /**
   * true:商品コード重複エラーあり
   */
  get hasDupulicate(): boolean {
    return this.path === Path.NEW
      && this.fArrayMakerReturnProducts.value
        .filter(val => val.partNoId != null)
        .some((val1, index, self) => self.findIndex(val2 => val2.productCode === val1.productCode) !== index);
  }

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private headerService: HeaderService,
    private fb: FormBuilder,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private translateService: TranslateService,
    private junpcTnpmstService: JunpcTnpmstHttpService,
    private numberUtils: NumberUtilsService,
    private makerReturnsHttpService: MakerReturnsHttpService,
    private makerReturnService: MakerReturnService,
    private orderService: OrderService,
    private modalService: NgbModal,
    private junpcSirmstService: JunpcSirmstService,
    private fileService: FileService,
    private dateUtils: DateUtilsService,
    private listUtils: ListUtilsService,
    private makerReturnProductsHttpService: MakerReturnProductsHttpService,
    private itemService: ItemService,
    private formUtils: FormUtilsService,
    private shopService: ShopService,
    private junpcCodmstService: JunpcCodmstService,
    private sessionService: SessionService,
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(isLoading => this.isLoading = isLoading);
    let loadingToken = null;

    this.session = this.sessionService.getSaveSession();

    combineLatest([this.route.paramMap, this.route.queryParamMap]).pipe(
      tap(this.clearMessage),
      tap(([_, queryParamMap]) => this.orderId = this.numberUtils.defaultNull(queryParamMap.get('orderId'))),
      flatMap(([paramMap, queryParamMap]) => forkJoin(
        of(paramMap.get('voucherNumber')),
        this.loadingService.start(),
        this.translateInitialText(this.numberUtils.defaultNull(queryParamMap.get('preEvent'))),
      )),
      tap(([_, token]) => {
        loadingToken = token;
        this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      }),
      flatMap(([voucherNumber]) => this.fetchInitialData(voucherNumber, this.orderId, this.path)),
      tap(data => {
        this.createForm(data);
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
   * @param voucherNumber 伝票番号
   * @param orderId 発注ID
   * @param path パス
   * @returns Observable<[MakerReturn, JunpcTnpmst[]]>
   */
  private fetchInitialData(voucherNumber: string, orderId: number, path: Path): Observable<[MakerReturn, JunpcTnpmst[]]> {
    return forkJoin(
      forkJoin(
        this.fetchMakerReturn(voucherNumber, orderId, path),
        this.fetchOrder(orderId, path),
      ).pipe(
        tap(([makerReturn, order]) => this.total = this.makerReturnService.caluculateTotal(makerReturn, order))
      ),
      this.fetchDistas()
    ).pipe(
      map(([[makerReturn], distas]) => {
        // タプルの型定義した変数に代入して返さないとコンパイルエラーになります
        const retVal: [MakerReturn, JunpcTnpmst[]] = [makerReturn, distas];
        return retVal;
      })
    );
  }

  /**
   * @param voucherNumber 伝票番号
   * @param orderId 発注ID
   * @param path パス
   * @returns メーカー返品情報
   */
  private fetchMakerReturn(voucherNumber: string, orderId: number, path: Path): Observable<MakerReturn> {
    if (path === Path.NEW) { return of(null); }

    return this.makerReturnsHttpService.fetchByVoucherNumberAndOrderId(voucherNumber, orderId).pipe(
      tap(res => this.fileId = res.makerReturnFileNoId)
    );
  }

  /**
   * @param orderId 発注ID
   * @param path パス
   * @returns 発注情報
   */
  private fetchOrder(orderId: number, path: Path): Observable<Order> {
    if (path === Path.NEW) { return of(null); }

    return this.orderService.getOrderForId(orderId);
  }

  /**
   * @returns ディスタリスト
   */
  private fetchDistas(): Observable<JunpcTnpmst[]> {
    return this.junpcTnpmstService.search({
      shpcdAhead: Const.MAKER_RETURN_SHPCD_PREFIX,
      shopkind: ShopKind.WARE_HOUSE as ShopKind
    } as JunpcTnpmstSearchCondition).pipe(
      map(res => this.distas = res.items)
    );
  }

  /**
   * テキストを翻訳する.
   * @param preEvent 遷移前イベント
   * @returns Observable<any>
   */
  private translateInitialText(preEvent: number): Observable<any> {
    return forkJoin(
      this.translateService.get('ERRORS.VALIDATE.NO_MORE_ADD'),
      this.translateService.get('TITLE.MAKER_RETURN').pipe(
        tap(value => this.setSubmitMessage(preEvent, value)),
        flatMap(value => this.translateService.get('INFO.DELETE_COMFIRM_MESSAGE', { value }).pipe(
          tap(conform => this.deleteConformMessage = conform)
        ))
      ),
    ).pipe(
      tap(([addError]) => this.addErrorMessage = addError)
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
   * フォームを作成する.
   * @param makerReturn メーカー返品情報
   * @param distas ディスタリスト
   */
  private createForm([makerReturn, distas]: [MakerReturn, JunpcTnpmst[]]): void {
    this.mainForm = this.fb.group({
      voucherNumber: [null],
      voucherLine: [null],
      manageNumber: [null],
      distaCode: [null, [Validators.required]],
      // TODO:店舗が不要であることが確認出来たら削除
      shpcd: [null
        // , [
        // Validators.required,
        // Validators.pattern(ValidatorsPattern.HALF_WIDTH_ALPHANUMERIC)]
      ],
      // shopName: [null, [Validators.required]],
      supplierCode: [null, [Validators.required]],
      supplierName: [null, [Validators.required]],
      mdfStaffCode: [null, [
        Validators.required,
        Validators.pattern(ValidatorsPattern.NUMERIC)]
      ],
      mdfStaffName: [null, [Validators.required]],
      returnAt: [this.dateUtils.generateCurrentDate(), [Validators.required]],
      memo: [null],
      makerReturnProducts: this.generateMakerReturnProductsFormArray(makerReturn)
    }, { updateOn: 'blur' }
    );
    this.patchStocksToMainForm(this.mainForm, makerReturn, distas);
    this.showScreen = true;
  }

  /**
   * フォームにメーカー返品情報の値を設定する.
   * @param mainForm フォーム
   * @param makerReturn メーカー返品情報
   * @param distas ディスタリスト
   */
  private patchStocksToMainForm(mainForm: FormGroup, makerReturn: MakerReturn, distas: JunpcTnpmst[]): void {
    if (makerReturn == null) {
      // JUN権限(いずれはROLE_EDI権限)の場合は、ログインユーザのログインIDと名称をセットする
      if (AuthUtils.isJun) {
        mainForm.patchValue({mdfStaffCode : this.session.accountName});
        this.onChangeStaff(this.session.accountName);
      }

      return;
    }
    mainForm.patchValue({
      voucherNumber: makerReturn.voucherNumber,
      voucherLine: makerReturn.voucherLine,
      manageNumber: makerReturn.manageNumber,
      distaCode: this.shopService.extractDistaCodeByLogisticsCode(distas, makerReturn.logisticsCode),
      shpcd: makerReturn.shpcd,
      shopName: makerReturn.shopName,
      supplierCode: makerReturn.supplierCode,
      supplierName: makerReturn.supplierName,
      mdfStaffCode: makerReturn.mdfStaffCode,
      mdfStaffName: makerReturn.mdfStaffName,
      returnAt: this.dateUtils.parse(makerReturn.returnAt as Date),
      memo: makerReturn.memo
    });
  }

  /**
   * @param makerReturn メーカー返品情報
   * @returns makerReturnProductsFormArray
   */
  private generateMakerReturnProductsFormArray(makerReturn: MakerReturn): FormArray {
    const len = this.determineFormArrayLength(makerReturn);
    const fa = this.fb.array(Array.from(Array(len)).map(() => this.generateMakerReturnProductsFormGroup()));
    this.patchDataToMakerReturnProductsFormArray(makerReturn, fa);
    return fa;
  }

  /**
   * @returns makerReturnProductsFormGroup
   */
  private generateMakerReturnProductsFormGroup(): FormGroup {
    return this.fb.group({
      id: [null],
      productCode: [null, { updateOn: 'blur' }], // 商品コード
      productName: [null], // 品名
      partNoId: [null], // 品番ID
      partNo: [null], // 品番
      colorCode: [null], // カラーコード
      colorName: [null], // カラー名
      size: [null], // サイズ
      orderId: [null], // 発注ID
      orderNumber: [null], // 発注番号
      returnLot: [ // 数量(返品数)
        null, {
          updateOn: 'blur',
          validators: [Validators.pattern(ValidatorsPattern.POSITIVE_INTEGER_)]
        }],
      retailPrice: [null], // 上代
      unitPrice: [null], // 下代(発注の単価)
      otherCost: [null], // 最新の単価(品番情報のその他原価)
      amount: [0], // 金額
      stockLot: [null] // 在庫数
    });
  }

  /**
   * makerReturnProductsの長さを決める.
   * makerReturnProductsの長さが5未満の場合は5を返す.
   * @param makerReturn メーカー返品情報
   * @return FormArrayの数
   */
  private determineFormArrayLength(makerReturn: MakerReturn): number {
    const DEFAUTL_ARRAY_LENGTH = 5;
    if (makerReturn == null) { return DEFAUTL_ARRAY_LENGTH; }

    const len = makerReturn.makerReturnProducts.length;
    return len > DEFAUTL_ARRAY_LENGTH ? len : DEFAUTL_ARRAY_LENGTH;
  }

  /**
   * makerReturnProductsFormArrayにメーカー返品情報の値を設定する.
   * @param makerReturn メーカー返品情報
   * @param fa makerReturnProductsFormArray
   */
  private patchDataToMakerReturnProductsFormArray(makerReturn: MakerReturn, fa: FormArray): void {
    if (makerReturn == null) { return; }

    const makerReturnProducts = makerReturn.makerReturnProducts;
    fa.controls.some((fc, idx) => {
      if (idx >= makerReturnProducts.length) { return true; } // メーカー返品商品数ループで終了(5未満の場合)
      fc.patchValue({
        id: makerReturnProducts[idx].id,
        productCode: this.makerReturnService.generateProductCode(makerReturnProducts[idx]),
        productName: makerReturnProducts[idx].productName,
        partNoId: makerReturnProducts[idx].partNoId,
        partNo: makerReturnProducts[idx].partNo,
        colorCode: makerReturnProducts[idx].colorCode,
        size: makerReturnProducts[idx].size,
        orderId: makerReturnProducts[idx].orderId,
        orderNumber: makerReturnProducts[idx].orderNumber,
        returnLot: makerReturnProducts[idx].returnLot,
        retailPrice: makerReturnProducts[idx].retailPrice,
        unitPrice: makerReturnProducts[idx].unitPrice,
        otherCost: makerReturnProducts[idx].otherCost,
        amount: makerReturnProducts[idx].returnLot * makerReturnProducts[idx].unitPrice,
        stockLot: makerReturnProducts[idx].stockLot
      });
    });
  }

  /**
   * 仕入先コード変更時の処理.
   * @param input フォーム入力値
   */
  onChangeSupplier(input: string): void {
    this.mainForm.patchValue({ supplierName: null });

    if (input == null || input.length !== 5) { return; }

    this.junpcSirmstService.getSirmst({
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: input
    } as JunpcSirmstSearchCondition).pipe(
      tap(this.extractSupplierName),
      catchError(this.showErrorModal)
    ).subscribe();
  }

  /**
   * 仕入マスタ検索結果からメーカー名を設定する.
   * @param result 仕入マスタ検索結果
   */
  private extractSupplierName = (result: GenericList<JunpcSirmst>): void =>
    this.mainForm.patchValue({
      supplierName: result == null || this.listUtils.isEmpty(result.items) ? null : result.items[0].name
    })

  /**
   * 仕入先検索アイコン押下時の処理.
   */
  onSearchSupplier(): void {
    const modalRef = this.modalService.open(SearchSupplierModalComponent);
    const supplierCode = this.mainForm.get('supplierCode').value;
    modalRef.componentInstance.searchCondition = {
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: supplierCode
    } as JunpcSirmstSearchCondition;
    modalRef.componentInstance.default = {
      sire: supplierCode,
      name: this.mainForm.get('supplierName').value
    } as JunpcSirmst;

    // モーダルからの値を設定する。
    from(modalRef.result).pipe(
      filter(result => result != null),
      tap((result: JunpcSirmst) => {
        this.mainForm.patchValue({
          supplierCode: result.sire,
          supplierName: result.name
        });
        this.clearMakerReturnProducts();
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * 店舗コード変更時の処理.
   * @param input フォーム入力値
   */
  onChangeShop(input: string): void {
    this.mainForm.patchValue({ shopName: null });

    if (input == null || input.length !== 8) { return; }

    this.junpcTnpmstService.search({
      shpcd: input,
      shopkind: ShopKind.SHOP as ShopKind
    } as JunpcTnpmstSearchCondition).pipe(
      tap(this.extractShopName),
      catchError(this.showErrorModal)
    ).subscribe();
  }

  /**
   * 店舗マスタ検索結果から店舗名を設定する.
   * @param result 店舗マスタ検索結果
   */
  private extractShopName = (result: GenericList<JunpcTnpmst>): void =>
    this.mainForm.patchValue({
      shopName: result == null || this.listUtils.isEmpty(result.items) ? null : result.items[0].name
    })

  /**
   * 店舗検索アイコン押下時の処理.
   */
  onSearchShop(): void {
    const modalRef = this.modalService.open(SearchShopModalComponent, { windowClass: 'shop' });
    modalRef.componentInstance.inputShpcd = this.mainForm.get('shpcd').value;
    modalRef.componentInstance.inputShopkind = ShopKind.SHOP;

    // モーダルからの値を設定する。
    from(modalRef.result).pipe(
      filter(result => result != null),
      tap((result: JunpcTnpmst) => {
        this.mainForm.patchValue({
          shpcd: result.shpcd,
          shopName: result.name
        });
      }),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * 担当者検索
   */
  onSearchStaff(): void {
    const modalRef = this.modalService.open(SearchStaffModalComponent);

    modalRef.componentInstance.staffType = StaffType.PRODUCTION;

    modalRef.componentInstance.defaultStaffCode = this.mainForm.get('mdfStaffCode').value;
    modalRef.componentInstance.defaultStaffName = this.mainForm.get('mdfStaffName').value;

    // モーダルからの値を設定する。
    from(modalRef.result).pipe(
      filter(result => result != null),
      tap((result: JunpcCodmst) => {
        this.mainForm.patchValue({
          mdfStaffCode: result.code1,
          mdfStaffName: result.item2
        });
      }),
      catchError(() => of(null))
    ).subscribe();

  }

　/**
   * 担当者コード変更時の処理。
   * @parm 担当者コード
   */
  onChangeStaff(input: string): void {

    this.mainForm.patchValue({ mdfStaffName: null });

    if (input == null || input.length !== 6) { return; }

    this.junpcCodmstService.getStaff(input).pipe(
      tap((result: JunpcCodmst) => {
        this.mainForm.patchValue({
          mdfStaffName: result.item2
        });
      }),
      catchError(() => of(null))
    ).subscribe();

  }

  /**
   * ファイルダウンロード処理.
   * @param fileId ファイルID
   */
  onFileDownLoad(fileId: string): void {
    this.fileService.downloadFile(fileId).pipe(catchError(this.showErrorModal)).subscribe();
  }

  /**
   * 品番情報検索アイコン押下時の処理.
   * @param productForm メーカー返品商品Form
   */
  onSearchProduct(productForm: FormGroup): void {
    const val = this.mainForm.getRawValue();
    const modalRef = this.modalService.open(SearchMakerReturnProductModalComponent, { windowClass: 'maker-return-products' });
    modalRef.componentInstance.inputProductCode = productForm.get('productCode').value;
    modalRef.componentInstance.inputShpcd = val.distaCode;
    modalRef.componentInstance.supplier = {
      code: val.supplierCode,
      name: val.supplierName
    };

    // モーダルからの値を設定する。
    from(modalRef.result).pipe(
      filter((result: { makerReturnProductComposite: MakerReturnProductComposite, productCode: string }) => result != null),
      tap(this.patchMakerReturnProductCompositeValue(productForm)),
      catchError(() => of(null))
    ).subscribe();
  }

  /**
   * メーカー返品商品レコードをFormGroupにpatchValueする.
   * @param productForm メーカー返品商品Form
   * @param result メーカー返品商品レコード
   */
  private patchMakerReturnProductCompositeValue = (productForm: FormGroup) =>
    (result: { makerReturnProductComposite: MakerReturnProductComposite, productCode: string }): void => {
      if (result.makerReturnProductComposite == null) {
        this.resetProductForm(productForm, result.productCode);
        return;
      }

      productForm.patchValue({
        productCode: result.productCode,
        productName: result.makerReturnProductComposite.productName,
        partNoId: result.makerReturnProductComposite.partNoId,
        partNo: result.makerReturnProductComposite.partNo,
        colorCode: result.makerReturnProductComposite.colorCode,
        size: result.makerReturnProductComposite.size,
        orderId: result.makerReturnProductComposite.orderId,
        orderNumber: result.makerReturnProductComposite.orderNumber,
        returnLot: null,
        retailPrice: result.makerReturnProductComposite.retailPrice,
        unitPrice: result.makerReturnProductComposite.unitPrice,
        otherCost: result.makerReturnProductComposite.otherCost,
        amount: 0,
        stockLot: result.makerReturnProductComposite.stockLot
      });

      this.total = this.makerReturnService.reCaluculateTotal(this.fArrayMakerReturnProducts);
    }

  /**
   * メーカー返品商品フォーム入力値リセット.
   * @param productForm メーカー返品商品Form
   * @param productCode 商品コード
   */
  private resetProductForm(productForm: FormGroup, productCode: string): void {
    productForm.reset();
    productForm.patchValue({ productCode, amount: 0 });
    this.total = this.makerReturnService.reCaluculateTotal(this.fArrayMakerReturnProducts);
  }

  /**
   * 商品コード変更時の処理.
   * 商品コードに該当する返品商品情報を取得してフォームに設定する.
   * ※商品コードに { updateOn: 'blur' } つけてるのでblur時でないと値は変更されません。
   * @param productForm メーカー返品商品Form
   * @param productCode 入力値
   */
  onChangeProductCode(productForm: FormGroup, productCode: string): void {
    // productForm.get('productCode').valueでは最新入力値とれないので使わないでください
    const splitProductCode = this.itemService.splitProductCode(productCode);
    const patchFn = this.patchMakerReturnProductCompositeValue(productForm);

    if (splitProductCode.partNo == null) {
      patchFn({ makerReturnProductComposite: null, productCode });
      return;
    }

    const val = this.mainForm.getRawValue();
    this.makerReturnProductsHttpService.search({
      productCode,
      supplierCode: val.supplierCode,
      shpcd: val.distaCode,
      latestOrderOnly: true
    } as MakerReturnProductSearchConditions).pipe(
      tap(result => patchFn({ makerReturnProductComposite: result.items[0], productCode })),
      catchError(this.showErrorModal)
    ).subscribe();
  }

  /**
   * 返品数変更時の処理.
   * 数量合計及び金額の計算を行う.
   * ※返品数に { updateOn: 'blur' } つけてるのでblur時でないと値は変更されません。
   * @param productForm メーカー返品商品Form
   * @param returnLot 入力値
   */
  onChangeReturnLot(productForm: FormGroup, returnLot: number): void {
    // productForm.get('returnLot').valueでは最新入力値とれないので使わないでください
    const amount = returnLot * productForm.get('unitPrice').value;
    productForm.patchValue({ returnLot, amount });
    this.total = this.makerReturnService.reCaluculateTotal(this.fArrayMakerReturnProducts);
  }

  /**
   * ゴミ箱アイコンクリック時の処理.
   * @param productForm メーカー返品商品Form
   */
  onProductTrash(productForm: FormGroup): void {
    this.resetProductForm(productForm, null);
  }

  /**
   * 追加ボタン押下時の処理.
   */
  onAdd(): void {
    const fa = (this.fArrayMakerReturnProducts);
    if (MaxLength.MAKER_RETURNS <= fa.length) {
      // 最大件数オーバー
      alert(this.addErrorMessage);
      return;
    }

    fa.push(this.generateMakerReturnProductsFormGroup());
  }

  /**
   * 返品商品FormArrayリセット.
   */
  clearMakerReturnProducts(): void {
    this.fArrayMakerReturnProducts.reset();
    this.fArrayMakerReturnProducts.controls.forEach(c => c.patchValue({ amount: 0 }));
    this.total = this.makerReturnService.reCaluculateTotal(this.fArrayMakerReturnProducts);
  }

  /**
   * 新規登録ボタン押下時の処理.
   */
  onNewCreatePage(): void {
    this.router.navigate(['makerReturns', Path.NEW]);
  }

  /**
   * 登録ボタン押下時の処理.
   */
  onCreate(): void {
    // 倉庫の店舗コードをshopコードに設定
    this.mainForm.patchValue({shpcd: this.mainForm.getRawValue().distaCode});
    this.loadingInSubmit(this.makerReturnsHttpService.create(this.mainForm.value));
  }

  /**
   * 更新ボタン押下時の処理.
   */
  onUpdate(): void {
    // 倉庫の店舗コードをshopコードに設定
    this.mainForm.patchValue({shpcd: this.mainForm.getRawValue().distaCode});
    this.loadingInSubmit(this.makerReturnsHttpService.update(this.mainForm.value));
  }

  /**
   * ローディング処理.
   * @param httpFn http通信関数
   */
  private loadingInSubmit(httpFn: () => Observable<any>): void {
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => {
        loadingToken = token;
        this.submitted = true;
        this.clearMessage();
      }),
      filter(this.isValid),
      flatMap(() => httpFn()),
      tap(this.navigateAftetSubmit),
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * @returns true:バリデーションエラーなし
   */
  private isValid = (): boolean => {
    if (!this.hasDupulicate && this.mainForm.valid) {
      return true;
    }

    // デバッグしたいときはコメント外してください. push時はコメントアウトしてください.
    // console.log(this.mainForm);
    // this.formUtils.logValidationErrors(this.mainForm);

    this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
    this.formUtils.markAsTouchedAllFields(this.mainForm);

    return false;
  }

  /**
   * 削除ボタン押下時の処理.
   */
  onDelete(): void {
    // 確認モーダルを表示
    const modalRef = this.modalService.open(MessageConfirmModalComponent);
    modalRef.componentInstance.message = this.deleteConformMessage;

    // 確認モーダルのメッセージの戻り値を確認して、削除処理を行う。
    let loadingToken = null;
    from(modalRef.result).pipe(
      catchError(() => of(null)), // バツボタン時
      filter((result: string) => result === 'OK'),
      flatMap(() => this.loadingService.start()),
      tap(token => {
        loadingToken = token;
        this.clearMessage();
      }),
      flatMap(() => this.makerReturnsHttpService.delete(this.mainForm.get('voucherNumber').value, this.orderId)),
      tap(() => this.router.navigate(['makerReturns', Path.NEW], { queryParams: { preEvent: PreEventParam.DELETE } })),
      catchError(this.showErrorModal), // 削除エラー時
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * Submit完了後の遷移処理.
   * @param res Submit結果のレスポンス
   */
  private navigateAftetSubmit = (res: MakerReturn): void => {
    const preEvent = this.path === Path.NEW ? PreEventParam.CREATE : PreEventParam.UPDATE;
    this.router.navigate(
      ['makerReturns', res.voucherNumber, Path.EDIT],
      { queryParams: { orderId: res.makerReturnProducts[0].orderId, preEvent: preEvent, t: new Date().valueOf() } });
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string) {
    const ngbDate = this.dateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.mainForm.patchValue({ [type]: ngbDate }); }
  }

  /**
   * @param product メーカー返品商品FormGroup
   * @returns true:非活性
   */
  isReturnLotDisable(product: FormGroup): boolean {
    return this.path === Path.VIEW || product.get('partNoId').value == null;
  }

  /**
   * @param product メーカー返品商品FormGroup
   * @returns true:表示
   */
  showProductTrash(product: FormGroup): boolean {
    return this.path !== Path.VIEW && this.formUtils.isNotEmpty(product.get('productCode').value);
  }

  /**
   * メッセージをクリアする.
   */
  private clearMessage(): void {
    this.message = {
      footer: {
        success: { code: '', param: null },
        error: { code: '', param: null }
      }
    };
  }

  /**
   * エラーモーダルを表示する.
   * @param error エラー情報
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  private showErrorModal = (error: any): Observable<boolean> => this.messageConfirmModalService.openErrorModal(error);
}
