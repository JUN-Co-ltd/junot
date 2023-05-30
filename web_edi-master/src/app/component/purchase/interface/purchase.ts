import { PurchaseSku } from './purchase-sku';
import { PurchaseType } from 'src/app/const/purchase-type';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { PurchaseDataType } from 'src/app/const/purchase-data-type';

export interface Purchase {
  /** データ種別. */
  dataType: PurchaseDataType;

  /** 仕入区分. */
  purchaseType: PurchaseType;

  /** 入荷場所. */
  arrivalPlace: string;

  /** 入荷店舗. */
  arrivalShop: string;

  /** 仕入先. */
  supplierCode: string;

  /** 製品工場. */
  mdfMakerFactoryCode: string;

  /** 入荷日. */
  arrivalAt: string | Date | NgbDateStruct;

  /** 計上日. */
  recordAt: string | Date;

  /** 仕入相手伝票No. */
  makerVoucherNumber: number;

  /** 仕入伝票No. */
  purchaseVoucherNumber: number;

  /** 仕入伝票行. */
  purchaseVoucherLine: number;

  /** 品番ID. */
  partNoId: number;

  /** 品番. */
  partNo: string;

  /** 仕入SKUリスト. */
  purchaseSkus: PurchaseSku[];

  /** 良品・不用品区分. */
  nonConformingProductType: boolean;

  /** 指示番号. */
  instructNumber: string;

  /** 指示番号行. */
  instructNumberLine: number;

  /** 発注ID. */
  orderId: number;

  /** 発注No. */
  orderNumber: number;

  /** 引取回数(納品明細の納品回数). */
  purchaseCount: number;

  /** 仕入単価. */
  purchaseUnitPrice: number;

  /** 納品ID. */
  deliveryId: number;

  /** 合計数量. */
  totalLot: number;
}
