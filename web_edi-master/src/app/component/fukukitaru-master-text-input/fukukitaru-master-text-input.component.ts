import { Component, OnInit, Input } from '@angular/core';
import { FukukitaruMaster } from '../../model/fukukitaru-master';

@Component({
  selector: 'app-fukukitaru-master-text-input',
  templateUrl: './fukukitaru-master-text-input.component.html',
  styleUrls: ['./fukukitaru-master-text-input.component.scss']
})
export class FukukitaruMasterTextInputComponent implements OnInit {
  @Input() fkList: FukukitaruMaster[];  // フクキタルマスタ型のリスト
  @Input() value: number = null;  // キー
  codeName = '';  // 名称

  constructor() { }

  ngOnInit() {
    this.setCodeName();
  }

  /**
   * 名称を設定する.
   */
  setCodeName(): void {
    this.fkList.some(data => {
      if (data.id === this.value) {
        this.codeName = data.codeName;
        return true;
      }
    });
  }
}
