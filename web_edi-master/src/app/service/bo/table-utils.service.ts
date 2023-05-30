import { Injectable } from '@angular/core';
import { ScrollEvent } from 'ngx-scroll-event';
import { StringUtilsService } from './string-utils.service';

@Injectable({
  providedIn: 'root'
})
export class TableUtilsService {

  constructor(
    private stringUtils: StringUtilsService
  ) { }

  /**
   * ウィンドウのスクロールではなくテーブルのスクロールで
   * 最下部までスクロールされ、次のページのトークンがある場合は、次のページを取得可能.
   * @param event スクロールイベント
   * @param nextPageToken 次のページのトークン
   * @returns true:次のページ取得不可
   */
  cannotSearchNextPage(event: ScrollEvent, nextPageToken: string): boolean {
    return event.isWindowEvent
      || !event.isReachingBottom
      || this.stringUtils.isEmpty(nextPageToken);
  }
}
