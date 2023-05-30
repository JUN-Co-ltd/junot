import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-misleading-approve-form',
  templateUrl: './misleading-approve-form.component.html',
  styleUrls: ['./misleading-approve-form.component.scss']
})
export class MisleadingApproveFormComponent implements OnInit {
  @Input() group: FormGroup;
  @Input() isDisable: boolean;
  @Output() private check = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  /**
   * @param key フォーム項目のkey
   * @returns フォーム値
   */
  getControlLabel(key: string): string {
    return this.group.get(key).value;
  }

  /**
   * チェックボックス押下時の処理.
   * @param checked チェック状態
   */
  onCheck = (checked: boolean) => this.check.emit(checked);
}
