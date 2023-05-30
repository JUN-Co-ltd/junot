import { Injectable } from '@angular/core';
import { ObjectUtils } from '../../util/object-utils';
import { ListUtils } from '../../util/list-utils';
import { FukukitaruMasterLinkingStatusType, FukukitaruMasterConfirmStatusType, MaterialOrderStatusName } from '../../const/const';
import { FukukitaruDestination } from '../../model/fukukitaru-destination';

@Injectable({
  providedIn: 'root'
})
export class MaterialOrderService {

  constructor() { }

  /**
   * ステータスラベルの表示文言を返却
   * @returns ステータスラベル文字
   * @param confirmStatus 確定ステータス
   * @param fOrderLinkingStatus 連携ステータス
   */
  getStatusLabel(confirmStatus: FukukitaruMasterConfirmStatusType, fOrderLinkingStatus: number): string {
    const linkingStatus
      = ObjectUtils.isNotNullAndNotUndefined(fOrderLinkingStatus) // 連携ステータスがnullではない、かつundefinedではないか判定
        ? fOrderLinkingStatus                                     // そのまま設定
        : FukukitaruMasterConfirmStatusType.ORDER_NOT_CONFIRMED;  // 未確定(0)を設定

    if ((linkingStatus === FukukitaruMasterLinkingStatusType.TARGET)
      && (confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_NOT_CONFIRMED)) {
      // 連携対象(資材発注確定待ち)
      return MaterialOrderStatusName.WAITING_CONFIRM;
    }

    if ((linkingStatus === FukukitaruMasterLinkingStatusType.TARGET)
      && (confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED)) {
      // 連携対象(資材発注確定)
      return MaterialOrderStatusName.CONFIRM;
    }

    if ((linkingStatus === FukukitaruMasterLinkingStatusType.TARGET)
      && (confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_UNAPPROVED)) {
      // 連携対象(資材発注承認待ち)
      return MaterialOrderStatusName.APPROVAL_PENDING;
    }

    if ((linkingStatus === FukukitaruMasterLinkingStatusType.LINKING)
      && (confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED)) {
      // 連携中(資材発注連携中)
      return MaterialOrderStatusName.LINKING;
    }

    if ((linkingStatus === FukukitaruMasterLinkingStatusType.LINK_COMPLETE)
      && (confirmStatus === FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED)) {
      // 連携済(資材発注連携済み)
      return MaterialOrderStatusName.LINKED;
    }

    if (linkingStatus === FukukitaruMasterLinkingStatusType.NON_TARGET) {
      // 連携対象外(資材発注連携エラー)
      return MaterialOrderStatusName.LINKAGE_ERROR;
    }

    // 上記以外は異常な状態のためエラーログを出力
    console.error('想定外のステータスが設定されています。linking_status: ' + linkingStatus + ', confirm_status: ' + confirmStatus);
    // 上記以外(資材発注確定待ち)
    return MaterialOrderStatusName.WAITING_CONFIRM;
  }

  /**
   * 請求先初期値取得
   * @param listBillingAddress 請求宛先リスト
   */
  getInitBillingCompany(listBillingAddress: FukukitaruDestination[]): FukukitaruDestination {
    const billingAddressInfo = ListUtils.isNotEmpty(listBillingAddress) ? listBillingAddress[0] : null; // 先頭のレコードを取得
    if (billingAddressInfo == null) {
      // 請求宛先リストが存在しない場合はエラーログを出力
      console.error('請求先の会社IDが存在しません。m_f_brand_destination: ' + listBillingAddress);
    }
    return billingAddressInfo;
  }
}
