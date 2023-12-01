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
import { JiraPluginConfiguration } from '@core/model/jira-plugin-configuration.model';

@Injectable({
    providedIn: 'root'
})
export class JiraPluginConfigurationService {

    private url = '/api/ui/jira/v1/configuration';

    constructor(private http: HttpClient) {
    }

    public get(): Observable<JiraPluginConfiguration> {
        return this.http.get<JiraPluginConfiguration>(environment.backend + this.url);
    }

    public getUrl(): Observable<string> {
        return this.http.get(environment.backend + this.url + '/url', {responseType: 'text'});
    }

    public save(configuration: JiraPluginConfiguration): Observable<String> {
        return this.http.post(environment.backend + this.url, configuration, {responseType: 'text'});
    }
}
