import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { StringUtils } from '../util/string-utils';
import { FileUtils } from '../util/file-utils';

import { JunotApiService } from './junot-api.service';

import { TFile } from '../model/t-file';

const BASE_URL = '/files';

/**
 * ファイルのアップロード/ダウンロードを行うサービス。
 */
@Injectable({
  providedIn: 'root'
})
export class FileService {
  constructor(
    private junotApiService: JunotApiService
  ) { }

  /**
   * ファイルをアップロードする。
   */
  fileUpload(file): Observable<TFile> {
    const formData = new FormData();
    formData.append('file', file);
    return this.junotApiService.fileUpload(BASE_URL, formData);
  }

  /**
   * ファイルをダウンロードする.
   * ※こちらは古いのでdownloadFileを使ってください.
   * ファイルをダウンロードする.
   * ※こちらは古いのでdownloadFileを使ってください.
   */
  fileDownload(id: string): Observable<any> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.fileDownload(URL);
  }

  /**
   * ファイル情報削除処理.
   */
  deleteFile(id: number): Observable<void> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.delete(URL);
  }

  /**
   * ファイルダウンロード処理のレスポンスからblobとファイル名を分割して取得する.
   * ※こちらは古いのでdownloadFileを使ってください.
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

  /**
   * ファイルダウンロード処理.
   * @param id ファイルID
   * @returns Observable<void>
   *   Error('ERRORS.FILE_DL_ERROR')
   */
  downloadFile = (id: string | number): Observable<void> => {
    const fileNoId = StringUtils.toStringSafe(id);
    return this.fileDownload(fileNoId).pipe(
      tap(res => {
        const data = this.splitBlobAndFileName(res);
        FileUtils.downloadFile(data.blob, data.fileName);
      }),
      catchError(err => {
        console.error(err);
        throw new Error('ERRORS.FILE_DL_ERROR');
      }));
  }
}
