import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ScrollEvent } from 'ngx-scroll-event';
import { PurchaseSearchResult } from 'src/app/model/purchase-search-result';
import { PurchaseListStoreService } from '../store/purchase-list-store.service';
import { tap, map } from 'rxjs/operators';
import { CarryType } from 'src/app/const/const';

@Component({
  selector: 'app-purchase-list-result',
  templateUrl: './purchase-list-result.component.html',
  styleUrls: ['./purchase-list-result.component.scss']
})
export class PurchaseListResultComponent implements OnInit {

  /** 直送. */
  readonly DIRECT_CARRY = CarryType.DIRECT;

  /** 最下部へのスクロールイベント */
  @Output()
  private scrollToBottom = new EventEmitter();

  /** チェックon offイベント */
  @Output()
  private checkEvents = new EventEmitter();

  /** 納品Noクリックイベント */
  @Output()
  private clickDeliveryNumberLink = new EventEmitter();

  /** フォーム */
  resultForm$ = this.store.resultFormSubject.asObservable();

  constructor(
    private store: PurchaseListStoreService
  ) { }

  ngOnInit() {
  }

  /**
   * チェックボックス押下時の処理.
   */
  onCheck(): void {
    this.store.resultFormValue$.pipe(
      map(val => val.purchases.some(p => p.check)),
      tap(somCheck => this.checkEvents.emit(somCheck))
    ).subscribe();
  }

  /**
   * 納品Noリンク押下時の処理.
   * @param purchase リンクした行のデータ
   */
  onClickDeliveryNumberLink(purchase: PurchaseSearchResult): void {
    this.clickDeliveryNumberLink.emit(purchase);
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
