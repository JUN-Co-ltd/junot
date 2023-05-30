import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { JunotApiService } from 'src/app/service/junot-api.service';
import { BulkRegistItem } from 'src/app/model/bulk-regist-item';

const BASE_URL = '/bulkRegistItems';

@Injectable({
  providedIn: 'root'
})
export class BulkRegistItemService {

  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 仮登録（商品登録）可能か検証する.
   * @param file 一括登録ファイル
   * @returns Observable<BulkRegistItem>
   */
  preRegistValidate = () => (file: File): Observable<BulkRegistItem> => {
    const formData = new FormData();
    formData.append('file', file);
    const URL = `${ BASE_URL }/preRegistValidate`;
    return this.junotApiService.fileUpload(URL, formData);
  }

  /**
   * 仮登録（商品登録）する.
   * @param file 一括登録ファイル
   * @returns Observable<BulkRegistItem>
   */
  preRegist = () => (file: File): Observable<BulkRegistItem> => {
    const formData = new FormData();
    formData.append('file', file);
    const URL = `${ BASE_URL }/preRegist`;
    return this.junotApiService.fileUpload(URL, formData);
  }

  /**
   * 本登録（品番登録）可能か検証する.
   * @param file 一括登録ファイル
   * @returns Observable<BulkRegistItem>
   */
  registValidate = () => (file: File): Observable<BulkRegistItem> => {
    const formData = new FormData();
    formData.append('file', file);
    const URL = `${ BASE_URL }/registValidate`;
    return this.junotApiService.fileUpload(URL, formData);
  }

  /**
   * 本登録（商品登録）する.
   * @param file 一括登録ファイル
   * @returns Observable<BulkRegistItem>
   */
  regist = () => (file: File): Observable<BulkRegistItem> => {
    const formData = new FormData();
    formData.append('file', file);
    const URL = `${ BASE_URL }/regist`;
    return this.junotApiService.fileUpload(URL, formData);
  }
}
