import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { EMPTY, Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { LoginService } from '@core/services';

@Injectable({
    providedIn: 'root'
  })
export class ErrorInterceptor implements HttpInterceptor {

    constructor(
        private router: Router,
        private loginService: LoginService
    ) {
    }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
        catchError(
            (err: any) => {
                if (err instanceof HttpErrorResponse) {
                    if (err.status === 401 && !this.loginService.isLoginUrl(request.url)) {
                        const requestURL = this.router.url;
                        const cause = this.loginService.isAuthenticated() ? '/expired' : '';
                        this.router.navigateByUrl('/login' + cause + (requestURL != null ? '?url=' + encodeURIComponent(requestURL) : ''));
                        return EMPTY;
                    }
                }
                return throwError(err);
            }
        )
    );
  }
}
