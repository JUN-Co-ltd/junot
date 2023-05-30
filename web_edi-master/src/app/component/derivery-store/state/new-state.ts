import { FormGroup, FormArray } from '@angular/forms';

import { Observable } from 'rxjs';

import { DeliveryStorePathState } from './delivery-store-path-state';

import { NewDistribute } from '../bo/new-distribute';
import { NewBaseRecordGet } from '../bo/new-base-record-get';

import { Path, Const } from '../../../const/const';

// PRD_0044 del SIT start
// 使用していないのでコメントアウト
//import { CompareResult } from '../../../enum/compare-result.enum';
// PRD_0044 del SIT end

import { DeliveryService } from '../../../service/bo/delivery.service';

import { SkuDeliveryFormValue, BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

/**
 * pathがNEWの時の状態クラス
 */
export class NewState implements DeliveryStorePathState {
  private newDistribute = new NewDistribute();

  constructor(
    private deliveryService: DeliveryService,
    private newBaseRecordGet: NewBaseRecordGet
  ) {
  }

  getPath(): string {
    return Path.NEW;
  }

  disableFormAtScreenInit(mainForm: FormGroup): void {
    // 初期表示は率手入力未選択なので配分率非活性
    (mainForm.get('deliveryStores') as FormArray).controls
      .forEach(store => store.get('storeDistributionRatio').disable());
    //PRD_0123 #7054 JFE mod start
    //店舗配分時、新規の場合でも納品先選択は非活性
    mainForm.controls.deliveryLocationCode.disable();
    //PRD_0123 #7054 JFE mod end
  }

  isDeliveryDataInValid(): string {
    return null; // チェックなし
  }

  distribute(mainForm: FormGroup): void {
    const skus: SkuDeliveryFormValue[] = mainForm.getRawValue().skuFormArray;
    this.newDistribute.distributeSku(mainForm, skus);
  }

  isEditableStore(): boolean {
    return true;  // 編集可能
  }

  //PRD_0123 #7054 JFE mod start
  // getBaseRecord(orderId: number, deliveryId: number,): Observable<BaseDataOfDeliveryStoreScreen> {
  //   return this.newBaseRecordGet.getBaseRecord(orderId, deliveryId);
  // }
  getBaseRecord(orderId: number, deliveryId: number,id: number): Observable<BaseDataOfDeliveryStoreScreen> {
    return this.newBaseRecordGet.getBaseRecord(orderId, deliveryId,id);
  }
  //PRD_0123 #7054 JFE mod end

  showDeleteBtn(): boolean {
    return false; // 表示しない
  }

  showRegistBtn(): boolean {
    return true;  // 表示する
  }

  showUpdateBtn(): boolean {
    return false; // 表示しない
  }

  showCorrectBtn(): boolean {
    return false; // 表示しない
  }

  showCorrectSaveBtn(): boolean {
    return false;  // 表示しない
  }

  showApproveBtn(): boolean {
    return false; // 表示しない
  }

  notMatchLotToDistribution(mainForm: FormGroup): boolean {
    // SKUごとの全合計を比較
    const val = mainForm.getRawValue();
    return this.deliveryService.isLotLessThanDistribution(val.deliveryStores, val.skuFormArray);
  }

  setUpDynamicDisable(mainForm: FormGroup): void {
    // 配分区分の変更検出
    mainForm.controls.distributionRatioType.valueChanges
      .subscribe(val => this.setStoreDistributionRatioDisable(mainForm, val));
  }

  /**
   * 配分率の活性・非活性制御を行う.
   * 率手入力は配分率活性
   * 率手入力以外は配分率非活性
   * @param mainForm mainForm
   * @param distributionRatioType 配分区分
   */
  private setStoreDistributionRatioDisable(mainForm: FormGroup, distributionRatioType: string): void {
    const stores = (mainForm.get('deliveryStores') as FormArray).controls;
    if (Const.MANUAL_INPUT_DISTRIBUTION === distributionRatioType) {
      stores.forEach(store => store.get('storeDistributionRatio').enable());
      return;
    }
    stores.forEach(store => store.get('storeDistributionRatio').disable());
  }
}
