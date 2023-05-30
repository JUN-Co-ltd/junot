import { FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';
// PRD_0044 mod SIT start
//import { map, flatMap } from 'rxjs/operators';
import { map } from 'rxjs/operators';
// PRD_0044 mod SIT end

import { EditState } from './edit-state';

import { Path, DeliveryApprovalStatus } from '../../../const/const';

import { BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

import { Delivery } from '../../../model/delivery';
// PRD_0044 del SIT start
//import { SpecialtyQubeRequest } from '../../../model/specialty-qube-request';
//import { SpecialtyQubeCancelResponse } from '../../../model/specialty-qube-cancel-response';
// PRD_0044 del SIT end
import { DeliveryDetail } from 'src/app/model/delivery-detail';

import { Purchase } from '../../purchase/interface/purchase';

/**
 * pathがCORRECTの時の状態クラス
 */
export class CorrectState extends EditState {

  getPath(): string {
    return Path.CORRECT;
  }

  //PRD_0123 #7054 JFE mod start
  // getBaseRecord(orderId: number, deliveryId: number): Observable<BaseDataOfDeliveryStoreScreen> {
    getBaseRecord(orderId: number, deliveryId: number,id: number): Observable<BaseDataOfDeliveryStoreScreen> {
  //PRD_0123 #7054 JFE mod end
    // PRD_0044 mod SIT start
    //return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId, Path.CORRECT).pipe(
    //  map((data: BaseDataOfDeliveryStoreScreen) =>
    //    this.getSqLock(data.delivery).pipe(map(sqRes => {
    //      data.specialtyQubeCancelResponse = sqRes;
    //      return data;
    //    }))
    //  ),
    //  flatMap(data => data)
    //);
    //PRD_0123 #7054 JFE mod start
    // return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId, Path.CORRECT);
    return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId,id, Path.CORRECT);
    //PRD_0123 #7054 JFE mod end
    // PRD_0044 mod SIT end
  }

  // PRD_0044 del SIT start
  ///**
  // * SQロック取得.
  // * @param delivery 納品依頼情報
  // * @returns Observable<SpecialtyQubeCancelResponse>
  // */
  //private getSqLock(delivery: Delivery): Observable<SpecialtyQubeCancelResponse> {
  //  return this.specialtyQubeService.cancelSq({ deliveryId: delivery.id } as SpecialtyQubeRequest).pipe(
  //    map(sqLockResponse => {
  //      console.debug('getSqLock:', sqLockResponse);
  //      return sqLockResponse;
  //    }));
  //}
  // PRD_0044 del SIT end

  isDeliveryDataInValid(delivery: Delivery): string {
    if (delivery == null) { // 納品依頼が取得できなかった場合、エラー
      return '400_D_15';
    }

    if (delivery.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT) { // 納品依頼が未承認の場合、エラー
      return '400_D_16';
    }

    if (delivery.deliveryDetails.some(dd => true === dd.shippingInstructionsFlg)) { // 配分出荷指示済の場合、エラー
      return '400_D_27';
    }

    if (this.deliveryService.isZeroFix(delivery)) { // ゼロ確の場合、エラー
      return '400_D_29';
    }

    return null;
  }

  showDeleteBtn(): boolean {
    return false; // 表示しない
  }

  showUpdateBtn(): boolean {
    return false; // 表示しない
  }

  showCorrectBtn(): boolean {
    return false; // 表示しない
  }

  showCorrectSaveBtn(): boolean {
    return true; // 表示する
  }

  showApproveBtn(): boolean {
    return false; // 表示しない
  }

  notMatchLotToDistribution(mainForm: FormGroup, deliveryDetails: DeliveryDetail[], purchase: Purchase): boolean {

    // 入荷済の場合は仕入との比較
    if (deliveryDetails.some(dd => true === dd.arrivalFlg)) {
      return this.deliveryService.isFormValueUnmatchPurchase(purchase, mainForm);
    }

    // 未入荷の場合は課別配分との比較
    return super.notMatchLotToDistribution(mainForm, deliveryDetails, purchase);
  }
}
