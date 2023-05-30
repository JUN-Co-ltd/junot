//PRD_0133 #10181 add JFE start
import { Injectable } from '@angular/core';

import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { Observable } from 'rxjs';
import { PurchaseRecordSearchCondition } from 'src/app/model/purchase-record-search-condition';
import { PurchaseRecordSearchResult } from 'src/app/model/purchase-record-search-result';
import { map } from 'rxjs/operators';

import { GenericList } from '../model/generic-list';

import { JunotApiService } from './junot-api.service';
import { DateUtilsService } from './bo/date-utils.service';

import { FormUtilsService } from './bo/form-utils.service';
import { StringUtilsService } from './bo/string-utils.service';
import { PurchaseRecordCsv } from '../model/purchase-record-csv';

const BASE_URL = '/purchasesRecord';

@Injectable({
  providedIn: 'root'
})
export class PurchaseRecordHttpService {

  constructor(
    private dateUtils: DateUtilsService,
    private formUtils: FormUtilsService,
    private stringUtils: StringUtilsService,
    private junotApiService: JunotApiService
  ) { }

  /**
   * 検索処理.
   * @param formValue 検索フォーム値
   * @returns レスポンス
   */
  search(formValue: PurchaseRecordSearchCondition): Observable<GenericList<PurchaseRecordSearchResult>> {
    const URL = `${ BASE_URL }/search`;
    const searchCondition = this.convertSearchParam(formValue);
    return this.junotApiService.listByPost(URL, searchCondition);
  }

  /**
   * 検索パラメータ整形処理.
   * @param form form入力データ
   * @returns 整形後入力データ
   */
  private convertSearchParam(form: PurchaseRecordSearchCondition): PurchaseRecordSearchCondition {
    const copyItem = Object.assign({}, form); // deep copy

    // 日付変換処理
    copyItem.recordAtFrom = this.dateUtils.toString(copyItem.recordAtFrom as NgbDateStruct);
    copyItem.recordAtTo = this.dateUtils.toString(copyItem.recordAtTo as NgbDateStruct);

    copyItem.partNo = this.stringUtils.toNullIfBlank(copyItem.partNo);
    return copyItem;
  }

  /**
   * CSV作成処理.
   * @param formValue 検索フォーム値
   * @returns 結果(CSV作成に必要な情報の取得)
   */
  searchCsv(formValue: PurchaseRecordSearchCondition): Observable<GenericList<PurchaseRecordCsv>> {
    const URL = `${ BASE_URL }/search-csv`;
    const searchCondition = this.convertSearchParam(formValue);
    return this.junotApiService.listByPost(URL, searchCondition).pipe(map(response => {
      const list = <GenericList<PurchaseRecordCsv>> response;
      return list;
    }));
  }

  /**
 * PDF作成処理.
 * @param formValue 検索フォーム値
 * @returns PDFファイル
 */
  createPdf(formValue: PurchaseRecordSearchCondition): Observable<any> {
    const URL = `${ BASE_URL }/create-pdf`;
    const searchCondition = this.convertSearchParam(formValue);
    return this.junotApiService.pdfDownload(URL, searchCondition);
  }
}
//PRD_0133 #10181 add JFE end
