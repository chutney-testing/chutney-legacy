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

    if (route.data['authenticate']) {
        if (!this.loginService.isAuthenticated()) {
            this.router.navigateByUrl('/login');
            return false;
        }
    }

    const authorizations: Array<Authorization> = route.data['authorizations'] || [];
    if (this.loginService.hasAuthorization(authorizations)) {
        return true;
    } else {
        this.router.navigateByUrl('/login/unauthorized');
        return false;
    }
  }
}
