import { Injectable } from '@angular/core';
import { Router, CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { LoginService } from '@core/services';
import { Authorization } from '@model';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  public constructor(private router: Router,
    private loginService: LoginService) { }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const requestURL = state.url !== undefined ? state.url : '';
    const authorizations: Array<Authorization> = route.data['authorizations'] || [];
    return this._canActivate(requestURL, authorizations);
  }

  private _canActivate(requestURL: string, authorizations: Array<Authorization>): Observable<boolean> | Promise<boolean> | boolean {
    if (this.loginService.hasAuthorization(authorizations)) {
        return true;
    } else {
        this.router.navigateByUrl('/login' + (requestURL != null ? '?url=' + encodeURIComponent(requestURL) : ''));
        return false;
    }
  }
}
