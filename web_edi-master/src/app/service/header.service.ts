import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { SessionService } from './session.service';

/**
 * ヘッダーの表示/非表示を切り替えるService
 */
@Injectable({
  providedIn: 'root'
})
export class HeaderService {
  headerDisplay = false;
  session = null;

  constructor(
    public sessionService: SessionService,
    private router: Router
  ) {
  }

  hide(): void {
    this.headerDisplay = false;
  }

  show(): void {
    this.headerDisplay = true;
    this.session = this.sessionService.getSaveSession();
  }

  isDisplay(): boolean {
    return this.headerDisplay;
  }

  async logout(): Promise<void> {
    await this.sessionService.deleteSession().toPromise().then(
      () => { },
      error => console.error('logout error:', error));

    // ログアウトが終了したらログイン画面へ遷移する
    this.router.navigate(['login']);
  }
}
