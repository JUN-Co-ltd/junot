//PRD_0133 #10181 add JFE start
import { PipeTransform, Pipe } from '@angular/core';
import { PurchaseRecordSearchResult } from '../model/purchase-record-search-result';

@Pipe({
  name: 'purchaseRecordStatus'
})
export class PurchaseRecordStatusPipe implements PipeTransform {

  /**
   * @param purchase 行のフォーム値
   * @returns 仕入区分
   */
  transform(purchase: PurchaseRecordSearchResult): string {
    //仕入区分の絞り込み条件に使用する入荷場所を定義
    /** 本社 */
    const mainOffice: string = '10';
    /** 店舗直送 */
    const directStore: string = '14';
    /** 店舗発注 */
    const orderStore: string = '18';

    switch (purchase.purchaseType) {
      case '1':
        return '追加仕入';

      case '3':
        return '仕入返品';

      case '4':
        return '附属仕入';

      case '5':
        return 'その他仕入';

      case '6':
        return '配分出荷';

      case '7':
        return '直送仕入';

      case '9':
        if (purchase.arrivalPlace == '18') {
          return '店舗発注店舗';
        } else {
          return '消化委託店舗';
        }

      default:
        return '';
    }

  }
}
//PRD_0133 #10181 add JFE end
