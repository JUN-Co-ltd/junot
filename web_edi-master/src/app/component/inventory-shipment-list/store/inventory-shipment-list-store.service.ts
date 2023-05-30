import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { FormGroup, FormBuilder } from '@angular/forms';
import { first, map } from 'rxjs/operators';
import { InventoryShipmentSearchCondition } from 'src/app/model/inventory-shipment-search-condition';
import { InventoryShipmentSearchResult } from 'src/app/model/inventory-shipment-search-result';
import { InventoryShipmentConfirmList } from 'src/app/model/inventory-shipment-confirm-list';

@Injectable()
export class InventoryShipmentListStoreService {


  /** 現在の検索条件 */
  searchConditionSubject = new BehaviorSubject<InventoryShipmentSearchCondition>(null);

  /** 検索結果 */
  searchResultSubject = new BehaviorSubject<InventoryShipmentSearchResult[]>([]);

  /** 検索結果フォーム */
  resultFormSubject = new BehaviorSubject<FormGroup>(null);

  /** 取得済みの検索結果 */
  previousSearchResult$ = this.searchResultSubject.pipe(first());

  /** 検索条件 */
  searchCondition$ = this.searchConditionSubject.pipe(first());

  /** 検索結果フォームの値 */
  resultFormValue$: Observable<InventoryShipmentConfirmList> = this.resultFormSubject.pipe(
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
  createResultForm(results: InventoryShipmentSearchResult[]): void {
    this.resultFormSubject.next(this.fb.group({
      inventoryShipmentConfirms: this.fb.array(results.map(i => this.fb.group(({
        check: [false],
        cargoAt: [i.cargoAt],
        cargoPlace: [i.cargoPlace],
        instructorSystem: [i.instructorSystem],
        brandCode: [i.brandCode],
        brandName: [i.brandName],
        divisionCode: [i.divisionCode],
        partNo: [i.partNo],
        productName: [i.productName],
        deliveryLotSum: [i.deliveryLotSum],
        retailPriceSum: [i.retailPriceSum],
        lgSendType: [i.lgSendType]
      }))))
    }));
  }
}
