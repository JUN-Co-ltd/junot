import { Injectable } from '@angular/core';

import { JunotApiService } from './junot-api.service';
import { Observable } from 'rxjs';

const BASE_URL = '/documentFiles';

/**
 * ドキュメントファイルのダウンロードを行うサービス。
 */
@Injectable({
  providedIn: 'root'
})
export class DocumentFileService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * 共有ドキュメントファイルをダウンロードする。
   */
  shareFileDownload(fileName: string): Observable<any> {
    const URL = `${ BASE_URL }/share/${ fileName }`;
    return this.junotApiService.fileDownload(URL);
  }

  /**
   * JUN権限のみアクセス可能なドキュメントをダウンロードする。
   */
  junFileDownload(fileName: string): Observable<any> {
    const URL = `${ BASE_URL }/jun/${ fileName }`;
    return this.junotApiService.fileDownload(URL);
  }

  /**
   * ファイルダウンロード処理のレスポンスからblobとファイル名を分割して取得する.
   * @param ファイルダウンロード処理のレスポンス
   * @return blobとファイル名
   */
  splitBlobAndFileName(res: any): { blob: Blob, fileName: string } {
    const blob: Blob = res.body;
    let fileName = '';
    const contentDisposition = res.headers.get('content-disposition');
    const arr = contentDisposition.split(/;/);
    arr.map(str => str.split('filename=')).some(x => {
      if (x.length === 2) {
        const encFileName = x[1].replace(/"/g, ''); // ダブルクォーテーション除去
        fileName = decodeURIComponent(encFileName); // デコード
        return true;
      }
    });
    return { blob: blob, fileName: fileName };
  }
}
