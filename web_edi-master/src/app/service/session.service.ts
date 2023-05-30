import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CookieService } from 'ngx-cookie-service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { environment } from '../../environments/environment';

import { Session } from '../model/session';

const BASE_URL = `${ environment.apiBaseUrl }/sessions`;
const SESSIONS_ME_URL = `${ BASE_URL }/me`;
const SESSIONS_ME_DELETE_URL = `${ BASE_URL }/me:delete`;
const TOKEN_NAME = 'XSRF-TOKEN';  // トークン名
const SESSION_GENERATION_TIME_NAME = 'SGT';  // セッション生成時刻
const CREATE_SESSION_HEADERS = new HttpHeaders({
  'Content-Type': 'application/x-www-form-urlencoded'
});

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private sessionSource: BehaviorSubject<Session> = new BehaviorSubject<Session>(null);
  private sessionGenerationTime: string = null;

  constructor(
    private http: HttpClient,
    private cookieService: CookieService
  ) { }

  /**
   * セッションを作成する.
   */
  createSession(company: string, accountName: string, password: string): Observable<Session> {
    this.deleteSaveSession();
    const body: string = 'accountName=' + encodeURIComponent(company + '\n' + accountName) +
      '&password=' + encodeURIComponent(password);
    return this.http.post<Session>(
      BASE_URL, body, {
        withCredentials: true,
        headers: CREATE_SESSION_HEADERS
      }).pipe(tap(response => this.saveSession(response)));
  }

  /**
   * セッションを削除する.
   */
  deleteSession(): Observable<void> {
    const headers = new HttpHeaders({ 'x-xsrf-token': this.getToken() });
    return this.http.post<void>(
      SESSIONS_ME_DELETE_URL, null, {
        withCredentials: true,
        headers: headers
      }).pipe(tap(() => this.deleteSaveSession()));
  }

  /**
   * 保存したセッションを削除する.
   */
  private deleteSaveSession() {
    this.sessionSource.next(null);
    // クッキーを削除
    this.cookieService.delete(SESSION_GENERATION_TIME_NAME);
    // 保存しているすべてのセッションオブジェクトを削除する
    window.sessionStorage.clear();
  }

  /**
   * トークンを取得する。
   */
  getToken(): string {
    return this.cookieService.get(TOKEN_NAME);
  }

  /**
   * 保存しているセッションを取得する.
   */
  getSaveSession(): Session {
    return this.sessionSource.getValue();
  }

  /**
   * セッションを取得する。
   */
  getSession(): Observable<Session> {
    const session = this.sessionSource.getValue();

    if ((session != null) && (this.cookieService.get(SESSION_GENERATION_TIME_NAME) === this.sessionGenerationTime)) {
      // 保存されたセッションがあり、クッキーのセッション生成時刻が一致する場合、保存されたセッションを返却
      return this.sessionSource.asObservable();
    }

    return this.fetch();
  }

  /**
   * セッションを保存する.
   */
  private saveSession(session: Session): void {
    // セッションを保存
    this.sessionSource.next(session);
    // セッション生成時刻を取得
    this.sessionGenerationTime = new Date().valueOf().toString();
    // セッション生成時刻をクッキーに設定
    this.cookieService.set(SESSION_GENERATION_TIME_NAME, this.sessionGenerationTime);
  }

  /**
   * セッションを取得する.
   */
  private fetch(): Observable<Session> {
    return this.http.get<Session>(
      SESSIONS_ME_URL,
      {
        withCredentials: true
      }
    ).pipe(
      tap(response => this.saveSession(response)));
  }
}
