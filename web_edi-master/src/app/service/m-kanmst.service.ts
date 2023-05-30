import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { MKanmst } from '../model/m-kanmst';

import { JunotApiService } from '../service/junot-api.service';

const BASE_URL = '/m/kanmst';

@Injectable({
  providedIn: 'root'
})
export class MKanmstService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 管理マスタ情報を取得します。
   *
   * @return 管理マスタ情報
   */
  getKanmst(): Observable<MKanmst> {
    return this.junotApiService.get(BASE_URL);
  }
}
