import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/internal/operators';

import { environment } from '../../../environments/environment';
import { User, UserSession } from '@model';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  public static readonly USER_SESSION_KEY = 'userSession';

  constructor(
    private http: HttpClient
  ) { }

  private SESSION_MAX_DURATION_IN_HOURS = 24;

  login(username: string, password: string): Observable<User> {

    const body = new URLSearchParams();
    body.set('username', username);
    body.set('password', password);

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post<User>(environment.backend + '/api/v1/user/login', body.toString(), options)
      .pipe(
        tap(user => this.addUser(user))
      );
  }

  logout() {
    this.removeUser();
    this.http.post(environment.backend + '/api/v1/user/logout', null).subscribe(() => { }, () => { });
  }

  getUser(): User | null {
    const userSessionString = localStorage.getItem(LoginService.USER_SESSION_KEY);
    if (userSessionString != null) {
      const userSession: UserSession = JSON.parse(userSessionString);
      if (new Date().getTime() - userSession.startTime > this.SESSION_MAX_DURATION_IN_HOURS * 60 * 60 * 1000) {
        this.removeUser();
        return null;
      } else {
        return userSession.user;
      }
    } else {
      return null;
    }
  }

  private removeUser() {
    localStorage.removeItem(LoginService.USER_SESSION_KEY);
  }

  private addUser(user: User) {
    if (user) {
        localStorage.setItem(LoginService.USER_SESSION_KEY, JSON.stringify(new UserSession(user, new Date().getTime())));
    }
  }
}

