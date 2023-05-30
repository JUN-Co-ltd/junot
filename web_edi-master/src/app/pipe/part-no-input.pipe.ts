import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'partNoInput'
})
export class PartNoInputPipe implements PipeTransform {

  /**
   * focus時
   * 入力中は「-」外す
   */
  parse(value: string): string {
    return value.replace(/-/g, '');
  }

  /**
   * focus out時
   * 全角英数字を半角に変換
   * 入力値が8文字だった場合
   * 3文字目に「-」を挿入
   */
  transform(value: any, args?: string): any {
    let retVal = value;

    // 入力チェック
    if (!retVal || retVal.length === 0) {
      return retVal;
    }

    // 全角英数字→半角変換
    retVal = retVal.replace(/[Ａ-Ｚａ-ｚ０-９]/g, function(s) {
      return String.fromCharCode(s.charCodeAt(0) - 65248);
    });

    // 8文字の場合は「-」挿入
    if (retVal.length === 8) {
      retVal = retVal.slice(0, 3) + '-' + retVal.slice(3);
    }
    return retVal;
  }
}
