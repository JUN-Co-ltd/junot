import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FukukitaruInputAssistSet, FukukitaruInputAssistSetDetails } from '../../model/fukukitaru-input-assist-set';
import { FukukitaruMasterMaterialTypeName } from '../../const/const';

@Component({
  selector: 'app-fukukitaru-input-assist-modal',
  templateUrl: './fukukitaru-input-assist-modal.component.html',
  styleUrls: ['./fukukitaru-input-assist-modal.component.scss']
})
export class FukukitaruInputAssistModalComponent implements OnInit {
  @Input() listInputAssistSet: FukukitaruInputAssistSet[] = [];

  readonly F_MASTER_MATERIAL_TYPE_NAME = FukukitaruMasterMaterialTypeName;

  /** 入力補助セット詳細のModel. */
  listInputAssistSetDetails: FukukitaruInputAssistSetDetails[] = [];

  constructor(
    public activeModal: NgbActiveModal
  ) { }

  ngOnInit() {
    // 初期表示
    this.initDisplay();
  }

  /**
   * 初期表示
   */
  initDisplay() {
    this.listInputAssistSetDetails = this.listInputAssistSet[0].listInputAssistSetDetails;
  }

  /**
   * セット変更時の処理
   */
  changeSet(selectedValue: number) {
    this.listInputAssistSetDetails = this.listInputAssistSet.find(val => val.id === Number(selectedValue)).listInputAssistSetDetails;
  }

  /**
   * 選択ボタンクリック時、親ページにデータを渡して、モーダルを閉じます。
   */
  onSelect() {
    this.activeModal.close(this.listInputAssistSetDetails);
  }
}
