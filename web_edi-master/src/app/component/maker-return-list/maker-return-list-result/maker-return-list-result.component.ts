import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { MakerReturnListStoreService } from '../store/maker-return-list-store.service';
import { map, tap } from 'rxjs/operators';
import { ScrollEvent } from 'ngx-scroll-event';
import { MakerReturnSearchResult } from 'src/app/model/maker-return-search-result';
import { Path } from 'src/app/const/const';
import { LgSendType } from 'src/app/const/lg-send-type';

@Component({
  selector: 'app-maker-return-list-result',
  templateUrl: './maker-return-list-result.component.html',
  styleUrls: ['./maker-return-list-result.component.scss']
})
export class MakerReturnListResultComponent implements OnInit {

  /** パス */
  readonly PATH = Path;

  /** LG送信フラグ */
  readonly LG_SEND_TYPE = LgSendType;

  /** 最下部へのスクロールイベント */
  @Output()
  private scrollToBottom = new EventEmitter();

  /** チェックon offイベント */
  @Output()
  private checkEvents = new EventEmitter();

  /** 詳細画面遷移リンククリックイベント */
  @Output()
  private clickDetailScreenInitLink = new EventEmitter();

  /** フォーム */
  resultForm$ = this.store.resultFormSubject.asObservable();

  constructor(
    private store: MakerReturnListStoreService
  ) { }

  ngOnInit() {
  }

  /**
   * チェックボックス押下時の処理.
   */
  onCheck(): void {
    this.store.resultFormValue$.pipe(
      map(val => val.makerReturnConfirms.some(m => m.check)),
      tap(somCheck => this.checkEvents.emit(somCheck))
    ).subscribe();
  }

  /**
   * 詳細画面遷移リンク押下時の処理.
   * @param data リンクした行のデータ
   */
  onClickDetailScreenInitLink(data: MakerReturnSearchResult): void {
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
}
