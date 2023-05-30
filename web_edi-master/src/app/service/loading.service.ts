import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { StringUtils } from 'src/app/util/string-utils';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  public isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);

  private loadingSet = new Set<string>();

  constructor() { }

  /**
   * 旧関数のため、こちらは使用しないでください。
   */
  public loadStart(): string {
    console.debug('loadStart');
    return this.startLoading();
  }

  /**
   * 旧関数のため、こちらは使用しないでください。
   */
  public loadEnd(): void {
    console.debug('loadEnd');
    this.clear();
  }

  /**
   * ローディングの状態をクリアする。
   */
  public clear(): void {
    this.loadingSet.clear();
    this.isLoading$.next(false);
  }

  /**
   * トークンの指定がない場合、トークンを生成する。
   * トークンの指定がある場合、トークンを返却する。
   *
   * @param token トークン
   * @returns トークン
   */
  private generateToken(token: string): string {
    if (StringUtils.isEmpty(token)) {
      return new Date().getTime().toString(16) + Math.floor(1000 * Math.random()).toString(16);
    }

    return token;
  }

  /**
   * トークンをキーとして、ローディングを開始する。
   * トークンの指定がない場合、トークンを生成する。
   *
   * @param token トークン（省略化） ローディング中のプロセスを識別する
   * @returns トークン
   */
  private startLoading(token?: string): string {
    const localToken = this.generateToken(token);

    this.loadingSet.add(localToken);

    if (!this.isLoading$.value) {
      this.isLoading$.next(true);
    }

    return localToken;
  }

  /**
   * トークンをキーとして、ローディングを開始する。
   *
   * @param token トークン（省略化） ローディング中のプロセスを識別する
   * @returns Observable<token>
   */
  public start(token?: string): Observable<string> {
    return of(token).pipe(
      map(localToken => this.startLoading(localToken)));
  }

  /**
   * トークンをキーとして、ローディングを停止する。
   * すべてのトークンが削除された場合、ローディングが停止される。
   *
   * @param token トークン
   */
  public stop(token: string): void {
    if (StringUtils.isEmpty(token)) {
      return;
    }

    this.loadingSet.delete(token);

    if (this.isLoading$.value && this.loadingSet.size === 0) {
      this.isLoading$.next(false);
    }
  }
}
