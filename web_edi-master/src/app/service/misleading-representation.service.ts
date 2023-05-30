import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { JunotApiService } from './junot-api.service';

import { GenericList } from '../model/generic-list';
import { ItemMisleadingRepresentation } from '../model/item-misleading-representation';
import { MisleadingRepresentation } from '../model/misleading-representation';
import { ItemMisleadingRepresentationSearchResult } from '../model/item-misleading-representation-search-result';
import { MisleadingRepresentationViewSearchCondition } from '../model/misleading-representation-view-search-condition';
import { MisleadingRepresentationRequest } from '../model/misleading-representation-request';
import { MisleadingRepresentationInspectionForm } from './form/interface';
import { DateUtils } from 'src/app/util/date-utils';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

const BASE_URL = '/itemMisleadingRepresentations';

/**
 * MisleadingRepresentation操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class MisleadingRepresentationService {

  constructor(
    private junotApiService: JunotApiService,
    private ngbDateParserFormatter: NgbDateParserFormatter,
  ) { }

  /**
   * 品番IDをキーに品番優良誤認情報取得.
   * @param partNoId 品番ID
   * @returns Observable<ItemMisleadingRepresentation>
   */
  get(partNoId: number): Observable<ItemMisleadingRepresentation> {
    const URL = `${ BASE_URL }/${ partNoId }`;
    return this.junotApiService.get(URL);
  }

  /**
   * 優良誤認情報リスト取得処理.
   * @param searchCondition 検索条件
   * @return 検索結果
   */
  search(searchCondition: MisleadingRepresentationViewSearchCondition): Observable<GenericList<ItemMisleadingRepresentationSearchResult>> {
    const url = `${ BASE_URL }/search`;
    return this.junotApiService.listByPost(url, this.convertDateBySearchCondition(searchCondition)).pipe(map(response => {
      const list = <GenericList<ItemMisleadingRepresentationSearchResult>>response;
      list.items = list.items.map(item => this.convertDateBySearchResult(item));
      return list;
    }));
  }

  /**
   * 優良誤認情報更新.
   * @param formValue フォームデータ
   * @returns Observable<ItemMisleadingRepresentation>
   */
  put(formValue: any): Observable<ItemMisleadingRepresentation> {
    const URL = `${ BASE_URL }/${ formValue.partNoId }`;
    const body = this.convertRequestData(formValue);
    return this.junotApiService.update(URL, body);
  }

  /**
   * リクエスト用フォームデータ整形処理.
   * @param formValue フォームデータ
   * @returns リクエスト用フォームデータ
   */
  private convertRequestData = (formValue: any): MisleadingRepresentationRequest => {
    const copyFormValue = JSON.parse(JSON.stringify(formValue));
    const cooInspectionValue: MisleadingRepresentationInspectionForm = copyFormValue.cooInspectionGp;
    const harmfulInspectionValue: MisleadingRepresentationInspectionForm = copyFormValue.harmfulInspectionGp;
    const compositionInspectionValues: MisleadingRepresentationInspectionForm[] = copyFormValue.compositionInspections;

    // 原産国検査で1件、有害物質検査で1件、組成検査はカラーごとに1件
    const misleadingRepresentations: MisleadingRepresentation[] = [];
    const generateMrFn = this.generateMisleadingRepresentation(copyFormValue.partNoId, copyFormValue.updatedAt);
    misleadingRepresentations.push(generateMrFn(cooInspectionValue));
    misleadingRepresentations.push(generateMrFn(harmfulInspectionValue));
    compositionInspectionValues.forEach(value => misleadingRepresentations.push(generateMrFn(value)));

    const requestData = new MisleadingRepresentationRequest();
    requestData.misleadingRepresentations = misleadingRepresentations;
    return requestData;
  }

  /**
   * リクエスト用の優良誤認承認情報を作成する.
   * @param partNoId 品番ID
   * @param updatedAt 更新日時
   * @param mrFormValue: 優良誤認検査フォームデータ
   * @returns リクエスト用の優良誤認承認情報
   */
  private generateMisleadingRepresentation = (partNoId: number, updatedAt: Date) =>
    (mrFormValue: MisleadingRepresentationInspectionForm): MisleadingRepresentation => {
      const misleadingRepresentation = new MisleadingRepresentation();
      misleadingRepresentation.updatedAt = updatedAt;
      misleadingRepresentation.partNoId = partNoId;

      misleadingRepresentation.id = mrFormValue.id;
      misleadingRepresentation.misleadingRepresentationType = mrFormValue.misleadingRepresentationType;
      misleadingRepresentation.memo = mrFormValue.memo;

      misleadingRepresentation.colorCode = mrFormValue.colorCode;
      misleadingRepresentation.cooCode = mrFormValue.cooCode;
      misleadingRepresentation.mdfMakerCode = mrFormValue.mdfMakerCode;

      if (mrFormValue.check) {
        misleadingRepresentation.approvalAt = mrFormValue.approvalAt;
        misleadingRepresentation.approvalUserAccountName = mrFormValue.approvalUserAccountName;
      }

      misleadingRepresentation.updatedAt = mrFormValue.updatedAt;

      return misleadingRepresentation;
    }

  /**
   * 日時を型変換します。
   * @param searchCondition 変換前の値
   * @return 変換後の値
   */
  private convertDateBySearchCondition(
    searchCondition: MisleadingRepresentationViewSearchCondition): MisleadingRepresentationViewSearchCondition {

      const repParam = JSON.parse(JSON.stringify(searchCondition));

        // 製品納期From
        if (repParam.productCorrectionDeliveryAtFrom && typeof repParam.productCorrectionDeliveryAtFrom !== 'string') {
          repParam.productCorrectionDeliveryAtFrom =
            this.ngbDateParserFormatter.format(repParam.productCorrectionDeliveryAtFrom).replace(/-/g, '/');
        }
        // 製品納期To
        if (repParam.productCorrectionDeliveryAtTo && typeof repParam.productCorrectionDeliveryAtTo !== 'string') {
          repParam.productCorrectionDeliveryAtTo =
            this.ngbDateParserFormatter.format(repParam.productCorrectionDeliveryAtTo).replace(/-/g, '/');
        }

        // 検査日From
        if (repParam.approvalAtFrom && typeof repParam.approvalAtFrom !== 'string') {
          repParam.approvalAtFrom =
            this.ngbDateParserFormatter.format(repParam.approvalAtFrom).replace(/-/g, '/');
        }
        // 検査日To
        if (repParam.approvalAtTo && typeof repParam.approvalAtTo !== 'string') {
          repParam.approvalAtTo =
            this.ngbDateParserFormatter.format(repParam.approvalAtTo).replace(/-/g, '/');
        }

    return repParam;
  }

  /**
   * 日時を型変換します。
   * @param item 変換前の値
   * @return 変換後の値
   */
  private convertDateBySearchResult(item: ItemMisleadingRepresentationSearchResult): ItemMisleadingRepresentationSearchResult {
    item.approvalAt = DateUtils.convertDateTime(item.approvalAt);

    return item;
  }
}
