import { Component, OnInit, Input } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ExceptionUtils } from 'src/app/util/exception-utils';
import { ListUtils } from 'src/app/util/list-utils';

import { Error } from 'src/app/model/error';
import { BusinessError } from 'src/app/model/bussiness-error';
import { ListUtilsService } from 'src/app/service/bo/list-utils.service';

@Component({
  selector: 'app-error-modal',
  templateUrl: './error-modal.component.html',
  styleUrls: ['./error-modal.component.scss']
})
export class ErrorModalComponent implements OnInit {
  /** エラーレスポンス */
  @Input()
  private response: any;

  /** 画面表示するエラーリスト */
  errorList: { code: string, arg: string | number }[] = [];

  constructor(
    public activeModal: NgbActiveModal,
    private listUtils: ListUtilsService
  ) { }

  ngOnInit() {
    this.setErrorList(this.response);
  }

  /**
   * エラーリストの設定.
   * @param response エラーレスポンス
   */
  private setErrorList(response: any): void {
    if (response instanceof HttpErrorResponse) {
      this.setCodeAtHttpError(response);
    } else if (response instanceof BusinessError) {
      this.setCodeAtBusinessError(response);
    }

    if (this.errorList.length === 0) {
      this.errorList.push({ code: 'ERRORS.ANY_ERROR', arg: null });
    }
  }

  /**
   * HTTPエラー時のコード設定.
   * @param response エラーレスポンス
   */
  private setCodeAtHttpError(response: HttpErrorResponse): void {
    const error = (<Error> response.error);
    const errors = error.errors;
    if (this.listUtils.isEmpty(errors)) {
      this.errorList.push({ code: this.decideCodeByStatus(error.status), arg: null });
      return;
    }

    errors.forEach(err =>
      this.errorList.push({
        code: ExceptionUtils.addErrorsToCode(err.code),
        arg: this.extractArgsValue(err.code, err.args)
      }));
  }

  /**
   * 業務エラー時のコード設定.
   * @param response エラーレスポンス
   */
  private setCodeAtBusinessError(response: BusinessError): void {
    this.errorList.push({
      code: ExceptionUtils.addErrorsToCode(response.code),
      arg: String(response.arg)
    });
  }

  /**
   * ステータス別のエラーコードを取得.
   * @param status ステータス
   * @returns エラーコード
   */
  private decideCodeByStatus(status: number): string {
    switch (status) {
      case 400:
        return 'ERRORS.VALID_ERROR';
      default:
        return 'ERRORS.ANY_ERROR';
    }
  }

  /**
   * エラーのargsの値を取得.
   * @param errCode エラーコード
   * @param args エラーの値
   * @returns argsの値
   */
  private extractArgsValue(errCode: string, args: any = null): string {
    switch (errCode) {
      // エラーコード毎に取得方法が変わる場合はcase文を追加してください
      default:
        return ListUtils.isEmpty(args) ? args : args[0];
    }
  }

  /**
   * OKボタン押下時の処理.
   */
  onOk(): void {
    this.activeModal.close('OK');
  }
}
