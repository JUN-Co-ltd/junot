import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { BrowserUtils } from '../../util/browser-utils';
import { ExceptionUtils } from '../../util/exception-utils';

import { Login } from '../../model/login';

import { SessionService } from '../../service/session.service';
import { HeaderService } from '../../service/header.service';
import { VersionService } from '../../service/version.service';
import { AvailableTimeService } from '../../service/available-time.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  /** 使用しているブラウザがサポート対象か判定 */
  readonly support = BrowserUtils.isSupport();

  login = {
    company: null,
    accountName: null,
    password: null
  } as Login;

  /** ログイン処理時のエラーメッセージのコード */
  messageCode = null;

  /** ログイン処理中フラグ */
  loginProcessing = false;

  /** ログイン成功後に遷移するURL */
  redirectUrl: string;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private sessionService: SessionService,
    private headerService: HeaderService,
    private versionService: VersionService,
    private availableTimeService: AvailableTimeService
  ) { }

  async ngOnInit() {
    this.headerService.hide();

    // バージョンを確認し、最新バージョン以外の場合は、リロードする
    await this.versionService.reload().catch(error => {
      console.error('versionService.reload:', error);
      this.messageCode = 'ERRORS.SERVER_ERROR';
      throw error;
    });

    // JUNoT利用時間を確認し、利用時間外の場合は、メンテナンス画面に遷移する
    await this.availableTimeService.get().toPromise().catch(() => {
      // 画面遷移はHttpApiInterceptorで行うため、ここでは何もしない
    });

    const status = this.activatedRoute.snapshot.queryParamMap.get('status');
    if (status != null) {
      this.messageCode = ExceptionUtils.generateErrorMessageCode(Number(status));
    }

    // ルートパラメータからリダイレクトURLを取得する。リダイレクトURLがない場合は、「top」を取得する
    this.redirectUrl = this.activatedRoute.snapshot.queryParamMap.get('redir') || 'top';
  }

  /**
   * ログインボタンクリック時に、ログイン処理を行います。
   */
  async onLogin(): Promise<void> {
    // メッセージクリア
    this.messageCode = null;
    this.loginProcessing = true;

    // バージョンを確認し、最新バージョン以外の場合は、リロードする
    await this.versionService.reload().catch(error => {
      console.error('versionService.reload:', error);
      this.messageCode = 'ERRORS.SERVER_ERROR';
      this.loginProcessing = false;
      throw error;
    });

    this.sessionService.createSession(
      this.login.company,
      this.login.accountName,
      this.login.password).subscribe(
        // セッション作成成功
        response => {
          console.debug('session success response:', response);
          this.loginSuccess();
        },
        // セッション作成失敗
        error => {
          this.setErrorMessageCode(error);
          this.loginProcessing = false;
        }
      );
  }

  /**
   * エラー時のメッセージコードを設定する。
   * @param error エラー情報
   */
  private setErrorMessageCode(error: any): void {
    const errorMessage = error.error.message;
    console.debug('session errorMessage:', errorMessage);
    console.debug('session errorStatus:', error.status);

    // 権限エラー以外の場合
    if (error.status !== 401) {
      this.messageCode = 'ERRORS.SERVER_ERROR';
      return;
    }

    // 利用ユーザ(ROLE_USER)でない場合
    if (errorMessage === '401_L_03') {
      this.messageCode = 'ERRORS.' + errorMessage;
      return;
    }

    // 会社コード未登録、またはアカウント名と会社コードが紐付いていない場合
    if (errorMessage === '401_L_01' || errorMessage === '401_L_02') {
      this.messageCode = 'ERRORS.' + errorMessage;
      return;
    }

    this.messageCode = 'ERRORS.AUTH_ERROR';
  }

  /**
   * ログイン成功後の画面遷移を行います。
   */
  loginSuccess(): void {
    this.router.navigateByUrl(this.redirectUrl);
  }
}
