import { AbstractControl, FormArray, FormGroup } from '@angular/forms';

import { NumberUtils } from '../../../util/number-utils';
import { FormUtils } from '../../../util/form-utils';

import { DeliveryStoreSkuFormValue } from '../interface/delivery-store-interface';
import { NumberUtilsService } from 'src/app/service/bo/number-utils.service';
import { store } from '@angular/core/src/render3/instructions';
import { debug } from 'console';

/**
 * 配分処理抽象クラス.
 */
export abstract class Distribute {

  /**
   * 配分対象の店舗フォームコントロールリストを返す.
   * @param mainForm メインフォーム
   * @param divisionCode 課コード
   * @returns 配分対象の店舗フォームコントロールリスト
   */
  abstract filterTargetDeliveryStores(mainForm: FormGroup, divisionCode: string): AbstractControl[];

  /**
   * 配分処理.
   * @param mainForm メインフォーム
   * @param skus 処理中のSKUリスト
   */
  distributeSku(mainForm: FormGroup, skus: { distribution: number, size: string, colorCode: string }[], divisionCode?: string): void {

    // 配分対象の店舗フォームコントロールリスト取得
    const targetDeliveryStores = this.filterTargetDeliveryStores(mainForm, divisionCode);
    // SKUごとに処理
    skus
      // 数量入力済以外の配分率合計,入力済の数量合計,配分する数量計算
      .map(sku => Object.assign(sku, this.calculateTotalAndDistributionLot(sku, targetDeliveryStores)))
      // SKU内で配分する数量が1以上のみ配分処理する
      .filter(sku => sku.distributionLot > 0)
      .forEach(sku => {
        // 数量入力済の納品数量合計を算出
        const totalInputtedLot = this.sumInputtedLot(targetDeliveryStores, sku);

        // PRD_0013 add SIT start
        // 2次配分用に配分対象をソートして保持
        const SortForPatchDistributeAgain = this.SortForPatchDistributeAgain(mainForm, targetDeliveryStores, sku);
        // PRD_0013 add SIT end

        // 店舗ごとに配分処理
        const distributeResult = targetDeliveryStores
          // 配分率が0より多いかつ、納品数量が活性状態かつ、納品数量が未入力の店舗のみ配分対象とする
          .filter(store => this.isDistributionMoreThanZero(store) && this.isMatchDeliveryLotSku(store, sku, FormUtils.isEmpty))
          // 配分処理.配分数合計と調整対象の店舗抽出
          .reduce((prev, store) => {
            const patchResult = this.patchDistribute(store, sku);
            return {
              totalPatchedLot: prev.totalPatchedLot += NumberUtils.defaultZero(patchResult.lot),  // 配分数合計
              adjustPatchTarget: prev.adjustPatchTarget == null && patchResult.lot > 0 ?
                patchResult.target : prev.adjustPatchTarget  // 調整対象SKU.数量0は調整しない
            };
          }, { totalPatchedLot: 0, adjustPatchTarget: null as AbstractControl });

        // PRD_0013 mod SIT start
        // 配分数調整
        //this.adjustDistribution(sku.distribution,
        //  (totalInputtedLot + distributeResult.totalPatchedLot), distributeResult.adjustPatchTarget);
        // 一次配分にて余りが発生しているかチェック
        let checklot = sku.distribution - (totalInputtedLot + distributeResult.totalPatchedLot)
        if (checklot > 0)
        {
          // 二次配分
          // 余りがなくなるまで複数回店舗全体をループ
          while (checklot != 0) {
            // PRD_0066 add SIT start
            FormUtils.isEmpty(SortForPatchDistributeAgain) ? checklot = 0 :
            // PRD_0066 add SIT end
            SortForPatchDistributeAgain
              .forEach(sortStore => {
                if (checklot != 0) {
                  this.patchDistributeAgain(sortStore, sku);
                  checklot = checklot - 1;
                }
                else
                  return;
              });
          }
        };
        // PRD_0013 mod SIT end
      });
  }

  /**
   * 数量入力済以外の配分率合計,入力済の数量合計,配分する数量を返す.
   * @param sku SKU
   * @returns 数量入力済以外の配分率合計,入力済の数量合計,配分する数量
   */
  private calculateTotalAndDistributionLot(sku: { distribution: number, size: string, colorCode: string },
    targetDeliveryStores: AbstractControl[])
    : { total: { ratio: number, inputtedLot: number }, distributionLot: number } {
    const total = this.sumRatioAndInputtedLot(sku, targetDeliveryStores);
    return {
      total: total, // 処理中のSKUの入力済数量合計
      distributionLot: sku.distribution - total.inputtedLot // 配分する数量.入力済は除く
    };
  }

  /**
   * 数量入力済以外の配分率合計、入力済の数量合計.
   * @param sku 処理中のsku
   * @param targetDeliveryStores 処理中の課の店舗フォームコントロールリスト
   * @returns 数量入力済以外の配分率合計、入力済の数量合計
   */
  private sumRatioAndInputtedLot(sku: { distribution: number, size: string, colorCode: string }, targetDeliveryStores: AbstractControl[])
    : { ratio: number, inputtedLot: number } {
    return targetDeliveryStores.reduce(
      (total, targetDeliveryStore) => {
        // 配分率が0より多いかつ、納品数量が活性状態の場合、配分率を取得
        const storeDistributionRatio =
          this.isDistributionMoreThanZero(targetDeliveryStore) ?
            NumberUtils.toNumberDefaultIfEmpty(targetDeliveryStore.get('storeDistributionRatio').value, 0.0) : 0.0;
        const deliveryStoreSkus = targetDeliveryStore.get('deliveryStoreSkus').value as DeliveryStoreSkuFormValue[];
        const targetSku = deliveryStoreSkus.find(storeSku => storeSku.colorCode === sku.colorCode && storeSku.size === sku.size);
        // PRD_0066 mod SIT start
        // targetSkuが存在し、DeliveryLotが取得できる場合のみaddNumに値を設定する
        //const addNum = FormUtils.isEmpty(targetSku.deliveryLot) ? storeDistributionRatio : 0;
        const addNum = FormUtils.isEmpty(targetSku) ? 0 : FormUtils.isEmpty(targetSku.deliveryLot) ? storeDistributionRatio : 0;
        // PRD_0066 mod SIT end
        return {
          // 納品数量未入力時のみ配分率加算
          ratio: NumberUtils.addDecimal(total.ratio, addNum),
          // PRD_0066 mod SIT start
          //inputtedLot: total.inputtedLot + NumberUtils.defaultZero(targetSku.deliveryLot)
          inputtedLot: total.inputtedLot + (FormUtils.isEmpty(targetSku) ? 0 : NumberUtils.defaultZero(targetSku.deliveryLot))
          // PRD_0066 mod SIT end
        };
      }, { ratio: 0, inputtedLot: 0 });
  }

  /**
   * 入力済の納品数量合計を算出
   * @param targetDeliveryStores 処理対象の店舗フォームコントロールリスト
   * @param sku SKU
   * @returns 入力済納品数量合計
   */
  private sumInputtedLot = (targetDeliveryStores: AbstractControl[], sku: { colorCode: string, size: string }): number =>
    targetDeliveryStores
      // 数量入力がある店舗のみ合計対象とする
      .filter(store => this.isMatchDeliveryLotSku(store, sku, FormUtils.isNotEmpty))
      // 納品数量合計
      .reduce((total, current) => total += NumberUtils.defaultZero(this.findSkuFormCtrl(current, sku).get('deliveryLot').value), 0)

  /**
   * 配分率が0より多いかつ、納品数量が活性状態
   * @param store 店舗フォームコントロール
   * @returns true:配分率が0より多いかつ、納品数量が活性状態
   */
  private isDistributionMoreThanZero = (store: AbstractControl): boolean => {
    const value = store.get('storeDistributionRatio').value;

    if (!(NumberUtils.isNumber(value) && value > 0)) {
      return false;
    }

    // 活性状態の納品数量があるか調べる
    return (store.get('deliveryStoreSkus') as FormArray).controls.some(
      deliveryStoreSku => deliveryStoreSku.get('deliveryLot').enabled
    );
  }

  /**
   * 処理中のSKUが数量の判定処理に該当するか
   * @param store 店舗
   * @param sku 処理中のSKU
   * @param fn 判定処理
   * @returns true:該当する
   */
  private isMatchDeliveryLotSku = (store: AbstractControl, sku: { colorCode: string, size: string },
    fn: (deliveryLot: any) => boolean): boolean =>
    store.get('deliveryStoreSkus').value
      .some(storeSku => storeSku.colorCode === sku.colorCode && storeSku.size === sku.size && fn(storeSku.deliveryLot))

  /**
   * 配分処理のpatch処理.
   * @param store 店舗フォームコントロール
   * @param sku SKU
   * @returns 配分数、配分対象SKUフォーム
   */
  private patchDistribute(store: AbstractControl,
    sku: { colorCode: string, size: string, total: { ratio: number }, distributionLot: number }): { lot: number, target: AbstractControl } {
    console.debug('**** sku ', sku);
    const lot = this.calclateDistributionLot(store, sku);
    // 配分数patch
    const target = this.findSkuFormCtrl(store, sku);
    target.patchValue({ deliveryLot: lot > 0 ? lot : null });
    return { lot: lot, target: target };
  }

  /**
   * 店舗フォームコントロールから指定したSKUのフォームコントロールを取得する.
   * @param store 店舗フォームコントロール
   * @param sku SKU
   * @returns 指定したSKUのフォームコントロール
   */
  private findSkuFormCtrl = (store: AbstractControl, sku: { colorCode: string, size: string }): AbstractControl =>
    (<FormArray> store.get('deliveryStoreSkus')).controls
      .find(storeSkus => storeSkus.get('colorCode').value === sku.colorCode && storeSkus.get('size').value === sku.size)

  /**
   * 全体に対しての配分数計算※小数5桁以下切り捨て(10000桁の入力に対して一の位まで計算)
   * @param store 店舗フォームコントロール
   * @param sku SKU
   * @returns 配分数
   */
  private calclateDistributionLot(store: AbstractControl, sku: { total: { ratio: number }, distributionLot: number }): number {
    const totalRatio = NumberUtils.floor(sku.total.ratio, 10);
    const storeDistributionRatio = NumberUtils.toNumberDefaultIfEmpty(store.get('storeDistributionRatio').value, 0);
    const rate = NumberUtils.floor(storeDistributionRatio / totalRatio, 10000);
    return NumberUtils.floor(sku.distributionLot * rate, 0);
  }

  /**
   * 配分数を調整する.
   * SKUの配分数量が入力された配分数合計と差分がある場合は、最初に配分した得意先に調整を行う.
   * @param skuDistribution SKUの配分数
   * @param totalPatchedLot 配分数合計
   * @param firstPatchTarget 調整対象のSKU
   */
  private adjustDistribution(skuDistribution: number, totalPatchedLot: number, firstPatchTarget: AbstractControl): void {
    if (firstPatchTarget == null) { return; }

    const diff = skuDistribution - totalPatchedLot;
    if (diff > 0) {
      const lot = NumberUtils.defaultZero(firstPatchTarget.value.deliveryLot) + diff;
      firstPatchTarget.patchValue({ deliveryLot: lot });
    }
  }

  /**
   * 配分処理のpatch処理.
   * @param store 店舗フォームコントロール
   * @param sku SKU
   * @returns 配分数、配分対象SKUフォーム
   */
   private patchDistributeAgain(store: AbstractControl,
    sku: { colorCode: string, size: string, total: { ratio: number }, distributionLot: number }): { lot: number, target: AbstractControl } {
    console.debug('**** sku ', store);
    // 配分数patch
    const target = this.findSkuFormCtrl(store, sku);
    const lot = NumberUtils.defaultZero(target.value.deliveryLot) + 1;
    target.patchValue({ deliveryLot: lot > 0 ? lot : null });
    return { lot: lot, target: target };
  }

  /**
   * 2次配分用にソート処理したフォーム作成処理.
   * @param targetDeliveryStores  配分対象の店舗フォームコントロールリスト
   * @param sku SKU
   * @returns ソート済配分対象の店舗フォームコントロールリスト
   */
  private SortForPatchDistributeAgain(mainForm: FormGroup, targetDeliveryStores:AbstractControl[] = (<FormArray> mainForm.get('deliveryStores')).controls,
    sku: { colorCode: string, size: string, total: { ratio: number }, distributionLot: number }): AbstractControl[] {
    // 1次配分と同条件で絞り込み　＋　2次配分用にソート条件を追加
     return targetDeliveryStores
       // 配分率が0より多いかつ、納品数量が活性状態かつ、納品数量が未入力の店舗のみ配分対象とする
      .filter(store => this.isDistributionMoreThanZero(store) && this.isMatchDeliveryLotSku(store, sku, FormUtils.isEmpty))
       .sort((val1, val2) => {
        // PRD_0079 mod SIT start
          //// 1:配分率の小数点第一位の降順
          //if (NumberUtils.decimalPart(val1.get('storeDistributionRatio').value)
          // > NumberUtils.decimalPart(val2.get('storeDistributionRatio').value)) { return -1; }
          //if (NumberUtils.decimalPart(val1.get('storeDistributionRatio').value)
          // < NumberUtils.decimalPart(val2.get('storeDistributionRatio').value)) { return 1; }
        // 1:1次配分数の小数点第一位の降順
          if (NumberUtils.decimalPart(NumberUtils.floor(NumberUtils.floor((val1.get('storeDistributionRatio').value / NumberUtils.floor(sku.total.ratio, 10)), 10000) * sku.distributionLot,10))
           > NumberUtils.decimalPart(NumberUtils.floor(NumberUtils.floor((val2.get('storeDistributionRatio').value / NumberUtils.floor(sku.total.ratio, 10)), 10000) * sku.distributionLot,10))){ return -1; }
          if (NumberUtils.decimalPart(NumberUtils.floor(NumberUtils.floor((val1.get('storeDistributionRatio').value / NumberUtils.floor(sku.total.ratio, 10)), 10000) * sku.distributionLot, 10))
           < NumberUtils.decimalPart(NumberUtils.floor(NumberUtils.floor((val2.get('storeDistributionRatio').value / NumberUtils.floor(sku.total.ratio, 10)), 10000) * sku.distributionLot,10))) { return 1; }
          // PRD_0079 mod SIT end
        // 2:配分順位の昇順
          if (NumberUtils.defaultZero(val1.get('distributionSort').value)  < NumberUtils.defaultZero(val2.get('distributionSort').value) ) { return -1; }
          if (NumberUtils.defaultZero(val1.get('distributionSort').value)  > NumberUtils.defaultZero(val2.get('distributionSort').value) ) { return 1; }
        // 2:店舗の所属する課の昇順
          if (NumberUtils.defaultZero(val1.get('divisionCode')  < val2.get('divisionCode')) ) { return -1; }
          if (NumberUtils.defaultZero(val1.get('divisionCode')  > val2.get('divisionCode')) ) { return 1; }
      });
  }

}
