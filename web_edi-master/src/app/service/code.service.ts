import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GenericList } from '../model/generic-list';
import { ItemPart } from '../model/item-part';
import { JunotApiService } from '../service/junot-api.service';

const BASE_URL = '/itemParts';

/**
 * コードマスタの情報を取得するService
 */
@Injectable({
  providedIn: 'root'
})
export class CodeService {
  constructor(
    private junotApiService: JunotApiService,
  ) { }

  /**
   * パーツの情報を取得する
   */
  getPartsMaster(item: string): Observable<GenericList<ItemPart>> {
    const params = {
      itemCode: item,
    };

    return this.junotApiService.list(BASE_URL, params);
  }
}
