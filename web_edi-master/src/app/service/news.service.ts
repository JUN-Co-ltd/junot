import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { JunotApiService } from './junot-api.service';

import { GenericList } from '../model/generic-list';
import { NewsSearchCondition } from '../model/news-search-condition';
import { News } from '../model/news';

import { DateUtils } from '../util/date-utils';

const BASE_URL = '/news';

@Injectable({
  providedIn: 'root'
})
export class NewsService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * お知らせ情報リスト取得処理
   * @param searchCondition 検索条件
   * @returns 検索結果
   */
  search(searchCondition: NewsSearchCondition): Observable<GenericList<News>> {
    const url = `${ BASE_URL }/search`;
    return this.junotApiService.listByPost(url, searchCondition).pipe(map(response => {
      const list = <GenericList<News>>response;
      list.items = list.items.map(item => this.convertDateByItem(item));
      return list;
    }));
  }

  /**
   * お知らせ情報取得処理
   * @param id ID
   * @returns 取得結果
   */
  get(id: number): Observable<News> {
    const url = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(url).pipe(map(response => {
      return this.convertDateByItem(<News>response);
    }));
  }

  /**
   * DateUtils.convertDateTimeを使用し、日時を型変換します。
   * @param item 変換後の値
   * @return 変換後の値
   */
  private convertDateByItem(item: News): News {
    item.openStartAt = DateUtils.convertDateTime(item.openStartAt);
    item.openEndAt = DateUtils.convertDateTime(item.openEndAt);
    item.newDisplayEndAt = DateUtils.convertDateTime(item.newDisplayEndAt);

    return item;
  }
}
