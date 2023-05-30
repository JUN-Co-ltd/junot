import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { LoadingService } from '../service/loading.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { environment } from '../../environments/environment';

const SESSION_URL = `${ environment.apiBaseUrl }/sessions`;

@Injectable()
export class HttpApiInterceptor implements HttpInterceptor {

  constructor(
    private router: Router,
    private loadingService: LoadingService,
    private activeModal: NgbModal,
  ) {

  }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          console.log('succeed');
        }
      }, error => {
        console.debug('HttpApiInterceptor error:', error);

        if (error.status === 401) {
          if (this.isRedirectLogin(req)) {
            console.log('error redirect to login');
            this.loadingService.loadEnd();
            this.activeModal.dismissAll();
            this.router.navigate(['login'], {
              queryParams: {
                status: 401,
                redir: this.router.url
              }
            });
          }
        } else if (error.status === 480) {
          console.log('error redirect to maintenance');
          this.loadingService.loadEnd();
          this.activeModal.dismissAll();
          this.router.navigate(['/maintenance']);
        }
      })
    );
  }

  /**
   * ログイン画面に遷移させるか判定する。
   *
   * - ログイン画面が表示されている場合は遷移しない。
   * - セッション関連のAPI呼び出し時は遷移しない。（ログイン画面や、AuthGuardで処理されるため）
   *
   * @param req リクエスト情報
   * @returns true : 遷移する / false : 遷移しない
   */
  private isRedirectLogin(req: HttpRequest<any>): boolean {
    if (this.router.url.startsWith('/login')) {
      return false;
    } else if (req.url.startsWith(SESSION_URL)) {
      return false;
    }

    return true;
  }
}
