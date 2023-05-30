import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-delivery-plan-submit-confirm-modal',
  templateUrl: './delivery-plan-submit-confirm-modal.component.html',
  styleUrls: ['./delivery-plan-submit-confirm-modal.component.scss']
})
export class DeliveryPlanSubmitConfirmModalComponent implements OnInit {
  confirmFormGroup: FormGroup;

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit() {
    this.confirmFormGroup = new FormGroup({ isFinalDeliveryPlan: new FormControl(false) });
  }

  /** 登録ボタン押下時、全入力確認のチェックの戻り値を設定してモーダルを閉じる。 */
  onSubmit(): void {
    this.activeModal.close(this.confirmFormGroup.getRawValue()['isFinalDeliveryPlan']);
  }

  /** キャンセルボタン押下時、戻り値を設定せずモーダルを閉じる。 */
  onCancel(): void {
    this.activeModal.dismiss();
  }
}
