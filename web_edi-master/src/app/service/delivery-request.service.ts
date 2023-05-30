import * as Moment_ from 'moment';

import { Injectable } from '@angular/core';

import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

import { Observable } from 'rxjs';

import { CarryType } from '../const/const';

import { ListUtils } from '../util/list-utils';
import { NumberUtils } from '../util/number-utils';

import { JunotApiService } from './junot-api.service';
import { DeliveryService } from './bo/delivery.service';

import {
  DeliveryStoreSkuFormValue, DeliveryStoreFormValue, GroupedDivision, PostDeliveryDetailIf, GroupedDivisionArg
} from '../component/derivery-store/interface/delivery-store-interface';

import { GenericList } from '../model/generic-list';
import { Delivery } from '../model/delivery';
import { DeliveryRequestSearchConditions } from '../model/search-conditions';
import { DeliveryDetail } from '../model/delivery-detail';
import { DeliverySkuFormValue } from '../interface/delivery-sku-form-value';
// PRD_0031 add SIT start
import { DeliveryStoreUploadCsv } from '../model/delivery-store-upload-csv';
import { MdeliveryLocation } from '../model/m-delivery-location';
import { isEmpty } from 'rxjs/operators';
// PRD_0031 add SIT end

const BASE_URL = '/deliveries';
const Moment = Moment_;

@Injectable({
  providedIn: 'root'
})
export class DeliveryRequestService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private junotApiService: JunotApiService,
    private deliveryService: DeliveryService
  ) { }

  /**
   * 納品依頼情報登録処理
   * @param postItem 登録パラメータ
   */
  postDeliveryRequest(postItem: any): Observable<Delivery> {
    const body = this.convertRequestData(postItem);
    return this.junotApiService.create(BASE_URL, body);
  }

  /**
   * 納品依頼情報取得処理
   * @param id 納品ID
   * @returns レスポンス
   */
  getDeliveryRequestForId(id: number): Observable<Delivery> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.get(URL);
  }

  /**
   * 納品依頼情報削除処理
   * @param id 納品ID
   * @returns レスポンス
   */
  deleteDeliveryRequest(id: number): Observable<Delivery> {
    const URL = `${ BASE_URL }/${ id }`;
    return this.junotApiService.delete(URL);
  }

  /**
   * 納品得意先情報削除処理
   * @param id 納品ID
   * @returns レスポンス
   */
  deleteDeliveryStoreRequest(id: number): Observable<Delivery> {
    const URL = `${ BASE_URL }/store/${ id }`;
    return this.junotApiService.delete(URL);
  }

  /**
   * 納品依頼情報更新処理
   * @param postItem 更新データ
   * @returns レスポンス
   */
  putDeliveryRequest(postItem: any): Observable<Delivery> {
    const URL = `${ BASE_URL }/${ postItem.id }`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 納品依頼情報一覧取得
   * @param searchConditions 検索条件
   * @returns レスポンス
   */
  getDeliveryRequestList(searchConditions: DeliveryRequestSearchConditions): Observable<GenericList<Delivery>> {
    return this.junotApiService.list(BASE_URL, searchConditions);
  }

  // PRD_0123 #7054 add JFE start
  /**
   * 納入場所一覧取得
   * @param id 品番情報.id
   * @returns レスポンス
   */
  getDeliveryLocationList(id: number): Observable<GenericList<MdeliveryLocation>>{
    const URL = `${ BASE_URL }/locationlist/${id}`;
    return this.junotApiService.list(URL,id);
  }
  // PRD_0123 #7054 add JFE end

  /**
   * 納品依頼情報承認
   * @param postItem 更新データ
   * @returns レスポンス
   */
  approvalDeliveryRequest(postItem: any): Observable<Delivery> {
    const URL = `${ BASE_URL }/${ postItem.id }/approval`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 納品依頼情報訂正処理
   * @param postItem 更新データ
   * @returns レスポンス
   */
  correctDeliveryRequest(postItem: any): Observable<Delivery> {
    const URL = `${ BASE_URL }/${ postItem.id }/correct`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 納品依頼情報直送確定
   * @param postItem 更新データ
   * @returns レスポンス
   */
  directConfirmDeliveryRequest(postItem: any): Observable<Delivery> {
    const URL = `${ BASE_URL }/${ postItem.id }/directConfirm`;
    const body = this.convertRequestData(postItem);
    return this.junotApiService.update(URL, body);
  }

  /**
   * 画面の入力データをrequestパラメータの型に整形して返す
   * @param inputData 画面の入力データ
   * @returns 整形後データ
   */
  private convertRequestData(inputData: any): string {
    let copyItem = JSON.parse(JSON.stringify(inputData));
    copyItem = this.convertDate(copyItem);  // 日付変換処理
    return copyItem;
  }

  /**
   * 日付変換処理
   * @param copyItem 変換データ
   * @returns 変換後データ
   */
  private convertDate(copyItem: any): any {
    // 納品依頼日
    if (copyItem.deliveryRequestAt) {
      copyItem.deliveryRequestAt = this.ngbDateParserFormatter.format(copyItem.deliveryRequestAt).replace(/-/g, '/');
    }
    // 納期
    if (copyItem.deliveryAt) {
      copyItem.deliveryAt = this.ngbDateParserFormatter.format(copyItem.deliveryAt).replace(/-/g, '/');
    }
    return copyItem;
  }

  /**
   * 得意先別UPSERT用にformの値からPOSTで必要な納品明細情報リストを作成して返す.
   * 納品明細情報は課ごとに1レコード.
   * @param formValue formの値
   * @param deliveryDetails 登録済の納品明細リスト
   * @returns Submti用の納品明細情報リスト
   */
  prepareDeliveryDetailsDataForSubmit(formValue: any, deliveryDetailList: DeliveryDetail[]): PostDeliveryDetailIf[] {
    // 店舗別のフォーム値を課別・キャリー区分別にまとめる
    const groupedDivisions = this.groupingByDivisionAndCarryType(formValue);

    // submit用に値設定
    const data = this.generateDeliveryDetailsForSubmit(deliveryDetailList, groupedDivisions, formValue);
    return data;
  }

  /**
   * 店舗別のフォーム値を課別・キャリー区分(直送)別にまとめる.
   * @param formValue フォームの値
   * @returns 店舗別のフォーム値を課別・キャリー区分(直送)別にまとめた値リスト
   */
  private groupingByDivisionAndCarryType(formValue: any): GroupedDivision[] {
    const deliveryStores: DeliveryStoreFormValue[] = formValue.deliveryStores;  // 店舗フォームのvalue

    let nonPushToDeliveryDetails = false;         // 納品明細リストへの未pushフラグ
    let groupedDivisions: GroupedDivision[] = []; // 課別・キャリー区分(直送)別にまとめたリスト(納品明細リスト)

    let workDivisionCode: string = null; // 処理中の課temp
    let workStoreDirectRecords: DeliveryStoreFormValue[] = [];  // 処理中の課の直送得意先リストtemp
    let workStoreSkuDirectRecords: DeliveryStoreSkuFormValue[] = []; // 処理中の課の直送得意先SKUリストtemp
    let workStoreNormalRecords: DeliveryStoreFormValue[] = [];  // 処理中の課の通常得意先リストtemp
    let workStoreSkuNormalRecords: DeliveryStoreSkuFormValue[] = []; // 処理中の課の通常得意先SKUリストtemp

    // formの店舗ごとに処理
    deliveryStores.forEach(store => {
      // 数量 > 0 のSKUリスト取得
      const inputtedSkus: DeliveryStoreSkuFormValue[] = store.deliveryStoreSkus.filter(dss => NumberUtils.defaultZero(dss.deliveryLot) > 0);
      if (ListUtils.isEmpty(inputtedSkus)) { return; } // 入力がなければ次の店舗を処理

      // 処理している課が変わったら、納品明細リストへpushして処理中のリスト初期化
      if (workDivisionCode != null && workDivisionCode !== store.divisionCode) {
        groupedDivisions = this.pushToGroupedDivisions({
          groupedDivisions, workStoreDirectRecords, workStoreSkuDirectRecords, workStoreNormalRecords, workStoreSkuNormalRecords
        });
        nonPushToDeliveryDetails = false;
        workStoreDirectRecords = [];
        workStoreSkuDirectRecords = [];
        workStoreNormalRecords = [];
        workStoreSkuNormalRecords = [];
      }

      // 処理中の課を更新し、tempリストにpush
      workDivisionCode = store.divisionCode;
      switch (store.direct) {
        case true:  // 直送
          workStoreSkuDirectRecords = [...workStoreSkuDirectRecords, ...inputtedSkus];
          workStoreDirectRecords.push(this.generateStoreRecordForSubmit(store, inputtedSkus));
          break;
        default:  // 通常
          workStoreSkuNormalRecords = [...workStoreSkuNormalRecords, ...inputtedSkus];
          workStoreNormalRecords.push(this.generateStoreRecordForSubmit(store, inputtedSkus));
          break;
      }
      nonPushToDeliveryDetails = true;
    });

    // 未pushのレコードがある場合
    if (nonPushToDeliveryDetails) {
      groupedDivisions = this.pushToGroupedDivisions({
        groupedDivisions, workStoreDirectRecords, workStoreSkuDirectRecords, workStoreNormalRecords, workStoreSkuNormalRecords
      });
    }

    return groupedDivisions;
  }

  /**
   * 直送別に明細リストにpush.
   * @param arg
   *  groupedDivisions 店舗別から課ごとにまとめた値リスト
   *  workStoreDirectRecords 処理中の課の直送得意先リスト
   *  workStoreSkuDirectRecords 処理中の課の直送得意先SKUリスト
   *  workStoreNormalRecords 処理中の課の通常得意先リスト
   *  workStoreSkuNormalRecords 処理中の課の通常得意先SKUリスト
   * @returns 店舗別から課ごとにまとめた値リスト
   */
  private pushToGroupedDivisions(arg: GroupedDivisionArg): GroupedDivision[] {
    const hasBothCarryType = ListUtils.isNotEmpty(arg.workStoreDirectRecords) && ListUtils.isNotEmpty(arg.workStoreNormalRecords);
    const groupedDivisions: GroupedDivision[] = [];

    if (ListUtils.isNotEmpty(arg.workStoreDirectRecords)) { // 直送
      // 納品数量をSKUごとにまとめる
      const deliverySkus = this.generateGroupingLotBySku(arg.workStoreSkuDirectRecords, arg.workStoreDirectRecords[0].divisionCode);
      groupedDivisions.push({
        hasBothCarryType: hasBothCarryType, carryType: CarryType.DIRECT,
        deliveryStores: arg.workStoreDirectRecords, deliverySkus: deliverySkus
      });
    }
    if (ListUtils.isNotEmpty(arg.workStoreNormalRecords)) { // 通常
      // 納品数量をSKUごとにまとめる
      const deliverySkus = this.generateGroupingLotBySku(arg.workStoreSkuNormalRecords, arg.workStoreNormalRecords[0].divisionCode);
      groupedDivisions.push({
        hasBothCarryType: hasBothCarryType, carryType: CarryType.NORMAL,
        deliveryStores: arg.workStoreNormalRecords, deliverySkus: deliverySkus
      });
    }

    return [...arg.groupedDivisions, ...groupedDivisions];
  }

  /**
   * Submit用の納品明細レコードリスト作成.
   * @param deliveryDetailList 登録済の納品明細リスト
   * @param groupedDivisions 課別にまとめたレコードリスト
   * @param formValue formの値
   * @returns Submit用の納品明細レコードリスト
   */
  private generateDeliveryDetailsForSubmit(deliveryDetailList: DeliveryDetail[],
    groupedDivisions: GroupedDivision[], formValue: any): PostDeliveryDetailIf[] {
    return groupedDivisions.map(data => this.generateValueSettedDeliveryDetail(deliveryDetailList, formValue, data));
  }

  /**
  * 納品数量をSKU別にまとめた納品SKUリストを作成する.
    * @param workStoreSkuRecords 処理中の課の得意先SKUリストtemp
   * @param divisionCode 処理中の課
   * @returns 納品数量をSKU別にまとめた納品SKUリスト
   */
  private generateGroupingLotBySku(workStoreSkuRecords: DeliveryStoreSkuFormValue[], divisionCode: string): DeliverySkuFormValue[] {
    const skuMapArray = this.deliveryService.groupLotBySku(workStoreSkuRecords);
    return skuMapArray.map(data => {
      const sku: { colorCode: string, size: string } = JSON.parse(data.keyString);
      return {
        divisionCode: divisionCode,
        colorCode: sku.colorCode,
        size: sku.size,
        deliveryLot: data.totalLot
      } as DeliverySkuFormValue;
    });
  }

  /**
   * Submit用の店舗レコードの作成.
   * @param store DeliveryStoreFormValue
   * @param inputtedSkus DeliveryStoreSkuFormValue[]
   * @returns 店舗レコード
   */
  private generateStoreRecordForSubmit(store: DeliveryStoreFormValue, inputtedSkus: DeliveryStoreSkuFormValue[]): DeliveryStoreFormValue {
    return {
      id: store.id, // 納品得意先ID
      deliveryDetailId: store.deliveryDetailId, // 納品明細ID
      divisionCode: store.divisionCode, // 課コード
      logisticsCode: store.logisticsCode,   // 物流コード
      allocationCode: store.allocationCode, // 場所コード
      allocationType: store.allocationType, // 配分区分
      direct: store.direct, // 直送フラグ
      distributionSort: store.distributionSort, // 配分順
      storeCode: store.storeCode, // 店舗コード
      storeDistributionRatioId: store.storeDistributionRatioId, // 店舗別配分率ID
      storeDistributionRatioType: store.storeDistributionRatioType, // 店舗別配分率区分
      storeDistributionRatio: store.storeDistributionRatio, // 店舗別配分率
      deliveryStoreSkus: inputtedSkus // 納品得意先SKUリスト
    } as DeliveryStoreFormValue;
  }

  /**
   * 送信用の納品明細レコードを作成する.
   * @param deliveryDetailList 登録済納品明細リスト
   * @param formValue フォーム値
   * @param groupedDivision 処理中の課別にまとめたレコード
   * @param 送信用の納品明細レコード
   */
  private generateValueSettedDeliveryDetail(deliveryDetailList: DeliveryDetail[], formValue: any,
    groupedDivision: GroupedDivision): PostDeliveryDetailIf {
    const deliveryStoreList = groupedDivision.deliveryStores;
    const firstDeliveryStore = deliveryStoreList[0];
    const divisionCode = firstDeliveryStore.divisionCode;
    const correctionAt = this.deliveryService.getDeliveryAtByDivisionCode(divisionCode, formValue);
    const deliveryDetailId = this.extractDeliveryDetailIdByDivisionCode(
      divisionCode, groupedDivision.hasBothCarryType, groupedDivision.carryType, deliveryDetailList);
    // PRD_0123 #7054 add JFE start
    //物流コードの設定
    let logisticsCode: string = "";
    if (formValue.deliveryLocationCode !== null) {
      logisticsCode = formValue.deliveryLocationCode;
    } else {
      logisticsCode = firstDeliveryStore.logisticsCode;
    }
    // PRD_0123 #7054 add JFE end
    return {
      id: deliveryDetailId, // 納品明細id
      deliveryRequestAt: Moment(new Date()).format('YYYY/MM/DD').toString(),  // 納品依頼日
      divisionCode: divisionCode, // 課コード
      allocationCode: firstDeliveryStore.allocationCode, // 場所コード
      // PRD_0123 #7054 mod JFE start
      //logisticsCode: firstDeliveryStore.logisticsCode, // 物流コード
      logisticsCode: logisticsCode, // 物流コード
      // PRD_0123 #7054 mod JFE end
      hasBothCarryType: groupedDivision.hasBothCarryType, // キャリー区分両方あり
      carryType: firstDeliveryStore.direct ? CarryType.DIRECT : CarryType.NORMAL,  // キャリー区分
      correctionAt: correctionAt, // 修正納期
      faxSend: formValue.faxSend, // ファックス送信フラグ
      deliverySkus: groupedDivision.deliverySkus, // 納品SKUリスト
      deliveryStores: deliveryStoreList, // 納品得意先リスト
      // PRD_0123 #7054 add JFE start
      deliveryLocationCode: formValue.deliveryLocationCode //最新納品先
      // PRD_0123 #7054 add JFE end
    } as PostDeliveryDetailIf;
  }

  /**
   * 登録済の納品明細レコードから指定した課コードの納品明細IDを抽出する.
   * @param divisionCode 課コード
   * @param hasBothCarryType 直送・通常両方入力あり
   * @param carryType キャリー区分
   * @param deliveryDetailList 登録済の納品明細リスト
   * @returns 納品明細ID
   */
  private extractDeliveryDetailIdByDivisionCode(divisionCode: string, hasBothCarryType: boolean,
    carryType: string, deliveryDetailList: DeliveryDetail[]): number {
    if (ListUtils.isEmpty(deliveryDetailList)
      || (hasBothCarryType && carryType === CarryType.DIRECT)) {
      // 直送・通常両方入力ありの場合は直送は新規登録にする
      return null;
    }

    // 課の納品明細を取得
    const target = deliveryDetailList.find(dd => dd.divisionCode === divisionCode);
    return target == null ? null : target.id;
  }

  // PRD_0031 add SIT start
  /**
   * 店舗配分アップロードファイルの読み込み.
   * @param file アップロードファイル
   * @returns Observable<BulkRegistItem>
   */
  // PRD_0120#8343 mod JFE start
  // deliveryStoreUploadRead(file: File): Observable<DeliveryStoreUploadCsv[]>{
  deliveryStoreUploadRead(file: File): Observable<DeliveryStoreUploadCsv> {
  // PRD_0120#8343 mod JFE end
    const formData = new FormData();
    formData.append('file', file);
    const URL = `${ BASE_URL }/deliveryStoreUploadCsvRead`;
     return this.junotApiService.fileUpload(URL, formData);
  }
  // PRD_0031 add SIT end
}
