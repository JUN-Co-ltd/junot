import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { FormGroup, FormBuilder } from '@angular/forms';
import { first, map } from 'rxjs/operators';
import { DistributionShipmentSearchCondition } from 'src/app/model/distribution-shipment-search-condition';
import { DistributionShipmentSearchResult } from 'src/app/model/distribution-shipment-search-result';
import { DistributionShipmentConfirmList } from 'src/app/model/distribution-shipment-confirm-list';

@Injectable()
export class DistributionShipmentListStoreService {

  /** 現在の検索条件 */
  searchConditionSubject = new BehaviorSubject<DistributionShipmentSearchCondition>(null);

  /** 検索結果 */
  searchResultSubject = new BehaviorSubject<DistributionShipmentSearchResult[]>([]);

  /** 検索結果フォーム */
  resultFormSubject = new BehaviorSubject<FormGroup>(null);

  /** 取得済みの検索結果 */
  previousSearchResult$ = this.searchResultSubject.pipe(first());

  /** 検索条件 */
  searchCondition$ = this.searchConditionSubject.pipe(first());

  /** 検索結果フォームの値 */
  resultFormValue$: Observable<DistributionShipmentConfirmList> = this.resultFormSubject.pipe(
    first(),
    map(form => form.value)
  );

  constructor(
    private fb: FormBuilder
  ) { }

  /**
   * 検索結果フォームを作成する.
   * @param results 検索結果リスト
   */
  createResultForm(results: DistributionShipmentSearchResult[]): void {
    this.resultFormSubject.next(this.fb.group({
      distributionShipmentConfirms: this.fb.array(results.map(d => this.fb.group(({
        check: [false],
        id: [d.id],
        shippingInstructionsAt: [d.shippingInstructionsAt],
        arrivalFlg: [d.arrivalFlg],
        arrivalAt: [d.arrivalAt],
        deliveryRequestAt: [d.deliveryRequestAt],
        sendStatus: [d.sendStatus],
        orderNumber: [d.orderNumber],
        deliveryNumber: [d.deliveryNumber],
        deliveryCount: [d.deliveryCount],
        divisionCode: [d.divisionCode],
        carryType: [d.carryType],
        partNo: [d.partNo],
        productName: [d.productName],
        deliveryLotSum: [d.deliveryLotSum],
        fixArrivalLotSum: [d.fixArrivalLotSum],
        retailPriceSum: [d.retailPriceSum]
      }))))
    }));
  }
}
