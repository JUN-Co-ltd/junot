import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { TFile } from '../model/t-file';

import { JunotApiService } from './junot-api.service';

const BASE_URL = '/misleadingRepresentations';

/**
 * FabricInspectionResult操作に関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class FabricInspectionResultService {

  constructor(private junotApiService: JunotApiService) { }

  /**
   * 生地検査結果情報登録処理
   */
  postFabricInspectionResult(postItem: any): Observable<Object> {
    const body = this.convertRequestData(postItem);
    console.debug('body:', body);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 画面の入力データをrequestパラメータの型に整形して返す
   * @param inputData 画面の入力データ
   */
  convertRequestData(inputData: any): string {
    const copyItem = JSON.parse(JSON.stringify(inputData));

    /** 品番リストを送信用フォーマットに変更 */
    const reqItemList: Number[] = [];
    copyItem.itemFormArray.forEach(item => {

      if (item.id != null) {
        reqItemList.push(item.id);
      }
    });
    copyItem.items = reqItemList;

    /** ファイルリストを送信用フォーマットに変更 */
    const reqFileList: TFile[] = [];
    copyItem.fileFormArray.forEach(file => {
      file.fileData = null; // stringfyでファイルの実態の型がJava側のModelと合わないため、応急処置としてnullをセット
      reqFileList.push(file);
    });
    copyItem.files = reqFileList;

    return copyItem;
  }
}
