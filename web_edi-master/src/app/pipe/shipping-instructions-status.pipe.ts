import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'shippingInstructionsStatus'
})
export class ShippingInstructionsStatusPipe implements PipeTransform {
  /**
   * 送信ステータスの文字列を返す
   *  - 未送信 : 0
   *  - 送信済 : 1
   * @param sendStatus 送信ステータス
   */
  transform(sendStatus: number): string {

    if (sendStatus.toString() === '0') {
      return '未送信';
    } else if (sendStatus.toString() === '1') {
      return '送信済';
    }
  }
}
