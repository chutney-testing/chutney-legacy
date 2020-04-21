import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { Campaign, CampaignExecutionReport, TestCase, ScenarioIndex, Dataset } from '@model';
import { HttpClient } from '@angular/common/http';
import { distinct } from '@shared/tools';

@Injectable({
    providedIn: 'root'
})
export class DataSetService {

    private resourceUrl = '';

    constructor(private http: HttpClient) { }

    findAll(): Observable<Array<Dataset>> {
        return of([]);
    }

}
