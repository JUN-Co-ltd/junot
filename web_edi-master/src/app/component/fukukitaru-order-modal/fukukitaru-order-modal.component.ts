import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';

import {
  Path, FukukitaruMasterOrderType
} from '../../const/const';

import { FukukitaruOrder01Service } from '../../service/fukukitaru-order01.service';
import { MaterialOrderService } from '../../service/material-order/material-order.service';

import { ScreenSettingFukukitaruOrderSearchCondition } from '../../model/screen-setting-fukukitaru-order-search-condition';
import { FukukitaruOrder } from '../../model/fukukitaru-order';

@Component({
  selector: 'app-fukukitaru-order-modal',
  templateUrl: './fukukitaru-order-modal.component.html',
  styleUrls: ['./fukukitaru-order-modal.component.scss']
})
export class FukukitaruOrderModalComponent implements OnInit {
  @Input() private partNoId: number = null; // 品番ID
  @Input() private orderId: number = null;  // 発注ID
  @Input() quantity = 0;            // 発注数

  // htmlからの定数値参照
  readonly PATH = Path;
  readonly WASH_NAME = FukukitaruMasterOrderType.WASH_NAME;
  readonly BOTTOM_BILL = FukukitaruMasterOrderType.BOTTOM_BILL;
  readonly WASH_NAME_KOMONO = FukukitaruMasterOrderType.WASH_NAME_KOMONO;
  readonly BOTTOM_BILL_KOMONO = FukukitaruMasterOrderType.BOTTOM_BILL_KOMONO;

  readonly WASH_NAME_URL = 'fukukitaruOrder01Wash';
  readonly BOTTOM_BILL_URL = 'fukukitaruOrder01HangTag';

  fukukitaruOrderList: FukukitaruOrder[] = [];  // 資材発注リスト
  searchLoading = true;

  constructor(
    public activeModal: NgbActiveModal,
    private router: Router,
    private fukukitaruOrder01Service: FukukitaruOrder01Service,
    private materialOrderService: MaterialOrderService
  ) { }

  ngOnInit() {
    const condition = { orderId: this.orderId, partNoId: this.partNoId } as ScreenSettingFukukitaruOrderSearchCondition;
    this.fukukitaruOrder01Service.listFukukitaruOrders(condition).subscribe(
      data => {
        this.fukukitaruOrderList = data.items;
        this.searchLoading = false;
      }, error => {
        console.debug('error:', error);
        this.searchLoading = false;
      });
  }

  /**
   * 資材発注新規登録画面遷移.
   * @param url url
   */
  onClickFukukitaruOrderCreate(url: string): void {
    this.router.navigate([url, Path.NEW], {
      queryParams: { 'orderId': this.orderId, 'partNoId': this.partNoId }
    });
    this.activeModal.close();
  }

  /**
   * レコード押下時の処理.
   * モーダルを閉じ、資材情報(編集/参照)画面へ遷移.
   */
  onSelectRow(fukukitaruOrder: FukukitaruOrder): void {
    const path = Path.EDIT; // 資材発注確定済および未確定いずれも更新画面へ遷移する

    let url = '';
    switch (fukukitaruOrder.orderType) {
      case FukukitaruMasterOrderType.WASH_NAME:
      case FukukitaruMasterOrderType.WASH_NAME_KOMONO:
        url = this.WASH_NAME_URL; // 洗濯ネーム
        break;
      case FukukitaruMasterOrderType.BOTTOM_BILL:
      case FukukitaruMasterOrderType.BOTTOM_BILL_KOMONO:
        url = this.BOTTOM_BILL_URL; // 下札
        break;
      default:
        break;
    }

    this.router.navigate([url, fukukitaruOrder.id, path], {
      queryParams: { 'orderId': fukukitaruOrder.orderId, 'partNoId': fukukitaruOrder.partNoId }
    });
    this.activeModal.close();
  }

  /**
   * ステータスラベルの表示文言を返却
   * @returns ステータスラベル文字
   */
  showStatuslabel(fOrder: FukukitaruOrder): string {
    const label = this.materialOrderService.getStatusLabel(fOrder.confirmStatus, fOrder.linkingStatus);
    return label;
  }
}
