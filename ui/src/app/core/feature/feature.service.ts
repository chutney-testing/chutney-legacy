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
import { BehaviorSubject, Observable } from 'rxjs';
import { Feature, FeatureName } from '@core/feature/feature.model';
import { environment } from '@env/environment';
import { tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class FeatureService {

    private readonly featuresApi = '/api/v2/features';

    private features$:BehaviorSubject<Feature[]> = new BehaviorSubject([]);

    constructor(private http: HttpClient) {
    }

    active(featuresName: FeatureName): boolean {
        if(!featuresName) {
            return true;
        }
        const activeFeatures = this.features$.value.filter(feature => feature.active).map(feature => feature.name);
        return  activeFeatures.includes(featuresName);
    }

    loadFeatures(): Observable<Feature[]> {
        return this.http.get<Feature[]>(environment.backend + this.featuresApi)
            .pipe(
                tap(features => this.features$.next(features))
            );
    }
}
