import {
  QualityApprovalStatus, AllCompleteStatus, CompleteStatus, RegistStatus,
  OrderApprovalStatus, FukukitaruMasterConfirmStatusType
} from '../const/const';

import { VOrder } from '../model/v-order';
import { Item } from '../model/item';
import { VDelischeOrder } from '../model/v-delische-order';
import { VDelischeDeliveryRequest } from '../model/v-delische-delivery-request';
import { VDelischeDeliverySku } from '../model/v-delische-delivery-sku';
import { Order } from '../model/order';
import { ItemMisleadingRepresentation } from '../model/item-misleading-representation';
import { ItemMisleadingRepresentationSearchResult } from '../model/item-misleading-representation-search-result';
import { FukukitaruOrder } from '../model/fukukitaru-order';
import { ObjectUtils } from './object-utils';

/**
 * ビジネスロジックチェック　ユーティリティ.
 */
export class BusinessCheckUtils {
  constructor(
  ) { }

  /**
   * 優良誤認承認済、または非対象か判定する。
   *
   * (優良誤認承認区分（組成）= 非対象 or 承認済)
   * AND
   * (優良誤認承認区分（国）= 非対象 or 承認済)
   * AND
   * (優良誤認承認区分（有害物質）= 非対象 or 承認済)
   * = true
   * @param data 発注情報(View)、品番情報、デリスケ発注、デリスケ納品依頼、デリスケ納品SKU、優良誤認一覧用品番いずれか
   * @returns 優良誤認承認済、または非対象であればtrue
   */
  static isQualityApprovalOk(data: VOrder | Item | VDelischeOrder | VDelischeDeliveryRequest
                                  | VDelischeDeliverySku | ItemMisleadingRepresentationSearchResult): boolean {
    const qComp = Number(data.qualityCompositionStatus); // 組成
    const qCoo = Number(data.qualityCooStatus);          // 国
    const qHarm = Number(data.qualityHarmfulStatus);     // 有害物質
    return (qComp === QualityApprovalStatus.NON_TARGET || qComp === QualityApprovalStatus.ACCEPT)
      && (qCoo === QualityApprovalStatus.NON_TARGET || qCoo === QualityApprovalStatus.ACCEPT)
      && (qHarm === QualityApprovalStatus.NON_TARGET || qHarm === QualityApprovalStatus.ACCEPT);
  }

  /**
   * 組成・国・有害物質全てが対象、または非対象か判定する。
   * →進捗なしの状態のこと
   *
   * (優良誤認承認区分（組成）= 対象 or 非対象)
   * AND
   * (優良誤認承認区分（国）= 対象 or 非対象)
   * AND
   * (優良誤認承認区分（有害物質）= 対象 or 非対象)
   * = true
   * @param data デリスケ発注、デリスケ納品依頼、デリスケ納品SKU、優良誤認一覧検索いずれか
   * @returns 対象、または非対象であればtrue
   */
  static isQualityApprovalNoProgress(data: VDelischeOrder | VDelischeDeliveryRequest
    | VDelischeDeliverySku | ItemMisleadingRepresentationSearchResult): boolean {
    const qComp = Number(data.qualityCompositionStatus); // 組成
    const qCoo = Number(data.qualityCooStatus);          // 国
    const qHarm = Number(data.qualityHarmfulStatus);     // 有害物質
    return (qComp === QualityApprovalStatus.TARGET || qComp === QualityApprovalStatus.NON_TARGET)
      && (qCoo === QualityApprovalStatus.TARGET || qCoo === QualityApprovalStatus.NON_TARGET)
      && (qHarm === QualityApprovalStatus.TARGET || qHarm === QualityApprovalStatus.NON_TARGET);
  }

  /**
   * 全て非対象か判定する.
   * @param data 優良誤認情報、拡張発注情報
   * @returns true:全て非対象
   */
  static isAllQualityStatusNonTarget = (data: ItemMisleadingRepresentation | VOrder): boolean =>
    QualityApprovalStatus.NON_TARGET === data.qualityCooStatus
    && QualityApprovalStatus.NON_TARGET === data.qualityCompositionStatus
    && QualityApprovalStatus.NON_TARGET === data.qualityHarmfulStatus

  /**
   * 品番確定済か判定する。
   * 登録ステータス = 品番登録
   * @param orderData 発注情報
   * @returns 品番確定済であればtrue
   */
  static isConfirmPartOk(orderData: VOrder): boolean {
    const registStatus = orderData.registStatus;
    return (registStatus === RegistStatus.PART);
  }

  /**
   * 受注確定済か判定する。
   * 発注承認ステータス = 発注承認 or MD差し戻し or 発注確定
   * @param orderData 発注情報
   * @returns 受注確定済であればtrue
   */
  static isConfirmOrderOk(orderData: VOrder | Order): boolean {
    const oas = orderData.orderApproveStatus;
    return oas != null
      && (oas === OrderApprovalStatus.ACCEPT
        || oas === OrderApprovalStatus.REJECT
        || oas === OrderApprovalStatus.CONFIRM);
  }

  /**
   * 完納判定を行う。
   * 全済区分 = 済 or 製品完納区分 = 自動完納 or 完納
   * @param orderData 発注情報
   * @returns 全済区分が済または製品完納区分が完納または自動完納の場合 true
   */
  static isCompleteOrder(orderData: VOrder): boolean {
    return (orderData.allCompletionType === AllCompleteStatus.COMPLETE
      || orderData.productCompleteOrder === CompleteStatus.AUTO_COMPLETE
      || orderData.productCompleteOrder === CompleteStatus.COMPLETE);
  }

  /**
   * 発注承認済か判定する。
   * @param orderData 発注情報
   * @returns 発注承認済であればtrue
   */
  static isApprovalOk(orderData: VOrder | Order): boolean {
    const oas = orderData.orderApproveStatus;
    return oas != null && oas === OrderApprovalStatus.ACCEPT;
  }

  /**
   * 閾値超過チェック.
   * @param input 入力値
   * @param base 比較対象
   * @param threshold 閾値
   * @returns true: 入力値が閾値比率超過
   */
  static isThresholdRateOver(input: number, base: number, threshold: number): boolean {
    return input > (base * threshold + base);
  }

  /**
   * フクキタル資材発注の責任発注表示対象か判定する。
   *
   * (優良誤認承認 非対象 or 承認済)
   * AND
   * (確定ステータス = 確定済)
   * = true
   * @param data 発注情報(View)、品番情報、デリスケ発注、デリスケ納品依頼、デリスケ納品SKUいずれか
   * @param fOrderData フクキタル資材発注
   * @returns 資材発注確定済 かつ 優良誤認未承認済であればtrue
   */
  static isShowResponsibleOrder(data: VOrder | Item | VDelischeOrder | VDelischeDeliveryRequest | VDelischeDeliverySku,
    fOrderData: FukukitaruOrder): boolean {
    if (ObjectUtils.isNullOrUndefined(data) || ObjectUtils.isNullOrUndefined(fOrderData)) {
      return false;
    }
    const isQualityApprovalOk = BusinessCheckUtils.isQualityApprovalOk(data);
    return fOrderData.confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED && !isQualityApprovalOk;
  }
}
