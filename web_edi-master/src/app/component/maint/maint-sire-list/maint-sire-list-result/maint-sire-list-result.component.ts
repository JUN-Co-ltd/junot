import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { MaintSireListStoreService } from '../store/maint-sire-list-store.service';
import { ScrollEvent } from 'ngx-scroll-event';
import { MaintSireSearchResult } from 'src/app/model/maint/maint-sire-search-result';
import { Path, MSirmstYugaikbnType } from 'src/app/const/const';

@Component({
  selector: 'app-maint-sire-list-result',
  templateUrl: './maint-sire-list-result.component.html',
  styleUrls: ['./maint-sire-list-result.component.scss']
})
export class MaintSireListResultComponent implements OnInit {
  /** CSS設定 */
  private CSS_RED = 'font-color-red';

  /** パス */
  readonly PATH = Path;

  /** 最下部へのスクロールイベント */
  @Output()
  private scrollToBottom = new EventEmitter();

  /** 詳細画面遷移リンククリックイベント */
  @Output()
  private clickDetailScreenInitLink = new EventEmitter();

  /** フォーム */
  resultForm$ = this.store.resultFormSubject.asObservable();

  constructor(
    private store: MaintSireListStoreService
  ) { }

  ngOnInit() {
  }

  /**
   * 詳細画面遷移リンク押下時の処理.
   * @param data リンクした行のデータ
   */
  onClickDetailScreenInitLink(data: MaintSireSearchResult): void {
    this.clickDetailScreenInitLink.emit(data);
  }

  /**
   * スクロール時の処理
   * @param event ScrollEvent
   */
  onScroll(event: ScrollEvent): void {
    if (event.isWindowEvent || !event.isReachingBottom) {
      return;
    }
    this.scrollToBottom.emit(event);
  }

  /**
   * 有害物質対応区分に応じたフォントカラーのCSS設定を返却する
   * @param status 有害物質対応区分
   * @returns フォントカラー
   */
  fontColorStatus(status: string): string {
    if (status == MSirmstYugaikbnType.UNSUBMITTED) {
        return this.CSS_RED;
    } else {
      return null;
    }
  }
}
