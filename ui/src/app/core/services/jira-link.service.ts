import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class JiraLinkService {

    private url = '/api/ui/jira/v1/';
    private scenarioUrl = this.url + 'scenario/';
    private campaignUrl = this.url + 'campaign/';

    constructor(private http: HttpClient) {
    }

    public findByScenarioId(scenarioId: string): Observable<string> {
        return this.http.get<Object>(environment.backend + this.scenarioUrl + scenarioId).pipe(map((res: Object) => {
            return res['message'];
        }));
    }

    public saveForScenario(scenarioId: string, jiraId: string): Observable<Object> {
        return this.http.post(environment.backend + this.scenarioUrl + scenarioId, {message: jiraId}, {responseType: 'text'});
    }

    public findByCampaignId(campaignId: number): Observable<string> {
        return this.http.get<Object>(environment.backend + this.campaignUrl + campaignId).pipe(map((res: Object) => {
            return res['message'];
        }));
    }

    public saveForCampaign(campaignId: number, jiraId: string): Observable<Object> {
        return this.http.post(environment.backend + this.campaignUrl + campaignId, {message: jiraId}, {responseType: 'text'});
    }
}

