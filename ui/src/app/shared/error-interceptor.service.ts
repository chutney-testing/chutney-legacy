/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { EMPTY, Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { LoginService } from '@core/services';
import { AlertService } from '@shared';

@Injectable({
    providedIn: 'root'
  })
export class ErrorInterceptor implements HttpInterceptor {

    private sessionExpiredMessage: string = '';

    constructor(
        private router: Router,
        private loginService: LoginService,
        private alertService: AlertService,
        private translateService: TranslateService
    ) {
        this.initTranslation();
    }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (request.headers.get('no-intercept-error') === '') {
        const newHeaders = request.headers.delete('no-intercept-error')
        const newRequest = request.clone({ headers: newHeaders });
        return next.handle(newRequest);
    } else {
        return next.handle(request).pipe(
            catchError(
                (err: any) => {
                    if (err instanceof HttpErrorResponse) {
                        if (err.status === 401 || err.status === 403) {
                            if (this.loginService.isAuthenticated()) {
                                this.loginService.logout();
                                this.alertService.error(this.sessionExpiredMessage, { timeOut: 0, extendedTimeOut: 0, closeButton: true });
                            } else {
                                const requestURL = this.router.url !== undefined ? this.router.url : '';
                                this.loginService.initLogin(requestURL);
                            }
                            return EMPTY;
                        }
                    }
                    return throwError(err);
                }
            )
        );
    }
  }

  private initTranslation() {
    this.getTranslation();
    this.translateService.onLangChange.subscribe(() => {
        this.getTranslation();
    });
  }

  private getTranslation() {
    this.translateService.get('login.expired').subscribe((res: string) => {
        this.sessionExpiredMessage = res;
    });
  }
}
