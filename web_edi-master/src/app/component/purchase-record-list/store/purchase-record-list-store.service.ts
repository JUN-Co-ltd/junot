//PRD_0133 #10181 add JFE start
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { PurchaseRecordSearchCondition } from 'src/app/model/purchase-record-search-condition';
import { PurchaseRecordSearchResult } from 'src/app/model/purchase-record-search-result';
import { FormGroup, FormBuilder } from '@angular/forms';
import { first, map } from 'rxjs/operators';
import { PurchaseConfirmList } from 'src/app/model/purchase-confirm-list';

/**
 * Store
 */
@Injectable()
export class PurchaseRecordListStoreService {

  /** 現在の検索条件 */
  searchConditionSubject = new BehaviorSubject<PurchaseRecordSearchCondition>(null);

  /** 仕入情報検索結果 */
  purchaseSearchResultSubject = new BehaviorSubject<PurchaseRecordSearchResult[]>([]);

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
   * @param purchasesRecord 仕入実績情報リスト
   */
  createForm(purchasesRecord: PurchaseRecordSearchResult[]): void {
    this.resultFormSubject.next(this.fb.group({
      purchasesRecord: this.fb.array(purchasesRecord.map(p => this.fb.group(({
        supplierCode: [p.supplierCode],
        supplierName: [p.supplierName],
        arrivalPlace: [p.arrivalPlace],
        logisticsCode:[p.logisticsCode],
        recordAt: [p.recordAt],
        purchaseVoucherNumber: [p.purchaseVoucherNumber],
        purchaseType: [p.purchaseType],
        partNo: [p.partNo],
        fixArrivalCount: [p.fixArrivalCount],
        mkyu: [p.mkyu],
        purchaseUnitPrice: [p.purchaseUnitPrice],
        // PRD_0166 #10181 jfe mod start
        // unitPriceSum: [p.unitPriceSum]
        unitPriceSum: [p.unitPriceSum],
        fileInfoId:[p.fileInfoId]
        // PRD_0166 #10181 jfe add end
      }))))
    }));
  }
}
//PRD_0133 #10181 add JFE end
