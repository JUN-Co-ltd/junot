import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { JunpcHrtmstSearchCondition } from '../model/junpc-hrtmst-search-condition';
import { JunpcHrtmst } from '../model/junpc-hrtmst';

import { JunotApiService } from '../service/junot-api.service';

const BASE_URL = '/junpc/hrtmsts';

@Injectable({
  providedIn: 'root'
})
export class JunpcHrtmstService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 配分率マスタをリストで取得する。
   *
   * @param searchCondition 検索条件
   * - brandCode ブランド (必須)
   * - itemCode アイテム (必須)
   * - season シーズン (必須)
   * - hrtkbn 配分率区分 (任意)
   * @return 配分率マスタのリスト
   */
  getHrtmst(searchCondition?: JunpcHrtmstSearchCondition): Observable<GenericList<JunpcHrtmst>> {
    return this.junotApiService.list(BASE_URL, searchCondition);
  }
}
