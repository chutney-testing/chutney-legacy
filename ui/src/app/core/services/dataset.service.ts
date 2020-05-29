import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { Dataset } from '@model';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class DataSetService {

    private resourceUrl = '/api/v1/datasets';

    constructor(private httpClient: HttpClient) { }

    findAll(): Observable<Array<Dataset>> {
        return this.httpClient.get<Array<Dataset>>(environment.backend + this.resourceUrl)
            .pipe(map((res: Array<any>) => {
                res = res.map(dto => new Dataset(
                    dto.name,
                    dto.description,
                    dto.tags,
                    dto.lastUpdated,
                    dto.uniqueValues,
                    dto.multipleValues,
                    dto.version,
                    dto.id
                ));

                return res;
            }));
    }

    findById(id: string): Observable<Dataset> {
        return this.httpClient.get<Dataset>(environment.backend + this.resourceUrl + '/' + id)
            .pipe(map((dto: any) => {
                    return new Dataset(
                        dto.name,
                        dto.description,
                        dto.tags,
                        dto.lastUpdated,
                        dto.uniqueValues,
                        dto.multipleValues,
                        dto.version,
                        dto.id);
                }
            ));
    }

    save(dataset: Dataset): Observable<Dataset> {
        return this.httpClient.post<Dataset>(environment.backend + this.resourceUrl, dataset);
    }

    delete(id: String): Observable<Object> {
        return this.httpClient.delete(environment.backend + this.resourceUrl + '/' + id);
    }
}
