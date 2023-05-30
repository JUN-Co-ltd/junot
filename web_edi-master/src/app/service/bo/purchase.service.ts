import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { NumberUtilsService } from 'src/app/service/bo/number-utils.service';
import { SkuService } from 'src/app/service/bo/sku.service';

import { ColorSize } from 'src/app/model/color-size';

import { PurchaseSku } from '../../component/purchase/interface/purchase-sku';
import { Division } from '../../component/purchase/interface/division';
import { Purchase } from '../../component/purchase/interface/purchase';
import { DeliverySku } from 'src/app/model/delivery-sku';
import { PurchaseTransactionData } from '../../component/purchase/interface/purchase-transaction-data';
import { LgSendType } from 'src/app/const/lg-send-type';
import { DeliveryDetail } from 'src/app/model/delivery-detail';

@Injectable({
  providedIn: 'root'
})
export class PurchaseService {
  constructor(
    private skuService: SkuService,
    private numberUtils: NumberUtilsService
  ) { }

  /**
   * @param purchase SkuFormArrayのvalue
   * @param skuFG SkuFormGroup
   * @returns 指定したskuの入荷数量合計
   */
  calculateSkuTotalLot = (purchase: Purchase) => (sku: ColorSize) => {
    const targetSku = purchase.purchaseSkus.find(ss => this.skuService.isMatchSku(ss, sku));
    if (targetSku == null) { return 0; }
    return targetSku.purchaseDivisions.reduce((acc, { arrivalCount }) => acc += this.numberUtils.defaultZero(arrivalCount), 0);
  }

  /**
   * ※purchaseSkus内に指定したSKUがないことは想定しません.
   * @param purchaseSkus PurchaseSkuFormArrayのvalue
   * @param skuFG SkuFormGroup
   * @returns 指定したSkuFormの入荷数量合計
   */
  calculateSkuFormTotalLot(purchaseSkus: PurchaseSku[], skuFG: FormGroup): number {
    const target = purchaseSkus.find(skuVal => this.skuService.isMatchSkuForm(skuVal, skuFG));
    return target.purchaseDivisions.reduce((acc, { arrivalCount }) => acc += this.numberUtils.defaultZero(arrivalCount), 0);
  }

  /**
   * @param purchase 仕入情報
   * @param deliveryDetail 納品明細
   * @returns 指定した納品明細の配分課の入荷数量合計
   */
  calculateDivisionTotalLot = (purchase: Purchase) => (deliveryDetail: DeliveryDetail) =>
    purchase.purchaseSkus.reduce((acc, { purchaseDivisions }) => {
      const target = purchaseDivisions.find(sd => sd.divisionCode === deliveryDetail.divisionCode);
      return acc += (target == null ? 0 : this.numberUtils.defaultZero(target.arrivalCount));
    }, 0)

  /**
   * ※purchaseSkus内に指定した配分課がないことは想定しません.
   * @param purchaseSkus purchaseSkuFormArrayのvalue
   * @param divisionCode 配分課コード
   * @returns 指定した配分課の入荷数量合計
   */
  calculateDivisionFormTotalLot(purchaseSkus: PurchaseSku[], divisionCode: string) {
    return purchaseSkus.reduce((acc, { purchaseDivisions }) => {
      const arrivalCount = purchaseDivisions.find(division => division.divisionCode === divisionCode).arrivalCount;
      return acc += this.numberUtils.defaultZero(arrivalCount);
    }, 0);
  }

  /**
   * @param divisionList 配分課リスト
   * @returns 全入荷数量合計
   */
  calculateTotalLot(divisionList: Division[]): number {
    return divisionList.reduce((acc, { totalLot }) => acc += totalLot, 0);
  }

  /**
   * @param deliverySku 納品SKU
   * @returns 入荷数の最大数
   */
  getMaxArrivalCountVaue(deliverySku: DeliverySku): number {
    return deliverySku == null ? 0 : deliverySku.deliveryLot;
  }

  /**
   * @param purchase 仕入情報
   * @returns LG送信未指示が1つでもある
   */
  existsLgNoInstruction(purchase: Purchase): boolean {
    return purchase.purchaseSkus
      .some(s => s.purchaseDivisions
        .some(d => LgSendType.NO_INSTRUCTION === d.lgSendType));
  }

  /**
   * @param data 取得データ
   * @returns LG送信指示済が1つもない
   */
  noExistsLgInstruction(data: PurchaseTransactionData): boolean {
    return data.purchase == null || data.purchase.purchaseSkus
      .every(s => s.purchaseDivisions
        .every(d => LgSendType.NO_INSTRUCTION === d.lgSendType));
  }

  /**
   * @param purchase 仕入情報
   * @param sku 処理中のSKU
   * @return 指定したSKUの仕入SKU情報リスト
   */
  filterBySku(purchase: Purchase, sku: ColorSize): PurchaseSku[] {
    return  purchase.purchaseSkus.filter(pSku => this.skuService.isMatchSku(sku, pSku));
  }
}
