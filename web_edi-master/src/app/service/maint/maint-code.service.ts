import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MaintCodeResult, UpdateCode } from 'src/app/model/maint/maint-code-result';
import { GenericList } from 'src/app/model/generic-list';
import { JunotApiService } from 'src/app/service/junot-api.service';

const BASE_URL = '/maint/maint-code';

@Injectable({
  providedIn: 'root'
})
export class MaintCodeService {

  constructor(
    private junotApiService: JunotApiService
  ) { }


  /**
    * マスタ一覧を取得する。
    *
  **/
  getTable(): Observable<GenericList<MaintCodeResult>> {
    const url = `${ BASE_URL }/list`;
    return this.junotApiService.list(url);
  }

  /**
    * マスター画面構成情報を取得する。
    * @param tblid テーブルID
    *
  **/
  screenSettings(tblid: string): Observable<any> {
    const url = `${ BASE_URL }/${ tblid }/screenSettings`;
    return this.junotApiService.get(url);
  }

  /**
   * コードマスタ情報をリストで取得する。
   * @param tblid テーブルID
   *
   **/
  maintCode(tblid: string, model: any): Observable<any> {
    const url = `${ BASE_URL }/${ tblid }/search`;
    return this.junotApiService.listByPost(url, model);
  }

  /**
   * テーブルデータを更新する。
   * @param tblid テーブルID
   * @param model post用のmodel
   *
   **/
  update(tblid: string, model: any): Observable<any> {
    const url = `${ BASE_URL }/${ tblid }/bulkUpdate`;
    return this.junotApiService.create(url, model);
  }
  /**
   * テーブルデータの指定した行を削除する。
   * @param tblid テーブルID
   * @param model post用のmodel
   *
   **/
  delete(tblid: string, model: UpdateCode): Observable<any> {
    const url = `${ BASE_URL }/${ tblid }/bulkDelete`;
    return this.junotApiService.create(url, model);
  }
}
