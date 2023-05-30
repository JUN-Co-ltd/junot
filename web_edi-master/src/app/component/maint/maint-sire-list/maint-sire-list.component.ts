import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription, Observable, forkJoin, of } from 'rxjs';
import { Router } from '@angular/router';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { HeaderService } from 'src/app/service/header.service';
import { SessionService } from 'src/app/service/session.service';
import { LocalStorageService } from 'src/app/service/local-storage.service';
import { LoadingService } from 'src/app/service/loading.service';
import { StringUtilsService } from 'src/app/service/bo/string-utils.service';
import { MaintSireListStoreService } from './store/maint-sire-list-store.service';
import { Session } from 'src/app/model/session';
import { tap, flatMap, map, catchError, finalize } from 'rxjs/operators';
import { StorageKey, SupplierType, SearchTextType, Path } from 'src/app/const/const';
import { MaintSireSearchCondition } from 'src/app/model/maint/maint-sire-search-condition';
import { JunpcSirmstService } from 'src/app/service/junpc-sirmst.service';
import { JunpcSirmstSearchCondition } from 'src/app/model/junpc-sirmst-search-condition';
import { MaintSireSearchResult } from 'src/app/model/maint/maint-sire-search-result';
import { MaintSireService } from 'src/app/service/maint/maint-sire.service';
import { GenericList } from 'src/app/model/generic-list';
import { JunpcSirmst } from 'src/app/model/junpc-sirmst';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';

@Component({
  selector: 'app-maint-sire-list',
  templateUrl: './maint-sire-list.component.html',
  styleUrls: ['./maint-sire-list.component.scss'],
  providers: [MaintSireListStoreService]
})
export class MaintSireListComponent implements OnInit, OnDestroy {

  /** ログインユーザーのセッション */
  private session: Session;

  /** 仕入先メーカー名 */
  sireName: String;

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
    private store: MaintSireListStoreService,
    private junpcSirmstService: JunpcSirmstService,
    private maintSireService: MaintSireService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.session = this.sessionService.getSaveSession();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe(isLoading => this.isLoading = isLoading);
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      map(() => this.generateInitialFormValue(this.session)),
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
   * @param session セッション
   * @returns 検索フォーム初期値
   */
  private generateInitialFormValue(session: Session): MaintSireSearchCondition {
    // storageから前回入力した検索条件を取得する。
    const storageCondition: MaintSireSearchCondition = JSON.parse(
      this.localStorageService.getSaveLocalStorage(session, StorageKey.MAINT_SIRE_LIST_SEARCH_CONDITIONS));
    const formCondition = storageCondition == null ? new MaintSireSearchCondition() : storageCondition;
    const searchCondition = Object.assign({}, { ...formCondition });
    this.store.searchConditionSubject.next(searchCondition);

    return searchCondition;
  }

  /**
   * 検索ボタン押下時イベント.
   * @param searchForm 検索フォーム値
   */
  onClickSearchBtn(searchForm: MaintSireSearchCondition): void {
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
  private search(searchFormValue: MaintSireSearchCondition): Observable<MaintSireSearchResult[]> {
    return this.maintSireService.search(searchFormValue).pipe(
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
  private setSearchResultMessage(results: MaintSireSearchResult[]) {
    if (results.length === 0) {
      this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
    }
  }

  /**
   * 検索条件をstorageに保持.
   * @param session セッション
   * @param searchCondition 検索条件
   */
  private saveSearchConditions(session: Session, searchCondition: MaintSireSearchCondition): void {
    this.localStorageService.createLocalStorage(session, StorageKey.MAINT_SIRE_LIST_SEARCH_CONDITIONS, searchCondition);
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
    this.search({ pageToken: nextPageToken } as MaintSireSearchCondition).subscribe();
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
   * 仕入先マスタ検索結果からメーカー名を設定する.
   * @param result 仕入先マスタ検索結果
   * @retrun 仕入先名
   */
  private extractMakerName = (result: GenericList<JunpcSirmst>): string =>
    this.sireName = result == null || this.listUtils.isEmpty(result.items) ? '' : result.items[0].name

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
    this.router.navigate(['/maint/sires', Path.NEW]);
  }

  /**
   * 詳細画面遷移リンク押下時の処理.
   * @param data リンクした行のデータ
   */
  onClickDetailScreenInitLink(data: MaintSireSearchResult): void {
    this.router.navigate(['/maint/sires', data.sireCode, Path.EDIT], { queryParams: { kojCode: data.kojCode } });
  }
}
