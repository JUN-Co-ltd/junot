//PRD_0137 #10669 add start
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MaintCodeResult, UpdateCode } from 'src/app/model/maint/maint-code-result';
import { GenericList } from 'src/app/model/generic-list';
import { JunotApiService } from 'src/app/service/junot-api.service';
import { MaintSizeList } from 'src/app/model/maint/maint-size-list';

const BASE_URL = '/maint/maint-size';

@Injectable({
  providedIn: 'root'
})
export class MaintSizeService {

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
   * サイズマスタ情報をリストで取得する。
   * @param hscd 品種コード
   *
   **/
  hsCode(hscd: string, model: any): Observable<GenericList<MaintSizeList>> {
    const url = `${ BASE_URL }/${ hscd }/search`;
    return this.junotApiService.listByPost(url, model);
  }

  /**
   * テーブルデータを更新する。
   * @param tblid テーブルID
   * @param model post用のmodel
   *
   **/
  update(model: any): Observable<any> {
    const url = `${ BASE_URL }/bulkUpdate`;
    return this.junotApiService.create(url, model);
  }
  /**
   * テーブルデータの指定した行を削除する。
   * @param tblid テーブルID
   * @param model post用のmodel
   *
   **/
  delete(model: UpdateCode): Observable<any> {
    const url = `${ BASE_URL }/bulkDelete`;
    return this.junotApiService.create(url, model);
  }


  /**
   * テーブルデータをコピー新規する。
   * @param tblid テーブルID
   * @param model post用のmodel
   *
   **/
  copy(model: any): Observable<any> {
    const url = `${ BASE_URL }/bulkCopy`;
    return this.junotApiService.create(url, model);
  }
}
//PRD_0137 #10669 add end
