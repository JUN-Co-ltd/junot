import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { JunotApiService } from 'src/app/service/junot-api.service';
import { MaintUserSearchCondition } from 'src/app/model/maint/maint-user-search-condition';
import { MaintUserSearchResult } from 'src/app/model/maint/maint-user-search-result';
import { MaintUser } from 'src/app/model/maint/maint-user';
import { GenericList } from 'src/app/model/generic-list';

const BASE_URL = '/maint/users';

@Injectable({
  providedIn: 'root'
})
export class MaintUserService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * ユーザ情報登録処理
   * @param item ユーザ情報
   * @returns 登録結果
   */
  create(item: MaintUser): Observable<MaintUser> {
    return this.junotApiService.create(BASE_URL, item);
  }

  /**
   * ユーザ情報削除処理
   * @param id ID
   */
  delete(id: number): Observable<void> {
    const url = `${ BASE_URL }/${ id }`;
    return this.junotApiService.delete(url);
  }

  /**
   * ユーザ情報取得処理
   * @param id ID
   * @returns 取得結果
   */
  get(id: number): Observable<MaintUser> {
    const url = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(url);
  }

  /**
   * ユーザ情報リスト取得処理
   * @param searchCondition 検索条件
   * @returns 検索結果
   */
  search(searchCondition: MaintUserSearchCondition): Observable<GenericList<MaintUserSearchResult>> {
    const url = `${ BASE_URL }/search`;
    return this.junotApiService.listByPost(url, searchCondition);
  }

  /**
   * ユーザ情報更新処理
   * @param item MaintUser
   * @returns 更新結果
   */
  update(item: MaintUser): Observable<MaintUser> {
    const url = `${ BASE_URL }/${ item.id }`;
    return this.junotApiService.update(url, item);
  }
}
