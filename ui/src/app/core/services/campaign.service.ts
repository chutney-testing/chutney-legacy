import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { Campaign, CampaignExecutionReport, TestCase, ScenarioIndex } from '@model';
import { HttpClient } from '@angular/common/http';
import { distinct } from '@shared/tools';

@Injectable({
    providedIn: 'root'
})
export class CampaignService {

    private resourceUrl = '/api/ui/campaign/v1';
    private ressourceUrlExecution = '/api/ui/campaign/execution/v1';

    constructor(private http: HttpClient) { }

    public static existRunningCampaignReport(lastCampaignReports: Array<CampaignExecutionReport>): boolean {
        return lastCampaignReports && distinct(lastCampaignReports.map(report => report.status)).includes('RUNNING')
    }

    findAllCampaigns(): Observable<Array<Campaign>> {
        return this.http.get<Array<Campaign>>(environment.backend + this.resourceUrl).pipe(map((res: Array<Campaign>) => {
            res.sort((a, b) => a.title.localeCompare(b.title));
            return res;
        }));
    }

    findLastCampaignReports(nbReports: number = 10): Observable<Array<CampaignExecutionReport>> {
        return this.http.get<Array<CampaignExecutionReport>>(environment.backend + this.resourceUrl + `/lastexecutions/${nbReports}`);
    }

    findAllScenarios(id: number): Observable<Array<ScenarioIndex>> {
        return this.http.get<Array<ScenarioIndex>>(environment.backend + this.resourceUrl + `/${id}/scenarios`)
        .pipe(map((res: Array<any>) => {
            return res.map(s => new ScenarioIndex(
                s.metadata.id,
                s.metadata.title,
                s.metadata.description,
                s.metadata.repositorySource,
                s.metadata.creationDate,
                s.metadata.tags,
                s.metadata.executions
            ));
        }));
    }

    findAllCampaignsForScenario(id: number): Observable<Array<Campaign>> {
        return this.http.get<Array<Campaign>>(environment.backend + this.resourceUrl + `/scenario/${id}`)
            .pipe(map((res: Array<Campaign>) => {
                return res;
            }));
    }

    find(id: number): Observable<Campaign> {
        return this.http.get<Campaign>(environment.backend + `${this.resourceUrl}/${id}`);
    }

    create(campaign: Campaign): Observable<Campaign> {
        const copy = this.convert(campaign);
        return this.http.post<Campaign>(environment.backend + this.resourceUrl, copy);
    }

    delete(id: number): Observable<Object> {
        return this.http.delete(environment.backend + `${this.resourceUrl}/${id}`);
    }

    update(campaign: Campaign): Observable<Campaign> {
        const copy = this.convert(campaign);
        return this.http.put<Campaign>(environment.backend + this.resourceUrl, copy);
    }

    executeCampaign(campaignId: number, env: string): Observable<CampaignExecutionReport> {
        return this.http.get<CampaignExecutionReport>(environment.backend + `${this.ressourceUrlExecution}/byID/${campaignId}/${env}`);
    }

    stopExecution(campaignId: number, executionId: number): Observable<void> {
        return this.http.post(environment.backend +
            `${this.ressourceUrlExecution}/${executionId}/stop`, {}).pipe(map((res: Response) => {
        }));
    }

    replayFailedScenario(executionId: number): Observable<Object> {
        return this.http.post<CampaignExecutionReport>(environment.backend + `${this.ressourceUrlExecution}/replay/${executionId}`, null);
    }

    private convert(campaign: Campaign): Campaign {
        delete campaign.campaignExecutionReports;
        return Object.assign({}, campaign);
    }

}
