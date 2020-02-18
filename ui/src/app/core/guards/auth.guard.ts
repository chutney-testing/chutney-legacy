import { Injectable } from '@angular/core';
import { Router, CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { LoginService } from '@core/services';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  public constructor(private router: Router,
    private loginService: LoginService) { }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const requestURL = state.url !== undefined ? state.url : '';
    return this._canActivate(requestURL);
  }

  public tryActivate(currentURL: string): boolean {
    return this._canActivate(currentURL);
  }

  private _canActivate(requestURL: string): boolean {
    if (this.loginService.getUser() != null) {
      return true;
    }
    this.router.navigateByUrl('/login' + (requestURL != null ? '?url=' + encodeURIComponent(requestURL) : ''));
    return false;
  }
}
