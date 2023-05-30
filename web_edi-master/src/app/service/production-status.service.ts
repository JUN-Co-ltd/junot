import { Injectable } from '@angular/core';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';

import { ProductionStatusHistory } from '../model/production-status-history';
import { ProductionStatusHistorySearchCondition } from '../model/production-status-history-search-condition';
import { GenericList } from '../model/generic-list';
import { ProductionStatusSearchCondition } from '../model/production-status-search-condition';
import { ProductionStatus } from '../model/production-status';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/productionStatus';
const HISTORY = '/:history';

@Injectable({
  providedIn: 'root'
})
export class ProductionStatusService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService
  ) { }

  /**
   * 生産ステータス登録
   * @param postItem 生産ステータス情報
   */
  postProductionStatus(postItem): Observable<ProductionStatus> {
    const body = this.convertRequestData(postItem);
    console.debug('body:', body);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 生産ステータス検索
   * @param searchConditions 生産ステータス検索条件
   */
  getProductionStatusSearch(searchConditions: ProductionStatusSearchCondition): Observable<GenericList<ProductionStatus>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }

  /**
   * 生産ステータス履歴検索
   * @param searchConditions 生産ステータス検索条件
   */
  getProductionStatusHistorySearch(searchConditions: ProductionStatusHistorySearchCondition)
    : Observable<GenericList<ProductionStatusHistory>> {
    return this.junotApiService.list(BASE_URL + HISTORY, searchConditions);
  }

  /**
   * データ整形処理
   * @param inputData 画面の入力データ
   */
  convertRequestData(inputData): string {
    const copyItem = JSON.parse(JSON.stringify(inputData));

    // 日付変換処理
    // サンプル上がり予定日
    if (copyItem.sampleCompletionAt) {
      copyItem.sampleCompletionAt = this.ngbDateParserFormatter.format(copyItem.sampleCompletionAt).replace(/-/g, '/');
    }
    // サンプル上がり確定予定日
    if (copyItem.sampleCompletionFixAt) {
      copyItem.sampleCompletionFixAt = this.ngbDateParserFormatter.format(copyItem.sampleCompletionFixAt).replace(/-/g, '/');
    }
    // 仕様確定予定日
    if (copyItem.specificationAt) {
      copyItem.specificationAt = this.ngbDateParserFormatter.format(copyItem.specificationAt).replace(/-/g, '/');
    }
    // 仕様確定日
    if (copyItem.specificationFixAt) {
      copyItem.specificationFixAt = this.ngbDateParserFormatter.format(copyItem.specificationFixAt).replace(/-/g, '/');
    }
    // 生地入荷予定日
    if (copyItem.textureArrivalAt) {
      copyItem.textureArrivalAt = this.ngbDateParserFormatter.format(copyItem.textureArrivalAt).replace(/-/g, '/');
    }
    // 生地入荷確定日
    if (copyItem.textureArrivalFixAt) {
      copyItem.textureArrivalFixAt = this.ngbDateParserFormatter.format(copyItem.textureArrivalFixAt).replace(/-/g, '/');
    }
    // 付属入荷予定日
    if (copyItem.attachmentArrivalAt) {
      copyItem.attachmentArrivalAt = this.ngbDateParserFormatter.format(copyItem.attachmentArrivalAt).replace(/-/g, '/');
    }
    // 付属入荷確定日
    if (copyItem.attachmentArrivalFixAt) {
      copyItem.attachmentArrivalFixAt = this.ngbDateParserFormatter.format(copyItem.attachmentArrivalFixAt).replace(/-/g, '/');
    }
    // 上がり予定日
    if (copyItem.completionAt) {
      copyItem.completionAt = this.ngbDateParserFormatter.format(copyItem.completionAt).replace(/-/g, '/');
    }
    // 上がり予定確定日
    if (copyItem.completionFixAt) {
      copyItem.completionFixAt = this.ngbDateParserFormatter.format(copyItem.completionFixAt).replace(/-/g, '/');
    }
    // 縫製検品到着予定日
    if (copyItem.sewInspectionAt) {
      copyItem.sewInspectionAt = this.ngbDateParserFormatter.format(copyItem.sewInspectionAt).replace(/-/g, '/');
    }
    // 縫製検品到着確定日
    if (copyItem.sewInspectionFixAt) {
      copyItem.sewInspectionFixAt = this.ngbDateParserFormatter.format(copyItem.sewInspectionFixAt).replace(/-/g, '/');
    }
    // 検品実施予定日
    if (copyItem.inspectionAt) {
      copyItem.inspectionAt = this.ngbDateParserFormatter.format(copyItem.inspectionAt).replace(/-/g, '/');
    }
    // 検品実施確定日
    if (copyItem.inspectionFixAt) {
      copyItem.inspectionFixAt = this.ngbDateParserFormatter.format(copyItem.inspectionFixAt).replace(/-/g, '/');
    }
    // 出荷予定日
    if (copyItem.leavePortAt) {
      copyItem.leavePortAt = this.ngbDateParserFormatter.format(copyItem.leavePortAt).replace(/-/g, '/');
    }
    // 出荷確定日
    if (copyItem.leavePortFixAt) {
      copyItem.leavePortFixAt = this.ngbDateParserFormatter.format(copyItem.leavePortFixAt).replace(/-/g, '/');
    }
    // 入港予定日
    if (copyItem.enterPortAt) {
      copyItem.enterPortAt = this.ngbDateParserFormatter.format(copyItem.enterPortAt).replace(/-/g, '/');
    }
    // 入港確定日
    if (copyItem.enterPortFixAt) {
      copyItem.enterPortFixAt = this.ngbDateParserFormatter.format(copyItem.enterPortFixAt).replace(/-/g, '/');
    }
    // 通関予定日
    if (copyItem.customsClearanceAt) {
      copyItem.customsClearanceAt = this.ngbDateParserFormatter.format(copyItem.customsClearanceAt).replace(/-/g, '/');
    }
    // 通関確定日
    if (copyItem.customsClearanceFixAt) {
      copyItem.customsClearanceFixAt = this.ngbDateParserFormatter.format(copyItem.customsClearanceFixAt).replace(/-/g, '/');
    }
    // DISTA入荷予定日
    if (copyItem.distaArrivalAt) {
      copyItem.distaArrivalAt = this.ngbDateParserFormatter.format(copyItem.distaArrivalAt).replace(/-/g, '/');
    }
    // DISTA入荷確定日
    if (copyItem.distaArrivalFixAt) {
      copyItem.distaArrivalFixAt = this.ngbDateParserFormatter.format(copyItem.distaArrivalFixAt).replace(/-/g, '/');
    }

    // 金額のカンマ除去
    if (copyItem.completionCount != null && typeof copyItem.completionCount === 'string') {
      copyItem.completionCount = copyItem.completionCount.replace(/,/g, '');
    }
    return copyItem;
  }
}
