import { Injectable } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';

import { JunotApiService } from './junot-api.service';

import { DateUtilsService } from './bo/date-utils.service';

import { MakerReturn } from '../model/maker-return';
import { GenericList } from '../model/generic-list';
import { MakerReturnSearchResult } from '../model/maker-return-search-result';
import { MakerReturnSearchCondition } from '../model/maker-return-search-condition';
import { MakerReturnConfirmList } from '../model/maker-return-confirm-list';
import { makerReturnSearchResultMock } from './mocks/maker-returns-http.service.mock';
import { FormUtilsService } from './bo/form-utils.service';

const BASE_URL = '/makerReturns';

@Injectable({
  providedIn: 'root'
})
export class MakerReturnsHttpService {

  constructor(
    private dateUtils: DateUtilsService,
    private junotApiService: JunotApiService,
    private formUtils: FormUtilsService
  ) { }

  /**
   * 伝票番号と発注IDをキーに取得.
   * @param voucherNumber 伝票番号
   * @param orderId 発注ID
   * @returns レスポンス
   */
  fetchByVoucherNumberAndOrderId(voucherNumber: string, orderId: number): Observable<MakerReturn> {
    const URL = `${ BASE_URL }/${ voucherNumber }`;
    return this.junotApiService.get(URL, { orderId });
  }

  /**
   * 登録処理.
   * @param form form入力データ
   * @returns レスポンス
   */
  create = (form: MakerReturn) => (): Observable<MakerReturn> => {
    const body = this.convertRequestData(form);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 更新処理.
   * @param form form入力データ
   * @returns レスポンス
   */
  update = (form: MakerReturn) => (): Observable<MakerReturn> => {
    const body = this.convertRequestData(form);
    return this.junotApiService.update(BASE_URL, body);
  }

  /**
   * 削除処理.
   * @param voucherNumber 伝票番号
   * @param orderId 発注ID
   * @returns レスポンス
   */
  delete(voucherNumber: string, orderId: number): Observable<MakerReturn> {
    const URL = `${ BASE_URL }/${ voucherNumber }`;
    return this.junotApiService.delete(URL, { orderId });
  }

  /**
   * 検索処理.
   * @param searchCondition 検索条件
   * @return 検索結果
   */
  search(searchCondition: MakerReturnSearchCondition): Observable<GenericList<MakerReturnSearchResult>> {
    const url = `${ BASE_URL }/search`;
    return this.junotApiService.listByPost(url, this.convertSearchParam(searchCondition));
  }

  /**
   * LG送信(確定)処理
   * @param form form入力データ
   * @returns レスポンス
   */
  confirm(form: MakerReturnConfirmList): Observable<MakerReturnConfirmList> {
    const URL = `${ BASE_URL }/confirm`;
    const body = this.convertLgRequestData(form);
    return this.junotApiService.update(URL, body);
  }

  /**
   * データ整形処理.
   * @param form form入力データ
   * @returns 整形後入力データ
   */
  private convertRequestData(form: MakerReturn): MakerReturn {
    const copyItem = Object.assign({}, form); // deep copy

    copyItem.returnAt = this.dateUtils.toString(copyItem.returnAt as NgbDateStruct);
    copyItem.makerReturnProducts = form.makerReturnProducts.filter(p => this.formUtils.isNotEmpty(p.returnLot));

    return copyItem;
  }

  /**
   * 検索パラメータ整形処理.
   * @param form form入力データ
   * @returns 整形後入力データ
   */
  private convertSearchParam(form: MakerReturnSearchCondition): MakerReturnSearchCondition {
    const copyItem = Object.assign({}, form); // deep copy

    copyItem.voucherNumberInputAtFrom = this.dateUtils.toString(copyItem.voucherNumberInputAtFrom as NgbDateStruct);
    copyItem.voucherNumberInputAtTo = this.dateUtils.toString(copyItem.voucherNumberInputAtTo as NgbDateStruct);
    copyItem.voucherNumberAtFrom = this.dateUtils.toString(copyItem.voucherNumberAtFrom as NgbDateStruct);
    copyItem.voucherNumberAtTo = this.dateUtils.toString(copyItem.voucherNumberAtTo as NgbDateStruct);

    return copyItem;
  }

  /**
   * @pram form form入力データ
   * @return LG送信リクエストデータ
   */
  private convertLgRequestData(form: MakerReturnConfirmList): MakerReturnConfirmList {
    // チェックがついているレコードのみ
    return { makerReturnConfirms: form.makerReturnConfirms.filter(p => p.check) };
  }
}
