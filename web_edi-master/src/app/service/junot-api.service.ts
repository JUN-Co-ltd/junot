import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { environment } from '../../environments/environment';

import { GenericList } from '../../app/model/generic-list';

import { CustomEncoder } from '../../app/lib/custom-encoder';
import { SessionService } from '../../app/service/session.service';

const HTTP_HEADER_CONTENT_TYPE_VALUE = 'application/json';

/**
 * JUNoT共通API.
 */
@Injectable({
  providedIn: 'root'
})
export class JunotApiService {
  constructor(
    private http: HttpClient,
    private sessionService: SessionService
  ) { }

  /**
   * JUNoT APIサーバーにデータを1件登録する。
   *
   * @param url JUNoT APIのURL
   * @param model 登録するデータ
   * @return レスポンス
   */
  create<T, V>(url: string, model: T): Observable<V> {
    console.debug(JSON.stringify({ create: { start: { url: url } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const HEADERS = new HttpHeaders({
      'x-xsrf-token': this.sessionService.getToken(),
      'Content-Type': HTTP_HEADER_CONTENT_TYPE_VALUE
    });
    const options = { headers: HEADERS, withCredentials: true };

    return this.http.post<V>(API_URL, model, options).pipe(tap(
      () => console.debug(JSON.stringify({ create: { end: { url: url } } })),
      e => console.error(e)
    ));
  }

  /**
   * JUNoT APIサーバーからデータを1件削除する。
   *
   * @param url JUNoT APIのURL
   * @param params パラメーター
   * @return レスポンス
   */
  delete<T, V>(url: string, params?: T): Observable<V> {
    console.debug(JSON.stringify({ delete: { start: { url: url } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const API_PARAMS = this.toParams(params);
    const HEADERS = new HttpHeaders({
      'x-xsrf-token': this.sessionService.getToken(),
      'Content-Type': HTTP_HEADER_CONTENT_TYPE_VALUE
    });
    const options = { params: API_PARAMS, headers: HEADERS, withCredentials: true };

    return this.http.delete<V>(API_URL, options).pipe(tap(
      () => console.debug(JSON.stringify({ delete: { end: { url: url } } })),
      e => console.error(e)
    ));
  }

  /**
   * JUNoT APIサーバーからファイルを取得する。
   *
   * @param url JUNoT APIのURL
   * @param params パラメーター
   * @return レスポンス
   */
  fileDownload<T>(url: string, params?: T): Observable<any> {
    console.debug(JSON.stringify({ getBlob: { start: { url: url, params: params } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const API_PARAMS = this.toParams(params);

    return this.http.get(API_URL,
      {
        params: API_PARAMS,
        withCredentials: true,
        responseType: 'blob',
        observe: 'response'
      }
    ).pipe(tap(
      () => console.debug(JSON.stringify({ getBlob: { end: { url: url } } })),
      e => console.error(e))
    );
  }

  /**
   * JUNoT APIサーバーにファイルを登録する。
   *
   * @param url JUNoT APIのURL
   * @param formData formData
   * @return レスポンス
   */
  fileUpload<T, V>(url: string, formData: T): Observable<V> {
    console.debug(JSON.stringify({ createBlob: { start: { url: url } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const HEADERS = new HttpHeaders({ 'x-xsrf-token': this.sessionService.getToken() });
    const options = { headers: HEADERS, withCredentials: true };

    return this.http.post<V>(API_URL, formData, options).pipe(tap(
      () => console.debug(JSON.stringify({ createBlob: { end: { url: url } } })),
      e => console.error(e)
    ));
  }

  /**
   * JUNoT APIサーバーからデータを1件取得する。
   *
   * @param url JUNoT APIのURL
   * @param params パラメーター
   * @return レスポンス
   */
  get<T, V>(url: string, params?: T): Observable<V> {
    console.debug(JSON.stringify({ get: { start: { url: url, params: params } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const API_PARAMS = this.toParams(params);
    const options = { params: API_PARAMS, withCredentials: true };

    return this.http.get<V>(API_URL, options).pipe(tap(
      () => console.debug(JSON.stringify({ get: { end: { url: url } } })),
      e => console.error(e)
    ));
  }

  /**
   * JUNoT APIサーバーからデータをリスト取得する。
   *
   * @param url JUNoT APIのURL
   * @param params パラメーター
   * @return レスポンス
   */
  list<T, V>(url: string, params?: T): Observable<GenericList<V>> {
    console.debug(JSON.stringify({ list: { start: { url: url, params: params } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const API_PARAMS = this.toParams(params);
    const options = { params: API_PARAMS, withCredentials: true };

    return this.http.get<GenericList<V>>(API_URL, options).pipe(tap(
      x => console.debug(JSON.stringify({ list: { end: { url: url, itemsCount: x.items.length } } })),
      e => console.error(e)
    ));
  }

  /**
   * JUNoT APIサーバーからデータをPOSTでリスト取得する。
   *
   * @param url JUNoT APIのURL
   * @param model POSTするデータ
   * @return レスポンス
   */
  listByPost<T, V>(url: string, model?: T): Observable<GenericList<V>> {
    console.debug(JSON.stringify({ list: { start: { url: url } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const HEADERS = new HttpHeaders({
      'x-xsrf-token': this.sessionService.getToken(),
      'Content-Type': HTTP_HEADER_CONTENT_TYPE_VALUE
    });
    const options = { headers: HEADERS, withCredentials: true };

    return this.http.post<GenericList<V>>(API_URL, model, options).pipe(tap(
      x => console.debug(JSON.stringify({ list: { end: { url: url, itemsCount: x.items.length } } })),
      e => console.error(e)
    ));
  }

  /**
   * JUNoT APIサーバーにデータを1件登録する。
   *
   * @param url JUNoT APIのURL
   * @param model POSTするデータ
   * @return レスポンス
   */
  update<T, V>(url: string, model: T): Observable<V> {
    console.debug(JSON.stringify({ update: { start: { url: url } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
    const HEADERS = new HttpHeaders({
      'x-xsrf-token': this.sessionService.getToken(),
      'Content-Type': HTTP_HEADER_CONTENT_TYPE_VALUE
    });
    const options = { headers: HEADERS, withCredentials: true };

    return this.http.put<V>(API_URL, model, options).pipe(tap(
      () => console.debug(JSON.stringify({ update: { end: { url: url } } })),
      e => console.error(e))
    );
  }

  /**
   * オブジェクトをHTTPパラメーターに変換する。
   *
   * @param params パラメーター
   * @return HTTPパラメーター
   */
  private toParams(params: any): HttpParams {
    if (params == null) {
      return null;
    }

    return Object.getOwnPropertyNames(params)
      .filter(key => params[key] != null)
      .reduce((p, key) => p.set(key, params[key]), new HttpParams({ encoder: new CustomEncoder() }));
  }

  //PRD_0133 #10181 add JFE start
  /**
   * JUNoT APIサーバーからPDFをダウンロードを取得する。
   *
   * @param url JUNoT APIのURL
   * @param params パラメーター
   * @return レスポンス
   */
   pdfDownload<T>(url: string, params?: T): Observable<any> {
    console.debug(JSON.stringify({ getBlob: { start: { url: url, params: params } } }));
    const API_URL = `${ environment.apiBaseUrl }${ url }`;
     const API_PARAMS = this.toParams(params);
     const HEADERS = new HttpHeaders({
      'x-xsrf-token': this.sessionService.getToken()
      ,'Content-Type': HTTP_HEADER_CONTENT_TYPE_VALUE
     });

    return this.http.get(API_URL,{
      params: API_PARAMS,
      headers: HEADERS,
      withCredentials: true,
      responseType: 'blob',
      observe: 'response'
     }
    ).pipe(tap(
      () => console.debug(JSON.stringify({ getBlob: { end: { url: url } } })),
      e => console.error(e))
    );
  }
  //PRD_0133 #10181 add JFE end
}
