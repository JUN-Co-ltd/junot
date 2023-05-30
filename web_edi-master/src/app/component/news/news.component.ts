import { Component, OnInit } from '@angular/core';
import { ScrollEvent } from 'ngx-scroll-event';
import { Observable } from 'rxjs';
import { tap, flatMap, catchError, finalize } from 'rxjs/operators';

import { NewsSearchCondition } from '../../model/news-search-condition';
import { GenericList } from '../../model/generic-list';
import { News } from '../../model/news';
import { NewsService } from 'src/app/service/news.service';
import { HeaderService } from '../../service/header.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { StringUtils } from '../../util/string-utils';

@Component({
  selector: 'app-news',
  templateUrl: './news.component.html',
  styleUrls: ['./news.component.scss']
})
export class NewsComponent implements OnInit {
  /** 画面に表示するメッセージ */
  message = {
    /** ボディ */
    body: {
      /** 異常系 */
      error: { code: '', param: null }
    }
  };
  /** 検索結果 */
  searchResultItems: News[] = [];
  /** 次のページのトークン */
  nextPageToken: string;

  constructor(
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private newsService: NewsService
  ) { }

  ngOnInit() {
    this.headerService.show();

    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // お知らせを検索
      flatMap(() => this.search()),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
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
  private search(): Observable<GenericList<News>> {
    // 検索結果をクリア
    this.searchResultItems = [];

    // お知らせを検索
    return this.newsService.search(this.generateApiSearchCondition()).pipe(
      tap(genericList => {
        // 次のページのトークンを保存
        this.nextPageToken = genericList.nextPageToken;

        if (genericList.items.length === 0 ) {
          // 検索結果なし
          this.message.body.error = { code: 'INFO.NO_NEWS', param: null };
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
  private nextSearch(): Observable<GenericList<News>> {
    const nextPageToken = this.nextPageToken;
    this.nextPageToken = null;

    // お知らせを検索
    return this.newsService.search({ pageToken: nextPageToken } as NewsSearchCondition).pipe(
        tap(genericList => {
          // 次のページのトークンを保存
          this.nextPageToken = genericList.nextPageToken;
          this.searchResultItems = this.searchResultItems.concat(genericList.items);
        })
    );
  }

  /**
   * API検索用Modelを設定する。
   * @param NewsList お知らせ一覧検索条件
   */
  private generateApiSearchCondition(): NewsSearchCondition {
    // お知らせ一覧検索条件
    const searchCondition = new NewsSearchCondition();

    // お知らせ一覧の件数を設定
    searchCondition.maxResults = 20;

    return searchCondition;
  }

  /**
   * スクロール時、最下部までスクロールされた場合は、次の検索結果を取得する。
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isWindowEvent || !event.isReachingBottom || StringUtils.isEmpty(this.nextPageToken)) {
      return;
    }

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
