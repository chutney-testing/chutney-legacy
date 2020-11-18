import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, finalize } from 'rxjs/internal/operators';

import { environment } from '../../../environments/environment';
import { User, UserSession } from '@model';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private user$: BehaviorSubject<User>;

  constructor(
    private http: HttpClient
  ) {
    this.user$ = new BehaviorSubject(null);
  }

  login(username: string, password: string): Observable<User> {

    const body = new URLSearchParams();
    body.set('username', username);
    body.set('password', password);

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post<User>(environment.backend + '/api/v1/user/login', body.toString(), options)
      .pipe(
        tap(user => this.setUser(user))
      );
  }

  logout() {
    this.http.post(environment.backend + '/api/v1/user/logout', null).pipe(
        finalize(() => this.setUser(null))
    ).subscribe(() => { }, () => { });
  }

  getUser(): Observable<User> {
    return this.user$;
  }

  private setUser(user: User) {
    this.user$.next(user);
  }
}
