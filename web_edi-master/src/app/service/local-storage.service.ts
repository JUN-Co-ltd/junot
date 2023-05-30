import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, WebStorageService } from 'angular-webstorage-service';
import { Session } from '../model/session';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

  constructor(
    @Inject(LOCAL_STORAGE) private storage: WebStorageService
  ) { }

  /**
   * ログインユーザーごとのストレージキーを作成する
   * @param session セッション
   * @param storageKey ストレージキー
   * @returns ログインユーザーごとのストレージキー
   */
  private generateStorageKeyByUser(session: Session, storageKey: string): string {
    const accountName = session.userId;
    const company = session.company;
    return accountName + company + storageKey;
  }

  /**
   * ストレージにObjectを登録する.
   * @param session セッション
   * @param storageKey ストレージキー
   * @param object 登録データ
   */
  createLocalStorage(session: Session, storageKey: string, object: any): void {
    const storageKeyByUser = this.generateStorageKeyByUser(session, storageKey);
    this.deleteSaveLocalStorage(storageKeyByUser);
    this.storage.set(storageKeyByUser, JSON.stringify(object));
  }

  /**
   * 保存しているストレージのObjectを取得する.
   * @param session セッション
   * @param storageKey ストレージキー
   * @returns ストレージのObject
   */
  getSaveLocalStorage(session: Session, storageKey: string): any {
    const storageKeyByUser = this.generateStorageKeyByUser(session, storageKey);
    const sessionObject = this.storage.get(storageKeyByUser);
    return sessionObject;
  }

  /**
   * 保存したストレージObjectを削除する.
   * @param storageKey ストレージキー
   */
  private deleteSaveLocalStorage(storageKey: string): void {
    this.storage.remove(storageKey);
  }

  /**
   * 保存しているすべてのローカルストレージオブジェクトを削除する
   */
  deleteAllSaveLocalStorage(): void {
    window.localStorage.clear();
  }
}
