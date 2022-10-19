import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { JiraScenario,JiraTestExecutionScenarios } from '@model';


@Injectable({
    providedIn: 'root'
})
export class JiraPluginService {

    private url = '/api/ui/jira/v1/';
    private scenarioUrl = this.url + 'scenario';
    private campaignUrl = this.url + 'campaign';
    private campaignExecutionUrl = this.url + 'campaign_execution';
    private testExecUrl = this.url + 'testexec';

    constructor(private http: HttpClient) {
    }

    public findScenarios(): Observable<Map<string, string>> {
        return this.http.get<any>(environment.backend + this.scenarioUrl )
        .pipe(map((res: Object) => new Map(Object.entries(res))));
    }

    public findCampaigns(): Observable<Map<string, string>> {
        return this.http.get<any>(environment.backend + this.campaignUrl )
        .pipe(map((res: Object) => new Map(Object.entries(res))));
    }

    public findTestExecScenarios(testExecId: string): Observable<JiraScenario[]> {
        return this.http.get<any>(environment.backend + this.testExecUrl + '/' + testExecId)
        .pipe(map((res: JiraScenario[]) => res));
    }

    public findTestExecScenariosByCampaignExecution(campaignExecutionId: number): Observable<JiraTestExecutionScenarios> {
        return this.http.get<any>(environment.backend + this.campaignExecutionUrl + '/' + campaignExecutionId)
        .pipe(map((res: JiraTestExecutionScenarios) => res));
    }

    public findByScenarioId(scenarioId: string): Observable<string> {
        return this.http.get<JiraScenario>(environment.backend + this.scenarioUrl + '/' + scenarioId)
            .pipe(map((jiraDto: JiraScenario) => {
                return jiraDto.id;
            }));
    }

    public saveForScenario(scenarioId: string, jiraId: string): Observable<JiraScenario> {
        return this.http.post<JiraScenario>(environment.backend + this.scenarioUrl, new JiraScenario(jiraId, scenarioId));
    }

    public removeForScenario(scenarioId: string) {
        return this.http.delete<HttpResponse<any>>(environment.backend + this.scenarioUrl  + '/' + scenarioId);
    }

    public findByCampaignId(campaignId: number): Observable<string> {
        return this.http.get<JiraScenario>(environment.backend + this.campaignUrl + '/' + campaignId)
            .pipe(map((jiraDto: JiraScenario) => {
                return jiraDto.id;
            }));
    }

    public saveForCampaign(campaignId: number, jiraId: string): Observable<JiraScenario> {
        return this.http.post<JiraScenario>(environment.backend + this.campaignUrl, new JiraScenario(jiraId, campaignId.toString()));
    }

    public removeForCampaign(campaignId: number) {
        return this.http.delete<HttpResponse<any>>(environment.backend + this.campaignUrl + '/' + campaignId);
    }

    public updateScenarioStatus(testExecId: string, scenarioId: string, newStatus: string) {
        return this.http.put<HttpResponse<any>>(environment.backend + this.testExecUrl + '/' + testExecId,
                                                new JiraScenario('', scenarioId, newStatus));
    }
}
