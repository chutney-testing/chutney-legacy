import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject} from 'rxjs';
import { tap, delay } from 'rxjs/operators';

import { environment } from '@env/environment';
import { User, Authorization } from '@model';
import { intersection, contains, isNullOrBlankString } from '@shared/tools';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private url = '/api/v1/user';
  private loginUrl = this.url + '/login';
  private NO_USER = new User('');
  private user$: BehaviorSubject<User> = new BehaviorSubject(this.NO_USER);

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

  initLogin(url?: string) {
    this.currentUser(true).pipe(
        tap(user => this.setUser(user))
    ).subscribe(
        () => this.navigateAfterLogin(url),
        () => {
            const queryParams: Object = isNullOrBlankString(url) ? {} : { queryParams: { url: url } };
            this.router.navigate(['login'], queryParams);
        }
    );
  }

  login(username: string, password: string): Observable<User> {

    if (isNullOrBlankString(username) && isNullOrBlankString(password)) {
      return this.currentUser().pipe(
        tap(user => this.setUser(user))
      );
    }

    const body = new URLSearchParams();
    body.set('username', username);
    body.set('password', password);

    const options = {
      headers: new HttpHeaders()
                .set('Content-Type', 'application/x-www-form-urlencoded')
                .set('no-intercept-error', '')
    };

    return this.http.post<User>(environment.backend + this.loginUrl, body.toString(), options)
      .pipe(
        tap(user => this.setUser(user))
      );
  }

  navigateAfterLogin(url?: string) {
    if (this.isAuthenticated()) {
        const user: User = this.user$.getValue();
        this.router.navigateByUrl(url ? url : this.defaultForwardUrl(user));
    }
  }

  logout() {
    this.http.post(environment.backend + this.url + '/logout', null).pipe(
        tap(() => this.setUser(this.NO_USER)),
        delay(500)
    ).subscribe(
        () => {
            this.router.navigateByUrl('/');
        }
    );
  }

  getUser(): Observable<User> {
    return this.user$;
  }

  isAuthenticated(): boolean {
    const user: User = this.user$.getValue();
    return this.NO_USER !== user;
  }

  hasAuthorization(authorization: Array<Authorization> | Authorization = [], u: User = null): boolean {
    const user: User = u || this.user$.getValue();
    const auth = [].concat(authorization);
    if (user != this.NO_USER) {
        return auth.length == 0 || intersection(user.authorizations, auth).length > 0;
    }
    return false;
  }

  isLoginUrl(url: string): boolean {
    return url.includes(this.loginUrl);
  }

  private setUser(user: User) {
    this.user$.next(user);
  }

  private currentUser(skipInterceptor: boolean = false): Observable<User> {
    const options = {
      headers: { 'no-intercept-error': ''}
    };
    return this.http.get<User>(environment.backend + this.url, skipInterceptor ? options : {});
  }

  private defaultForwardUrl(user: User): string {
    const authorizations = user.authorizations;
    if (authorizations) {
        if (contains(authorizations, Authorization.SCENARIO_READ)) return '/scenario';
        if (contains(authorizations, Authorization.CAMPAIGN_READ)) return '/campaign';
        if (contains(authorizations, Authorization.COMPONENT_READ)) return '/component';
        if (contains(authorizations, Authorization.ENVIRONMENT_ACCESS)) return '/environmentAdmin';
        if (contains(authorizations, Authorization.GLOBAL_VAR_READ)) return '/variable';
        if (contains(authorizations, Authorization.DATASET_READ)) return '/dataset';
        if (contains(authorizations, Authorization.ADMIN_ACCESS)) return '/';
    }

    return '/login';
  }
}
