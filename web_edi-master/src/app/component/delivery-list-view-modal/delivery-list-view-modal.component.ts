import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { Path, LastDeliveryStatus, AuthType, DeliveryApprovalStatus } from '../../const/const';

import { VDelivery } from '../../model/v-delivery';

import { SessionService } from '../../service/session.service';

@Component({
  selector: 'app-delivery-list-view-modal',
  templateUrl: './delivery-list-view-modal.component.html',
  styleUrls: ['./delivery-list-view-modal.component.scss']
})
export class DeliveryListViewModalComponent implements OnInit {
  @Input() deliveryList: VDelivery[] = null;  // 納品情報リスト
  @Input() isQualityApproved = true;          // 優良誤認承認済フラグ

  // htmlから参照したい定数を定義
  readonly AUTH_INTERNAL = AuthType.AUTH_INTERNAL;
  readonly LAST_DELIVERY_STATUS = LastDeliveryStatus;
  readonly ACCEPT = DeliveryApprovalStatus.ACCEPT;

  affiliation: AuthType; // ログインユーザ権限

  constructor(
    public activeModal: NgbActiveModal,
    private router: Router,
    private sessionService: SessionService
  ) { }

  ngOnInit() {
    this.sortRecord();
    this.affiliation = this.sessionService.getSaveSession().affiliation;  // ログインユーザの権限を取得する
  }

  /**
   * ソート処理.
   */
  private sortRecord(): void {
    this.deliveryList.sort((val1, val2) => {
      // 1:納期昇順
      if (val1.correctionAt < val2.correctionAt) { return -1; }
      if (val1.correctionAt > val2.correctionAt) { return 1; }
      // 2:納品依頼回数昇順
      if (val1.deliveryCount < val2.deliveryCount) { return -1; }
      if (val1.deliveryCount > val2.deliveryCount) { return 1; }
      // 3:納品ID昇順
      return val1.id - val2.id;
    });
  }

  /**
   * 行選択時の処理。
   * モーダルを閉じて納品依頼画面へ画面遷移する。
   * @param delivery 選択した納品情報
   */
  onSelectRow(delivery: VDelivery): void {
    const path = delivery.deliveryApproveStatus === DeliveryApprovalStatus.ACCEPT ? Path.VIEW : Path.EDIT; // 承認の場合VIEW、未承認の場合EDIT
    // 納品依頼画面へ遷移(納品依頼が削除されていた場合に新規登録画面へ遷移する為、queryParamsにorderIdをつける)
    this.router.navigate(['deliveries', delivery.id, path], { queryParams: { orderId: delivery.orderId } });

    this.activeModal.close();
  }
}
