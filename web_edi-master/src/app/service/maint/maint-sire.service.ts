import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { JunotApiService } from 'src/app/service/junot-api.service';
import { MaintSireSearchCondition } from 'src/app/model/maint/maint-sire-search-condition';
import { MaintSireSearchResult } from 'src/app/model/maint/maint-sire-search-result';
import { MaintSire } from 'src/app/model/maint/maint-sire';
import { GenericList } from 'src/app/model/generic-list';

const BASE_URL = '/maint/sires';

@Injectable({
  providedIn: 'root'
})
export class MaintSireService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 取引先情報登録処理
   * @param item 取引先情報
   * @returns 登録結果
   */
  create(item: MaintSire): Observable<MaintSire> {
    return this.junotApiService.create(BASE_URL, item);
  }

  /**
   * 取引先情報削除処理
   * @param sireCode 仕入先コード
   * @param kojCode 工場コード
   * @returns レスポンス
   */
  delete(sireCode: String, kojCode: String, reckbn: String): Observable<void> {
    const url = `${ BASE_URL }/${ sireCode }`;
    return this.junotApiService.delete(url, { kojCode, reckbn });
  }

  /**
   * 取引先情報取得処理
   * @param sireCode 仕入先コード
   * @param kojCode 工場コード
   * @returns 取得結果
   */
  get(sireCode: String, kojCode: String): Observable<MaintSire> {
    const url = `${ BASE_URL }/${ sireCode }`;
    return this.junotApiService.get(url, { kojCode });
  }

  /**
   * 取引先情報リスト取得処理
   * @param searchCondition 検索条件
   * @returns 検索結果
   */
  search(searchCondition: MaintSireSearchCondition): Observable<GenericList<MaintSireSearchResult>> {
    const url = `${ BASE_URL }/search`;
    return this.junotApiService.listByPost(url, searchCondition);
  }

  /**
   * 取引先情報更新処理
   * @param item MaintSire
   * @returns 更新結果
   */
  update(item: MaintSire): Observable<MaintSire> {
    const url = `${ BASE_URL }/${ item.sireCode }`;
    return this.junotApiService.update(url, item);
  }

  /**
   * 仕入先コードと工場コードをキーに取得.
   * @param sireCode 仕入先コード
   * @param kojCode 工場コード
   * @returns レスポンス
   */
  /**
  fetchBySireCodeAndKojCode(sireCode: string, kojCode: string): Observable<MaintSire> {
    const URL = `${ BASE_URL }/${ sireCode }`;
    return this.junotApiService.get(URL, { kojCode });
  }
　 */
}
