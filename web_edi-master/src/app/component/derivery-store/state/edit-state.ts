import { FormGroup, AbstractControl, FormArray } from '@angular/forms';

import { Observable } from 'rxjs';

import { DeliveryStorePathState } from './delivery-store-path-state';

import { EditDistribute } from '../bo/edit-distribute';
import { EditOrViewBaseRecordGet } from '../bo/edit-or-view-base-record-get';

import { Path, DeliveryApprovalStatus, AuthType, AllocationCode, Const } from '../../../const/const';

import { CompareResult } from '../../../enum/compare-result.enum';

import { ListUtils } from '../../../util/list-utils';
import { NumberUtils } from '../../../util/number-utils';
import { FormUtils } from '../../../util/form-utils';


import { DeliveryDetail } from '../../../model/delivery-detail';
import { Delivery } from '../../../model/delivery';

import {
  DeliveryStoreFormValue, DeliveryStoreSkuFormValue, BaseDataOfDeliveryStoreScreen, SkuDeliveryFormValue
} from '../interface/delivery-store-interface';
import { Purchase } from '../../purchase/interface/purchase';

import { DeliveryService } from '../../../service/bo/delivery.service';
import { StaticInjector } from '@angular/core/src/di/injector';
// PRD_0044 del SIT start
//import { SpecialtyQubeService } from 'src/app/service/specialty-qube.service';
// PRD_0044 del SIT end


/**
 * pathがEDITの時の状態クラス
 */
export class EditState implements DeliveryStorePathState {
  private editDistribute = new EditDistribute();

  constructor(
    private _deliveryService: DeliveryService,
    private _editOrViewBaseRecordGet: EditOrViewBaseRecordGet
    // PRD_0044 del SIT start
    //private _specialtyQubeService: SpecialtyQubeService
    // PRD_0044 del SIT end
  ) {
  }

  getPath(): string {
    return Path.EDIT;
  }

  get deliveryService(): DeliveryService {
    return this._deliveryService;
  }

  // PRD_0044 del SIT start
  //get specialtyQubeService(): SpecialtyQubeService {
  //  return this._specialtyQubeService;
  //}
  // PRD_0044 del SIT end

  get editOrViewBaseRecordGet() {
    return this._editOrViewBaseRecordGet;
  }

  disableFormAtScreenInit(mainForm: FormGroup, deliveryDetailList: DeliveryDetail[]): void {
    // いったん全部非活性
    mainForm.disable();

    // 直送チェックボックス(全体)
    mainForm.controls.allDirectCheckbox.enable();

    // B級品活性
    mainForm.controls.nonConformingProductType.enable();
    if (mainForm.value.nonConformingProductType === true) {
      mainForm.controls.nonConformingProductUnitPrice.enable();
    }

    // 通知メール送信
    mainForm.controls.faxSend.enable();

    // 配分処理活性
    mainForm.controls.distributionRatioType.enable();

    // 納期と店舗フォームの活性
    this.enableDeliveryAtAndStore(mainForm, deliveryDetailList);
  }

  /**
   * 納期と店舗フォームを活性にする.
   * @param mainForm mainForm
   * @param deliveryDetailList DB登録済納品明細リスト
   */
  private enableDeliveryAtAndStore(mainForm: FormGroup, deliveryDetailList: DeliveryDetail[]): void {
    let filterFn;
    switch (deliveryDetailList[0].divisionCode) {
      case AllocationCode.SEWING: // 縫検を訂正する場合は、縫検を活性にする
        mainForm.controls.sewingDeliveryAt.enable();
        filterFn = (fCtrlDeliveryStore: AbstractControl): boolean => fCtrlDeliveryStore.get('divisionCode').value === AllocationCode.SEWING;
        break;
      case AllocationCode.PHOTO:  // 撮影を訂正する場合は、撮影を活性にする
        mainForm.controls.photoDeliveryAt.enable();
        filterFn = (fCtrlDeliveryStore: AbstractControl): boolean => fCtrlDeliveryStore.get('divisionCode').value === AllocationCode.PHOTO;
        break;
      default:  // 製品を訂正する場合は、製品を活性にする
        mainForm.controls.deliveryAt.enable();
        filterFn = (fCtrlDeliveryStore: AbstractControl): boolean => this.isEditTargetProduct(fCtrlDeliveryStore.value);
        break;
    }

    const enableFn = (fCtrlDeliveryStore: AbstractControl) => {
      this.enableDistributionRatio(deliveryDetailList, mainForm)(fCtrlDeliveryStore);
      this.enableSku(deliveryDetailList, mainForm.get('allDirectCheckbox').value)(fCtrlDeliveryStore);
    };
    (mainForm.get('deliveryStores') as FormArray).controls.filter(filterFn).forEach(enableFn);
  }

  /**
   * 下記の条件に合致する店舗の配分率を活性にする.
   * 率手入力選択中
   * かつ編集可能の店舗
   * かつ(全体チェックボックスOFFまたは直送フラグがtrue)
   * @param deliveryDetailList: 納品明細リスト
   * @param mainForm mainForm
   * @param fCtrlDeliveryStore 店舗フォームコントロール
   */
  private enableDistributionRatio = (deliveryDetailList: DeliveryDetail[], mainForm: FormGroup) =>
    (fCtrlDeliveryStore: AbstractControl): void => {
      const distributionRatioType = mainForm.get('distributionRatioType').value as string;
      const allDirectCheckOn = mainForm.get('allDirectCheckbox').value as boolean;
      const deliveryStoreSkuFormArray = fCtrlDeliveryStore.get('deliveryStoreSkus') as FormArray;
      const directDeliveryOk = fCtrlDeliveryStore.get('directDeliveryFlg').value as boolean;
      const divisionCode = fCtrlDeliveryStore.get('divisionCode').value as string;

      if (Const.MANUAL_INPUT_DISTRIBUTION === distributionRatioType
        && this.isEditableStore(deliveryStoreSkuFormArray, deliveryDetailList, divisionCode)
        && (!allDirectCheckOn || directDeliveryOk)) {
        fCtrlDeliveryStore.get('storeDistributionRatio').enable();
      }
    }

  /**
   * SKU登録済のSKUフォームを活性にする.
   * @param deliveryDetailList DB登録済納品明細リスト
   * @param allDirectCheck 全体直送チェック
   * @param fCtrlDeliveryStore 店舗フォームコントロール
   */
  private enableSku = (deliveryDetailList: DeliveryDetail[], allDirectCheck: boolean) =>
    (fCtrlDeliveryStore: AbstractControl): void => {
    (fCtrlDeliveryStore.get('deliveryStoreSkus') as FormArray).controls
      .filter(storeSku => this.isEnableInitSku(fCtrlDeliveryStore, deliveryDetailList, allDirectCheck, storeSku))
      .forEach(fCtrlStoreSku => fCtrlStoreSku.enable());
  }

  /**
   * 編集中の製品の場所コードと同一の製品か判定する.
   * @param storeValue 処理中の店舗
   * @param allocationCode 場所コード
   * @returns true:編集中の製品の場所コードと同一の製品
   */
  private isEditTargetProduct(storeValue: DeliveryStoreFormValue): boolean {
    const divisionCode = storeValue.divisionCode;
    return divisionCode !== AllocationCode.SEWING
      && divisionCode !== AllocationCode.PHOTO
      && storeValue.allocationCode === storeValue.allocationCode;
  }

  /**
   * 初期設定時にSKU活性化可能か判定
   * @param fCtrlDeliveryStore 店舗フォームコントロール
   * @param deliveryDetailList DB登録済納品明細リスト
   * @param allDirectCheck 全体直送チェック
   * @param storeSku 店舗SKUフォームコントロール
   */
  private isEnableInitSku(fCtrlDeliveryStore: AbstractControl, deliveryDetailList: DeliveryDetail[],
    allDirectCheck: boolean, storeSku: AbstractControl): boolean {
    return this.isRegisteredSku(deliveryDetailList, storeSku.value, fCtrlDeliveryStore.get('divisionCode').value)
      && (!allDirectCheck || fCtrlDeliveryStore.get('directDeliveryFlg').value === true);
  }

  /**
   * t_delivery_skuに登録されているか判定.
   * @param deliveryDetailList DB登録済納品明細リスト
   * @param storeSkuVal 得意先SKU
   * @param divisionCode 課コード
   * @returns true:t_delivery_skuに登録されている
   */
  private isRegisteredSku(deliveryDetailList: DeliveryDetail[], storeSkuVal: DeliveryStoreSkuFormValue, divisionCode: string): boolean {
    return deliveryDetailList.filter(dd => dd.divisionCode === divisionCode)
      .some(deliveryDetail => deliveryDetail.deliverySkus
        .some(sku => sku.size === storeSkuVal.size && sku.colorCode === storeSkuVal.colorCode));
  }

  isDeliveryDataInValid(delivery: Delivery): string {
    if (delivery == null) {
      // 納品依頼が取得できなかった場合、エラー
      return 'NO_DELIVERY_FOR_UPDATE';
    }

    if (delivery.deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT) {
      // 納品依頼が承認済の場合、エラー
      return 'ALREADY_APPROVE';
    }

    return null;
  }

  distribute(mainForm: FormGroup, deliveryDetails: DeliveryDetail[]): void {


    // 課毎に処理
    deliveryDetails.forEach(deliveryDetail => {
      const skus = deliveryDetail.deliverySkus.map(sku => {
        return {
          distribution: deliveryDetail.arrivalFlg ? sku.arrivalLot : sku.deliveryLot,
          size: sku.size,
          colorCode: sku.colorCode
        };
      });

      this.editDistribute.distributeSku(mainForm, skus, deliveryDetail.divisionCode);
    });
  }

  isEditableStore(deliveryStoreSkuFormArray: FormArray, deliveryDetailList: DeliveryDetail[], divisionCode: string): boolean {
    const filteredStoreSkus = deliveryStoreSkuFormArray.value.filter((storeSku: DeliveryStoreSkuFormValue) =>
      this.isRegisteredSku(deliveryDetailList, storeSku, divisionCode));
    return ListUtils.isNotEmpty(filteredStoreSkus);
  }

  //PRD_0123 #7054 JFE mod start
  // getBaseRecord(orderId: number, deliveryId: number): Observable<BaseDataOfDeliveryStoreScreen> {
  //   return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId, Path.EDIT);
  // }
  getBaseRecord(orderId: number, deliveryId: number,id: number): Observable<BaseDataOfDeliveryStoreScreen> {
    return this.editOrViewBaseRecordGet.getBaseRecord(orderId, deliveryId,id, Path.EDIT);
  }
  //PRD_0123 #7054 JFE mod end
  showDeleteBtn(existsRegistedDeliveryStore: boolean): boolean {
    return existsRegistedDeliveryStore; // 得意先登録済であれば表示する
  }

  showRegistBtn(): boolean {
    return false;  // 表示しない
  }

  showUpdateBtn(): boolean {
    return true; // 表示する
  }

  showCorrectBtn(): boolean {
    return false;  // 表示しない
  }

  showCorrectSaveBtn(): boolean {
    return false;  // 表示しない
  }

  showApproveBtn(existsRegistedDeliveryStore: boolean, affiliation: AuthType): boolean {
    // 得意先登録済で社内権限であれば表示する
    return existsRegistedDeliveryStore && affiliation === AuthType.AUTH_INTERNAL;
  }

  notMatchLotToDistribution(mainForm: FormGroup, deliveryDetails: DeliveryDetail[], _: Purchase): boolean {
    // 店舗別のフォーム値を課別にまとめて課ごとに精査
    return this.groupingByDivision(mainForm.getRawValue().deliveryStores).some(grouped => {
      const distributionsBySku = this.generateDistributionsBySku(deliveryDetails, grouped);
      // 不一致が1つでもあれば終了
      return CompareResult.Equal !== this.deliveryService.compareLotToDistribution(grouped.stores, distributionsBySku);
    });
  }

  /**
   * 店舗フォーム値リストを課別にまとめる.
   * ※SKUIDがある、または数量入力がある課のみ
   * @param storeValues 店舗フォームの値
   * @returns 課別にまとめた店舗フォーム値リスト
   */
  private groupingByDivision(storeValues: DeliveryStoreFormValue[]): { divisionCode: string, stores: DeliveryStoreFormValue[] }[] {
    const divisionMap = storeValues
      .filter(storeValue => storeValue.deliveryStoreSkus.some(sku => FormUtils.isNotEmpty(sku.id) || NumberUtils.isNumber(sku.deliveryLot)))
      .reduce((map, store) => {
        const priviousList = map.get(store.divisionCode);
        return map.set(store.divisionCode, priviousList == null ? [store] : [...priviousList, store]);
      }, new Map<string, DeliveryStoreFormValue[]>());
    return Array.from(divisionMap, ([divisionCode, stores]) => ({ divisionCode, stores }));
  }

  /**
   * SKUごとの配分数リストを返す.
   * @param deliveryDetails DB登録済の納品明細リスト
   * @param groupedByDivision 課別にまとめた店舗フォーム値
   * @return SKUごとの配分数リスト
   */
  private generateDistributionsBySku(deliveryDetails: DeliveryDetail[],
    groupedByDivision: { divisionCode: string, stores: DeliveryStoreFormValue[] }): SkuDeliveryFormValue[] {
    const targetSkus = deliveryDetails.find(dd => dd.divisionCode === groupedByDivision.divisionCode).deliverySkus;
    const skuMapArray = this.deliveryService.groupLotBySku(targetSkus);
    return targetSkus.map(sku => {
      const colorCode = sku.colorCode;
      const size = sku.size;
      const keyString = JSON.stringify({ colorCode, size });
      return {
        colorCode: colorCode,
        size: size,
        distribution: skuMapArray.find(arr => arr.keyString === keyString).totalLot // 配分数は登録済SKUの合計
      } as SkuDeliveryFormValue;
    });
  }

  setUpDynamicDisable(mainForm: FormGroup, deliveryDetailList: DeliveryDetail[]): void {
    const stores = (mainForm.get('deliveryStores') as FormArray).controls;

    // 現在の値
    const val = mainForm.getRawValue();
    let latestAllDirectCheckbox = val.allDirectCheckbox;
    let latestDistributionRatioType = val.distributionRatioType;

    // 全体直送チェックボックスが変更された場合
    mainForm.controls.allDirectCheckbox.valueChanges.subscribe((allDirectCheck: boolean) => {
      latestAllDirectCheckbox = allDirectCheck;
      this.setDisableAtAllDirect(allDirectCheck, latestDistributionRatioType, stores, deliveryDetailList);
    });

    // 配分率区分が変更された場合
    mainForm.controls.distributionRatioType.valueChanges.subscribe((distributionRatioType: string) => {
      latestDistributionRatioType = distributionRatioType;
      this.setDisableAtAistributionRatioType(distributionRatioType, latestAllDirectCheckbox, stores);
    });
  }

  /**
   * 全体直送チェック変更時の店舗ごとの配分率及び納品数量活性制御.
   * 全体直送チェックが
   *  - onの場合、directDeliveryFlg === falseの店舗の配分率と納品数量を非活性にする
   *  - offの場合、isEditableStore === trueの店舗の配分率と納品数量を活性にする
   *    ※配分率は配分率区分に率手入力が選択されている場合のみ
   *    ※納品数量はt_delivery_skuに登録されているSKUのみ
   * @param allDirectCheck 変更された全体直送チェック状態
   * @param latestDistributionRatioType 現在の配分率区分の値
   * @param stores 店舗フォームコントロールリスト
   * @param deliveryDetailList 納品明細リスト
   */
  private setDisableAtAllDirect(allDirectCheck: boolean, latestDistributionRatioType: string,
    stores: AbstractControl[], deliveryDetailList: DeliveryDetail[]): void {
    if (allDirectCheck) {
      stores
        .filter(store => store.get('directDeliveryFlg').value === false)
        .forEach(store => {
          store.get('storeDistributionRatio').disable();
          store.get('deliveryStoreSkus')['controls'].forEach((storeSku: FormGroup) => storeSku.controls.deliveryLot.disable());
        });
      return;
    }

    stores
      .filter(store => store.get('isEditableStore').value === true)
      .forEach(store => {
        this.setDeliveryLotEnable(store, deliveryDetailList);
        if (Const.MANUAL_INPUT_DISTRIBUTION === latestDistributionRatioType) {
          store.get('storeDistributionRatio').enable();
        }
      });
  }

  /**
   * t_delivery_skuに登録されているSKUの納品数量を活性にする.
   * @param store 店舗フォームコントロール
   * @param deliveryDetailList 納品明細リスト
   */
  private setDeliveryLotEnable(store: AbstractControl, deliveryDetailList: DeliveryDetail[]): void {
    const skuCtrls = (store.get('deliveryStoreSkus') as FormArray).controls;
    skuCtrls
      .filter(storeSku => {
        const value = storeSku.value;
        return this.isRegisteredSku(deliveryDetailList, value, value.divisionCode);
      })
      .forEach((storeSku: FormGroup) => storeSku.controls.deliveryLot.enable());
  }

  /**
   * 配分率区分変更時の店舗ごとの配分率活性制御.
   * 配分率区分が
   *  - 率手入力の場合、活性にする※canEnableStorDeliveryLot関数の条件に合致するもののみ
   *  - 率手入力以外の場合、非活性にする
   * @param distributionRatioType 変更された配分率区分
   * @param latestAllDirectCheck 現在の全体直送チェック状態
   * @param stores 店舗フォームコントロールリスト
   */
  private setDisableAtAistributionRatioType(distributionRatioType: string, latestAllDirectCheck: boolean, stores: AbstractControl[]): void {
    if (Const.MANUAL_INPUT_DISTRIBUTION === distributionRatioType) {
      stores
        .filter(store => this.canEnableStorDeliveryLot(latestAllDirectCheck, store))
        .forEach(store => store.get('storeDistributionRatio').enable());
      return;
    }

    stores.forEach(store => store.get('storeDistributionRatio').disable());
  }

  /**
   * 店舗ごとの納品数量が活性化可能か判定
   * 店舗はisEditableStore===trueでなければ問答無用で不可
   * isEditableStore===trueの場合、全体直送チェックがoffなら可能。
   * 全体直送チェックがonの場合は、店舗はdirectDeliveryFlg===trueでなければ不可
   * @param allDirectCheck 全体直送チェック
   * @param store 店舗のフォームコントロール
   * @return true：店舗活性化可能
   */
  private canEnableStorDeliveryLot(allDirectCheck: boolean, store: AbstractControl): boolean {
    return store.get('isEditableStore').value === true
      && (!allDirectCheck || store.get('directDeliveryFlg').value === true);
  }
}
