import { Injectable } from '@angular/core';
import { Router, CanActivate, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { LoginService } from '@core/services';
import { AlertService } from '@shared';
import { Authorization } from '@model';

@Injectable({
  providedIn: 'root'
})
// TODO Separate authentication and authorization
export class AuthGuard implements CanActivate {

  private unauthorizedMessage: string = '';

  public constructor(
    private router: Router,
    private loginService: LoginService,
    private alertService: AlertService,
    private translateService: TranslateService
  ) {
    this.initTranslation();
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const requestURL = state.url !== undefined ? state.url : '';

    if (!this.loginService.isAuthenticated()) {
        this.loginService.initLogin(requestURL);
        return false;
    }

    const authorizations: Array<Authorization> = route.data['authorizations'] || [];
    if (this.loginService.hasAuthorization(authorizations)) {
        return true;
    } else {
        this.alertService.error(this.unauthorizedMessage, { timeOut: 0, extendedTimeOut: 0, closeButton: true });
        this.loginService.navigateAfterLogin();
        return false;
    }
  }

  private initTranslation() {
    this.getTranslation();
    this.translateService.onLangChange.subscribe(() => {
        this.getTranslation();
    });
  }

  private getTranslation() {
    this.translateService.get('login.unauthorized').subscribe((res: string) => {
        this.unauthorizedMessage = res;
    });
  }
}
