import { Injectable } from '@angular/core';
import { Observable, of, from } from 'rxjs';
import { map, flatMap, catchError } from 'rxjs/operators';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { MessageConfirmModalComponent } from 'src/app/component/message-confirm-modal/message-confirm-modal.component';
import { ErrorModalComponent } from 'src/app/component/error-modal/error-modal.component';

@Injectable({
  providedIn: 'root'
})
export class MessageConfirmModalService {
  constructor(
    private translateService: TranslateService,
    private modal: NgbModal
  ) { }

  /**
   * メッセージ取得後に確認モーダルを表示する。
   * @param key 確認モーダルに表示するメッセージのキー
   * @param interpolateParams 確認モーダルに表示するメッセージのパラメータ（省略化）
   * @returns 確認モーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  public translateAndOpenConfirmModal(key: string, interpolateParams?: Object): Observable<boolean> {
    return this.translateService.get(key, interpolateParams).pipe(
      flatMap((message) => this.openConfirmModal(message))
    );
  }

  /**
   * 確認モーダルを表示する。
   * @param message 確認モーダルに表示するメッセージ
   * @returns 確認モーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  public openConfirmModal(message: string): Observable<boolean> {
    // 確認モーダルを表示
    const modalRef = this.modal.open(MessageConfirmModalComponent);
    modalRef.componentInstance.message = message;
    return from(modalRef.result).pipe(
      map((result) => result === 'OK'),
      catchError(() => of(false))
    );
  }

  /**
   * エラーメッセージ表示用のモーダルを表示する。
   * @param response エラーレスポンス
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  public openErrorModal(response: any): Observable<boolean> {
    // ログにエラー情報を出力
    console.error(response);
    // エラーモーダルを表示
    const modalRef = this.modal.open(ErrorModalComponent, { windowClass: 'error' });
    modalRef.componentInstance.response = response;
    return from(modalRef.result).pipe(
      map((result) => result === 'OK'),
      catchError(() => of(false))
    );
  }
}
