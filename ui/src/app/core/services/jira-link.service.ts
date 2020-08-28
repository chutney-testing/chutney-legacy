import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class JiraLinkService {

    private url = '/api/ui/jira/v1/';
    private scenarioUrl = this.url + 'scenario';
    private campaignUrl = this.url + 'campaign';

    constructor(private http: HttpClient) {
    }

    public findByScenarioId(scenarioId: string): Observable<string> {
        return this.http.get<jiraDto>(environment.backend + this.scenarioUrl + '/' + scenarioId)
            .pipe(map((jiraDto: jiraDto) => {
                return jiraDto.id;
        }));
    }

    public saveForScenario(scenarioId: string, jiraId: string): Observable<jiraDto> {
        return this.http.post<jiraDto>(environment.backend + this.scenarioUrl , new jiraDto(jiraId,scenarioId));
    }

    public findByCampaignId(campaignId: number): Observable<string> {
        return this.http.get<jiraDto>(environment.backend + this.campaignUrl + '/' + campaignId)
            .pipe(map((jiraDto: jiraDto) => {
                return jiraDto.id;
        }));
    }

    public saveForCampaign(campaignId: number, jiraId: string): Observable<jiraDto> {
        return this.http.post<jiraDto>(environment.backend + this.campaignUrl , new jiraDto(jiraId,campaignId.toString()));
    }
}

export class jiraDto {
    constructor(
        public id: string,
        public chutneyId: string) {
    }
}