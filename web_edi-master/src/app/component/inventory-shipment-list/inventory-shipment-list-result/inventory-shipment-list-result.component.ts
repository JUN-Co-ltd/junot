import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { map, tap } from 'rxjs/operators';
import { ScrollEvent } from 'ngx-scroll-event';
import { InventoryShipmentListStoreService } from '../store/inventory-shipment-list-store.service';

@Component({
  selector: 'app-inventory-shipment-list-result',
  templateUrl: './inventory-shipment-list-result.component.html',
  styleUrls: ['./inventory-shipment-list-result.component.scss']
})
export class InventoryShipmentListResultComponent implements OnInit {

  /** 最下部へのスクロールイベント */
  @Output()
  private scrollToBottom = new EventEmitter();

  /** チェックon offイベント */
  @Output()
  private checkEvents = new EventEmitter();

  /** フォーム */
  resultForm$ = this.store.resultFormSubject.asObservable();

  constructor(
    private store: InventoryShipmentListStoreService
  ) { }

  ngOnInit() {
  }

  /**
   * チェックボックス押下時の処理.
   */
  onCheck(): void {
    this.store.resultFormValue$.pipe(
      map(val => val.inventoryShipmentConfirms.some(i => i.check)),
      tap(somCheck => this.checkEvents.emit(somCheck))
    ).subscribe();
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
