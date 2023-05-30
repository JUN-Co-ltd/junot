import { Component, OnInit, Input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import {
  AuthType, QualityApprovalStatus, DeliveryApprovalStatus, Path, ViewMode, LastDeliveryStatus
} from '../../const/const';
import { BusinessCheckUtils } from '../../util/business-check-utils';
import { FileUtils } from '../../util/file-utils';
import { FileService } from '../../service/file.service';
import { DateUtils } from '../../util/date-utils';

import { VOrder } from '../../model/v-order';
import { Delivery } from '../../model/delivery';

import { ProductionStatusModalComponent } from '../production-status-modal/production-status-modal.component';
import { DeliveryListViewModalComponent } from '../delivery-list-view-modal/delivery-list-view-modal.component';
import { FukukitaruOrderModalComponent } from '../fukukitaru-order-modal/fukukitaru-order-modal.component';

@Component({
  selector: 'app-order-status-list',
  templateUrl: './order-status-list.component.html',
  styleUrls: ['./order-status-list.component.scss']
})
export class OrderStatusListComponent implements OnInit {
  @Input() orderList: VOrder[] = [];  // 当タブ内で表示する発注情報リスト
  @Input() affiliation = false;       // ログインユーザーの権限

  // htmlからの定数値参照
  readonly AUTH_SUPPLIERS = AuthType.AUTH_SUPPLIERS;
  readonly AUTH_INTERNAL = AuthType.AUTH_INTERNAL;
  readonly QUALITY_APPROVAL_STATUS = QualityApprovalStatus;
  readonly PATH = Path;
  readonly VIEW_MODE = ViewMode;

  // 発注ファイルの実態リスト
  private orderFileList: { id: number, fileName: string, fileBlob: Blob }[] = [];
  // 発注ファイルDLエラーメッセージコード
  fileDLErrorMessageCode: string = null;
  // 発注ファイルDLエラー位置のIndex
  fileDLErrorIndex: number;

  constructor(
    private modalService: NgbModal,
    private fileService: FileService
  ) { }

  ngOnInit() { }

  /**
   * 受注確定済か判定する。
   * @param orderData 発注情報
   * @returns 受注確定済であればtrue
   */
  isConfirmOrderOk(orderData: VOrder): boolean {
    return BusinessCheckUtils.isConfirmOrderOk(orderData);
  }

  /**
   * 発注承認済か判定する。
   * @param orderData 発注情報
   * @returns 発注承認済であればtrue
   */
  isMdApprovalOk(orderData: VOrder): boolean {
    return BusinessCheckUtils.isApprovalOk(orderData);
  }

  /**
   * 優良誤認承認済、または非対象か判定する。
   * @param orderData 発注情報
   * @returns 優良誤認承認済、または非対象であればtrue
   */
  isQualityApprovalOk(orderData: VOrder): boolean {
    return BusinessCheckUtils.isQualityApprovalOk(orderData);
  }

  /**
   * 優良誤認の承認対象か判定する。
   * @param orderData 発注情報
   * @returns 優良誤認承認済、または非対象であればtrue
   */
  isAllQualityStatusNonTarget(orderData: VOrder): boolean {
    return BusinessCheckUtils.isAllQualityStatusNonTarget(orderData);
  }

  /**
   * 品番確定済か判定する。
   * @param orderData 発注情報
   * @returns 品番確定済であればtrue
   */
  isConfirmPartOk(orderData: VOrder): boolean {
    return BusinessCheckUtils.isConfirmPartOk(orderData);
  }

  /**
   * 納品依頼数を取得する。
   * @param orderData 発注情報
   * @returns 納品依頼数
   */
  getDeliverysCnt(orderData: VOrder): number {
    return orderData.deliverys == null ? 0 : orderData.deliverys.length;
  }

  /**
   * 納品依頼未承認のデータを返す。
   * @param orderData 発注情報
   * @returns 納品依頼未承認の納品情報リスト
   */
  getNotAcceptDeliveryList(orderData: VOrder): Delivery[] {
    const deliverys = orderData.deliverys;
    if (deliverys == null) { return null; }
    const pendingDeliveryList = deliverys.filter(delivery => delivery.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT);
    return pendingDeliveryList;
  }

  /**
   * 最終納品ステータスが最終納品であるデータが存在するか判定する。
   * @param orderData 発注情報
   * @returns 最終納品であるデータが存在する場合、true
   */
  isExsitsLastDeliveryStatus(orderData: VOrder): boolean {
    const deliverys = orderData.deliverys;
    if (deliverys == null) { return false; }
    return deliverys.some(delivery => delivery.lastDeliveryStatus === LastDeliveryStatus.LAST);
  }

  /**
   * 完納または自動完納か判定する。
   * @param orderData 発注情報
   * @returns 完納または自動完納であればtrue
   */
  isCompleteOrder(orderData: VOrder): boolean {
    return BusinessCheckUtils.isCompleteOrder(orderData);
  }

  /**
   * 生産ステータスのモーダル表示を表示する。
   * @param orderData 発注情報
   */
  openProductStatusModal(orderData: VOrder): void {
    const modalRef = this.modalService.open(ProductionStatusModalComponent, { windowClass: 'production-status' });
    modalRef.componentInstance.orderId = orderData.id;
    modalRef.componentInstance.orderNumber = orderData.orderNumber;
  }

  /**
   * 納品依頼リスト一覧モーダルを表示する。
   * @param orderData 発注情報
   */
  openDeliveryListModal(orderData: VOrder): void {
    const modalRef = this.modalService.open(DeliveryListViewModalComponent, { windowClass: 'delivery-approval-list' });
    modalRef.componentInstance.isQualityApproved = BusinessCheckUtils.isQualityApprovalOk(orderData); // 優良誤認承認済判定
    modalRef.componentInstance.deliveryList = orderData.deliverys;
  }

  /**
   * 資材発注一覧のモーダル表示を表示する。
   * @param orderData 発注情報
   */
  openFukukitaruOrderModal(orderData: VOrder): void {
    const modalRef = this.modalService.open(FukukitaruOrderModalComponent, { windowClass: 'fukukitaru-order' });
    modalRef.componentInstance.partNoId = orderData.partNoId;
    modalRef.componentInstance.orderId = orderData.id;
    modalRef.componentInstance.quantity = orderData.quantity;
  }

  /**
   * ファイルダウンロードアイコンを表示可否チェック
   * @param index ファイルリストのindex
   */
  isAbleToDownloadFile(index: number): boolean {
    const order = this.orderList[index].orderFileInfo;
    return order != null && DateUtils.isWithinPeriod(new Date(), order.publishedAt, order.publishedEndAt);
  }

  /**
   * 発注書ファイルダウンロードアイコン押下処理
   * @param orderFileNoId ファイルID
   * @param errorIndex エラー位置のIndex
   */
  onFileDownload(orderFileNoId: number, errorIndex: number): void {
    this.fileDLErrorMessageCode = null;

    const idExists = this.orderFileList.some(orderFile => {
      if (orderFile.id === orderFileNoId) { // 一度ファイルをダウンロードしている
        FileUtils.downloadFile(orderFile.fileBlob, orderFile.fileName);
        return true;
      }
    });

    // キャッシュにない場合はAPIから取得
    if (!idExists) {
      this.fileService.fileDownload(orderFileNoId.toString()).subscribe(res => {
        const data = this.fileService.splitBlobAndFileName(res);
        this.orderFileList.push({ id: orderFileNoId, fileName: data.fileName, fileBlob: data.blob });
        FileUtils.downloadFile(data.blob, data.fileName);
      }, () => {
        this.fileDLErrorMessageCode = 'ERRORS.FILE_DL_ERROR';
        this.fileDLErrorIndex = errorIndex;
      });
    }
  }
}
