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
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { Dataset, KeyValue } from '@model';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class DataSetService {

    private resourceUrl = '/api/v1/datasets';

    constructor(private httpClient: HttpClient) {
    }

    findAll(): Observable<Array<Dataset>> {
        return this.httpClient.get<Array<Dataset>>(environment.backend + this.resourceUrl)
            .pipe(map((res: Array<any>) => {
                res = res.map(dto => new Dataset(
                    dto.name,
                    dto.description,
                    dto.tags,
                    dto.lastUpdated,
                    [],
                    [],
                    dto.version,
                    dto.id
                ));

                return res;
            }));
    }

    findById(id: string): Observable<Dataset> {

        return this.httpClient.get<Dataset>(environment.backend + this.resourceUrl + '/' + id)
            .pipe(
                map(dto => this.fromDto(dto))
            );
    }

    save(dataset: Dataset, oldId?: string): Observable<Dataset> {
        DataSetService.cleanTags(dataset);
        if (dataset.id && dataset.id.length > 0) {
            return this.httpClient.put<Dataset>(environment.backend + this.resourceUrl, dataset, {params: {oldId}})
                .pipe(
                    map(dto => this.fromDto(dto))
                );
        } else {
            return this.httpClient.post<Dataset>(environment.backend + this.resourceUrl, dataset)
                .pipe(
                    map(dto => this.fromDto(dto))
                );
        }
    }

    delete(id: String): Observable<Object> {
        return this.httpClient.delete(environment.backend + this.resourceUrl + '/' + id);
    }

    private fromDto(dto: any): Dataset {
        return new Dataset(
            dto.name,
            dto.description,
            dto.tags,
            dto.lastUpdated,
            dto.uniqueValues.map(o => new KeyValue(o.key, o.value)),
            dto.multipleValues.map(l => l.map(o => new KeyValue(o.key, o.value))),
            dto.version,
            dto.id);
    }

    private static cleanTags(dataset: Dataset) {
        if (dataset.tags != null && dataset.tags.length > 0) {
            dataset.tags = dataset.tags.map((tag) => tag.toLocaleUpperCase().trim())
                .reduce((filteredTags, tag) => {
                    if (filteredTags.indexOf(tag) < 0) {
                        filteredTags.push(tag);
                    }
                    return filteredTags;
                }, []);
        }
    }

}
