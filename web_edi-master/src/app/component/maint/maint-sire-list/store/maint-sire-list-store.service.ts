import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { MaintSireSearchCondition } from 'src/app/model/maint/maint-sire-search-condition';
import { FormGroup, FormBuilder } from '@angular/forms';
import { first } from 'rxjs/operators';
import { MaintSireSearchResult } from 'src/app/model/maint/maint-sire-search-result';


@Injectable()
export class MaintSireListStoreService {

  /** 現在の検索条件 */
  searchConditionSubject = new BehaviorSubject<MaintSireSearchCondition>(null);

  /** 検索結果 */
  searchResultSubject = new BehaviorSubject<MaintSireSearchResult[]>([]);

  /** 検索結果フォーム */
  resultFormSubject = new BehaviorSubject<FormGroup>(null);

  /** 取得済みの検索結果 */
  previousSearchResult$ = this.searchResultSubject.pipe(first());

  /** 検索条件 */
  searchCondition$ = this.searchConditionSubject.pipe(first());


  constructor(
    private fb: FormBuilder
  ) { }

  /**
   * 検索結果フォームを作成する.
   * @param results 検索結果リスト
   */
  createResultForm(results: MaintSireSearchResult[]): void {
    this.resultFormSubject.next(this.fb.group({
      maintSireConfirms: this.fb.array(results.map(m => this.fb.group(({
        reckbn: [m.reckbn],
        sireCode: [m.sireCode],
        sireName: [m.sireName],
        kojCode: [m.kojCode],
        kojName: [m.kojName],
        hkiji: [m.hkiji],
        hseihin: [m.hseihin],
        hnefuda: [m.hnefuda],
        hfuzoku: [m.hfuzoku],
        brandCode: [m.brandCode],
        hsofkbn: [m.hsofkbn],
        nsofkbn: [m.nsofkbn],
        ysofkbn: [m.ysofkbn],
        yugaikbn: [m.yugaikbn]
      }))))
    }));
  }
}
