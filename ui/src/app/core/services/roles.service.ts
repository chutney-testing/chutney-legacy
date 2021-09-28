import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class RolesService {

  private url = '/api/v1/authorizations';

  constructor(private http: HttpClient) { }

  read(): Observable<Object> {
    return this.http.get(environment.backend + this.url);
  }

  save(roles: Object): Observable<Object> {
    return this.http.post(environment.backend + this.url, roles);
  }
}
