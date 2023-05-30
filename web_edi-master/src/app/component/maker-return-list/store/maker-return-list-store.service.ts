import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { MakerReturnSearchCondition } from 'src/app/model/maker-return-search-condition';
import { FormGroup, FormBuilder } from '@angular/forms';
import { first, map } from 'rxjs/operators';
import { MakerReturnSearchResult } from 'src/app/model/maker-return-search-result';
import { MakerReturnConfirmList } from 'src/app/model/maker-return-confirm-list';

@Injectable()
export class MakerReturnListStoreService {

  /** 現在の検索条件 */
  searchConditionSubject = new BehaviorSubject<MakerReturnSearchCondition>(null);

  /** 検索結果 */
  searchResultSubject = new BehaviorSubject<MakerReturnSearchResult[]>([]);

  /** 検索結果フォーム */
  resultFormSubject = new BehaviorSubject<FormGroup>(null);

  /** 取得済みの検索結果 */
  previousSearchResult$ = this.searchResultSubject.pipe(first());

  /** 検索条件 */
  searchCondition$ = this.searchConditionSubject.pipe(first());

  /** 検索結果フォームの値 */
  resultFormValue$: Observable<MakerReturnConfirmList> = this.resultFormSubject.pipe(
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
  createResultForm(results: MakerReturnSearchResult[]): void {
    this.resultFormSubject.next(this.fb.group({
      makerReturnConfirms: this.fb.array(results.map(m => this.fb.group(({
        check: [false],
        voucherNumber: [m.voucherNumber],
        lgSendType: [m.lgSendType],
        returnAt: [m.returnAt],
        supplierCode: [m.supplierCode],
        supplierName: [m.supplierName],
        returnLot: [m.returnLot],
        unitPrice: [m.unitPrice],
        orderId: [m.orderId],
        orderNumber: [m.orderNumber],
        createdAt: [m.createdAt]
      }))))
    }));
  }
}
