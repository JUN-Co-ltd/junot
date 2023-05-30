import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { DelischeFileInfo } from '../model/delische-file-info';
import { DelischeOrderSearchConditions } from '../model/delische-order-search-conditions';

import { JunotApiService } from './junot-api.service';
import { DelischeService } from './delische.service';

const BASE_URL = '/delische-files';

/**
 * DelischeFileInfo操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class DelischeFileService {

  constructor(
    private junotApiService: JunotApiService,
    private delischeService: DelischeService
  ) { }

  /**
   * デリスケファイル情報リスト取得処理
   * @returns デリスケファイル情報取得結果
   */
  listDelischeFile(): Observable<GenericList<DelischeFileInfo>> {
    return this.junotApiService.list(BASE_URL);
  }

  /**
   * デリスケファイル作成処理
   * @param searchConditions 検索条件
   * @returns リクエスト結果
   */
  createDelischeFile(searchConditions: DelischeOrderSearchConditions): Observable<any> {
    const body = this.delischeService.convertDelischeOrderRequestData(searchConditions);
    return this.junotApiService.create(BASE_URL, body);
  }
}
