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
import { map, Observable } from 'rxjs';
import { Metric } from '@core/model/metric.model';

@Injectable({
    providedIn: 'root'
})
export class PrometheusService {

    private url = '/actuator';
    constructor(private http: HttpClient) {
    }

    public getMetrics(): Observable<Metric[]> {
        return this.http.get(environment.backend + this.url + '/prometheus', { responseType: 'text' })
        .pipe(map((res: string) => {
            const metricRegex = new RegExp('(?<name>[^{]*)(?<tags>{.*})? (?<value>.*)');
            return res.split('\n')
            .filter(element => element && !element.startsWith('#'))
            .map(element => {
                const [, name, tags, value] = metricRegex.exec(element) || [];
                return new Metric(name, tags, value);
            });
        }));        
    }
}
