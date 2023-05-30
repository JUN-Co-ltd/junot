import { Inject, Injectable } from '@angular/core';
import { SESSION_STORAGE, WebStorageService } from 'angular-webstorage-service';

@Injectable({
  providedIn: 'root'
})
export class ClientSessionService {
  constructor(
    @Inject(SESSION_STORAGE) private storage: WebStorageService
  ) { }

  /**
   * セッションObjectを登録する.
   */
  createSession(storage_key: string, object: any): void {
    this.deleteSaveSession(storage_key);
    this.storage.set(storage_key, JSON.stringify(object));
  }

  /**
   * 保存しているセッションObjectを取得する.
   */
  getSaveSession(storage_key): any {
    const sessionObject = this.storage.get(storage_key);
    return sessionObject;
  }

  /**
   * 保存したセッションObjectを削除する.
   */
  private deleteSaveSession(storage_key: string): void {
    this.storage.remove(storage_key);
  }
}
