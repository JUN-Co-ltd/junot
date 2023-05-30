import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subscription, forkJoin, of } from 'rxjs';
import { map, tap, flatMap, catchError, finalize } from 'rxjs/operators';
// PRD_0021 add SIT start
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { BrandCode } from 'src/app/model/brand-code';
// PRD_0021 add SIT end
import { GenericList } from 'src/app/model/generic-list';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';
import { Session } from 'src/app/model/session';
import { PurchaseSearchCondition } from 'src/app/model/purchase-search-condition';
import { PurchaseSearchResult } from 'src/app/model/purchase-search-result';

import { HeaderService } from 'src/app/service/header.service';
// PRD_0021 add SIT start
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
// PRD_0021 add SIT end
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { LoadingService } from 'src/app/service/loading.service';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { PurchaseHttpService } from 'src/app/service/purchase-http.service';
import { SessionService } from 'src/app/service/session.service';
import { PurchaseListStoreService } from './store/purchase-list-store.service';

import { DateUtilsService } from 'src/app/service/bo/date-utils.service';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';

import { SearchTextType, Path, SupplierType, Const } from 'src/app/const/const';
import { StorageKey } from 'src/app/const/storage-key';

import { StringUtils } from 'src/app/util/string-utils';
// PRD_0021 add SIT start
import { CompareResult } from 'src/app/enum/compare-result.enum';
import { FormUtils } from 'src/app/util/form-utils';
// PRD_0021 add SIT end

@Component({
  selector: 'app-purchase-list',
  templateUrl: './purchase-list.component.html',
  styleUrls: ['./purchase-list.component.scss'],
  providers: [PurchaseListStoreService]
})
export class PurchaseListComponent implements OnInit, OnDestroy {

  /** ログインユーザーのセッション */
  private session: Session;

  /** ディスタリスト. */
  stores: JunpcTnpmst[];

  // PRD_0021 add SIT start
  /** ブランドリスト */
  brands: BrandCode[] = [];
  // PRD_0021 add SIT end

  /** 仕入先メーカー名 */
  mdfMakerName: String;

  /** 次のページのトークン */
  private nextPageToken = '';

  /** 画面を表示する */
  visibled = false;

  /** ローディング表示フラグ */
  isLoading: boolean;

  /** チェック0件 */
  noCheck = true;

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

  /** 画面に表示するメッセージ */
  message = {
    /** Body */
    body: {
      /** 異常系 */
      error: { code: '', param: null }
    },
    /** フッター */
    footer: {
      /** 正常系 */
      success: { code: '', param: null }
    }
  };

  constructor(
    private router: Router,
    private purchaseHttpService: PurchaseHttpService,
    private messageConfirmModalService: MessageConfirmModalService,
    private headerService: HeaderService,
    private sessionService: SessionService,
    // PRD_0021 add SIT start
    private junpcCodmstService: JunpcCodmstService,
    // PRD_0021 add SIT end
    private junpcTnpmstService: JunpcTnpmstHttpService,
    private junpcSirmstService: JunpcSirmstService,
    private dateUtilsService: DateUtilsService,
    private localStorageService: LocalStorageService,
    private loadingService: LoadingService,
    private listUtils: ListUtilsService,
    private stringUtils: StringUtilsService,
    private store: PurchaseListStoreService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();

    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(isLoading => this.isLoading = isLoading);
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      // PRD_0021 add SIT start
      flatMap(() =>
        forkJoin(this.junpcCodmstService.getBrandCodes(),).pipe(
          map(([brand]) => {this.brands = brand.items;})
        )),
      // PRD_0021 add SIT end
      flatMap(() => this.fetchStores()),
      map(stores => this.generateInitialFormValue(this.session, stores)),
      flatMap(searchFormValue => this.searchPurchase(searchFormValue)),
      tap(() => this.visibled = true),
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.loadingSubscription.unsubscribe();
  }

  /**
   * @returns ディスタリスト
   */
  private fetchStores(): Observable<JunpcTnpmst[]> {
    return this.junpcTnpmstService.search({ shpcdAhead: Const.PURCHASE_SHPCD_PREFIX } as JunpcTnpmstSearchCondition).pipe(
      map(res => this.stores = res.items)
    );
  }

  /**
   * @param session セッション
   * @param stores ディスタリスト
   * @returns 検索フォーム初期値
   */
  private generateInitialFormValue(session: Session, stores: JunpcTnpmst[]): PurchaseSearchCondition {
    // storageから前回入力した検索条件を取得する。
    const storageCondition: PurchaseSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(session, StorageKey.PURCHASE_LIST_SEARCH_CONDITION));

    const formCondition = storageCondition == null ? new PurchaseSearchCondition() : storageCondition;

    const dayToday = this.dateUtilsService.generateCurrentDate();
    // 納品日fromは当日日付
    const searchCondition = Object.assign({ ...formCondition }, { correctionAtFrom: dayToday });
    // PRD_0021 mod SIT start
    // 納品日toの初期値は当日日付
    //searchCondition.correctionAtTo = storageCondition == null ? dayToday : searchCondition.correctionAtTo;
    // 納品日toの値が当日より前の場合、当日を設定
    searchCondition.correctionAtTo =
      // PRD_0077 mod SIT start
      // storageCondition == null ? dayToday :
      (FormUtils.isEmpty(storageCondition) || FormUtils.isEmpty(storageCondition.correctionAtTo)) ? dayToday :
      // PRD_0077 mod SIT end
        this.dateUtilsService.compare(searchCondition.correctionAtTo as NgbDateStruct, dayToday) == CompareResult.Less
          ? dayToday : searchCondition.correctionAtTo;
    // PRD_0021 mod SIT end
    // ディスタの初期値はマスタの先頭
    searchCondition.arrivalShop = this.stringUtils.isEmpty(searchCondition.arrivalShop) ?
      stores[0].shpcd : searchCondition.arrivalShop;

    // 生産メーカー名取得
    const mdfMakerCode = searchCondition.mdfMakerCode;
    if (this.stringUtils.isNotEmpty(mdfMakerCode) && mdfMakerCode.length === 5) {
      this.onChangeMaker(mdfMakerCode);
    }

    this.store.searchConditionSubject.next(searchCondition);
    return searchCondition;
  }

  /**
   * 生産メーカーフォーム内容変更時イベント
   * @param mdfMakerCode 生産メーカーフォーム内容
   */
  onChangeMaker(mdfMakerCode: String): void {
    this.mdfMakerName = '';

    this.junpcSirmstService.getSirmst({
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: mdfMakerCode
    } as JunpcSirmstSearchCondition).pipe(
      tap(this.extractMakerName)
    ).subscribe();
  }

  /**
   * 仕入マスタ検索結果からメーカー名を設定する.
   * @param result 仕入マスタ検索結果
   * @retrun メーカー名
   */
  private extractMakerName = (result: GenericList<JunpcSirmst>): string =>
    this.mdfMakerName = result == null || this.listUtils.isEmpty(result.items) ?
      '' : result.items[0].name

  /**
   * 検索ボタン押下時イベント.
   * @param searchForm 検索フォーム値
   */
  onClickSearchBtn(searchForm: PurchaseSearchCondition): void {
    this.noCheck = true;
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      tap(() => this.saveSearchConditions(this.session, searchForm)), // 検索条件保持
      tap(() => this.store.purchaseSearchResultSubject.next([])),  // 既存の検索結果クリア
      flatMap(() => this.searchPurchase(searchForm)),  // 検索処理
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 仕入一覧検索処理.
   * @param searchFormValue 検索フォーム値
   * @returns 検索結果
   */
  private searchPurchase(searchFormValue: PurchaseSearchCondition): Observable<PurchaseSearchResult[]> {
    return this.purchaseHttpService.search(searchFormValue).pipe(
      tap(result => this.nextPageToken = result.nextPageToken),
      tap(result => this.setSearchResultMessage(result.items)),
      flatMap(result => forkJoin(this.store.previousSearchResult$, of(result))),
      map(([previous, result]) => previous.concat(result.items)),
      tap(result => this.store.purchaseSearchResultSubject.next(result)),
      tap(result => this.store.createForm(result))
    );
  }

  /**
   * 検索条件をstorageに保持.
   * @param session セッション
   * @param searchCondition 検索条件
   */
  private saveSearchConditions(session: Session, searchCondition: PurchaseSearchCondition): void {
    this.localStorageService.createLocalStorage(session, StorageKey.PURCHASE_LIST_SEARCH_CONDITION, searchCondition);
  }

  /**
   * 納品Noリンク押下時の処理.
   * @param purchase リンクした行のデータ
   */
  onClickDeliveryNumberLink(purchase: PurchaseSearchResult): void {
    if (purchase.purchaseRegisteredCount === 0) {
      // 未仕入
      this.navigate(purchase.deliveryId, Path.NEW);
      return;
    }

    // 以下、仕入済
    if (purchase.purchaseConfirmedCount === 0) {
      // 仕入指示未送信
      this.navigate(purchase.deliveryId, Path.EDIT);
      return;
    }

    // 仕入指示送信済
    this.navigate(purchase.deliveryId, Path.VIEW);
  }

  /**
   * 一括仕入画面へ遷移する.
   * @param deliveryId 納品ID
   * @param path 遷移先画面のパス種別
   */
  private navigate(deliveryId, path: Path) {
    this.store.searchCondition$.pipe(
      tap(val => this.router.navigate(['/purchases', deliveryId, path], {
        queryParams: { arrivalShop: val.arrivalShop }
      }))
    ).subscribe();
  }

  /**
   * チェック押下処理.
   * @param someCheck チェックあり
   */
  onCheck(someCheck: boolean): void {
    this.noCheck = !someCheck;
  }

  /**
   * 最下部までスクロール時の処理.
   * 次のページのトークンがある場合、次のリストを取得する.
   */
  onScrollToBottom(): void {
    if (StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }

    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    this.searchPurchase({ pageToken: nextPageToken } as PurchaseSearchCondition).subscribe();
  }

  /**
   * LG送信処理.
   */
  onLgSubmit(): void {
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      flatMap(() => this.store.resultFormValue$),
      flatMap(formValue => this.purchaseHttpService.confirm(formValue)),
      tap(() => this.message.footer.success = { code: 'SUCSESS.LG_SEND_COMPLETE', param: null }),
      tap(() => this.noCheck = true),
      tap(() => this.store.purchaseSearchResultSubject.next([])),  // 既存の検索結果クリア
      // 再検索
      flatMap(() => this.store.searchCondition$),
      flatMap(searchCondition => this.searchPurchase(searchCondition)),
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
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
   * 検索結果に応じたメッセージをセットする.
   * @param results 検索結果
   */
  private setSearchResultMessage(results: PurchaseSearchResult[]): void {
    if (results.length === 0) {
      this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
    }
  }


  /**
   * メッセージをクリアする.
   */
  private clearMessage = (): void => {
    this.message = {
      body: {
        error: { code: '', param: null }
      },
      footer: {
        success: { code: '', param: null }
      }
    };
  }
}
