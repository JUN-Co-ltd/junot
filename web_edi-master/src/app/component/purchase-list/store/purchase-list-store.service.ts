import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { PurchaseSearchCondition } from 'src/app/model/purchase-search-condition';
import { PurchaseSearchResult } from 'src/app/model/purchase-search-result';
import { FormGroup, FormBuilder } from '@angular/forms';
import { first, map } from 'rxjs/operators';
import { PurchaseConfirmList } from 'src/app/model/purchase-confirm-list';

/**
 * Store
 */
@Injectable()
export class PurchaseListStoreService {

  /** 現在の検索条件 */
  searchConditionSubject = new BehaviorSubject<PurchaseSearchCondition>(null);

  /** 仕入情報検索結果 */
  purchaseSearchResultSubject = new BehaviorSubject<PurchaseSearchResult[]>([]);

  /** 検索結果フォーム */
  resultFormSubject = new BehaviorSubject<FormGroup>(null);

  /** 取得済みの検索結果 */
  previousSearchResult$ = this.purchaseSearchResultSubject.pipe(first());

  /** 検索条件 */
  searchCondition$ = this.searchConditionSubject.pipe(first());

  /** フォームの値 */
  resultFormValue$: Observable<PurchaseConfirmList> = this.resultFormSubject.pipe(
    first(),
    map(form => form.value)
  );

  constructor(
    private fb: FormBuilder
  ) { }

  /**
   * フォームを作成する.
   * @param purchases 仕入情報リスト
   */
  createForm(purchases: PurchaseSearchResult[]): void {
    this.resultFormSubject.next(this.fb.group({
      purchases: this.fb.array(purchases.map(p => this.fb.group(({
        check: [false],
        purchaseRegisteredCount: [p.purchaseRegisteredCount],
        purchaseConfirmedCount: [p.purchaseConfirmedCount],
        orderNumber: [p.orderNumber],
        deliveryNumber: [p.deliveryNumber],
        deliveryId: [p.deliveryId],
        purchaseCount: [p.deliveryCount],
        divisionCode: [p.divisionCode],
        carryType: [p.carryType],
        arrivalFlg: [p.arrivalFlg],
        correctionAt: [p.correctionAt],
        mdfMakerCode: [p.mdfMakerCode],
        mdfMakerName: [p.mdfMakerName],
        partNo: [p.partNo],
        productName: [p.productName],
        deliveryLot: [p.deliveryLot],
        arrivalCountSum: [p.arrivalCountSum],
        fixArrivalCountSum: [p.fixArrivalCountSum],
        lgSendType: [p.lgSendType]
      }))))
    }));
  }
}
