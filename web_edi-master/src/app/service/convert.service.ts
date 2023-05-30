import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Text } from '../model/text';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/convert';

/**
 * 変換操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class ConvertService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * カナ変換処理処理.
   * @param text 変換対象文字列
   * @return 変換後文字列
   */
  convertToKana(text: string): Observable<Text> {
    return this.junotApiService.create(BASE_URL + '/kana', { text: text } as Text);
  }
}
