import { Injectable } from '@angular/core';

import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { Observable } from 'rxjs';
import { PurchaseSearchCondition } from 'src/app/model/purchase-search-condition';
import { PurchaseSearchResult } from 'src/app/model/purchase-search-result';

import { PurchaseConfirmList } from 'src/app/model/purchase-confirm-list';

import { GenericList } from '../model/generic-list';

import { JunotApiService } from './junot-api.service';
import { DateUtilsService } from './bo/date-utils.service';

import { Purchase } from '../component/purchase/interface/purchase';
import { FormUtilsService } from './bo/form-utils.service';
import { StringUtilsService } from './bo/string-utils.service';
import { FormGroup } from '@angular/forms';
import { LgSendType } from '../const/lg-send-type';
import { PurchaseDivision } from '../component/purchase/interface/purchase-division';

const BASE_URL = '/purchases';

@Injectable({
  providedIn: 'root'
})
export class PurchaseHttpService {

  constructor(
    private dateUtils: DateUtilsService,
    private formUtils: FormUtilsService,
    private stringUtils: StringUtilsService,
    private junotApiService: JunotApiService
  ) { }

  /**
   * 検索処理.
   * @param formValue 検索フォーム値
   * @returns レスポンス
   */
  search(formValue: PurchaseSearchCondition): Observable<GenericList<PurchaseSearchResult>> {
    const URL = `${ BASE_URL }/search`;
    const searchCondition = this.convertSearchParam(formValue);
    return this.junotApiService.listByPost(URL, searchCondition);
  }

  /**
   * 納品IDがキーの仕入リスト取得処理.
   * @param deliveryId 納品ID
   * @returns レスポンス
   */
  getByDeliveryId(deliveryId: number): Observable<Purchase> {
    const URL = `${ BASE_URL }/${ deliveryId }`;
    return this.junotApiService.get(URL);
  }

  /**
   * 登録処理.
   * @param form form
   * @returns レスポンス
   */
  create = (form: FormGroup) => (): Observable<Purchase> => {
    const body = this.convertRequestDataForCreate(form);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 更新処理.
   * @param form form
   * @returns レスポンス
   */
  update = (form: FormGroup) => (): Observable<Purchase> => {
    const body = this.convertRequestDataForUpdate(form);
    return this.junotApiService.update(BASE_URL, body);
  }

  /**
   * LG送信(仕入確定)
   * @param form form入力データ
   * @returns レスポンス
   */
  confirm(form: PurchaseConfirmList): Observable<PurchaseConfirmList> {
    const URL = `${ BASE_URL }/confirm`;
    const body = this.convertLgRequestData(form);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 検索パラメータ整形処理.
   * @param form form入力データ
   * @returns 整形後入力データ
   */
  private convertSearchParam(form: PurchaseSearchCondition): PurchaseSearchCondition {
    const copyItem = Object.assign({}, form); // deep copy

    // 日付変換処理
    copyItem.correctionAtFrom = this.dateUtils.toString(copyItem.correctionAtFrom as NgbDateStruct);
    copyItem.correctionAtTo = this.dateUtils.toString(copyItem.correctionAtTo as NgbDateStruct);
    copyItem.arrivalAtFrom = this.dateUtils.toString(copyItem.arrivalAtFrom as NgbDateStruct);
    copyItem.arrivalAtTo = this.dateUtils.toString(copyItem.arrivalAtTo as NgbDateStruct);
    copyItem.deliveryNumberFrom = this.stringUtils.toNullIfBlank(copyItem.deliveryNumberFrom);
    copyItem.deliveryNumberTo = this.stringUtils.toNullIfBlank(copyItem.deliveryNumberTo);
    // PRD_0021 add SIT start
    copyItem.partNo = this.stringUtils.toNullIfBlank(copyItem.partNo);
    copyItem.brandCode = this.stringUtils.toNullIfBlank(copyItem.brandCode);
    // PRD_0021 add SIT end

    return copyItem;
  }

  /**
   * 登録用データ整形処理.
   * @param form form
   * @returns 整形後入力データ
   */
  private convertRequestDataForCreate(form: FormGroup): Purchase {
    // 非活性項目なし
    const copyItem: Purchase = Object.assign({}, form.value); // deep copy

    // 日付変換処理
    copyItem.arrivalAt = this.dateUtils.toString(copyItem.arrivalAt as NgbDateStruct);

    // 納品依頼未登録の項目除去
    copyItem.purchaseSkus
      .forEach(sku => sku.purchaseDivisions = sku.purchaseDivisions
        .filter(div => this.formUtils.isNotEmpty(div.arrivalCount)));

    return copyItem;
  }

  /**
   * 更新用データ整形処理.
   * @param form form
   * @returns 整形後入力データ
   */
  private convertRequestDataForUpdate(form: FormGroup): Purchase {
    // 非活性項目あり
    const copyItem: Purchase = Object.assign({}, form.getRawValue()); // deep copy

    // 日付変換処理
    copyItem.arrivalAt = this.dateUtils.toString(copyItem.arrivalAt as NgbDateStruct);

    // LG送信指示済・納品依頼未登録除去
    const purchaseSkus = copyItem.purchaseSkus
      .filter(sku => {
        const purchaseDivisions = sku.purchaseDivisions.filter(this.isUpdateTargetPurchaseDivision);
        sku.purchaseDivisions = purchaseDivisions;
        return sku.purchaseDivisions.length > 0;
      });

    copyItem.purchaseSkus = purchaseSkus;
    return copyItem;
  }

  /**
   * @param purchaseDivision 仕入の課フォーム値
   * @returns true: 更新対象の仕入の課
   */
  private isUpdateTargetPurchaseDivision = (purchaseDivision: PurchaseDivision): boolean =>
    LgSendType.NO_INSTRUCTION === purchaseDivision.lgSendType
      && this.formUtils.isNotEmpty(purchaseDivision.arrivalCount)

  /**
   * @pram form form入力データ
   * @return LG送信リクエストデータ
   */
  private convertLgRequestData(form: PurchaseConfirmList): PurchaseConfirmList {
    // チェックがついているレコードのみ
    return { purchases: form.purchases.filter(p => p.check) };
  }
}
