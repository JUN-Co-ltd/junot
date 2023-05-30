import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { map, tap } from 'rxjs/operators';
import { ScrollEvent } from 'ngx-scroll-event';
import { DistributionShipmentListStoreService } from '../store/distribution-shipment-list-store.service';
import { CarryType } from 'src/app/const/const';
import { DistributionShipmentSearchResult } from 'src/app/model/distribution-shipment-search-result';
import { LgSendType } from 'src/app/const/lg-send-type';

@Component({
  selector: 'app-distribution-shipment-list-result',
  templateUrl: './distribution-shipment-list-result.component.html',
  styleUrls: ['./distribution-shipment-list-result.component.scss']
})
export class DistributionShipmentListResultComponent implements OnInit {

  /** 最下部へのスクロールイベント */
  @Output()
  private scrollToBottom = new EventEmitter();

  /** チェックon offイベント */
  @Output()
  private checkEvents = new EventEmitter();

  /** フォーム */
  resultForm$ = this.store.resultFormSubject.asObservable();

  /** キャリー区分 */
  readonly CARRY_TYPE = CarryType;

  constructor(
    private store: DistributionShipmentListStoreService
  ) { }

  ngOnInit() {
  }

  /**
   * チェックボックス押下時の処理.
   */
  onCheck(): void {
    this.store.resultFormValue$.pipe(
      map(val => val.distributionShipmentConfirms.some(d => d.check)),
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

 /**
  * 指示可否判定
  * @param distributionShipment 検索結果の値
  * @returns true:指示可 false:指示不可
  */
  isSendInstruction(distributionShipment: DistributionShipmentSearchResult): boolean {

　　 // 送信済判定
    if (distributionShipment.sendStatus === LgSendType.INSTRUCTION) { return false; }

    // 仕入数超過判定
    if (this.isOrverFixArrivalLot(distributionShipment)) {
      return false;
    }

    // 直送判定
    if (this.isDirectDelivery(distributionShipment.carryType)) {
      return false;
    }

    return true;
  }

  /**
   * 直送判定処理
   * @param value キャリー区分
   * @returns true:直送 false:通常
   */
  isDirectDelivery(value: CarryType): boolean {
    if (value === CarryType.DIRECT) { return true; }

    return false;
  }

  /**
   * 仕入数超過判定
   * @param distributionShipment 検索結果の値
   * @returns true:超過 false:未超過
   */
  isOrverFixArrivalLot(distributionShipment: DistributionShipmentSearchResult): boolean {

    // 未入荷の場合はチェックしない
    if (!distributionShipment.arrivalFlg) { return false; }

    if (distributionShipment.fixArrivalLotSum < distributionShipment.deliveryLotSum) {
      return true;
    }

    return false;

  }
}
