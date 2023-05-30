import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { SessionService } from '../service/session.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private sessionService: SessionService,
    private router: Router
  ) { }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.isAuth(next, state.url);
  }

  /**
   * 操作権限を確認する.
   * @param next ActivatedRouteSnapshot
   * @param url URL
   * @returns Observable<boolean>
   */
  private isAuth(next: ActivatedRouteSnapshot, url: string): Observable<boolean> {
    return this.sessionService.getSession().pipe(
      map((session) => {
        return next.data.authFn ? next.data.authFn(session) : true;
      }),
      catchError(() => {
        this.router.navigate(['login'], { queryParams: { redir: url } });
        return of(false);
      })
    );
  }
}
