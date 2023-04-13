import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { Dataset, KeyValue } from '@model';
import { HttpClient } from '@angular/common/http';
import { FeatureName } from '@core/feature/feature.model';
import { FeatureService } from '@core/feature/feature.service';

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
        if (dataset.id && dataset.id.length > 0) {
            return this.httpClient.put<Dataset>(environment.backend + this.resourceUrl, dataset, {params: {oldId: oldId}})
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

}
