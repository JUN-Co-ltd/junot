import { Injectable } from '@angular/core';
import { FormArray } from '@angular/forms';
import { MakerReturn } from 'src/app/model/maker-return';
import { NumberUtilsService } from 'src/app/service/bo/number-utils.service';
import { Order } from 'src/app/model/order';

@Injectable({
  providedIn: 'root'
})
export class MakerReturnService {

  constructor(
    private numberUtils: NumberUtilsService
  ) { }

  /**
   * @param makerReturn メーカー返品情報
   * @param order 発注情報
   * @returns 合計返品数、合計金額
   */
  caluculateTotal(makerReturn: MakerReturn, order: Order): { returnLot: number, amount: number } {
    if (makerReturn == null || order == null) {
      return { returnLot: 0, amount: 0 };
    }

    return makerReturn.makerReturnProducts.reduce((acc, { returnLot }) =>
      ({
        returnLot: acc.returnLot += returnLot,
        amount: acc.amount += (returnLot * order.unitPrice)
      }), { returnLot: 0, amount: 0 });
  }

  /**
   * @param itemInfo 品番、カラー・サイズ
   * @returns 商品コード
   */
  generateProductCode(itemInfo: { partNo: string, colorCode: string, size: string }): string {
    return `${ itemInfo.partNo }-${ itemInfo.colorCode }-${ itemInfo.size }`;
  }

  /**
   * @param makerReturnProductsFA メーカー返品商品FormArray
   * @returns 合計返品数、合計金額再計算結果
   */
  reCaluculateTotal(makerReturnProductsFA: FormArray): { returnLot: number, amount: number } {
    return makerReturnProductsFA.controls
      .filter(f => this.numberUtils.isNumber(f.get('returnLot').value))
      .reduce((acc, cur) =>
        ({
          returnLot: acc.returnLot + this.numberUtils.defaultZero(cur.get('returnLot').value),
          amount: acc.amount + cur.get('amount').value
        }), { amount: 0, returnLot: 0 });
  }
}
