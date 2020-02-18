import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserSession } from '@model';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/internal/operators';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  public static readonly USER_SESSION_KEY = 'userSession';

  private currentUser: User;

  constructor(
    private http: HttpClient
  ) { }

  private SESSION_MAX_DURATION_IN_HOURS = 24;

  login(username: string, password: string): Observable<{} | User> {

    const body = new URLSearchParams();
    body.set('username', username);
    body.set('password', password);

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post<User>(environment.backend + '/api/v1/user/login', body.toString(), options)
      .pipe(
        map(user => {
          if (user) {
            this.currentUser = user;
            localStorage.setItem(LoginService.USER_SESSION_KEY, JSON.stringify(new UserSession(user, new Date().getTime())));
          }
          return user;
        }));
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
}

