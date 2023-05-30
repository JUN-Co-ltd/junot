import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { JunotApiService } from 'src/app/service/junot-api.service';

import { GenericList } from 'src/app/model/generic-list';
import { MaintNewsSearchCondition } from 'src/app/model/maint/maint-news-search-condition';
import { MaintNewsSearchResult } from 'src/app/model/maint/maint-news-search-result';
import { MaintNews } from 'src/app/model/maint/maint-news';

import { DateUtils } from 'src/app/util/date-utils';

const BASE_URL = '/maint/news';

@Injectable({
  providedIn: 'root'
})
export class MaintNewsService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * お知らせ情報登録処理
   * @param item お知らせ情報
   * @returns 登録結果
   */
  create(item: MaintNews): Observable<MaintNews> {
    const body = this.convertDateByItem(item);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * お知らせ情報削除処理
   * @param id ID
   */
  delete(id: number): Observable<void> {
    const url = `${ BASE_URL }/${ id }`;
    return this.junotApiService.delete(url);
  }

  /**
   * お知らせ情報取得処理
   * @param id ID
   * @returns 取得結果
   */
  get(id: number): Observable<MaintNews> {
    const url = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(url).pipe(map(response => {
      return this.convertDateByItem(<MaintNews>response);
    }));
  }

  /**
   * お知らせ情報リスト取得処理
   * @param searchCondition 検索条件
   * @returns 検索結果
   */
  search(searchCondition: MaintNewsSearchCondition): Observable<GenericList<MaintNewsSearchResult>> {
    const url = `${ BASE_URL }/search`;
    return this.junotApiService.listByPost(url, searchCondition).pipe(map(response => {
      const list = <GenericList<MaintNewsSearchResult>>response;
      list.items = list.items.map(item => this.convertDateBySearchResult(item));
      return list;
    }));
  }

  /**
   * お知らせ情報更新処理
   * @param item MaintNews
   * @returns 更新結果
   */
  update(item: MaintNews): Observable<MaintNews> {
    const url = `${ BASE_URL }/${ item.id }`;
    const body = this.convertDateByItem(item);
    return this.junotApiService.update(url, body);
  }

  /**
   * DateUtils.convertDateTimeを使用し、日時を型変換します。
   * @param item 変換後の値
   * @return 変換後の値
   */
  private convertDateByItem(item: MaintNews): MaintNews {
    item.openStartAt = DateUtils.convertDateTime(item.openStartAt);
    item.openEndAt = DateUtils.convertDateTime(item.openEndAt);
    item.newDisplayEndAt = DateUtils.convertDateTime(item.newDisplayEndAt);

    return item;
  }

  /**
   * DateUtils.convertDateTimeを使用し、日時を型変換します。
   * @param item 変換後の値
   * @return 変換後の値
   */
  private convertDateBySearchResult(item: MaintNewsSearchResult): MaintNewsSearchResult {
    item.openStartAt = DateUtils.convertDateTime(item.openStartAt);
    item.openEndAt = DateUtils.convertDateTime(item.openEndAt);
    item.newDisplayEndAt = DateUtils.convertDateTime(item.newDisplayEndAt);

    return item;
  }
}
