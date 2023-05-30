import { FormGroup, FormArray } from '@angular/forms';

import { Observable } from 'rxjs';

import { AuthType } from '../../../const/const';

import { BaseDataOfDeliveryStoreScreen } from '../interface/delivery-store-interface';

import { Delivery } from '../../../model/delivery';
import { DeliveryDetail } from '../../../model/delivery-detail';
import { Purchase } from '../../purchase/interface/purchase';

/**
 * 納品得意先画面のPathの状態インターフェース.
 */
export interface DeliveryStorePathState {

  /**
   * 返すPathを定義する.
   * @returns path
   */
  getPath(): string;

  /**
   * 画面表示時のForm非活性制御を定義する.
   * @param mainForm フォーム
   * @param deliveryDetailList DB登録済納品明細リスト
   */
  disableFormAtScreenInit(mainForm: FormGroup, deliveryDetailList: DeliveryDetail[]): void;

  /**
   * 画面表示時に取得した納品依頼データが正しいかチェックし、エラーの場合はエラーコードを返す.
   * @param delivery 納品依頼データ
   * @returns エラーコード
   */
  isDeliveryDataInValid(delivery: Delivery): string;

  /**
   * 配分処理.
   * @param mainForm メインフォーム
   * @param deliveryDetails 納品明細リスト
   */
  distribute(mainForm: FormGroup, deliveryDetails: DeliveryDetail[]): void;

  /**
   * 編集可能な店舗か判定する.
   * @param deliveryStoreSkuFormArray 納品得意先SKUのFormArray
   * @param deliveryDetailList 納品明細リスト
   * @param divisionCode 課コード
   * @returns true:編集可能
   */
  isEditableStore(deliveryStoreSkuFormArray: FormArray, deliveryDetailList: DeliveryDetail[], divisionCode: string): boolean;

  /**
   * 画面で必要なレコードを取得する.
   * @param orderId 発注ID
   * @param deliveryId 納品ID
   * @returns BaseDataOfDeliveryStoreScreen
   */
  //PRD_0123 #7054 JFE mod start
  // getBaseRecord(orderId: number, deliveryId: number): Observable<BaseDataOfDeliveryStoreScreen>;
  getBaseRecord(orderId: number, deliveryId: number,id: number): Observable<BaseDataOfDeliveryStoreScreen>;
  //PRD_0123 #7054 JFE mod end
  /**
   * 削除ボタン表示判定.
   * @param existsRegistedDeliveryStore 得意先情報登録済
   * @param affiliation　ログインユーザ権限
   * @param deliveryDetailList 納品明細リスト
   * @returns true:表示
   */
  showDeleteBtn(existsRegistedDeliveryStore: boolean, affiliation: AuthType, deliveryDetailList: DeliveryDetail[]): boolean;

  /**
   * 登録ボタン表示判定.
   * @returns true:表示
   */
  showRegistBtn(): boolean;

  /**
   * 更新ボタン表示判定.
   * @returns true:表示
   */
  showUpdateBtn(): boolean;

  /**
   * 訂正ボタン表示判定.
   * @param affiliation　ログインユーザ権限
   * @param deliveryDetailList 納品明細リスト
   * @returns true:表示
   */
  showCorrectBtn(affiliation: AuthType, deliveryDetailList: DeliveryDetail[]): boolean;

  /**
   * 訂正保存ボタン表示判定.
   * @returns true:表示
   */
  showCorrectSaveBtn(): boolean;

  /**
   * 承認ボタン表示判定.
   * @param existsRegistedDeliveryStore 得意先情報登録済
   * @param affiliation　ログインユーザ権限
   * @returns true:表示
   */
  showApproveBtn(existsRegistedDeliveryStore: boolean, affiliation: AuthType): boolean;

  /**
   * 納品数量と配分数を比較する.
   * @param mainForm mainForm
   * @param deliveryDetails DB登録済納品明細リスト
   * @param purchase 仕入情報
   * @returns true:編集・訂正画面で過不足あり、または新規画面で不足あり
   */
  notMatchLotToDistribution(mainForm: FormGroup, deliveryDetails: DeliveryDetail[], purchase: Purchase): boolean;

  /**
   * 動的非活性の設定.
   * @param mainForm メインフォーム
   * @param deliveryDetailList 納品明細リスト
   */
  setUpDynamicDisable(mainForm: FormGroup, deliveryDetailList: DeliveryDetail[]): void;
}
