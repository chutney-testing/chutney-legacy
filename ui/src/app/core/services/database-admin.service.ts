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
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '@env/environment';
import { Execution } from '@core/model';

@Injectable({
  providedIn: 'root'
})
export class DatabaseAdminService {

  private adminUrl = '/api/v1/admin/database';

  constructor(private http: HttpClient) { }

  getExecutionReportMatchQuery(query: string): Observable<Execution[]> {
    return this.http.get<Execution[]>(environment.backend + this.adminUrl + '/execution', {params: {query: query}})
    .pipe(
      map((res: Execution[]) => {
          return res.map((execution) => Execution.deserialize(execution));
      })
    )
  }

  compactDatabase(): Observable<number[]> {
    return this.http.post<number[]>(environment.backend + this.adminUrl + '/compact', null);
  }

  computeDatabaseSize(): Observable<number> {
    return this.http.get<number>(environment.backend + this.adminUrl + '/size');
  }
}
