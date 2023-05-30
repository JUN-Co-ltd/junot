import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { JunpcSizmstSearchCondition } from '../model/junpc-sizmst-search-condition';
import { JunpcSizmst } from '../model/junpc-sizmst';

import { JunotApiService } from '../service/junot-api.service';

const BASE_URL = '/junpc/sizmst';

/**
 * 発注生産システムのサイズマスタを取得するService。
 */
@Injectable({
  providedIn: 'root'
})
export class JunpcSizmstService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * サイズマスタのリスト取得する。
   * @param searchCondition 検索条件
   * - hscd 品種 (必須)
   * @return サイズマスタのリスト
   */
  getSizmst(searchCondition?: JunpcSizmstSearchCondition): Observable<GenericList<JunpcSizmst>> {
    return this.junotApiService.list(BASE_URL, searchCondition);
  }
}
