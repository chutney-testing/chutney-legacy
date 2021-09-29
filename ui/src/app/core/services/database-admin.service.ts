import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class DatabaseAdminService {

  private adminUrl = '/api/v1/admin/database';

  constructor(private http: HttpClient) { }

  execute(statement: string, database: string = 'jdbc'): Observable<Object> {
    return this.http.post(environment.backend + this.adminUrl + '/execute/' + database, statement);
  }

  paginate(statement: string, database: string = 'jdbc', pageNumber: number = 1, elementPerPage: number = 5): Observable<Object> {
    return this.http.post(environment.backend + this.adminUrl + '/paginate/' + database,
      {
        pageNumber: pageNumber,
        elementPerPage: elementPerPage,
        wrappedRequest: statement
      });
  }
}
