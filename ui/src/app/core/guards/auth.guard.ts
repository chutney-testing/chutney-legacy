import { Injectable } from '@angular/core';
import { Router, CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable } from 'rxjs';

import { LoginService } from '@core/services';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  public constructor(private router: Router,
    private loginService: LoginService) { }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const requestURL = state.url !== undefined ? state.url : '';
    return this._canActivate(requestURL);
  }

  private _canActivate(requestURL: string): Observable<boolean> | Promise<boolean> | boolean {
    if (this.loginService.getUser() != null) {
      return true;
    }
    this.router.navigateByUrl('/login' + (requestURL != null ? '?url=' + encodeURIComponent(requestURL) : ''));
    return false;
  }
}
