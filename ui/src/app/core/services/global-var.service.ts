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
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class GlobalVariableService {

    private url = '/api/ui/globalvar/v1';

    constructor(private http: HttpClient) {
    }

    public get(fileName: string): Observable<string> {
        return this.http.get<Object>(environment.backend + this.url + '/' + fileName).pipe(map((res: Object) => {
            return res['message'];
        }));
    }

    public save(fileName: string, content: string): Observable<Object> {
        return this.http.post(environment.backend + this.url + '/' + fileName, {message: content}, {responseType: 'text'});
    }

    public list() {
        return this.http.get<Object>(environment.backend + this.url).pipe(map((res: Object) => {
            return res;
        }));
    }

    delete(fileName: string) {
        return this.http.delete<Object>(environment.backend + this.url + '/' + fileName);
    }
}
