import { Pipe, PipeTransform } from '@angular/core';
import { MakerReturnSearchResult } from '../model/maker-return-search-result';
import { LgSendType } from '../const/lg-send-type';

@Pipe({
  name: 'makerReturnStatus'
})
export class MakerReturnStatusPipe implements PipeTransform {

    /**
   * @param purchase 行のフォーム値
   * @returns メーカー返品状態文言
   * 未送信：LG送信フラグ===送信未指示
   * 送信済：LG送信フラグ!==送信未指示
   */
  transform(makerReturn: MakerReturnSearchResult): string {

    if (LgSendType.NO_INSTRUCTION === makerReturn.lgSendType) {
      return '未送信';
    }
    return '送信済';
  }


}
