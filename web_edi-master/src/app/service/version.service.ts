import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Version } from '../model/version';
import { v } from '../../assets/version.json';

/**
 * バージョンチェック用.
 */
@Injectable({
  providedIn: 'root'
})
export class VersionService {
  constructor(
    private http: HttpClient
  ) { }

  /**
   * バージョンを確認し、最新バージョン以外の場合は、リロードする
   */
  async reload(): Promise<void> {
    const isLatestVersion = await this.isLatestVersion().catch(error => { throw error; } );
    console.debug('isLatestVersion:', isLatestVersion);

    if (!isLatestVersion) {
      location.reload();
    }
  }

  /**
   * 最新バージョンか判定する。
   *
   * @return 判定結果
   */
  private async isLatestVersion(): Promise<boolean> {
    const serverVersion = await this.get().catch(error => { throw error; });
    return v === this.getVersionNumber(serverVersion);
  }

  /**
   * バージョン情報を取得する。
   *
   * @return バージョン情報
   */
  async get(): Promise<Version> {
    const url = '/assets/version.json?t=' + new Date().getTime();

    return await this.http.get<Version>(url).toPromise();
  }

  /**
   * バージョン情報を取得する。
   *
   * @param version バージョン情報
   * @return バージョン番号
   */
  private getVersionNumber(version: Version): string {
    if (version === null) {
      throw new Error('serverVersion is null');
    } else if (version.v === null || version.v === void 0) {
      throw new Error('serverVersionNumber is null');
    }

    return version.v;
  }
}
