import { PipeTransform, Pipe } from '@angular/core';
import { PurchaseSearchResult } from '../model/purchase-search-result';
import { LgSendType } from '../const/lg-send-type';

@Pipe({
  name: 'purchaseStatus'
})
export class PurchaseStatusPipe implements PipeTransform {

  /**
   * @param purchase 行のフォーム値
   * @returns 仕入状態文言
   * 未入荷：仕入情報テーブルに登録なし
   * 未送信：仕入情報テーブルに登録あり、LG送信フラグ===送信未指示
   * 送信済：仕入情報テーブルに登録あり、LG送信フラグ!==送信未指示、入荷フラグ === false
   * 入荷済：仕入情報テーブルに登録あり、LG送信フラグ!==送信未指示、入荷フラグ === true
   */
  transform(purchase: PurchaseSearchResult): string {

    if (purchase.purchaseRegisteredCount === 0) {
      return '未入荷';
    }

    if (LgSendType.NO_INSTRUCTION === purchase.lgSendType) {
      return '未送信';
    }

    if (LgSendType.INSTRUCTION === purchase.lgSendType && purchase.arrivalFlg === false) {
      return '送信済';
    }

    return '入荷済';
  }
}
