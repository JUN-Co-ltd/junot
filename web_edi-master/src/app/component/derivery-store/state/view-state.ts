import { FormGroup } from '@angular/forms';

import { Observable } from 'rxjs';

import { DeliveryStorePathState } from './delivery-store-path-state';

import { EditOrViewBaseRecordGet } from '../bo/edit-or-view-base-record-get';

import { Path, AuthType } from '../../../const/const';

import { BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

import { Delivery } from '../../../model/delivery';
import { DeliveryDetail } from 'src/app/model/delivery-detail';
import { DeliveryService } from 'src/app/service/bo/delivery.service';

/**
 * pathがVIEWの時の状態クラス
 */
export class ViewState implements DeliveryStorePathState {

  constructor(
    private deliveryService: DeliveryService,
    private editOrViewBaseRecordGet: EditOrViewBaseRecordGet
  ) {
  }

  getPath(): string {
    return Path.VIEW;
  }

  disableFormAtScreenInit(mainForm: FormGroup): void {
    mainForm.disable();
  }

  isDeliveryDataInValid(delivery: Delivery): string {
    if (delivery == null) {
      // 納品依頼が取得できなかった場合、エラー
      return 'NO_DELIVERY_FOR_VIEW';
    }

    if (this.deliveryService.isZeroFix(delivery)) { // ゼロ確の場合、エラー
      return '400_D_29';
    }

    return null;
  }

  distribute(): void {
    // 処理なし
  }

  isEditableStore(): boolean {
    return false;  // 編集不可
  }

    //PRD_0123 #7054 JFE mod start
  // getBaseRecord(orderId: number, deliveryId: number): Observable<BaseDataOfDeliveryStoreScreen> {
  //   return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId, Path.VIEW);
  // }
  getBaseRecord(orderId: number, deliveryId: number,id: number): Observable<BaseDataOfDeliveryStoreScreen> {
    return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId,id, Path.VIEW);
  }
    //PRD_0123 #7054 JFE mod end

  showDeleteBtn(existsRegistedDeliveryStore: boolean, affiliation: AuthType, deliveryDetailList: DeliveryDetail[]): boolean {
    // 配分出荷未指示
    // かつ得意先登録済で社内の場合表示する
    return !deliveryDetailList.some(dd => true === dd.shippingInstructionsFlg)
      && existsRegistedDeliveryStore
      && AuthType.AUTH_INTERNAL === affiliation;
  }

  showRegistBtn(): boolean {
    return false;  // 表示しない
  }

  showUpdateBtn(): boolean {
    return false; // 表示しない
  }

  showCorrectBtn(affiliation: AuthType, deliveryDetailList: DeliveryDetail[]): boolean {
    // 配分出荷未指示
    // かつ社内の場合、訂正ボタンを表示
    return !deliveryDetailList.some(dd => true === dd.shippingInstructionsFlg)
      && AuthType.AUTH_INTERNAL === affiliation;
  }

  showCorrectSaveBtn(): boolean {
    return false;  // 表示しない
  }

  showApproveBtn(): boolean {
    return false; // 表示しない
  }

  notMatchLotToDistribution(): boolean {
    return false;  // 呼ばれない
  }

  setUpDynamicDisable(): void {
    // 何もしない
  }
}
