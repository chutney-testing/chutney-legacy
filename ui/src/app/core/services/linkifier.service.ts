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
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';
import { Linkifier } from '@model';

@Injectable({
    providedIn: 'root'
})
export class LinkifierService {

    private url = '/api/v1/ui/plugins/linkifier/';

    constructor(private http: HttpClient) {
    }

    public loadLinkifiers(): Observable<Array<Linkifier>> {
        return this.http.get<Array<Linkifier>>(environment.backend + this.url)
            .pipe(
                map(x => LinkifierService.updateSessionStorage(x))
            );
    }

    private static updateSessionStorage(linkifiers: Array<Linkifier>): Array<Linkifier> {
        sessionStorage.setItem('linkifiers', JSON.stringify(linkifiers));
        return linkifiers;
    }

    public add(linkifier: Linkifier): Observable<String> {
        return this.http.post(environment.backend + this.url, linkifier, {responseType: 'text'});
    }

    public remove(linkifier: Linkifier): Observable<String> {
        return this.http.delete(environment.backend + this.url + linkifier.id, {responseType: 'text'});
    }
}
