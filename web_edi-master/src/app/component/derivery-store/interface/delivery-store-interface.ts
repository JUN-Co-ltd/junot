import { ScreenSettingDelivery } from 'src/app/model/screen-setting-delivery';
import { Delivery } from 'src/app/model/delivery';
import { Order } from 'src/app/model/order';
import { Item } from 'src/app/model/item';
import { DeliveryPlan } from 'src/app/model/delivery-plan';
import { SpecialtyQubeCancelResponse } from 'src/app/model/specialty-qube-cancel-response';
import { DeliverySkuFormValue } from 'src/app/interface/delivery-sku-form-value';
import { Purchase } from '../../purchase/interface/purchase';

/**
 * 納品得意先Formの入力値の型定義.
 */
export interface DeliveryStoreFormValue {
  /** 納品得意先ID */
  id: number;
  /** 納品明細ID */
  deliveryDetailId: number;
  /** 課コード */
  divisionCode: string;
  /** 物流コード */
  logisticsCode: string;
  /** 場所コード */
  allocationCode: string;
  /** 配分区分 */
  allocationType: string;
  /** (表示順が)最後の配分課 */
  lastDivisionCode: boolean;
  /** 店舗コード */
  storeCode: string;
  /** 店舗別配分率ID */
  storeDistributionRatioId: number;
  /** 店舗別配分率区分 */
  storeDistributionRatioType: string;
  /** 店舗別配分率 */
  storeDistributionRatio: number;
  /** 配分順 */
  distributionSort: number;
  /** 店舗名 */
  sname: string;
  /** 直送可否フラグ */
  directDeliveryFlg: boolean;
  /** 直送フラグ */
  direct: boolean;
  /** 納品得意先SKU情報のリスト */
  deliveryStoreSkus: DeliveryStoreSkuFormValue[];
}

/**
 * 納品得意先SKUFormの入力値の型定義.
 */
export interface DeliveryStoreSkuFormValue {
  /** id */
  id: number;
  /** 納品得意先ID */
  deliveryStoreId: number;
  /** サイズ */
  size: string;
  /** カラーコード */
  colorCode: string;
  /** 納品数量 */
  deliveryLot: number;
  /** 配分率ID */
  distributionRatioId: number;
  // PRD_0031 add SIT start
  /** 在庫数 */
  stockLot: number;
  // PRD_0031 add SIT end
  // PRD_0033 add SIT start
  /** 売上数 */
  salesScore: number;
  // PRD_0033 add SIT end
}

/**
 * 店舗別から課ごとにまとめる値の型定義.
 */
export interface GroupedDivision {
  /** キャリー区分両方あり */
  hasBothCarryType: boolean;
  /** キャリー区分 */
  carryType: string;
  /** 納品得意先フォーム値リスト */
  deliveryStores: DeliveryStoreFormValue[];
  /** 納品SKUフォーム値リスト */
  deliverySkus: DeliverySkuFormValue[];
}

/**
 * 納品明細リストへpusuする関数の引数の型定義.
 */
export interface GroupedDivisionArg {
  /** 店舗別から課ごとにまとめる値 */
  groupedDivisions: GroupedDivision[];
  /** 処理中の課の直送得意先リスト */
  workStoreDirectRecords: DeliveryStoreFormValue[];
  /** 処理中の課の直送得意先SKUリスト */
  workStoreSkuDirectRecords: DeliveryStoreSkuFormValue[];
  /** 処理中の課の通常得意先リスト */
  workStoreNormalRecords: DeliveryStoreFormValue[];
  /** 処理中の課の通常得意先SKUリスト */
  workStoreSkuNormalRecords: DeliveryStoreSkuFormValue[];
}

/**
 * 納期・納品数量エラーの型定義.
 */
export interface DeliveryAtAndLotError {
  /** 納期エラー */
  deliveryAtError: boolean;
  /** 納品数量エラー */
  lotError: boolean;
}

/**
 * 納品依頼画面のSKUFormの入力値の型定義.
 */
export interface SkuDeliveryFormValue {
  /** サイズ */
  size: string;
  /** カラーコード */
  colorCode: string;
  /** 返品数 */
  returnLot: number;
  /** 配分数 */
  distribution: number;
  /** 納品可能数 */
  deliverableLot: number;
  /** 発注数 */
  productOrderLot: number;
}

/**
 * post時の納品明細の型定義.
 */
export interface PostDeliveryDetailIf {
  /** 納品明細id */
  id: number;
  /** 納品依頼日 */
  deliveryRequestAt: string;
  /** 課コード */
  divisionCode: string;
  /** 物流コード */
  logisticsCode: string;
  /** 場所コード */
  allocationCode: string;
  /** キャリータイプ2種類あり */
  hasBothCarryType: boolean;
  /** キャリー区分 */
  carryType: string;
  /** FAX送信フラグ */
  faxSend: boolean;
  /** 修正納期 */
  correctionAt: string;
  /** 配分率ID */
  distributionRatioId: number;
  /** 配分率 */
  distributionRatio: string;
  /** 納品SKUリスト */
  deliverySkus: DeliverySkuFormValue[];
  /** 納品得意先リスト */
  deliveryStores: DeliveryStoreFormValue[];
  // PRD_0123 #7054 add JFE start
  deliveryLocationCode: any;
  // PRD_0123 #7054 add JFE end
}

/**
 * 店舗配分画面の基本データ.
 */
export interface BaseDataOfDeliveryStoreScreen {
  /** 納品依頼画面基本データ */
  screenSetting: ScreenSettingDelivery;
  /** 納品情報 */
  delivery: Delivery;
  //PRD_0123 #7054 JFE add start
  /**品番情報.iD */
  id: number
  //PRD_0123 #7054 JFE add end
  /** 発注情報 */
  order: Order;
  /** 品番情報 */
  item: Item;
  /** 納品履歴 */
  deliveryHistory: Delivery[];
  /** 納品予定情報 */
  deliveryPlan: DeliveryPlan;
  /** 仕入情報 */
  purchase: Purchase;
  // PRD_0044 del SIT start
  ///** 店別配分キャンセルAPIレスポンス情報 */
  //specialtyQubeCancelResponse: SpecialtyQubeCancelResponse;
  // PRD_0044 del SIT end
}
