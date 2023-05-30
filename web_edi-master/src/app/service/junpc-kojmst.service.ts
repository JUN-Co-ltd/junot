import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { JunpcKojmstSearchCondition } from '../model/junpc-kojmst-search-condition';
import { JunpcKojmst } from '../model/junpc-kojmst';

import { JunotApiService } from '../service/junot-api.service';

const BASE_URL = '/junpc/kojmst';

/**
 * 発注生産システムの工場マスタを取得するService。
 */
@Injectable({
  providedIn: 'root'
})
export class JunpcKojmstService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 工場マスタをリスト取得する。
   *
   * @param searchCondition 検索条件
   * - sire 仕入先 (必須)
   * - sirkbn 仕入先区分 (必須)
   * - searchType 検索区分 (任意)
   * - - 0 : コード/名称 部分一致（デフォルト）
   * - - 1 : コード 部分一致
   * - - 2 : 名称 部分一致
   * - searchText 検索文字列 (必須)
   * @return 工場マスタのリスト
   */
  getKojmst(searchCondition?: JunpcKojmstSearchCondition): Observable<GenericList<JunpcKojmst>> {
    return this.junotApiService.list(BASE_URL, searchCondition);
  }
}
