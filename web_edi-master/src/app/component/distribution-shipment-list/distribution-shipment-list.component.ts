import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription, Observable, forkJoin, of } from 'rxjs';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { HeaderService } from 'src/app/service/header.service';
import { SessionService } from 'src/app/service/session.service';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { LoadingService } from 'src/app/service/loading.service';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';
import { Session } from 'src/app/model/session';
import { tap, flatMap, map, catchError, finalize } from 'rxjs/operators';
import { StorageKey, Const } from 'src/app/const/const';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { JunpcCodmstSearchCondition } from 'src/app/model/junpc-codmst-search-condition';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { GenericList } from 'src/app/model/generic-list';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { DistributionShipmentListStoreService } from './store/distribution-shipment-list-store.service';
import { DistributionShipmentSearchCondition } from 'src/app/model/distribution-shipment-search-condition';
import { DistributionShipmentSearchResult } from 'src/app/model/distribution-shipment-search-result';
import { DistributionShipmentHttpService } from 'src/app/service/distribution-shipment-http.service';
import { BrandCode } from 'src/app/model/brand-code';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';

@Component({
  selector: 'app-distribution-shipment-list',
  templateUrl: './distribution-shipment-list.component.html',
  styleUrls: ['./distribution-shipment-list.component.scss'],
  providers: [DistributionShipmentListStoreService]
})
export class DistributionShipmentListComponent implements OnInit, OnDestroy {

  /** ログインユーザーのセッション */
  private session: Session;

  /** ディスタリスト. */
  stores: JunpcTnpmst[];

  /** ブランドリスト */
  brands: BrandCode[] = [];

  /** アイテムリスト */
  items: JunpcCodmst[] = [];

  /** 事業部リスト */
  departments: JunpcCodmst[] = [];

  /** 配分課名 */
  divisionName: String;

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
    /** 本文 */
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
    private messageConfirmModalService: MessageConfirmModalService,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private localStorageService: LocalStorageService,
    private loadingService: LoadingService,
    private stringUtils: StringUtilsService,
    private listUtils: ListUtilsService,
    private store: DistributionShipmentListStoreService,
    private junpcCodmstService: JunpcCodmstService,
    private distributionShipmentHttpService: DistributionShipmentHttpService,
    private junpcTnpmstService: JunpcTnpmstHttpService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(isLoading => this.isLoading = isLoading);
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      flatMap(() =>
        forkJoin(
          this.junpcCodmstService.getAllDivisions(),
          this.junpcCodmstService.getBrandCodes(),
          this.junpcCodmstService.getItems()
        ).pipe(
          map(([department, brand, item]) => {
            this.departments = department.items;
            this.brands = brand.items;
            this.items = item.items;
          })
        )
      ),
      flatMap(() => this.fetchStores()),
      map(stores => this.generateInitialFormValue(this.session, stores)),
      flatMap(searchFormValue => this.searchDistributionShipment(searchFormValue)),
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
   * @param stores 店舗マスタリスト
   * @returns 検索フォーム初期値
   */
  private generateInitialFormValue(session: Session, stores: JunpcTnpmst[]): DistributionShipmentSearchCondition {
    // storageから前回入力した検索条件を取得する。
    const storageCondition: DistributionShipmentSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(session, StorageKey.DISTRIBUTION_SHIPMENT_LIST_SEARCH_CONDITIONS));
    const formCondition = storageCondition == null ? new DistributionShipmentSearchCondition() : storageCondition;

    const searchCondition = Object.assign({}, { ...formCondition });
    // ディスタの初期値はマスタの先頭
    searchCondition.shpcd = this.stringUtils.isEmpty(searchCondition.shpcd) ?
      stores[0].shpcd : searchCondition.shpcd;

    this.store.searchConditionSubject.next(searchCondition);
    return searchCondition;
  }

  /**
   * 検索ボタン押下時イベント.
   * @param searchForm 検索フォーム値
   */
  onClickSearchBtn(searchForm: DistributionShipmentSearchCondition): void {
    this.noCheck = true;
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      tap(() => this.saveSearchConditions(this.session, searchForm)), // 検索条件保持
      tap(() => this.store.searchResultSubject.next([])),  // 既存の検索結果クリア
      flatMap(() => this.searchDistributionShipment(searchForm)),  // 検索処理
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 一覧検索処理.
   * @param searchFormValue 検索フォーム値
   * @returns 検索結果
   */
  private searchDistributionShipment(searchFormValue: DistributionShipmentSearchCondition): Observable<DistributionShipmentSearchResult[]> {
    return this.distributionShipmentHttpService.search(searchFormValue).pipe(
      tap(result => this.nextPageToken = result.nextPageToken),
      tap(result => this.setSearchResultMessage(result.items)),
      flatMap(result => forkJoin(this.store.previousSearchResult$, of(result))),
      map(([previous, result]) => previous.concat(result.items)),
      tap(result => this.store.searchResultSubject.next(result)),
      tap(result => this.store.createResultForm(result))
    );
  }

  /**
   * 検索結果に応じたメッセージをセットする.
   * @param results 検索結果
   */
  private setSearchResultMessage(results: DistributionShipmentSearchResult[]) {
    if (results.length === 0) {
      this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
    }
  }

  /**
   * 検索条件をstorageに保持.
   * @param session セッション
   * @param searchCondition 検索条件
   */
  private saveSearchConditions(session: Session, searchCondition: DistributionShipmentSearchCondition): void {
    this.localStorageService.createLocalStorage(session, StorageKey.DISTRIBUTION_SHIPMENT_LIST_SEARCH_CONDITIONS, searchCondition);
  }

  /**
   * 最下部までスクロール時の処理.
   * 次のページのトークンがある場合、次のリストを取得する.
   */
  onScrollToBottom(): void {
    if (this.stringUtils.isEmpty(this.nextPageToken)) {
      return;
    }

    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;
    this.searchDistributionShipment({ pageToken: nextPageToken } as DistributionShipmentSearchCondition).subscribe();
  }

  /**
   * 出荷指示送信処理.
   */
  onLgSubmit(): void {
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      flatMap(() => this.store.resultFormValue$),
      flatMap(formValue => this.distributionShipmentHttpService.confirm(formValue)),
      tap(() => this.message.footer.success = { code: 'SUCSESS.LG_SEND_COMPLETE', param: null }),
      tap(() => this.noCheck = true),
      tap(() => this.store.searchResultSubject.next([])),  // 既存の検索結果クリア
      // 再検索
      flatMap(() => this.store.searchCondition$),
      flatMap(searchCondition => this.searchDistributionShipment(searchCondition)),
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
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

  /**
   * エラーモーダルを表示する.
   * @param error エラー情報
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  private showErrorModal = (error: any): Observable<boolean> => this.messageConfirmModalService.openErrorModal(error);

  /**
   * 課フォーム項目入力値変更時イベント
   * @param input 課フォーム項目入力値
   */
  onChangeDivision(input: String): void {
    this.junpcCodmstService.fetchDistributionSections({ divisionCode: input } as JunpcCodmstSearchCondition).pipe(
      tap(this.extractDivisionName)
    ).subscribe();
  }

  /**
   * 配分課マスタ検索結果から配分課名を設定する.
   * @param result 配分課マスタ検索結果
   * @retrun 配分課名
   */
  private extractDivisionName = (result: GenericList<JunpcCodmst>): string =>
    this.divisionName = result == null
      || this.listUtils.isEmpty(result.items)
      ? ''
      : result.items[0].item1

  /**
   * チェック押下処理.
   * @param someCheck チェックあり
   */
  onCheck(someCheck: boolean): void {
    this.noCheck = !someCheck;
  }
}
