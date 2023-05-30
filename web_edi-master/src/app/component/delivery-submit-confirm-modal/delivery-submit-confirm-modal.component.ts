import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { SubmitType } from '../../const/const';

@Component({
  selector: 'app-delivery-submit-confirm-modal',
  templateUrl: './delivery-submit-confirm-modal.component.html',
  styleUrls: ['./delivery-submit-confirm-modal.component.scss']
})
export class DeliverySubmitConfirmModalComponent implements OnInit {
  @Input() isOverLot = false;   // 納品数が発注数を超えるか
  @Input() isLowerLot = false;  // 納品数が配分数不足か
  // PRD_0125 #9079 add JFE start
  @Input() isDRUpDate = false;  // 納品依頼編集画面のアップデートかどうか
  // PRD_0125 #9079 add JFE end
  @Input() isLastDeliveryRegistered = false;  // 最終納品として登録済か
  @Input() private submitType: SubmitType;    // Submit種類

  submitBtnName = '';               // Submitボタン名
  confirmFormGroup: FormGroup;      // 確認フォームグループ

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit() {
    this.setSubmitBtnName();
    this.confirmFormGroup = new FormGroup({ isLastDelivery: new FormControl(false) });
  }

  /**
   * ボタンの文字列を設定する.
   */
  private setSubmitBtnName(): void {
    switch (this.submitType) {
      case SubmitType.ENTRY:
        this.submitBtnName = '登録する';
        return;
      case SubmitType.UPDATE:
        this.submitBtnName = '更新する';
        return;
      case SubmitType.CORRECT:
        this.submitBtnName = '訂正する';
        return;
      default:
        break;
    }
  }

  /** Submitボタン押下時、最終登録確認のチェックの戻り値を設定してモーダルを閉じる。 */
  onSubmit(): void {
    this.activeModal.close(this.confirmFormGroup.getRawValue()['isLastDelivery']);
  }

  /** キャンセルボタン押下時、戻り値を設定せずモーダルを閉じる。 */
  onCancel(): void {
    this.activeModal.dismiss();
  }
}
