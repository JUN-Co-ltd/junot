import * as Moment_ from 'moment';

import { Injectable } from '@angular/core';

import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { AllocationCode, Path } from '../../const/const';

import { CompareResult } from '../../enum/compare-result.enum';

import { StringUtils } from '../../util/string-utils';
import { NumberUtils } from '../../util/number-utils';
import { ListUtils } from '../../util/list-utils';

import { DeliveryStoreFormValue, SkuDeliveryFormValue } from '../../component/derivery-store/interface/delivery-store-interface';

import { JunpcCodmst } from '../../model/junpc-codmst';
import { Delivery } from '../../model/delivery';
import { DeliveryDetail } from '../../model/delivery-detail';
import { DeliveryPlan } from '../../model/delivery-plan';
import { DeliveryPlanDetail } from '../../model/delivery-plan-detail';
import { Order } from '../../model/order';
import { ObjectUtils } from 'src/app/util/object-utils';
import { DeliveryOrderSkuFormValue } from 'src/app/interface/delivery-order-sku-form-value';

import { SkuService } from './sku.service';
import { ColorSize } from 'src/app/model/color-size';
import { DeliverySku } from 'src/app/model/delivery-sku';
import { Purchase } from 'src/app/component/purchase/interface/purchase';
import { FormArray, FormGroup, AbstractControl } from '@angular/forms';
import { DeliveryStoreSku } from 'src/app/model/delivery-store-sku';
import { OrderSku } from 'src/app/model/order-sku';
import { PurchaseSku } from 'src/app/component/purchase/interface/purchase-sku';
import { ListUtilsService } from './list-utils.service';
import { PurchaseService } from './purchase.service';
import { DeliveryStore } from 'src/app/model/delivery-store';
import { NumberUtilsService } from './number-utils.service';

const Moment = Moment_;

/**
 * 納品依頼画面ビジネスロジックサービス.
 */
@Injectable({
  providedIn: 'root'
})
export class DeliveryService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private skuService: SkuService,
    private listUtils: ListUtilsService,
    private numberUtils: NumberUtilsService,
    private purchaseService: PurchaseService
  ) { }

  /**
   * 課別入力画面の納品数量の入力値を合計して返す.
   * @param allDeliveryAllocationList 全配分課リスト
   * @param noDeliveryOrderSkuValues 非分配先_納品場所(撮影、縫製)発注skuFormArrayのvalue
   * @param deliveryOrderSkuValues 分配先_納品場所(撮影、縫製)発注skuFormArrayのvalue
   */
  sumDivisionScreenInputLot(allDeliveryAllocationList: JunpcCodmst[],
    noDeliveryOrderSkuValues: DeliveryOrderSkuFormValue[], deliveryOrderSkuValues: DeliveryOrderSkuFormValue[]): number {
    let sumLot = 0;
    allDeliveryAllocationList.forEach(allocation => sumLot += this.sumInputLotByDivision(
      allocation.code1, noDeliveryOrderSkuValues, deliveryOrderSkuValues));
    return sumLot;
  }

  /**
   * 指定した配分課の納品数量の入力値を合計して返す。
   * @param allocationTblCode1 配分課テーブルcode1
   * @param noDeliveryOrderSkuValues 非分配先_納品場所(撮影、縫製)発注skuFormArrayのvalue
   * @param deliveryOrderSkuValues 分配先_納品場所(撮影、縫製)発注skuFormArrayのvalue
   * @returns 配分課別の合計
   */
  sumInputLotByDivision(allocationTblCode1: string,
    noDeliveryOrderSkuValues: DeliveryOrderSkuFormValue[], deliveryOrderSkuValues: DeliveryOrderSkuFormValue[]): number {
    const divisionCode = allocationTblCode1.substring(2, 4);  // 課コード(配分課テーブルcode1の末尾2桁)
    let orderSkuFormValues = deliveryOrderSkuValues;
    if (divisionCode === AllocationCode.PHOTO || divisionCode === AllocationCode.SEWING) {
      orderSkuFormValues = noDeliveryOrderSkuValues;
    }

    let lot = 0;
    orderSkuFormValues.forEach(orderSku => {
      orderSku.deliverySkus.forEach(deliverySkus => {
        if (deliverySkus.divisionCode === divisionCode) {
          lot += Number(deliverySkus.deliveryLot ? deliverySkus.deliveryLot : 0);
        }
      });
    });
    return lot;
  }

  /**
   * 指定した店舗の納品数量の入力値を合計して返す.
   * @param storeCode 店舗コード
   * @param deliveryStoreValues 店舗FormArrayのvalue
   * @returns 店舗の納品数量合計
   */
  sumInputLotByStore(storeCode: string, deliveryStoreValues: DeliveryStoreFormValue[]): number {
    const totalLot = deliveryStoreValues.find(store => store.storeCode === storeCode)
      .deliveryStoreSkus.reduce((total, { deliveryLot }) => total + NumberUtils.defaultZero(deliveryLot), 0);
    return totalLot;
  }

  /**
   * 指定したSKUの納品数量の入力値を合計して返す.
   * @param colorCode カラーコード
   * @param size サイズ
   * @param deliveryStoreValues 店舗FormArrayのvalue
   * @returns SKUの納品数量合計
   */
  sumInputLotBySku(colorCode: string, size: string, deliveryStoreValues: DeliveryStoreFormValue[]): number {
    let totalLot = 0;
    deliveryStoreValues.forEach(store => {
      store.deliveryStoreSkus.some(storeSku => {
        if (storeSku.colorCode === colorCode && storeSku.size === size) {
          const deliveryLot = storeSku.deliveryLot;
          totalLot += NumberUtils.defaultZero(deliveryLot);
          return true;
        }
      });
    });
    return totalLot;
  }

  /**
   * 全ての過去納品リストから納品数を全て合計して返す。
   * 仕入が確定している(納品明細の入荷フラグがtrue)場合は入荷数量を足す。
   * 未確定の場合は納品依頼数を足す。
   * @param deliveryHistoryList 過去納品リスト
   * @returns 納品数量合計
   */
  sumLotFromHistoryList(deliveryHistoryList: Delivery[]): number {
    if (deliveryHistoryList == null) {
      return 0;
    }

    let allLot = 0;
    deliveryHistoryList.forEach(history =>
      history.deliveryDetails.forEach(detail =>
        detail.deliverySkus.forEach(sku => allLot += (detail.arrivalFlg ? sku.arrivalLot : sku.deliveryLot))));
    return allLot;
  }

  // PRD_0044 del SIT start
  ///**
  // * SQロック判定
  // * 訂正ボタンの非活性判定に使用する。
  // * 以下のいずれかの条件に合致する場合は訂正不可となる。
  // * ・SQロックユーザとログインユーザが一致
  // */
  //isSQLock(accountName: string, sqLockUserAccountName: string): boolean {
  //  // SQロックユーザチェック
  //  return StringUtils.isNotEmpty(sqLockUserAccountName) && accountName !== sqLockUserAccountName;
  //}
  // PRD_0044 del SIT end

  /**
   * formから指定した課コードに対応する納期を返す.
   * @param divisionCode 課コード
   * @param deliveryAt　Formの納期
   * @returns 納期(yyyy/MM/dd)
   */
  getDeliveryAtByDivisionCode(divisionCode: string,
    deliveryAt: { photoDeliveryAt: NgbDateStruct, sewingDeliveryAt: NgbDateStruct, deliveryAt: NgbDateStruct }): string {
    let target: NgbDateStruct;
    switch (divisionCode) {
      case AllocationCode.PHOTO:  // 本社撮影
        target = deliveryAt.photoDeliveryAt;
        break;
      case AllocationCode.SEWING: // 縫製検品
        target = deliveryAt.sewingDeliveryAt;
        break;
      default: // 製品
        target = deliveryAt.deliveryAt;
        break;
    }
    return this.ngbDateParserFormatter.format(target).replace(/-/g, '/');
  }

  /**
   * 納品明細情報から本社撮影、縫製検品、その他の課のデータの納期(修正納期)を抽出する.
   * @param deliveryDetails 納品明細情報
   * @return photoDataDeliveryAt,sewingDataDeliveryAt,divisionDataDeliveryAt
   */
  extractDeliveryAtFromDeliveryDetails(deliveryDetails: DeliveryDetail[]): {
    photoDataDeliveryAt: NgbDateStruct, sewingDataDeliveryAt: NgbDateStruct, divisionDataDeliveryAt: NgbDateStruct
  } {
    const photoData = deliveryDetails.find(deliveryDetail => deliveryDetail.divisionCode === AllocationCode.PHOTO);
    const sewingData = deliveryDetails.find(deliveryDetail => deliveryDetail.divisionCode === AllocationCode.SEWING);
    const divisionData = deliveryDetails.find(deliveryDetail =>
      deliveryDetail.divisionCode !== AllocationCode.PHOTO && deliveryDetail.divisionCode !== AllocationCode.SEWING);

    return {
      photoDataDeliveryAt: this.parseToNgbDateStruct(photoData),
      sewingDataDeliveryAt: this.parseToNgbDateStruct(sewingData),
      divisionDataDeliveryAt: this.parseToNgbDateStruct(divisionData)
    };
  }

  /**
   * NgbDateStructへ変換する.
   * @param data 取得データ
   * @returns NgbDateStruct
   */
  private parseToNgbDateStruct(data: DeliveryDetail): NgbDateStruct {
    return data != null ? this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(data.correctionAt).replace(/\//g, '-')) : null;
  }

  /**
   * 過去納品数と今回の納品得意先の入力数を合計する.
   * @param fValDeliveryStores 納品得意先リストのform値
   * @param deliveryHistoryList 過去納品リスト
   * @param deliveryId 納品ID
   * @returns 過去納品数と今回の入力数合計
   */
  sumAllDeliveryStoreLot(fValDeliveryStores: DeliveryStoreFormValue[], deliveryHistoryList: Delivery[], deliveryId: number): number {
    // 今回入力された納品数量取得
    const sumCurrentInputLot = fValDeliveryStores
      .reduce((acc, val) => acc + val.deliveryStoreSkus
        .reduce((acc2, val2) => acc2 + NumberUtils.defaultZero(val2.deliveryLot), 0), 0);

    // 納品依頼済数を取得(過去納品数).ただし編集中の納品依頼は除く
    const excludedHistoryList = deliveryHistoryList.filter(deliveryHistory =>
      deliveryHistory.id !== NumberUtils.toInteger(deliveryId));
    const historyLot = this.sumLotFromHistoryList(excludedHistoryList);

    return historyLot + sumCurrentInputLot;
  }

  /**
   * 閾値超過チェック.
   * @param input 入力値
   * @param base 比較対象
   * @param threshold 閾値
   * @returns true: 入力値が閾値比率超過
   */
  isThresholdRateOver(input: number, base: number, threshold: number): boolean {
    return input > (base * threshold + base);
  }

  /**
   * string変換した納期3種を返す.
   * @param formValue フォーム値
   * @returns string変換した納期3種
   */
  getFormattedDeliveryAt(formValue: { photoDeliveryAt: NgbDateStruct, sewingDeliveryAt: NgbDateStruct, deliveryAt: NgbDateStruct })
    : { photoDeliveryAt: string, sewingDeliveryAt: string, deliveryAt: string } {
    return {
      photoDeliveryAt: this.ngbDateParserFormatter.format(formValue.photoDeliveryAt).replace(/-/g, '/'),
      sewingDeliveryAt: this.ngbDateParserFormatter.format(formValue.sewingDeliveryAt).replace(/-/g, '/'),
      deliveryAt: this.ngbDateParserFormatter.format(formValue.deliveryAt).replace(/-/g, '/')
    };
  }

  /**
   * 発注時納期より遅れる納期が存在するか
   * @param productDeliveryAt 製品修正納期
   * @param formValue フォーム値
   * @return true:遅れる納期あり/false:遅れる納期なし
   */
  isDeliveryAtLate(productDeliveryAt: Date, formValue: {
    photoDeliveryAt: NgbDateStruct, sewingDeliveryAt: NgbDateStruct, deliveryAt: NgbDateStruct
  }): boolean {
    const formattedDeliveryAt = this.getFormattedDeliveryAt(formValue);
    return Moment(formattedDeliveryAt.photoDeliveryAt).isAfter(productDeliveryAt)
      || Moment(formattedDeliveryAt.sewingDeliveryAt).isAfter(productDeliveryAt)
      || Moment(formattedDeliveryAt.deliveryAt).isAfter(productDeliveryAt);
  }

  /**
   * 納品履歴リストから指定したカラーサイズの納品数を合計して返す.
   * 仕入が確定している(納品明細の入荷フラグがtrue)場合は入荷数量を足す.
   * 未確定の場合は納品依頼数を足す.
   * @param colorCode カラーコード
   * @param size サイズ
   * @param deliveryHistoryList 納品履歴リスト
   * @returns 過去納品数合計、入荷数量と過去納品数混合の合計
   */
  sumLotFromHistoryListBySku(colorCode: string, size: string, deliveryHistoryList: Delivery[])
    : { deliveryHistoryLotSum: number, deliveryHistoryLotMixArrivalLotSum: number } {
    let deliveryHistoryLotSum = 0;       // 過去納品数合計
    let deliveryHistoryLotMixArrivalLotSum = 0; // 入荷数量と過去納品数混合の合計
    deliveryHistoryList.forEach(deliveryHistory =>
      deliveryHistory.deliveryDetails.forEach(deliveryDetail => {
        const target = deliveryDetail.deliverySkus.find(deliverySku =>
          deliverySku.colorCode === colorCode && deliverySku.size === size && NumberUtils.isNumber(deliverySku.deliveryLot));
        if (target != null) {
          deliveryHistoryLotSum += target.deliveryLot;
          deliveryHistoryLotMixArrivalLotSum += (deliveryDetail.arrivalFlg ? target.arrivalLot : target.deliveryLot);
        }
      }));
    return { deliveryHistoryLotSum: deliveryHistoryLotSum, deliveryHistoryLotMixArrivalLotSum: deliveryHistoryLotMixArrivalLotSum };
  }

  /**
   * formの納期3種のうち、最大の日付を取得する.
   * 日付型のデータが1件もない場合はnullを返す.
   * @param formValue フォーム値
   * @return 最も大きい日付(moment型)
   */
  private extractMaxDeliveryAt(formValue: {
    photoDeliveryAt: NgbDateStruct, sewingDeliveryAt: NgbDateStruct, deliveryAt: NgbDateStruct
  }): any {
    const format = this.getFormattedDeliveryAt(formValue);
    const deliveryAtArray: string[] = [];
    const photoDeliveryAt = format.photoDeliveryAt;
    const sewingDeliveryAt = format.sewingDeliveryAt;
    const deliveryAt = format.deliveryAt;
    const regex = 'undefined';

    // 空、または形式変換エラーは除外
    if (StringUtils.isNotEmpty(photoDeliveryAt) && !photoDeliveryAt.match(regex)) { deliveryAtArray.push(photoDeliveryAt); }
    if (StringUtils.isNotEmpty(sewingDeliveryAt) && !sewingDeliveryAt.match(regex)) { deliveryAtArray.push(sewingDeliveryAt); }
    if (StringUtils.isNotEmpty(deliveryAt) && !deliveryAt.match(regex)) { deliveryAtArray.push(deliveryAt); }

    if (deliveryAtArray.length === 0) { return null; }

    return Moment.max(deliveryAtArray.map(d => Moment(d)));
  }

  /**
   * 指定されたskuのうち、納期3種の中で最大日以内の納品予定数量合計を返す.
   * skuの指定がなければ納期最大日以内の納品予定数全て合計する.
   * 日付変換可能な納期が1つもなければnullを返す.
   * @param formValue フォーム値
   * @param deliveryPlan 納品予定
   * @param sku カラー・サイズ
   * @return 納品予定数量合計
   */
  extractDeliveryPlanLotWitinMaxDeliveryAt(formValue: {
    photoDeliveryAt: NgbDateStruct, sewingDeliveryAt: NgbDateStruct, deliveryAt: NgbDateStruct
  }, deliveryPlan: DeliveryPlan, sku: { colorCode: string, size: string }): number {
    let deliveryPlanLot = 0;
    // 納品予定情報未取得の場合は0を返す.
    if (deliveryPlan == null) { return deliveryPlanLot; }

    // 日付変換可能な納期の入力がない場合はnullを返す.
    const maxDate = this.extractMaxDeliveryAt(formValue);
    if (maxDate == null) { return null; }

    deliveryPlan.deliveryPlanDetails.filter(deliveryPlanDetail =>
      // 納品予定明細から指定日以下抽出※日付未登録は除外
      deliveryPlanDetail.deliveryPlanAt != null && Moment(deliveryPlanDetail.deliveryPlanAt).isSameOrBefore(maxDate))
      .forEach(deliveryPlanDetail =>
        deliveryPlanDetail.deliveryPlanSkus.filter(deliveryPlanSku =>
          // sku指定されていたらskuで抽出、指定なければ全て
          sku == null || (deliveryPlanSku.colorCode === sku.colorCode && deliveryPlanSku.size === sku.size))
          .forEach(deliveryPlanSku => deliveryPlanLot += deliveryPlanSku.deliveryPlanLot)
      );
    return deliveryPlanLot;
  }

  /**
   * 納品予定数を全て合計する.
   * @param deliveryPlanDetails 納品予定明細情報リスト
   * @param isOnlyRegisteredDate true:日付が決まっている明細のみ/false:全ての明細
   * @returns 納品予定数合計
   */
  private sumPlanLot(deliveryPlanDetails: DeliveryPlanDetail[], isOnlyRegisteredDate: boolean): number {
    let sumPlanLot = 0;
    deliveryPlanDetails
      .filter(deliveryPlanDetail => !isOnlyRegisteredDate || deliveryPlanDetail.deliveryPlanAt != null)
      .forEach(deliveryPlanDetail =>
        deliveryPlanDetail.deliveryPlanSkus.forEach(deliveryPlanSku => sumPlanLot += deliveryPlanSku.deliveryPlanLot)
      );
    return sumPlanLot;
  }

  /**
   * 集計部分の計算を行う.
   * @param deliveryPlanData 納品予定情報
   * @returns 納品済数,納品残数,増減産数,増減産率
   */
  calculateAggregate(deliveryPlanData: DeliveryPlan, orderData: Order, deliveryHistoryList: Delivery[]): {
    allDeliveredLot: number, remainingLot: number, changesInLot: number, changesInLotRatio: string
  } {
    // 納品済数
    const allDeliveredLot = this.sumLotFromHistoryList(deliveryHistoryList)
      - orderData.orderSkus.reduce((total, { returnLot }) => total += returnLot, 0);
    // 納品残数
    const remainingLot = (deliveryPlanData == null ? 0 : this.sumPlanLot(deliveryPlanData.deliveryPlanDetails, true))
      - allDeliveredLot;
    // 増減産数
    const changesInLot = (deliveryPlanData == null ? 0 : this.sumPlanLot(deliveryPlanData.deliveryPlanDetails, false))
      - orderData.quantity;
    // 増減産率※小数2桁まで(四捨五入)。発注数量0はありえない
    const changesInLotRatio = ((changesInLot / orderData.quantity) * 100).toFixed(2);

    return {
      allDeliveredLot: allDeliveredLot,
      remainingLot: remainingLot,
      changesInLot: changesInLot,
      changesInLotRatio: changesInLotRatio
    };
  }

  /**
   * 納品数量をSKU別にまとめる.
   * @param sku 処理中のsku
   * @returns 納品数量をSKU別にまとめた納品SKUリスト
   */
  groupLotBySku(sku: { colorCode: string, size: string, deliveryLot: number }[]): { keyString: string; totalLot: number; }[] {
    // SKUごとに納品数量をまとめる
    const skuMap = sku.reduce((map, { colorCode, size, deliveryLot }) => {
      const keyString = JSON.stringify({ colorCode, size });  // 2項目をキーとして設定する為string化
      const prevLotSum = NumberUtils.defaultZero(map.get(keyString));
      return map.set(keyString, prevLotSum + NumberUtils.defaultZero(deliveryLot));
    }, new Map<string, number>());

    return Array.from(skuMap, ([keyString, totalLot]) => ({ keyString, totalLot }));
  }

  /**
   * 入荷数量をSKU別にまとめる.
   * @param sku 処理中のsku
   * @returns 入荷数量をSKU別にまとめた納品SKUリスト
   */
  groupArrivalLotBySku(sku: { colorCode: string, size: string, arrivalLot: number }[]): { keyString: string; totalLot: number; }[] {

    // SKUごとに入荷数をまとめる
    const skuMap = sku.reduce((map, { colorCode, size, arrivalLot }) => {
      const keyString = JSON.stringify({ colorCode, size });  // 2項目をキーとして設定する為string化
      const prevLotSum = NumberUtils.defaultZero(map.get(keyString));
      return map.set(keyString, prevLotSum + NumberUtils.defaultZero(arrivalLot));
    }, new Map<string, number>());

    return Array.from(skuMap, ([keyString, totalLot]) => ({ keyString, totalLot }));
  }

  /**
   * 納品数量と配分数を比較する.
   * @param storeValues 店舗フォーム値リスト
   * @param skuValues カラー・サイズ・配分数リスト
   * @returns 比較結果
   *  1つでも超過あり：CompareResult.Over
   *  超過なしで、1つでも不足あり：CompareResult.Less
   *  超過も不足もなし：CompareResult.Equal
   */
  compareLotToDistribution(storeValues: DeliveryStoreFormValue[], skuValues: SkuDeliveryFormValue[]): CompareResult {
    const groupingSkus = this.generateGroupingSkus(storeValues);

    // 配分数超過するSKUが存在する
    if (this.existsOverDistribution(groupingSkus, skuValues)) {
      return CompareResult.Over;
    }

    // 配分数に不足するSKUが存在する
    if (this.existsLessDistribution(groupingSkus, skuValues)) {
      return CompareResult.Less;
    }

    // 過不足なし
    return CompareResult.Equal;
  }
  /**
   * @param storeValues 店舗フォーム値リスト
   * @param skuValues カラー・サイズ・配分数リスト
   * @returns true:配分数に不足する納品数量あり
   */
  isLotLessThanDistribution(storeValues: DeliveryStoreFormValue[], skuValues: SkuDeliveryFormValue[]): boolean {
    const groupingSkus = this.generateGroupingSkus(storeValues);

    // 配分数に不足するSKUが存在するか
    return this.existsLessDistribution(groupingSkus, skuValues);
  }

  /**
   * SKUごとの納品数量を作成する.
   * @param storeValue 店舗フォーム値
   * @returns SKUごとの納品数量
   */
  private generateGroupingSkus(storeValues: DeliveryStoreFormValue[]): { colorCode: string; size: string; deliveryLot: number; }[] {
    const allStoreSkus = storeValues
      .filter(storeValue => storeValue.deliveryStoreSkus.some(sku => NumberUtils.isNumber(sku.deliveryLot)))
      .reduce((list, { deliveryStoreSkus }) =>
        [...list, ...deliveryStoreSkus], [] as { colorCode: string, size: string, deliveryLot: number }[]);
    const skuMapArray = this.groupLotBySku(allStoreSkus);
    return skuMapArray
      .map(data => {
        const sku: { colorCode: string, size: string } = JSON.parse(data.keyString);
        return { colorCode: sku.colorCode, size: sku.size, deliveryLot: data.totalLot };
      });
  }

  /**
   * 配分数を超過するSKUが存在するか.
   * @param groupingSkus SKUごとの納品数量
   * @param skuValues カラー・サイズ・配分数リスト
   * @returns true:存在する
   */
  private existsOverDistribution(groupingSkus: { colorCode: string; size: string, deliveryLot: number }[],
    skuValues: SkuDeliveryFormValue[]): boolean {
    return groupingSkus.some(gSku => {
      const target = skuValues.find(sku => sku.colorCode === gSku.colorCode && sku.size === gSku.size);
      return gSku.deliveryLot > (ObjectUtils.isNullOrUndefined(target) ? 0 : target.distribution);
    });
  }

  /**
   * 配分数に不足するSKUが存在するか.
   * @param groupingSkus SKUごとの納品数量
   * @param skuValues カラー・サイズ・配分数リスト
   * @returns true:存在する
   */
  private existsLessDistribution(groupingSkus: { colorCode: string; size: string, deliveryLot: number }[],
    skuValues: SkuDeliveryFormValue[]): boolean {
    if (ListUtils.isEmpty(groupingSkus) && ListUtils.isNotEmpty(skuValues)) {
      return true;  // groupingSkusなし、skuValueあり：不足
    }

    return groupingSkus.some(gSku => {
      const target = skuValues.find(sku => sku.colorCode === gSku.colorCode && sku.size === gSku.size);
      return gSku.deliveryLot < (target == null ? 0 : target.distribution);
    });
  }

  /**
   * @param deliveryDetails 納品明細リスト
   * @param colorCode カラーコード
   * @param size サイズ
   * @returns 指定したSKUの納品得意先数量合計
   */
  totalStoreSkuLot(deliveryDetails: DeliveryDetail[], colorCode: string, size: string): number {
    if (ObjectUtils.isNullOrUndefined(deliveryDetails)) { return 0; }
    return deliveryDetails.reduce((allTotal, { deliveryStores }) =>
      allTotal += deliveryStores.reduce((storeTotal, { deliveryStoreSkus }) => {
        const target = deliveryStoreSkus.find(sku => sku.size === size && sku.colorCode === colorCode);
        return storeTotal += (ObjectUtils.isNullOrUndefined(target) ? 0 : target.deliveryLot);
      }, 0)
      , 0);
  }

  /**
   * @param deliveryDetails 納品明細リスト
   * @returns 重複除去したカラーサイズリスト
   */
  distinctColorSize(deliveryDetails: DeliveryDetail[]): ColorSize[] {
    return deliveryDetails.reduce((acc, { deliverySkus }, idx) => {
      const filteredSkus = deliverySkus.filter(sku => !acc.some(accVal => this.skuService.isMatchSku(sku, accVal)));
      return idx === 0 ? filteredSkus : acc.concat(filteredSkus);
    }, [{ colorCode: '', colorName: '', size: '' }]);
  }

  /**
   * @param colorSizeList カラー・サイズリスト
   * @param column ソート対象のカラム
   * @returns 昇順ソート
   */
  sortByAsc(colorSizeList: ColorSize[], column: 'colorCode' | 'size' = 'colorCode'): ColorSize[] {
    const copyList = [...colorSizeList];  // deep copy
    return copyList.sort((val1, val2) => (val1[column] < val2[column]) ? -1 : 1);
  }

  /**
   * @param deliverySkus 納品SKUリスト
   * @param sku SKU
   * @returns true:納品SKUリストから指定したSKU.
   */
  findSku(deliverySkus: DeliverySku[], sku: ColorSize): DeliverySku {
    return deliverySkus.find(ds => this.skuService.isMatchSku(ds, sku));
  }

  /**
   * @param deliveryData 納品情報
   * @param divisionCode 課コード
   * @returns 配分率ID
   */
  findDistributionRatioId(deliveryData: Delivery, divisionCode: string): number | null {
    if (deliveryData == null || deliveryData.deliveryDetails == null) {
      return null;
    }

    const target = deliveryData.deliveryDetails.find(deliveryDetail => deliveryDetail.divisionCode === divisionCode);
    return target == null ? null : target.distributionRatioId;
  }

  /**
   * @param deliverySku 納品SKU
   * @param sku SKU
   * @param divisionCode 課コード
   */
  isMatchDivisionCodeAndSku(deliverySku: DeliverySku, sku: ColorSize, divisionCode: string): boolean {
    return deliverySku.colorCode === sku.colorCode
      && deliverySku.size === sku.size
      && deliverySku.divisionCode === divisionCode;
  }

  /**
   * @param purchase 仕入情報
   * @param sku SKU
   * @param divisionCode 課コード
   * @param 仕入SKUの入荷数量
   */
  extractPurchaseSkuArrivalCount(purchase: Purchase, sku: ColorSize, divisionCode: string): number {
    if (purchase == null) {
      return null;
    }

    let arrivalCount = null;
    purchase.purchaseSkus
      .filter(s => this.skuService.isMatchSku(s, sku))
      .some(s => {
        const division = s.purchaseDivisions.find(d => d.divisionCode === divisionCode);
        if (division != null) {
          arrivalCount = division.arrivalCount;
          return true;
        }
      });
    return arrivalCount;
  }

  /**
   * @param mainForm FormGroup
   * @param path Path
   * @returns true:引数の入荷数量超過エラーあり
   */
  isDeliveryLotMaxError(mainForm: FormGroup, path: Path): boolean {
    if (path !== Path.CORRECT) {
      return false;
    }
    return this.isDeliveryLotErrorInArg('max', mainForm.controls.deliveryOrderSkuFormArray as FormArray)
      || this.isDeliveryLotErrorInArg('max', mainForm.controls.noDeliveryOrderSkuFormArray as FormArray);
  }

  /**
   * @param mainForm FormGroup
   * @returns true:引数の入荷数量超過エラーあり
   */
  isDeliveryLotPatternError(mainForm: FormGroup): boolean {
    return this.isDeliveryLotErrorInArg('pattern', mainForm.controls.deliveryOrderSkuFormArray as FormArray)
      || this.isDeliveryLotErrorInArg('pattern', mainForm.controls.noDeliveryOrderSkuFormArray as FormArray);
  }

  /**
   * @param type エラー項目
   * @param formArray FormArray
   * @returns true:引数のformArrayに指定したエラーあり
   */
  private isDeliveryLotErrorInArg(type: string, formArray: FormArray): boolean {
    return ((formArray.controls) as FormGroup[])
      .some(f => (((f.controls.deliverySkus as FormArray).controls) as FormGroup[])
        .some(ff => {
          const errors = ff.controls.deliveryLot.errors;
          return errors != null && errors[type];
        }));
  }

  /**
   * @param detail 納品明細
   * @returns 納品数量合計
   */
  calculateDivisionTotalLot = (detail: DeliveryDetail): number =>
    detail.deliverySkus.reduce((acc, { deliveryLot }) => acc += deliveryLot, 0)

  /**
   * @param details 納品明細リスト
   * @param sku 処理中のSKU
   * @returns 納品数量合計
   */
  calculateSkuTotalLot = (details: DeliveryDetail[]) => (sku: ColorSize): number =>
    details.reduce((acc, { deliverySkus }) => {
      const target = deliverySkus.find(ss => this.skuService.isMatchSku(ss, sku));
      return acc += target == null ? 0 : target.deliveryLot;
    }, 0)

  /**
   * @param delivery 納品情報
   * @return true:入荷済
   */
  isArrived(delivery: Delivery): boolean {
    return delivery.deliveryDetails.some(dd => true === dd.arrivalFlg);
  }

  /**
   * @param purchase 仕入情報
   * @param orderSkus 発注SKU情報
   * @param deliveryDetails 納品明細情報リスト
   * @return true:仕入数を超えた得意先納品数量合計の課・SKUあり
   */
  isOverPurchase(purchase: Purchase, orderSkus: OrderSku[], deliveryDetails: DeliveryDetail[]): boolean {
    return orderSkus.some(sku => {
      const targetPSkus = this.purchaseService.filterBySku(purchase, sku);
      return this.listUtils.isNotEmpty(targetPSkus)
        // 処理中の課・SKUの仕入数 < 得意先納品数量があればtrue
        && deliveryDetails.some(dd => {
          const purchaseLot = this.sumPurchaseSkusFixArrivalCountByDivision(targetPSkus, dd.divisionCode);
          const storeLot = this.sumStoresDeliveryLotBySku(dd.deliveryStores, sku);
          return purchaseLot < storeLot;
        });
    });
  }

  /**
   * @param purchase 仕入情報
   * @param mainForm フォーム
   * @return true:仕入数を超えた得意先納品数量(フォーム入力値)合計の課・SKUあり
   */
  isFormValueUnmatchPurchase(purchase: Purchase, mainForm: FormGroup): boolean {
    return mainForm.getRawValue().skuFormArray.some((sku: ColorSize) => {
      const targetPSkus = this.purchaseService.filterBySku(purchase, sku);
      const targetStores = this.filterStoreBySku(mainForm.getRawValue().deliveryStores, sku);
      return this.listUtils.isNotEmpty(targetPSkus)
        && this.listUtils.isNotEmpty(targetStores)
        // 処理中の課・SKUの仕入数 < 得意先納品数量があればtrue
        && targetStores.some(ds => {
          const purchaseLot = this.sumPurchaseSkusFixArrivalCountByDivision(targetPSkus, ds.divisionCode);
          const storeLot = this.sumStoresDeliveryLotByDivision(targetStores, ds.divisionCode);
          return purchaseLot !== storeLot;
        });
    });
  }

  /**
   * @param deliveryStores 得意先情報リスト
   * @param sku 処理中のSKU
   * @returns 指定したSKUでフィルターした得意先情報リスト
   */
  private filterStoreBySku(deliveryStores: DeliveryStoreFormValue[], sku: ColorSize): DeliveryStoreFormValue[] {
    return deliveryStores.map(ds =>
      Object.assign({...ds}, {deliveryStoreSkus: ds.deliveryStoreSkus.filter(dss => this.skuService.isMatchSku(dss, sku))})
    );
  }

  /**
   * @param deliveryStores 納品得意先フォームリスト値
   * @param divisionCode 課コード
   * @return 指定した課コードの納品数量合計
   */
  private sumStoresDeliveryLotByDivision(deliveryStores: DeliveryStoreFormValue[], divisionCode: string): number {
    return deliveryStores
      .filter(ds => ds.divisionCode === divisionCode)
      .reduce((acc, { deliveryStoreSkus }) =>
        acc += deliveryStoreSkus.reduce((dssAcc, cur) => dssAcc += this.numberUtils.defaultZero(cur.deliveryLot), 0)
        , 0);
  }

  /**
   * @param deliveryStores 納品得意先情報リスト
   * @param sku 処理中のSKU
   * @returns 指定したSKUの納品数量合計
   */
  private sumStoresDeliveryLotBySku(deliveryStores: DeliveryStore[], sku: ColorSize): number {
    return deliveryStores
      .reduce((acc, { deliveryStoreSkus }) =>
        acc += deliveryStoreSkus
          .filter(dSku => this.skuService.isMatchSku(sku, dSku))
          .reduce((dSkuAcc, cur) => dSkuAcc += this.numberUtils.defaultZero(cur.deliveryLot), 0)
        , 0);
  }

  /**
   * @param fValDeliveryStores 納品得意先フォーム値リスト
   * @param sku 処理中のSKU
   * @returns 指定したSKUの納品数量合計
   */
  sumStoresFValDeliveryLotBySku(fValDeliveryStores: DeliveryStoreFormValue[], sku: ColorSize): number {
    return fValDeliveryStores.reduce((acc, { deliveryStoreSkus }) => {
      const target = deliveryStoreSkus.find(ds => this.skuService.isMatchSku(ds, sku));
      return acc += target == null ? 0 : target.deliveryLot;
    }, 0);
  }

  /**
   * @param ds 得意先フォーム
   * @returns 納品数量合計
   */
  sumStoresFormDeliveryLot(ds: AbstractControl): number {
    return ds.get('deliveryStoreSkus').value.reduce((acc, { deliveryLot }) => acc += this.numberUtils.defaultZero(deliveryLot), 0);
  }

  /**
   * @param purchaseSkus 仕入SKUリスト
   * @param divisionCode 処理中の課コード
   * @returns 指定したの課コードの仕入確定数合計
   */
  private sumPurchaseSkusFixArrivalCountByDivision(purchaseSkus: PurchaseSku[], divisionCode: string): number {
    return purchaseSkus
      .reduce((acc, cur) =>
        acc += cur.purchaseDivisions
          .filter(pDiv => pDiv.divisionCode === divisionCode)
          .reduce((acc2, cur2) => acc2 += this.numberUtils.defaultZero(cur2.fixArrivalCount), 0)
    , 0);
  }

  /**
   * @param purchaseSkus 仕入SKUリスト
   * @param divisionCode 処理中の課コード
   * @returns 指定したの課コードの仕入確定数合計
   */
  totalPurchaseSkusFixArrivalLot(purchaseSkus: PurchaseSku[], colorCode: string, size: string): number {
    if (ObjectUtils.isNullOrUndefined(purchaseSkus)) { return 0; }
    return purchaseSkus
    .filter(pDiv => pDiv.colorCode === colorCode && pDiv.size === size)
    .reduce((acc, cur) =>
      acc += cur.purchaseDivisions
        .reduce((acc2, cur2) => acc2 += this.numberUtils.defaultZero(cur2.fixArrivalCount), 0)
  , 0);
  }

  /**
   * 納品SKUリストから、指定したSKUの納品数量を返す.
   * @param deliverySkus 納品SKUリスト
   * @param colorCode カラー
   * @param size サイズ
   * @returns 納品数量
   */
  findDeliveryLotBySku(deliverySkus: DeliverySku[] | DeliveryStoreSku[], colorCode: string, size: string) {
    const target = deliverySkus.find(sku => sku.colorCode === colorCode && sku.size === size);
    return target != null ? target.deliveryLot : 0;
  }

  /**
   * @param totalStoreSkuLot 処理中のSKUの納品得意先の納品数量合計
   * @param deliveryDetails 納品明細情報リスト
   * @param sku 処理中のSKU
   * @returns true:指定したSKUの得意先合計数量が課別の合計数量と合致しない
   */
  isNotMatchLotBetweenDivisionAndStore(totalStoreSkuLot: number, deliveryDetails: DeliveryDetail[], sku: ColorSize): boolean {
    if (this.listUtils.isEmpty(deliveryDetails)) {
      return false; // 課別未登録
    }
    return totalStoreSkuLot !== deliveryDetails.reduce((total, { deliverySkus }) =>
      total += this.findDeliveryLotBySku(deliverySkus, sku.colorCode, sku.size), 0);
  }

  /**
   * @param delivery 納品情報
   * @returns true:ゼロ確
   */
  isZeroFix(delivery: Delivery): boolean {
    if (delivery == null) {
      return false;
    }

    const filtered = delivery.deliveryDetails.filter(dd => true === dd.arrivalFlg);
    return this.listUtils.isNotEmpty(filtered)
      && filtered.some(dd => 0 === this.sumArrivaLot(dd));
  }

  /**
   * @param deliveryDetail 納品明細
   * @returns 入荷数量合計
   */
  private sumArrivaLot(deliveryDetail: DeliveryDetail): number {
    return deliveryDetail.deliverySkus
      .reduce((acc, { arrivalLot }) => acc += this.numberUtils.defaultZero(arrivalLot), 0);
  }
}
