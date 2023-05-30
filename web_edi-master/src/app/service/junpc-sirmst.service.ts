import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { JunpcSirmstSearchCondition } from '../model/junpc-sirmst-search-condition';
import { JunpcSirmst } from '../model/junpc-sirmst';

import { JunotApiService } from '../service/junot-api.service';

const BASE_URL = '/junpc/sirmst';

/**
 * 発注生産システムの仕入先マスタを取得するService。
 */
@Injectable({
  providedIn: 'root'
})
export class JunpcSirmstService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 仕入先マスタをリスト取得する。
   *
   * @param searchCondition 検索条件
   * - sirkbn 仕入先区分 (必須)
   * - searchType 検索区分 (任意)
   * - - 0 : コード/名称 部分一致（デフォルト）
   * - - 1 : コード 部分一致
   * - - 2 : 名称 部分一致
   * - searchText 検索文字列 (必須)
   * @return 仕入先マスタのリスト
   */
  getSirmst(searchCondition?: JunpcSirmstSearchCondition): Observable<GenericList<JunpcSirmst>> {
    return this.junotApiService.list(BASE_URL, searchCondition);
  }
}
