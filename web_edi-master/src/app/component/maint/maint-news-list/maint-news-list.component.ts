import { Component, OnInit, OnDestroy } from '@angular/core';
import { ScrollEvent } from 'ngx-scroll-event';
import { Observable, Subscription } from 'rxjs';
import { tap, flatMap, catchError, finalize } from 'rxjs/operators';

import { Path } from 'src/app/const/const';
import { GenericList } from 'src/app/model/generic-list';
import { MaintNewsSearchCondition } from 'src/app/model/maint/maint-news-search-condition';
import { MaintNewsSearchResult } from 'src/app/model/maint/maint-news-search-result';
import { MaintNewsService } from 'src/app/service/maint/maint-news.service';
import { HeaderService } from 'src/app/service/header.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { StringUtils } from 'src/app/util/string-utils';

@Component({
  selector: 'app-maint-news-list',
  templateUrl: './maint-news-list.component.html',
  styleUrls: ['./maint-news-list.component.scss']
})
export class MaintNewsListComponent implements OnInit, OnDestroy {
  readonly PATH = Path;

  /** 画面を非表示にする */
  invisibled = true;
  /** 画面を非活性にする */
  disabled = true;
  /** 画面に表示するメッセージ */
  message = {
    /** ボディ */
    body: {
      /** 異常系 */
      error: { code: '', param: null }
    }
  };
  /** 検索結果 */
  searchResultItems: MaintNewsSearchResult[] = [];
  /** 次のページのトークン */
  nextPageToken: string;

  /** ローディングサブスクリプション */
  private loadingSubscription: Subscription;

  constructor(
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private maintNewsService: MaintNewsService
  ) { }

  ngOnInit() {
    this.headerService.show();

    // ローディングクリア（親画面のみ）
    this.loadingService.clear();

    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.disabled = isLoading);

    let loadingToken = null;
    let isError = false;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // 画面非表示
      tap(() => this.invisibled = true),
      // お知らせを検索
      flatMap(() => this.search()),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => {
        isError = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      finalize(() => {
        // 画面表示（エラーが発生した場合は、非表示のままとする）
        this.invisibled = isError;
        // ローディング停止
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  ngOnDestroy() {
    // ローディングサブスクリプション停止
    this.loadingSubscription.unsubscribe();
  }

  /**
   * メッセージをクリアする。
   */
  private clearMessage(): void {
    this.message = {
      body: {
        error: { code: '', param: null }
      }
    };
  }

  /**
   * 検索処理を行う。
   */
  private search(): Observable<GenericList<MaintNewsSearchResult>> {
    // 検索結果をクリア
    this.searchResultItems = [];

    // お知らせを検索
    return this.maintNewsService.search(this.generateApiSearchCondition()).pipe(
      tap(genericList => {
        // 次のページのトークンを保存
        this.nextPageToken = genericList.nextPageToken;

        if (genericList.items.length === 0 ) {
          // 検索結果なし
          this.message.body.error = { code: 'INFO.RESULT_ZERO', param: null };
        } else {
          // 検索結果が1件以上の場合はリストに設定
          this.searchResultItems = genericList.items;
        }
      })
    );
  }

  /**
   * 次の検索処理を行う。
   */
  private nextSearch(): Observable<GenericList<MaintNewsSearchResult>> {
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    // お知らせを検索
    return this.maintNewsService.search({ pageToken: nextPageToken } as MaintNewsSearchCondition).pipe(
        tap(genericList => {
          // 次のページのトークンを保存
          this.nextPageToken = genericList.nextPageToken;
          this.searchResultItems = this.searchResultItems.concat(genericList.items);
        })
    );
  }

  /**
   * API検索用Modelを設定する。
   * @param maintNewsList お知らせ一覧検索条件
   */
  private generateApiSearchCondition(): MaintNewsSearchCondition {
    // お知らせ一覧検索条件
    const searchCondition = new MaintNewsSearchCondition();

    return searchCondition;
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得する。
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isReachingBottom && StringUtils.isNotEmpty(this.nextPageToken)) {
      let loadingToken = null;

      // ローディング開始
      this.loadingService.start().pipe(
        // ローディングトークン待避
        tap((token) => loadingToken = token),
        // メッセージをクリア
        tap(() => this.clearMessage()),
          // お知らせを検索
        flatMap(() => this.nextSearch()),
        // エラーが発生した場合は、エラーモーダルを表示
        catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
        // ローディング停止
        finalize(() => this.loadingService.stop(loadingToken))
      ).subscribe();
    }
  }
}
