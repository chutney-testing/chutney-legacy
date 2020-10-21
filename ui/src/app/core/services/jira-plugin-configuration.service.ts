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
        return this.http.get<JiraPluginConfiguration>(environment.backend + this.url );
    }

    public save(configuration: JiraPluginConfiguration): Observable<String> {
        return this.http.post(environment.backend + this.url, configuration, {responseType: 'text'});
    }
}
