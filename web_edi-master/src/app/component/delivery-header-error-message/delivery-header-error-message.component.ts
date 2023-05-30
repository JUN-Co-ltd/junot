import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';

import { DeliveryApprovalStatus, Path, AuthType } from '../../const/const';

import { ExceptionUtils } from '../../util/exception-utils';
import { StringUtils } from '../../util/string-utils';

@Component({
  selector: 'app-delivery-header-error-message',
  templateUrl: './delivery-header-error-message.component.html',
  styleUrls: ['./delivery-header-error-message.component.scss']
})
export class DeliveryHeaderErrorMessageComponent implements OnInit, OnChanges {
  @Input() path = '';                 // new,view,edit,delete
  @Input() affiliation: AuthType;     // ユーザ権限
  @Input() deliveryApproveStatus: string; // 納品承認ステータス
  @Input() isQualityApproved = false;     // 優良誤認承認済フラグ
  @Input() fatalErrorMsgCode = '';        // 致命的エラーメッセージコード

  // htmlから参照したい定数を定義
  readonly APPROVAL_STATUS = DeliveryApprovalStatus;
  readonly AUTH_INTERNAL: AuthType = AuthType.AUTH_INTERNAL;      // JUN権限(社内)
  readonly PATH = Path;

  constructor() { }

  ngOnInit() {
    this.showFatalErrorMessage(this.fatalErrorMsgCode);
  }

  /**
   * データバインドされた入力プロパティが変更される度に呼び出される。
   * @param changes 変更されたプロパティ
   */
  ngOnChanges(changes: SimpleChanges) {
    for (const propertyName in changes) {
      // 致命的エラー変更時の処理
      if (propertyName === 'fatalErrorMsgCode') {
        this.showFatalErrorMessage(this.fatalErrorMsgCode);
        break;  // loop終了
      }
    }
  }

  /**
   * 致命的エラーメッセージ表示.
   * @param errorMsgCode エラーメッセージコード
   */
  private showFatalErrorMessage(errorMsgCode: string): void {
    if (StringUtils.isEmpty(errorMsgCode)) {
      ExceptionUtils.clearErrorInfo();  // カスタムエラーメッセージクリア
      return;
    }
    ExceptionUtils.displayErrorInfo('fatalErrorInfo', errorMsgCode);
  }
}
