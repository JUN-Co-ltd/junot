import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import {
  LateType, DelischeRecordType, CompleteStatus,
  DelischeProductionStatusType, OrderApprovalStatus, Path, AuthType
} from '../../../const/const';

import { ProductionStatusModalComponent } from '../../production-status-modal/production-status-modal.component';

import { VDelischeOrder } from '../../../model/v-delische-order';
import { VDelischeDeliveryRequest } from '../../../model/v-delische-delivery-request';
import { VDelischeDeliverySku } from '../../../model/v-delische-delivery-sku';
import { BusinessCheckUtils } from '../../../util/business-check-utils';

@Component({
  selector: 'app-delische-record',
  templateUrl: './delische-record.component.html',
  styleUrls: ['./delische-record.component.scss']
})
export class DelischeRecordComponent implements OnInit {
  /** デリスケレコード区分 */
  @Input() delischeRecordType: string;
  /** 発注レコード */
  @Input() delischeOrder: VDelischeOrder;
  /** 納品依頼レコード */
  @Input() delischeDeliveryRequest: VDelischeDeliveryRequest;
  /** 納品SKUレコード */
  @Input() delischeDeliverySku: VDelischeDeliverySku;
  /** 子要素表示フラグ。変更感知しないので@Input()項目にする */
  @Input() isOpenChild: boolean;
  /** 製品完納区分 */
  @Input() private productCompleteOrder: string;
  /** 納品SKUを開閉フラグ */
  @Input() isOpeningDeliverySku: boolean;
  /** 子要素開閉ボタンを押下した時のEventEmitter */
  @Output() openOrCloseChild = new EventEmitter();

  /** 発注ID */
  orderId: number;
  /** 発注No */
  orderNumber: number;
  /** 子要素存在フラグ */
  childExists: boolean;
  /** 納品月度 */
  deliveryAtMonthly: number;
  /** MD週 */
  mdWeek: Date;
  /** ブランド */
  brandCode: string;
  /** アイテム */
  itemCode: string;
  /** 品番 */
  partNo: string;
  /** 品名 */
  productName: string;
  /** 上代合計 */
  calculateRetailPrice: number;
  /** 原価合計 */
  calculateProductCost: number;
  /** カラー */
  colorCode: string;
  /** サイズ */
  size: string;
  /** シーズン */
  season: string;
  // PRD_0146 #10776 add JFE start
  /** 費目 */
  expenseItem: string;
  /** 関連No */
  relationNumber: string;
  // PRD_0146 #10776 add JFE end
  /** メーカー */
  mdfMakerName: string;
  /** 発注日 */
  productOrderAt: Date;
  /** 生産工程 */
  productionStatus: string;
  /** 発注納期 */
  productDeliveryAt: Date;
  /** 納品日遅延数 */
  lateDeliveryAtCnt: number;
  /** 納品日遅延フラグ */
  lateDeliveryAtFlg: string;
  /** 納品日 */
  deliveryAt: Date;
  /** 発注数 */
  productOrderLot: number;
  /** 納品依頼数 */
  deliveryLot: number;
  /** 仕入実数 */
  arrivalLot: number;
  /** 売上数. */
  posSalesQuantity: number;
  /** 在庫数. */
  stockQuantity: number;
  /** 発注残 */
  orderRemainingLot: number;
  /** 上代単価 */
  retailPrice: number;
  /** 原価単価 */
  productCost: number;
  /** 原価率 */
  costRate: number;
  /** 納品依頼回数 */
  deliveryCount: number;
  /** 発注承認ステータスCSS */
  orderApproveStatusCls: string;
  /** 優良誤認CSS */
  qualityStatusCls: string;
  /** レコードCSS */
  recordCls: string;

  readonly LATE = LateType.LATE;
  readonly PROD_ST_LATE = DelischeProductionStatusType.LATE;
  readonly PROD_ST_NO_LATE = DelischeProductionStatusType.NO_LATE;
  readonly PROD_ST_NO_DATA = DelischeProductionStatusType.NO_DATA;
  readonly ORDER = DelischeRecordType.ORDER;
  readonly DERIVERY_REQUEST = DelischeRecordType.DERIVERY_REQUEST;
  readonly DERIVERY_SKU = DelischeRecordType.DERIVERY_SKU;
  readonly PATH = Path;
  readonly AUTH_TYPE = AuthType;

  private readonly CSS_BLUE = 'font-blue';
  private readonly CSS_GREEN = 'font-green';
  private readonly CSS_RED = 'font-red';
  private readonly CSS_ROW_PRODUCT_COMPLETE = 'row-product-complete';
  private readonly CSS_ROW_ORDER = 'row-order';
  private readonly CSS_ROW_DELIVERY_REQUEST = 'row-derivery-request';
  private readonly CSS_ROW_DELIVERY_SKU = 'row-derivery-sku';

  constructor(
    private modalService: NgbModal
  ) { }

  ngOnInit() {
    this.setRecordCss();
  }

  /**
   * レコードのCSSを設定する.
   */
  private setRecordCss(): void {
    let recordCls = '';
    switch (this.delischeRecordType) {
      case DelischeRecordType.ORDER:
        this.setOrderRecord(this.delischeOrder);
        recordCls = this.CSS_ROW_ORDER;
        break;
      case DelischeRecordType.DERIVERY_REQUEST:
        this.setDeliveryRequestRecord(this.delischeDeliveryRequest);
        recordCls = this.CSS_ROW_DELIVERY_REQUEST;
        break;
      case DelischeRecordType.DERIVERY_SKU:
        this.setDeliverySkuRecord(this.delischeDeliverySku);
        recordCls = this.CSS_ROW_DELIVERY_SKU;
        break;
      default:
        break;
    }

    // 完納であればレコードCSS区分に完納を設定
    this.recordCls = this.productCompleteOrder === CompleteStatus.AUTO_COMPLETE
      || this.productCompleteOrder === CompleteStatus.COMPLETE ? this.CSS_ROW_PRODUCT_COMPLETE : recordCls;
  }

  /**
   * 発注承認ステータスのCSSを設定する.
   * @param orderApproveStatus 発注承認ステータス
   */
  private setOrderApproveStatusCls(orderApproveStatus: OrderApprovalStatus): void {
    // 発注承認済
    if (orderApproveStatus === OrderApprovalStatus.ACCEPT) {
      this.orderApproveStatusCls = this.CSS_BLUE;
      return;
    }
    // 受注確定済
    if (orderApproveStatus === OrderApprovalStatus.REJECT || orderApproveStatus === OrderApprovalStatus.CONFIRM) {
      this.orderApproveStatusCls = this.CSS_GREEN;
      return;
    }
    // 未確定
    this.orderApproveStatusCls = this.CSS_RED;
  }

  /**
   * 優良誤認区分のCSSを設定する.
   * @param data レコード
   */
  private setQualityStatusCls(data: VDelischeOrder | VDelischeDeliveryRequest | VDelischeDeliverySku): void {
    // 承認済
    if (BusinessCheckUtils.isQualityApprovalOk(data)) {
      this.qualityStatusCls = this.CSS_BLUE;
      return;
    }
    // 進捗なし
    if (BusinessCheckUtils.isQualityApprovalNoProgress(data)) {
      this.qualityStatusCls = this.CSS_RED;
      return;
    }
    // 一部承認
    this.qualityStatusCls = this.CSS_GREEN;
  }

  /**
   * 発注レコードを設定する.
   * @param delischeOrder 発注レコード
   */
  private setOrderRecord(delischeOrder: VDelischeOrder): void {
    this.childExists = delischeOrder.childExists;
    this.deliveryAtMonthly = delischeOrder.productDeliveryAtMonthly;
    this.mdWeek = delischeOrder.productDeliveryAt;
    this.brandCode = delischeOrder.brandCode;
    this.itemCode = delischeOrder.itemCode;
    this.partNo = delischeOrder.partNo;
    this.productName = delischeOrder.productName;
    this.season = delischeOrder.season;
    // PRD_0146 #10776 add JFE start
    this.expenseItem = delischeOrder.expenseItem;
    this.orderNumber = delischeOrder.orderNumber;
    this.relationNumber = (delischeOrder.relationNumber != 0 && delischeOrder.relationNumber != null)
      ? delischeOrder.relationNumber.toString().padStart(6, '0')
      : delischeOrder.relationNumber != null ? delischeOrder.relationNumber.toString() : null;
    // PRD_0146 #10776 add JFE end
    this.mdfMakerName = delischeOrder.mdfMakerName;
    this.productOrderAt = delischeOrder.productOrderAt;
    this.productionStatus = delischeOrder.productionStatus;
    this.productDeliveryAt = delischeOrder.productDeliveryAt;
    this.lateDeliveryAtCnt = delischeOrder.lateDeliveryAtCnt;
    this.productOrderLot = delischeOrder.quantity;
    this.deliveryLot = delischeOrder.deliveryLotSum;
    this.arrivalLot = delischeOrder.arrivalLotSum;
    this.posSalesQuantity = delischeOrder.netSalesQuantity;
    this.stockQuantity = delischeOrder.stockQuantity;
    this.orderRemainingLot = delischeOrder.orderRemainingLot;
    this.retailPrice = delischeOrder.retailPrice;
    this.productCost = delischeOrder.productCost;
    this.calculateRetailPrice = delischeOrder.calculateRetailPrice;
    this.calculateProductCost = delischeOrder.calculateProductCost;
    this.costRate = delischeOrder.costRate;
    this.setOrderApproveStatusCls(delischeOrder.orderApproveStatus);
    this.setQualityStatusCls(delischeOrder);
    this.orderId = delischeOrder.id;
    this.orderNumber = delischeOrder.orderNumber;
  }

  /**
   * 納品依頼レコードを設定する.
   * @param delischeDeliveryRequest 納品依頼レコード
   */
  private setDeliveryRequestRecord(delischeDeliveryRequest: VDelischeDeliveryRequest): void {
    this.childExists = true;
    this.deliveryAtMonthly = delischeDeliveryRequest.deliveryAtMonthly;
    this.mdWeek = delischeDeliveryRequest.deliveryAt;
    this.brandCode = delischeDeliveryRequest.brandCode;
    this.itemCode = delischeDeliveryRequest.itemCode;
    this.partNo = delischeDeliveryRequest.partNo;
    this.productName = delischeDeliveryRequest.productName;
    this.season = delischeDeliveryRequest.season;
    this.mdfMakerName = delischeDeliveryRequest.mdfMakerName;
    this.productOrderAt = delischeDeliveryRequest.productOrderAt;
    this.productDeliveryAt = delischeDeliveryRequest.productDeliveryAt;
    this.lateDeliveryAtFlg = delischeDeliveryRequest.lateDeliveryAtFlg;
    this.deliveryAt = delischeDeliveryRequest.deliveryAt;
    this.productOrderLot = delischeDeliveryRequest.productOrderLotSum;
    this.deliveryLot = delischeDeliveryRequest.deliveryLotSum;
    this.arrivalLot = delischeDeliveryRequest.arrivalLotSum;
    this.calculateRetailPrice = delischeDeliveryRequest.calculateRetailPrice;
    this.calculateProductCost = delischeDeliveryRequest.calculateProductCost;
    this.deliveryCount = delischeDeliveryRequest.deliveryCount;
    this.setOrderApproveStatusCls(delischeDeliveryRequest.orderApproveStatus);
    this.setQualityStatusCls(delischeDeliveryRequest);
    this.orderId = delischeDeliveryRequest.orderId;
  }

  /**
   * 納品SKUレコードを設定する.
   * @param delischeDeliverySku 納品SKUレコード
   */
  private setDeliverySkuRecord(delischeDeliverySku: VDelischeDeliverySku): void {
    this.childExists = false;
    this.deliveryAtMonthly = delischeDeliverySku.deliveryAtMonthly;
    this.mdWeek = delischeDeliverySku.deliveryAt;
    this.brandCode = delischeDeliverySku.brandCode;
    this.itemCode = delischeDeliverySku.itemCode;
    this.partNo = delischeDeliverySku.partNo;
    this.productName = delischeDeliverySku.productName;
    this.colorCode = delischeDeliverySku.colorCode;
    this.size = delischeDeliverySku.size;
    this.season = delischeDeliverySku.season;
    this.mdfMakerName = delischeDeliverySku.mdfMakerName;
    this.productOrderAt = delischeDeliverySku.productOrderAt;
    this.productDeliveryAt = delischeDeliverySku.productDeliveryAt;
    this.lateDeliveryAtFlg = delischeDeliverySku.lateDeliveryAtFlg;
    this.deliveryAt = delischeDeliverySku.deliveryAt;
    this.productOrderLot = delischeDeliverySku.productOrderLot;
    this.deliveryLot = delischeDeliverySku.deliveryLot;
    this.arrivalLot = delischeDeliverySku.arrivalLot;
    this.calculateRetailPrice = delischeDeliverySku.calculateRetailPrice;
    this.calculateProductCost = delischeDeliverySku.calculateProductCost;
    this.setOrderApproveStatusCls(delischeDeliverySku.orderApproveStatus);
    this.setQualityStatusCls(delischeDeliverySku);
    this.orderId = delischeDeliverySku.orderId;
  }

  /**
   * 子要素開閉ボタン押下時の処理
   */
  onOpenOrCloseChild(): void {
    if (!this.childExists) { return; }
    this.openOrCloseChild.emit();
  }

  /**
   * 生産ステータスのモーダル表示を表示する。
   */
  openProductStatusModal(): void {
    if (this.delischeRecordType !== DelischeRecordType.ORDER) {
      return;
    }
    const modalRef = this.modalService.open(ProductionStatusModalComponent, { windowClass: 'production-status' });
    modalRef.componentInstance.orderId = this.orderId;
    modalRef.componentInstance.orderNumber = this.orderNumber;
  }
}
