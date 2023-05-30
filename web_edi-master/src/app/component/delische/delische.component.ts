import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ScrollEvent } from 'ngx-scroll-event';

import { Observable, of, forkJoin } from 'rxjs';
import { tap, catchError, filter, first } from 'rxjs/operators';

import { CodeMaster } from '../../const/code-master';
import {
  LateType, DelischeSortColumnType, OrderByType, DelischeRecordType, DelischeFileStatus, DelischeSearchType, StorageKey
} from '../../const/const';

import { StringUtils } from '../../util/string-utils';
import { ListUtils } from '../../util/list-utils';
import { DateUtils } from '../../util/date-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { FileUtils } from '../../util/file-utils';
import { BooleanUtils } from '../../util/boolean-utils';
import { CalculationUtils } from '../../util/calculation-utils';
import { FormUtils } from '../../util/form-utils';

import { LoadingService } from '../../service/loading.service';
import { HeaderService } from '../../service/header.service';
import { SessionService } from '../../service/session.service';
import { LocalStorageService } from '../../service/local-storage.service';
import { DelischeOrderService } from '../../service/delische-order.service';
import { DelischeDeliveryRequestService } from '../../service/delische-delivery-request.service';
import { DelischeDeliverySkuService } from '../../service/delische-delivery-sku.service';
import { JunpcCodmstService } from '../../service/junpc-codmst.service';
import { DelischeFileService } from '../../service/delische-file.service';
import { FileService } from '../../service/file.service';
import { DelischeService } from '../../service/delische.service';

import { GenericList } from '../../model/generic-list';
import { BrandCodesSearchConditions } from '../../model/brand-codes-search-conditions';
import { VDelischeOrder } from '../../model/v-delische-order';
import { DelischeOrderSearchConditions } from '../../model/delische-order-search-conditions';
import { DelischeDeliveryRequestSearchConditions } from '../../model/delische-delivery-request-search-conditions';
import { DelischeDeliverySkuSearchConditions } from '../../model/delische-delivery-sku-search-conditions';
import { VDelischeDeliveryRequest } from '../../model/v-delische-delivery-request';
import { Session } from '../../model/session';
import { DelischeFileInfo } from '../../model/delische-file-info';

@Component({
  selector: 'app-delische',
  templateUrl: './delische.component.html',
  styleUrls: ['./delische.component.scss']
})
export class DelischeComponent implements OnInit {
  readonly LATE = LateType.LATE;
  readonly NO_LATE = LateType.NO_LATE;
  readonly SORT_COLUMN_TYPE = DelischeSortColumnType;
  readonly ORDER_TYPE = OrderByType;
  readonly ORDER = DelischeRecordType.ORDER;
  readonly DERIVERY_REQUEST = DelischeRecordType.DERIVERY_REQUEST;
  readonly DERIVERY_SKU = DelischeRecordType.DERIVERY_SKU;
  readonly CREATING = DelischeFileStatus.CREATING;
  readonly COMPLETE_CREATE = DelischeFileStatus.COMPLETE_CREATE;
  readonly CNT_ERROR = DelischeFileStatus.CNT_ERROR;
  readonly OTHER_ERROR = DelischeFileStatus.OTHER_ERROR;
  readonly SEARCH_TYPE_ORDER = DelischeSearchType.ORDER;
  readonly SEARCH_TYPE_DERIVERY = DelischeSearchType.DERIVERY;

  /** 次のページのトークン */
  private nextPageToken = '';

  /** シーズンリスト */
  seasonList: { id: number, value: string, selected: boolean }[] = [];
  /** 事業部マスタリスト */
  divisionMasterList: { id: string, value: string }[] = [];
  /** デリスケ発注情報リスト */
  delischeOrderList: VDelischeOrder[] = [];
  /** デリスケファイル */
  delischeFile: DelischeFileInfo = null;

  /** ソート中のカラム */
  sorting = { column: '', sort: '' };
  /** Formの入力値 */
  formConditions: DelischeOrderSearchConditions = new DelischeOrderSearchConditions();

  /** 上代合計 */
  retailPriceSum = 0;
  /** 下代合計 */
  productCostSum = 0;
  /** 納品SKUを開いている数 */
  openingDeliverySkuNum = 0;

  /** バリデーションエラー表示フラグ */
  showValidationError = false;
  /** エラーメッセージ */
  overallMsgCode = '';

  /** ログインユーザーのセッション */
  private session: Session;

  /** 検索処理中にボタンをロックするためのフラグ */
  isSearchBtnLock = true;
  /** 検索項目表示フラグ */
  isSearchItemsCollapsed = true;

  constructor(
    private loadingService: LoadingService,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private localStorageService: LocalStorageService,
    private fileService: FileService,
    private delischeFileService: DelischeFileService,
    private junpcCodmstService: JunpcCodmstService,
    private delischeOrderService: DelischeOrderService,
    private delischeDeliveryRequestService: DelischeDeliveryRequestService,
    private delischeDeliverySkuService: DelischeDeliverySkuService,
    private delischeService: DelischeService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();
    this.setFormCollapsed(this.session);
    this.seasonList = CodeMaster.subSeason.map(subSeason => ({ id: subSeason.id, value: subSeason.value, selected: false }));

    forkJoin(
      this.getDivisionList(),
      this.setDataToForm(this.session, this.seasonList),
      this.getDelischeFile()
    ).pipe(
      tap(() => this.onSearch()),
      catchError(error => of(this.handleApiError(error)))
    ).subscribe();
  }

  /**
   * フォームの折りたたみ設定.
   * @param session セッション
   */
  private setFormCollapsed(session: Session): void {
    const storageObj = this.localStorageService.getSaveLocalStorage(session, StorageKey.DELISCHE_SEARCH_COLLAPSED);
    const parsedIsCollapsed = BooleanUtils.parseStrToBoolean(storageObj);
    this.isSearchItemsCollapsed = parsedIsCollapsed == null ? this.isSearchItemsCollapsed : parsedIsCollapsed;
  }

  /**
   * フォームにデータを設定する.
   * @param session セッション
   * @param seasonList シーズンリスト
   * @returns Observable<void>
   */
  private setDataToForm(session: Session, seasonList: {id: number, selected: boolean}[]): Observable<void> {
    const storageObj = this.localStorageService.getSaveLocalStorage(session, StorageKey.DELISCHE_SEARCH_CONDITIONS);
    return storageObj == null ?
      this.setInitialDataToForm(session) :
      this.setStorageDataToForm(JSON.parse(storageObj), seasonList);
  }

  /**
   * ストレージに保存した入力値をFormに設定する.
   * @param storageConditions ストレージに保存した入力値
   * @param seasonList シーズンリスト
   * @returns  Observable<void>
   */
  private setStorageDataToForm(storageConditions: DelischeOrderSearchConditions,
    seasonList: { id: number, selected: boolean }[]): Observable<void> {
    this.formConditions = storageConditions;
    const storageSeason = this.formConditions.season;

    const fn = this.setSelected(seasonList);
    if (typeof storageSeason === 'string') { // ※複数選択。当機能リリース前はstringで保存されている
      fn(storageSeason);
    } else if (typeof storageSeason === 'object') { // ※複数選択。当機能リリース後はリスト(object)で保存されている
      storageSeason.forEach(sesssionData => fn(sesssionData));
    }

    return of(null);
  }

  /**
   * シーズンリストの選択状態を設定する.
   * @param seasonList シーズンリスト
   * @param sesssionData セッションデータ
   */
  private setSelected = (seasonList: {id: number, selected: boolean}[]) => (sesssionData: string | object): void =>
    seasonList.filter(season => season.id.toString() === sesssionData).forEach(season => season.selected = true)

  /**
   * Formに初期値を設定する.
   * @param session セッション
   * @returns  Observable<void>
   */
  private setInitialDataToForm(session: Session): Observable<void> {
    // storageから検索条件が取得されなかった場合、初期設定する
    const accountName = session.accountName;
    this.formConditions.mdfStaff = accountName;
    this.formConditions.searchSelect = DelischeSearchType.ORDER;
    return this.setBrandCodes(accountName);
  }

  /**
   * 事業部リストを取得する.
   * @returns Observable<any>
   */
  private getDivisionList(): Observable<any> {
    return this.junpcCodmstService.getAllDivisions().pipe(
      tap(result =>
        this.divisionMasterList = result.items.map(item => ({ id: item.code1, value: item.item2 })))
    );
  }

  /**
   * デリスケファイルを取得する.
   * @returns Observable<GenericList<DelischeFileInfo>>
   */
  private getDelischeFile(): Observable<GenericList<DelischeFileInfo> | void> {
    return this.delischeFileService.listDelischeFile().pipe(
      filter(result => result != null),
      tap(result => this.delischeFile = result.items[0]),
      first()
    );
  }

  /**
   * デリスケファイル取得イベント.
   */
  onGetDelischeFile(): void {
    this.delischeFile = null; // いったんクリア
    this.getDelischeFile().subscribe();
  }

  /**
   * ラジオボタン選択切り替え時の処理.
   * 片側の入力値をクリア.
   */
  onChangeSearchRadio(): void {
    const searchSelect = this.formConditions.searchSelect;
    switch (searchSelect) {
      case DelischeSearchType.ORDER:
        this.formConditions.productDeliveryAtFrom = null; // 生産納期from
        this.formConditions.productDeliveryAtTo = null; // 生産納期to

        this.formConditions.productDeliveryAtMonthlyYearFrom = null;  // 年度from
        this.formConditions.productDeliveryAtMonthlyFrom = null;  // 月度from
        this.formConditions.productDeliveryAtFromByMonthly = null;  // 年度・月度から作成した生産納期from

        this.formConditions.productDeliveryAtMonthlyYearTo = null;  // 年度to
        this.formConditions.productDeliveryAtMonthlyTo = null;  // 月度to
        this.formConditions.productDeliveryAtToByMonthly = null;  // 年度・月度から作成した生産納期to

        this.formConditions.excludeCompleteOrder = false;  // 完納は対象外
        this.formConditions.existsOrderRemaining = false;  // 発注残あり
        break;
      case DelischeSearchType.DERIVERY:
        this.formConditions.deliveryAtFrom = null;  // 納品日from
        this.formConditions.deliveryAtTo = null;  // 納品日to

        this.formConditions.mdWeekYearFrom = null;  // 年度from
        this.formConditions.mdWeekFrom = null;  // 納品週from
        this.formConditions.deliveryAtFromByMdweek = null;  // 年度・納品週から作成した納品日from

        this.formConditions.mdWeekYearTo = null;  // 年度to
        this.formConditions.mdWeekTo = null;  // 納品週to
        this.formConditions.deliveryAtToByMdweek = null;  // 年度・納品週から作成した納品日to

        this.formConditions.deliveryAtLateFlg = false;  // 納期遅れ
        break;
      default:
        break;
    }
  }

  /**
   * 検索項目の開閉状態をstargeに保持する.
   */
  onSaveSearchItemsCollapsed(): void {
    this.localStorageService.createLocalStorage(this.session, StorageKey.DELISCHE_SEARCH_COLLAPSED, this.isSearchItemsCollapsed);
  }

  /**
   * ログインユーザーに紐づくブランドコードリストを取得して検索フォームに設定する.
   * @param accountName アカウント名
   * @returns Observable<any>
   */
  private setBrandCodes(accountName: string): Observable<any> {
    return this.junpcCodmstService.getBrandCodes({ accountName: accountName } as BrandCodesSearchConditions).pipe(
      // ブランドコードは半角スペース区切り
      tap(result =>
        this.formConditions.brandCode = result.items.reduce((acc, cur) => acc + ' ' + cur.brandCode, '').trim())
    );
  }

  /**
   * 発注データ検索処理
   * @param searchForm form情報
   */
  onSearch(searchForm?: NgForm): void {
    this.isSearchBtnLock = true;
    this.setProductDeliveryAt();
    this.setDeliveryAt();
    if (this.isValidatorError(searchForm)) {
      this.isSearchBtnLock = false;
      return;
    }
    this.loadingService.loadStart();
    this.delischeOrderList = [];  // デリスケリストをクリア
    this.overallMsgCode = '';   // エラーメッセージをクリア
    this.retailPriceSum = 0;      // 上代初期化
    this.productCostSum = 0;      // 下代初期化
    this.openingDeliverySkuNum = 0; // 開き中納品SKU0件
    this.sorting = { column: '', sort: '' };  // ソート初期化
    this.nextPageToken = null;  // トークンクリア

    // 検索条件をstorageに保持
    this.saveFormConditions();

    const formatConditions = { ...this.formConditions };
    formatConditions.season = this.extractSelectedSeason(); // シーズンリスト設定

    this.delischeOrderService.listDelischeOrder(formatConditions).subscribe(
      genericList => this.setDelischeOrderData(genericList),
      error => this.handleApiError(error));
  }

  /**
   * 検索条件をstorageに保持.
   */
  private saveFormConditions(): void {
    this.formConditions.season = this.extractSelectedSeason();
    this.localStorageService.createLocalStorage(this.session, StorageKey.DELISCHE_SEARCH_CONDITIONS, this.formConditions);
  }

  /**
   * 選択中のシーズンを取得する.
   * @returns 選択中のシーズンリスト
   */
  private extractSelectedSeason(): string[] {
    return this.seasonList.filter(season => season.selected).map(selected => selected.id.toString());
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得.
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isWindowEvent || !event.isReachingBottom || StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }
    // 一番下までスクロールされ、次のページのトークンがある場合、次のリストを取得する
    this.loadingService.loadStart();
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    this.delischeOrderService.listDelischeOrder({ pageToken: nextPageToken } as DelischeOrderSearchConditions).subscribe(
      genericList => this.setDelischeOrderData(genericList),
      error => this.handleApiError(error));
  }

  /**
   * カラムのソート処理.
   * @param column 対象カラム
   */
  onSort(column: string): void {
    this.isSearchBtnLock = true;
    const sort = this.sorting.column === column && this.sorting.sort === OrderByType.DESC ? OrderByType.ASC : OrderByType.DESC;
    this.sorting = { column: column, sort: sort };

    // 未登録時の比較用値設定。未登録の可能性がある項目は数値
    const unregistVal = (-Number.MAX_VALUE);

    if (sort === OrderByType.DESC) {
      this.delischeOrderList.sort((val1, val2) =>
        (val1[column] != null ? val1[column] : unregistVal) > (val2[column] != null ? val2[column] : unregistVal) ? -1 : 1);
      this.isSearchBtnLock = false;
      return;
    }
    this.delischeOrderList.sort((val1, val2) =>
      (val1[column] != null ? val1[column] : unregistVal) < (val2[column] != null ? val2[column] : unregistVal) ? -1 : 1);
    this.isSearchBtnLock = false;
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // pipeの変換値がformにセットされないので別途格納
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.formConditions[type] = ngbDate; }
  }

  /**
   * デリスケ発注データ取得後の設定処理.
   * @param genericList デリスケ発注データリスト
   */
  private setDelischeOrderData(genericList: GenericList<VDelischeOrder>): void {
    const delischeOrderList = genericList.items;
    console.debug('nextPage result:', delischeOrderList);
    this.nextPageToken = genericList.nextPageToken; // 次のページのトークンを保存する
    delischeOrderList.forEach(delischeOrder => {
      // 初期値設定
      delischeOrder.isOpenChild = false;
      delischeOrder.openingDeliverySkuNum = 0;
      delischeOrder.delischeDeliveryRequestList = [];
      // 上代・下代加算
      this.retailPriceSum += delischeOrder.calculateRetailPrice;
      this.productCostSum += delischeOrder.calculateProductCost;
    });
    this.delischeOrderList = this.delischeOrderList.concat(delischeOrderList);
    this.loadingService.loadEnd();
    this.isSearchBtnLock = false;
  }

  /**
   * リスト取得APIエラーハンドリング
   * @param error エラー情報
   */
  private handleApiError(error: any): void {
    this.overallMsgCode = 'ERRORS.ANY_ERROR';
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      this.overallMsgCode = apiError.viewErrorMessageCode;
    }
    this.loadingService.loadEnd();
    this.isSearchBtnLock = false;
  }
  /**
   * 発注情報の子要素開閉ボタン押下処理.
   * @param tagertDelische クリック行の発注レコード
   */
  onOpenOrCloseOrderChild(tagertDelische: VDelischeOrder): void {
    this.isSearchBtnLock = true;
    if (tagertDelische.isOpenChild) {
      tagertDelische.isOpenChild = false;  // 閉じる
      this.openingDeliverySkuNum = this.openingDeliverySkuNum - tagertDelische.openingDeliverySkuNum;
      tagertDelische.openingDeliverySkuNum = 0;
      tagertDelische.delischeDeliveryRequestList.forEach(deliveryRequest => deliveryRequest.isOpenChild = false); // SKUも閉じる
      this.isSearchBtnLock = false;
      return;
    }

    tagertDelische.isOpenChild = true; // 開く
    if (ListUtils.isEmpty(tagertDelische.delischeDeliveryRequestList)) {
      // 未取得済の場合、納品依頼データ取得
      this.getDelischeDeliveryRequestData(tagertDelische);
      return;
    }
    this.isSearchBtnLock = false;
  }

  /**
   * 納品依頼データを取得する.
   * @param tagertDelische クリック行の発注レコード
   */
  private getDelischeDeliveryRequestData(tagertDelische: VDelischeOrder): void {
    this.loadingService.loadStart();
    this.setDeliveryAt();

    const conditions = {
      orderId: tagertDelische.id,
      deliveryAtFrom: this.formConditions.deliveryAtFrom,
      deliveryAtTo: this.formConditions.deliveryAtTo,
      deliveryAtFromByMdweek: this.formConditions.deliveryAtFromByMdweek,
      deliveryAtToByMdweek: this.formConditions.deliveryAtToByMdweek,
      deliveryAtLateFlg: this.formConditions.deliveryAtLateFlg,
      searchSelect: this.formConditions.searchSelect
    } as DelischeDeliveryRequestSearchConditions;
    this.delischeDeliveryRequestService.listDelischeDeliveryRequest(conditions).subscribe(
      genericList => {
        const delischeDeliveryRequestList = genericList.items;
        console.debug('listDelischeDeliveryRequest result:', delischeDeliveryRequestList);
        delischeDeliveryRequestList.forEach(deliveryRequest => deliveryRequest.isOpenChild = false);
        tagertDelische.delischeDeliveryRequestList = delischeDeliveryRequestList;
        this.loadingService.loadEnd();
        this.isSearchBtnLock = false;
      }, error => this.handleApiError(error));
  }

  /**
   * 納品依頼情報の子要素開閉ボタン押下処理.
   * @param tagertDelischeDeriveryRequest クリック行の納品依頼レコード
   * @param tagertDelischeOrder クリック行の納品依頼レコード
   */
  onOpenOrCloseDeliveryRequestChild(tagertDelischeDeriveryRequest: VDelischeDeliveryRequest, tagertDelischeOrder: VDelischeOrder): void {
    this.isSearchBtnLock = true;
    if (tagertDelischeDeriveryRequest.isOpenChild) {  // 閉じる
      tagertDelischeDeriveryRequest.isOpenChild = false;
      tagertDelischeOrder.openingDeliverySkuNum--;
      this.openingDeliverySkuNum--;
      this.isSearchBtnLock = false;
      return;
    }

    // 開く
    tagertDelischeDeriveryRequest.isOpenChild = true;
    tagertDelischeOrder.openingDeliverySkuNum++;
    this.openingDeliverySkuNum++;
    if (ListUtils.isEmpty(tagertDelischeDeriveryRequest.delischeDeliverySkuList)) {
      this.getDelischeDeliverySkuData(tagertDelischeDeriveryRequest);  // 未取得の場合、納品SKUデータ取得
      return;
    }

    this.isSearchBtnLock = false;
  }

  /**
   * 納品SKUデータを取得する.
   * @param tagertDelische クリック行の納品依頼レコード
   */
  private getDelischeDeliverySkuData(tagertDelische: VDelischeDeliveryRequest): void {
    this.loadingService.loadStart();
    const conditions = {
      orderId: tagertDelische.orderId,
      deliveryId: tagertDelische.deliveryId
    } as DelischeDeliverySkuSearchConditions;
    this.delischeDeliverySkuService.listDelischeDeliverySku(conditions).subscribe(
      genericList => {
        const delischeDeliverySkuList = genericList.items;
        console.debug('listDelischeDeliverySku result:', delischeDeliverySkuList);
        tagertDelische.delischeDeliverySkuList = delischeDeliverySkuList;
        this.loadingService.loadEnd();
        this.isSearchBtnLock = false;
      }, error => this.handleApiError(error));
  }

  /**
   * ファイル作成ボタン押下処理.
   * @param searchForm 検索条件入力値
   */
  onFileCreate(searchForm: NgForm): void {
    this.setProductDeliveryAt();
    this.setDeliveryAt();
    if (this.isValidatorError(searchForm)) { return; }

    // 検索条件をstorageに保持
    this.saveFormConditions();

    const formatConditions = { ...this.formConditions }; // deep copy
    formatConditions.season = this.extractSelectedSeason(); // シーズンリスト設定
    this.delischeFileService.createDelischeFile(formatConditions).subscribe(
      () => this.onGetDelischeFile(),
      error => this.handleApiError(error)
    );
  }

  /**
   * ファイルダウンロードリンク押下処理.
   * @param fileNoId ファイルID
   */
  onFileDownLoad(fileNoId: number): void {
    this.overallMsgCode = '';
    this.fileService.fileDownload(fileNoId.toString()).subscribe(res => {
      const data = this.fileService.splitBlobAndFileName(res);
      FileUtils.downloadFile(data.blob, data.fileName);
    }, () => this.overallMsgCode = 'ERRORS.FILE_DL_ERROR');
  }

  /**
   * 年度、月度の日付計算.
   */
  private setProductDeliveryAt(): void {
    this.formConditions.productDeliveryAtFromByMonthly = this.delischeService.generateProductDeliveryAtDateFrom(this.formConditions);
    this.formConditions.productDeliveryAtToByMonthly = this.delischeService.generateProductDeliveryAtDateTo(this.formConditions);
  }

  /**
   * 年度、納品週の日付計算.
   */
  private setDeliveryAt(): void {
    this.formConditions.deliveryAtFromByMdweek = this.delischeService.generateDeliveryAtDateFrom(this.formConditions);
    this.formConditions.deliveryAtToByMdweek = this.delischeService.generateDeliveryAtDateTo(this.formConditions);
  }

  /**
   * バリデーションエラーのチェック.
   * @param searchForm 検索条件入力値
   * @returns true:バリデーションエラー,false:エラーなし
   */
  private isValidatorError(searchForm: NgForm): boolean {
    if (searchForm == null) {
      return false;
    }

    // 週番号(from)バリデーションチェック
    if (this.isMdweekValidationError(this.formConditions.deliveryAtFromByMdweek,
      this.formConditions.mdWeekYearFrom, this.formConditions.mdWeekFrom)) {
      searchForm.controls['mdWeekFrom'].setErrors({'pattern': true});
    } else {
      searchForm.controls['mdWeekFrom'].setErrors(null);
    }

    // 週番号(to)バリデーションチェック
    if (this.isMdweekValidationError(this.formConditions.deliveryAtToByMdweek,
      this.formConditions.mdWeekYearTo, this.formConditions.mdWeekTo)) {
      searchForm.controls['mdWeekTo'].setErrors({'pattern': true});
    } else {
      searchForm.controls['mdWeekTo'].setErrors(null);
    }

    if (searchForm.invalid) {
      this.showValidationError = true;
      return true; // validationエラーがある場合終了
    }

    this.showValidationError = false;
    return false;
  }

  /**
   * 週番号のバリデーションエラーチェック.
   * @param deliveryAtByMdweek 年度・週番号から算出した日付
   * @param mdWeekYear 年度
   * @param mdWeek 週番号
   * @returns true:週番号バリデーションエラー,false:週番号エラーなし
   */
  private isMdweekValidationError(deliveryAtByMdweek: string, mdWeekYear: number, mdWeek: number): boolean {
    if (FormUtils.isEmpty(mdWeekYear) || FormUtils.isEmpty(mdWeek)) {
      return false;
    }

    const calcWeek = CalculationUtils.calcWeek(DateUtils.convertSlashStringToNgbDate(deliveryAtByMdweek));

    if (Number(mdWeek) !== calcWeek) {
      return true;
    }

    return false;
  }
}
