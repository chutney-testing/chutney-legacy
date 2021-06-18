import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, finalize } from 'rxjs/internal/operators';

import { environment } from '../../../environments/environment';
import { User, Authorization } from '@model';
import { intersection } from '@shared/tools/array-utils';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private url = '/api/v1/user';
  private loginUrl = '/api/v1/user/login';
  private NO_USER = new User('');
  private user$: BehaviorSubject<User> = new BehaviorSubject(this.NO_USER);

  constructor(
    private http: HttpClient
  ) {
    this.currentUser().subscribe(
        res => this.setUser(res),
        err => this.setUser(this.NO_USER)
    );
  }

  login(username: string, password: string): Observable<User> {

    const body = new URLSearchParams();
    body.set('username', username);
    body.set('password', password);

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post<User>(environment.backend + this.loginUrl, body.toString(), options)
      .pipe(
        tap(user => this.setUser(user))
      );
  }

  logout() {
    this.http.post(environment.backend + this.url + '/logout', null).pipe(
        finalize(() => this.setUser(this.NO_USER))
    ).subscribe(() => { }, () => { });
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

  private currentUser(): Observable<User> {
    return this.http.get<User>(environment.backend + this.url);
  }
}
