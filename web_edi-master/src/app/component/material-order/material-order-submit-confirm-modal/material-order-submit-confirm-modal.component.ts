import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-material-order-submit-confirm-modal',
  templateUrl: './material-order-submit-confirm-modal.component.html',
  styleUrls: ['./material-order-submit-confirm-modal.component.scss']
})
export class MaterialOrderSubmitConfirmModalComponent implements OnInit {
  @Input() isQualityApprovalOk = false;  // 優良誤認承認済か

  confirmFormGroup: FormGroup;      // 確認フォームグループ

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit() {
    this.confirmFormGroup = new FormGroup({ isOrderResponsible: new FormControl(false) });
  }

  /** OKボタン押下時、責任発注確認のチェックの戻り値を設定してモーダルを閉じる。 */
  onOk(): void {
    this.activeModal.close(this.confirmFormGroup.getRawValue()['isOrderResponsible']);
  }
}
