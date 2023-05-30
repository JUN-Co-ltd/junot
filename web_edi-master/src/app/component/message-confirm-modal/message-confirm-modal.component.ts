import { Component, OnInit, Input } from '@angular/core';
import { SafeHtml, DomSanitizer } from '@angular/platform-browser';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-message-confirm-modal',
  templateUrl: './message-confirm-modal.component.html',
  styleUrls: ['./message-confirm-modal.component.scss']
})
export class MessageConfirmModalComponent implements OnInit {
  @Input() private message;
  @Input() buttonName = 'OK';
  @Input() private title;

  safeHtml: SafeHtml;
  messageTitle: string;

  constructor(
    public activeModal: NgbActiveModal,
    private domSanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    if (this.title === undefined) {
      this.messageTitle = '確認';
    } else {
      this.messageTitle = this.title;
    }
    this.safeHtml = this.domSanitizer.bypassSecurityTrustHtml(this.message);
  }

  /**
   * OKボタン押下時の処理.
   */
  onOk(): void {
    this.activeModal.close('OK');
  }
}
