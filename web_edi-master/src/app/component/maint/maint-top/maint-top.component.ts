import { Component, OnInit } from '@angular/core';
import { HeaderService } from 'src/app/service/header.service';
import { SessionService } from 'src/app/service/session.service';

import { AuthUtils } from 'src/app/util/auth-utils';

@Component({
  selector: 'app-maint-top',
  templateUrl: './maint-top.component.html',
  styleUrls: ['./maint-top.component.scss']
})
export class MaintTopComponent implements OnInit {

  // 権限
  auth = {
    // マスタメンテナンス
    masterMaintenance: false
  };

  constructor(
    private headerService: HeaderService,
    private sessionService: SessionService
  ) { }

  ngOnInit() {
    this.headerService.show();
    // ログイン情報を取得
    const session = this.sessionService.getSaveSession();
    // ログインユーザの権限を取得する
    this.auth = {
      masterMaintenance: AuthUtils.isMasterMaintenance(session)
    };
  }
}
