import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '@env/environment';
import { Configuration } from '@core/model/configuration.model';

@Injectable({
    providedIn: 'root'
})
export class ConfigurationService {

    private url = '/api/ui/jira/v1/configuration';

    constructor(private http: HttpClient) {
    }

    public get(): Observable<Configuration> {
        return this.http.get<Configuration>(environment.backend + this.url );
    }

    public save(configuration: Configuration): Observable<String> {
        return this.http.post(environment.backend + this.url, configuration, {responseType: 'text'});
    }
}
