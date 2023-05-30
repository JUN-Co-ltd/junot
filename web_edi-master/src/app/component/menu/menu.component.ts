import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { SessionStorageKey, SelectTabName, AuthType } from '../../const/const';
import { FileUtils } from '../../util/file-utils';
import { AuthUtils } from '../../util/auth-utils';

import { ClientSessionService } from '../../service/client-session.service';
import { SessionService } from '../../service/session.service';
import { DocumentFileService } from '../../service/document-file.service';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {
  // htmlから参照したい定数を定義
  readonly TAB_NAME = SelectTabName;

  // 権限
  auth = {
    // JUN
    jun: false,
    // メーカー
    maker: false,
    // マスタメンテナンス
    masterMaintenance: false,
    // EDI
    edi: false,
    // DISTA
    dista: false,
    // メーカー返品
    makerReturn: false
  };

  errorMsgCode = '';  // エラーメッセージコード

  constructor(
    private router: Router,
    private clientSessionService: ClientSessionService,
    private sessionService: SessionService,
    private documentFileService: DocumentFileService
  ) { }

  ngOnInit() {
    // ログイン情報を取得
    const session = this.sessionService.getSaveSession();
    // ログインユーザの権限を取得する
    this.auth = {
      jun: AuthUtils.isJun(session),
      maker: AuthUtils.isMaker(session),
      masterMaintenance: AuthUtils.isMasterMaintenance(session),
      dista: AuthUtils.isDista(session),
      edi: AuthUtils.isEdi(session),
      makerReturn: AuthUtils.isMakerReturn(session)
    };
  }

  /**
   * 一覧画面への遷移がクリックされた時のイベントハンドラ
   * @param tabName 選択した一覧
   */
  onListPageClick(link: string, tabName: string): void {
    // 選択した一覧をsessionに保持
    this.clientSessionService.createSession(SessionStorageKey.LIST_SELECTED_TAB_NAME, tabName);
    // 一覧画面へ遷移
    this.router.navigate([link]);
  }

  /**
   * 共有ドキュメントファイルダウンロードリンク押下処理.
   * @param fileName ファイル名
   */
  onShareFileDownLoad(fileName: string): void {
    this.errorMsgCode = '';
    this.documentFileService.shareFileDownload(fileName).subscribe(res => {
      const data = this.documentFileService.splitBlobAndFileName(res);
      FileUtils.downloadFile(data.blob, data.fileName);
    }, () => this.errorMsgCode = 'ERRORS.FILE_DL_ERROR');
  }

  /**
   * JUN権限ドキュメントファイルダウンロードリンク押下処理.
   * @param fileName ファイル名
   */
  onJunFileDownLoad(fileName: string): void {
    this.errorMsgCode = '';
    this.documentFileService.junFileDownload(fileName).subscribe(res => {
      const data = this.documentFileService.splitBlobAndFileName(res);
      FileUtils.downloadFile(data.blob, data.fileName);
    }, () => this.errorMsgCode = 'ERRORS.FILE_DL_ERROR');
  }
}
