import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Threshold } from '../model/threshold';
import { ThresholdSearchConditions } from '../model/search-conditions';
import { GenericList } from '../model/generic-list';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/thresholds';

@Injectable({
  providedIn: 'root'
})
export class ThresholdService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 閾値情報取得処理
   * @param searchConditions ThresholdSearchConditions
   */
  getThresholdByBrandCode(searchConditions: ThresholdSearchConditions): Observable<GenericList<Threshold>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }
}
