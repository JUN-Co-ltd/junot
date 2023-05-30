import { Injectable } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { ColorSize } from 'src/app/model/color-size';

@Injectable({
  providedIn: 'root'
})
export class SkuService {

  constructor() { }

  /**
   * @param data データ
   * @param form フォーム
   * @returns true:カラーサイズが一致
   */
  isMatchSkuForm(data: ColorSize, form: AbstractControl): boolean {
    return data.colorCode === form.get('colorCode').value
      && data.size === form.get('size').value;
  }

  /**
   * @param data1 データ1
   * @param data2 データ2
   * @returns true:カラーサイズが一致
   */
  isMatchSku(data1: ColorSize, data2: ColorSize): boolean {
    return data1.colorCode === data2.colorCode && data1.size === data2.size;
  }

  /**
   * @paramm colors カラーリスト
   * @param colorCode カラーコード
   * @return カラー名称
   */
  findColorName(colors: JunpcCodmst[], colorCode: string): string {
    const target = colors.find(color => color.code1 === colorCode);
    return target == null ? null : target.item1;
  }

  /**
   * 並び順で最初のカラーか判別.
   * @param skus 店舗マスタリスト
   * @param sku 処理中のSKU
   * @param index index
   * @return true:最初
   */
  isFirstColor(skus: ColorSize[], sku: ColorSize, index: number): boolean {
    if (index === 0) { return true; }
    // カラーが変わった
    return skus[index - 1].colorCode !== sku.colorCode;
  }
}
