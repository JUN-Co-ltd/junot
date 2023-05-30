import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { JunotApiService } from './junot-api.service';
import { GenericList } from '../model/generic-list';
import { JunpcTnpmstSearchCondition } from '../model/junpc-tnpmst-search-condition';
import { JunpcTnpmst } from '../model/junpc-tnpmst';

const BASE_URL = '/junpc/tnpmst';

@Injectable({
  providedIn: 'root'
})
export class JunpcTnpmstHttpService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 店舗マスタを検索する。
   * @param searchCondition 検索条件
   * @return 店舗マスタのリスト
   */
  search(searchCondition?: JunpcTnpmstSearchCondition): Observable<GenericList<JunpcTnpmst>> {
    return this.junotApiService.listByPost(`${ BASE_URL }/search`, searchCondition);
  }
}
