import { HttpErrorResponse } from '@angular/common/http';
import { Error } from '../model/error';
import { ErrorView } from '../model/error-view';
import { ErrorDetail } from '../model/error-detail';
import { ErrorDetailView } from '../model/error-detail-view';
import { HttpStatusCode } from '../const/const';
import { StringUtils } from './string-utils';

/**
 * 例外ユーティリティ.
 */
export class ExceptionUtils {
  constructor(
  ) { }

  /**
   * APIエラー情報設定.
   * APIエラーの内容からエラー情報を設定する。
   *
   * @param errorResponse HttpErrorResponse
   * @returns ErrorView 画面表示用エラー情報
   */
  static apiErrorHandler(errorResponse: HttpErrorResponse): ErrorView {
    if (errorResponse == null || errorResponse.error == null) { return null; }
    console.error(errorResponse);
    // 画面表示用のエラー情報にレスポンスデータを格納
    const errorView = this.generateErrorView(errorResponse);
    const errors = errorResponse.error['errors'] as Array<any>;
    if (errors != null) {
      // httpレスポンスにerrorsがある場合はエラー詳細情報リストを格納
      const errorDetailListInfo = this.generateErrorDetails(errors);
      errorView.errors = errorDetailListInfo.errors;
      errorView.viewErrors = errorDetailListInfo.viewErrors;
      // errors[0]のcodeがnullでなければ画面表示用エラーメッセージコード設定
      errorView.viewErrorMessageCode = (errors[0]['code'] != null) ? ('ERRORS.' + errors[0]['code']) : null;
    }
    // httpレスポンスにerrorsがない、またはerrorsはあるがerrorsにcodeがない場合はエラー情報の画面表示用エラーメッセージコードを作成
    if (errorView.viewErrorMessageCode == null) {
      errorView.viewErrorMessageCode = this.generateErrorMessageCode(errorResponse.error['status']);
    }
    return errorView;
  }

  /**
   * 画面表示用のエラー情報にレスポンスデータを格納して返す。
   * @param errorResponse HttpErrorResponse
   * @return ErrorView
   */
  private static generateErrorView(errorResponse: HttpErrorResponse): ErrorView {
    return {
      error: errorResponse.error['error'],
      errors: [],
      message: errorResponse.error['message'],
      path: errorResponse.error['path'],
      status: errorResponse.error['status'],
      timestamp: errorResponse.error['timestamp'],
      viewErrorMessageCode: null,
      viewErrors: null
    };
  }

  /**
   * エラー詳細情報リストを作成して返す。
   * @param errors httpレスポンスのエラー詳細リスト
   * @return errorDetailListInfo: { errors: ErrorDetail[]; viewErrors: ErrorDetailView[]; }
   */
  private static generateErrorDetails(errors: any[]): { errors: ErrorDetail[]; viewErrors: ErrorDetailView[]; } {
    const viewErrors: Array<ErrorDetailView> = [];
    const errorDetails: Array<ErrorDetail> = [];
    // エラー詳細情報リストにレスポンスデータを格納
    errors.forEach(error => {
      errorDetails.push(error);
      viewErrors.push({
        code: error['code'],
        args: error['args'],
        message: error['message'],
        viewErrorMessageCode: (error['code'] != null) ? ('ERRORS.' + error['code']) : null, // codeがnullでなければ画面表示用エラーメッセージコード設定
      } as ErrorDetailView);
    });
    return { errors: errorDetails, viewErrors: viewErrors };
  }

  /**
   * 画面表示用エラーメッセージコード取得.
   * HTTPステータスによってエラーメッセージコードを設定する。
   *
   * @param status number HTTPステータス
   * @returns エラーメッセージコード
   */
  static generateErrorMessageCode(status: number): string {
    switch (status) {
      case HttpStatusCode.UNAUTHORIZED:
        return 'ERRORS.SESSION_ERROR';
      case HttpStatusCode.INTERNAL_SERVER_ERROR:
        return 'ERRORS.SERVER_ERROR';
      case HttpStatusCode.BAD_REQUEST:
        return 'ERRORS.VALID_ERROR';
      default:
        return 'ERRORS.ANY_ERROR';
    }
  }

  /**
   * エラーメッセージ画面表示.
   * 指定されたオブジェクトIdと、エラーメッセージコードを基に、画面上にエラーメッセージを表示する。
   *
   * @param objId string オブジェクトId
   * @param messageCode string エラーメッセージコード
   * @returns errorCode エラーメッセージコード
   */
  static displayErrorInfo(objId: string, messageCode: string): void {
    console.debug('objId:', objId, 'messageCode:', messageCode);
    const element = document.getElementById(objId);
    element.innerHTML = messageCode;
    element.style.display = 'block';
  }

  /**
   * エラーメッセージクリア.
   * 画面上のエラーメッセージを非表示にする。
   *
   * @param objId string オブジェクトId
   * @param messageCode string エラーメッセージコード
   * @returns errorCode エラーメッセージコード
   */
  static clearErrorInfo(): void {
    const objChilds: NodeListOf<HTMLElement> = document.getElementsByName('errorInfo');
    for (let i = 0; i < objChilds.length; i++) {
      objChilds[i].innerHTML = '';
      objChilds[i].style.display = 'none';
    }
  }

  /**
   * エラーレスポンスからエラーメッセージコードを取得する.
   * @param error エラーレスポンス
   * @returns エラーメッセージコード
   */
  static getErrorMessageCode(error: HttpErrorResponse): string {
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null && apiError.viewErrors != null) {
      return apiError.viewErrors[0].code;
    } else if (StringUtils.isNotEmpty(apiError.viewErrorMessageCode)) {
      return apiError.viewErrorMessageCode;
    } else {
      return 'ANY_ERROR';
    }
  }

  /**
   * codeに'ERRORS.'がなければつける
   * @returns 'ERRORS.'がついたコード
   */
  static addErrorsToCode(errorCode: string): string {
    return errorCode.includes('ERRORS.') ? errorCode : ('ERRORS.' + errorCode);
  }

  /**
   * ErrorModelにエラーレスポンスデータを格納する。
   * @param errorResponse HttpErrorResponse
   * @return Error
   */
  static generateError(errorResponse: HttpErrorResponse): Error {
    if (errorResponse == null || errorResponse.error == null) { return null; }
    return {
      error: errorResponse.error['error'],
      errors: errorResponse.error['errors'] as ErrorDetail[],
      message: errorResponse.error['message'],
      path: errorResponse.error['path'],
      status: errorResponse.error['status'],
      timestamp: errorResponse.error['timestamp']
    };
  }
}
