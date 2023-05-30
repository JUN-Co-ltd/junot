import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { JunotApiService } from './junot-api.service';

import { SpecialtyQubeRequest } from '../model/specialty-qube-request';
import { SpecialtyQubeCancelResponse } from '../model/specialty-qube-cancel-response';

const BASE_URL = '/specialtyQubes';

@Injectable({
  providedIn: 'root'
})
export class SpecialtyQubeService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * SQキャンセル処理
   * @param request 登録データ
   * @returns 処理結果
   */
  cancelSq(request: SpecialtyQubeRequest): Observable<SpecialtyQubeCancelResponse> {
    return this.junotApiService.update(BASE_URL + '/cancel', request);
  }
}
