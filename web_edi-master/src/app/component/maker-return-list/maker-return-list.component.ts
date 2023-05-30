import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription, Observable, forkJoin, of } from 'rxjs';
import { Router } from '@angular/router';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { HeaderService } from 'src/app/service/header.service';
import { SessionService } from 'src/app/service/session.service';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { LoadingService } from 'src/app/service/loading.service';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';
import { MakerReturnListStoreService } from './store/maker-return-list-store.service';
import { Session } from 'src/app/model/session';
import { tap, flatMap, map, catchError, finalize } from 'rxjs/operators';
import { StorageKey, SupplierType, SearchTextType, StaffType, Path, Const } from 'src/app/const/const';
import { MakerReturnSearchCondition } from 'src/app/model/maker-return-search-condition';
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { JunpcCodmstSearchCondition } from 'src/app/model/junpc-codmst-search-condition';
import { MakerReturnSearchResult } from 'src/app/model/maker-return-search-result';
import { MakerReturnsHttpService } from 'src/app/service/maker-returns-http.service';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { JunpcTnpmstHttpService } from 'src/app/service/junpc-tnpmst-http.service';
import { LgSendType } from 'src/app/const/lg-send-type';
import { GenericList } from 'src/app/model/generic-list';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { JunpcTnpmstSearchCondition } from 'src/app/model/junpc-tnpmst-search-condition';

@Component({
  selector: 'app-maker-return-list',
  templateUrl: './maker-return-list.component.html',
  styleUrls: ['./maker-return-list.component.scss'],
  providers: [MakerReturnListStoreService]
})
export class MakerReturnListComponent implements OnInit, OnDestroy {

  /** ログインユーザーのセッション */
  private session: Session;

  /** ディスタリスト. */
  stores: JunpcTnpmst[];

  /** 仕入先メーカー名 */
  supplierName: String;

  /** 担当者名 */
  mdfStaffName: String;

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
    private router: Router,
    private messageConfirmModalService: MessageConfirmModalService,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private localStorageService: LocalStorageService,
    private loadingService: LoadingService,
    private stringUtils: StringUtilsService,
    private listUtils: ListUtilsService,
    private store: MakerReturnListStoreService,
    private junpcSirmstService: JunpcSirmstService,
    private junpcCodmstService: JunpcCodmstService,
    private makerReturnsHttpService: MakerReturnsHttpService,
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
      flatMap(() => this.fetchStores()),
      map(stores => this.generateInitialFormValue(this.session, stores)),
      flatMap(searchFormValue => this.search(searchFormValue)),
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
    return this.junpcTnpmstService.search({ shpcdAhead: Const.MAKER_RETURN_SHPCD_PREFIX } as JunpcTnpmstSearchCondition).pipe(
      map(res => this.stores = res.items)
    );
  }

  /**
   * @param session セッション
   * @returns 検索フォーム初期値
   */
  private generateInitialFormValue(session: Session, stores: JunpcTnpmst[]): MakerReturnSearchCondition {
    // storageから前回入力した検索条件を取得する。
    const storageCondition: MakerReturnSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(session, StorageKey.MAKER_RETURN_LIST_SEARCH_CONDITIONS));
    const formCondition = storageCondition == null ? new MakerReturnSearchCondition() : storageCondition;
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
  onClickSearchBtn(searchForm: MakerReturnSearchCondition): void {
    this.noCheck = true;
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      tap(() => this.saveSearchConditions(this.session, searchForm)), // 検索条件保持
      tap(() => this.store.searchResultSubject.next([])),  // 既存の検索結果クリア
      flatMap(() => this.search(searchForm)),  // 検索処理
      catchError(this.showErrorModal),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 一覧検索処理.
   * @param searchFormValue 検索フォーム値
   * @returns 検索結果
   */
  private search(searchFormValue: MakerReturnSearchCondition): Observable<MakerReturnSearchResult[]> {
    return this.makerReturnsHttpService.search(searchFormValue).pipe(
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
  private setSearchResultMessage(results: MakerReturnSearchResult[]) {
    if (results.length === 0) {
      this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
    }
  }

  /**
   * 検索条件をstorageに保持.
   * @param session セッション
   * @param searchCondition 検索条件
   */
  private saveSearchConditions(session: Session, searchCondition: MakerReturnSearchCondition): void {
    this.localStorageService.createLocalStorage(session, StorageKey.MAKER_RETURN_LIST_SEARCH_CONDITIONS, searchCondition);
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
    this.search({ pageToken: nextPageToken } as MakerReturnSearchCondition).subscribe();
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
      flatMap(formValue => this.makerReturnsHttpService.confirm(formValue)),
      tap(() => this.message.footer.success = { code: 'SUCSESS.MAKER_RETURN_LG_SEND_COMPLETE', param: null }),
      tap(() => this.noCheck = true),
      tap(() => this.store.searchResultSubject.next([])),  // 既存の検索結果クリア
      // 再検索
      flatMap(() => this.store.searchCondition$),
      flatMap(searchCondition => this.search(searchCondition)),
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
   * 仕入先フォーム項目入力値変更時イベント
   * @param input 仕入先フォーム項目入力値
   */
  onChangeSupplier(input: String): void {
    this.junpcSirmstService.getSirmst({
      sirkbn: SupplierType.MDF_MAKER,
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: input
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
    this.supplierName = result == null || this.listUtils.isEmpty(result.items) ? '' : result.items[0].name

  /**
   * 担当者フォーム項目入力値変更時イベント
   * @param input 担当者フォーム項目入力値
   */
  onChangeMaker(input: String): void {
    this.mdfStaffName = '';
    this.junpcCodmstService.getStaffs({
      staffType: StaffType.NO_SELECT.toString(),
      searchType: SearchTextType.CODE_PERFECT_MATCH,
      searchText: input
    } as JunpcCodmstSearchCondition).pipe(
      tap(this.extractStaffName)
    ).subscribe();
  }

  /**
   * 担当者マスタ検索結果から担当者名を設定する.
   * @param result 担当者マスタ検索結果
   * @retrun 担当者名
   */
  private extractStaffName = (result: GenericList<JunpcCodmst>): string =>
    this.mdfStaffName = result == null || this.listUtils.isEmpty(result.items) ? '' : result.items[0].item2

  /**
   * チェック押下処理.
   * @param someCheck チェックあり
   */
  onCheck(someCheck: boolean): void {
    this.noCheck = !someCheck;
  }

  /**
   * 新規登録画面遷移リンク押下時の処理.
   */
  onClickNew(): void {
    this.router.navigate(['/makerReturns', Path.NEW]);
  }

  /**
   * 詳細画面遷移リンク押下時の処理.
   * @param data リンクした行のデータ
   */
  onClickDetailScreenInitLink(data: MakerReturnSearchResult): void {
    if (data.lgSendType === LgSendType.NO_INSTRUCTION) {
      this.router.navigate(['/makerReturns', data.voucherNumber, Path.EDIT], { queryParams: { orderId: data.orderId } });
      return;
    }

    this.router.navigate(['/makerReturns', data.voucherNumber, Path.VIEW], { queryParams: { orderId: data.orderId } });
  }
}
