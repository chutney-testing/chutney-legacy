import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { isNullOrBlankString } from '@shared/tools';

@Injectable({
  providedIn: 'root'
})
export class StepService {

  private stepUrl = '/api/steps/v1';

  constructor(private http: HttpClient) {
  }

  findAllSteps(start: number, limit: number, name: string, usage: string, sort: string): Observable<Object> {
    let params = '?start=' + start + '&limit=' + limit;

    if (!isNullOrBlankString(name)) {

      params += '&name=' + encodeURIComponent(name);
    }

    if (usage != null && usage !== 'all') {
      params += '&usage=' + usage;
    }

    if (sort != null) {
      if (sort === 'desc') {
        params += '&sort=name&desc=name';
      } else {
        params += '&sort=name';
      }
    }

    return this.http.get(environment.backend + this.stepUrl + params);
  }

  findById(stepId: string): Observable<Object> {
    return this.http.get(environment.backend + this.stepUrl + '/' + encodeURIComponent(stepId));
  }

  findIdenticalStep(nameQuery: string = ''): Observable<Object> {
    return this.http.post(environment.backend + this.stepUrl + '/search/name', nameQuery);
  }
}
