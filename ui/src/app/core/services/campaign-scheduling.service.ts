import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { HttpClient } from '@angular/common/http';
import { CampaignScheduling } from '@core/model/campaign/campaign-scheduling.model';

@Injectable({
    providedIn: 'root'
})
export class CampaignSchedulingService {

    private resourceUrl = '/api/ui/campaign/v1/scheduling';

    constructor(private http: HttpClient) { }


    findAll(): Observable<Array<CampaignScheduling>> {
        return this.http.get<Array<CampaignScheduling>>(environment.backend + this.resourceUrl).pipe(map((res: Array<CampaignScheduling>) => {
            //res.sort((a, b) => a.executionScheduled < b.executionScheduled));
            return res;
        }));
    }

    create(campaignScheduling: CampaignScheduling): Observable<CampaignScheduling> {
        return this.http.post<CampaignScheduling>(environment.backend + this.resourceUrl, campaignScheduling);
    }

    delete(id: number): Observable<Object> {
        return this.http.delete(environment.backend + `${this.resourceUrl}/${id}`);
    }


}
