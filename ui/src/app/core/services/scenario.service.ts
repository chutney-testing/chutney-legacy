import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { GwtTestCase, ScenarioIndex, TestCase } from '@model';
import { environment } from '@env/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class ScenarioService {

    private resourceUrl = '/api/scenario/v2/raw';
    private resourceUrlV2 = '/api/scenario/v2';

    private static convert(testCase: TestCase): TestCase {
        const copy: TestCase = Object.assign({}, testCase);
        ScenarioService.cleanTags(copy);
        return copy;
    }

    private static convertGwt(gwtTestCase: GwtTestCase): any {
        ScenarioService.cleanTags(gwtTestCase);
        return gwtTestCase.serialize();
    }

    private static cleanTags(scenario: any) {
        if (scenario.tags != null && scenario.tags.length > 0) {
            scenario.tags = scenario.tags.map((tag) => tag.toLocaleUpperCase().trim())
                .reduce((filteredTags, tag) => {
                    if (filteredTags.indexOf(tag) < 0) {
                        filteredTags.push(tag);
                    }
                    return filteredTags;
                }, []);
        }
    }

    constructor(private httpClient: HttpClient) {
    }

    findRawTestCase(id: string): Observable<TestCase> {
        return this.httpClient.get(environment.backend + `${this.resourceUrl}/${id}`).pipe(map((res: TestCase) => {
            return TestCase.fromRaw(res);
        }));
    }

    createOrUpdateRawTestCase(testCase: TestCase): Observable<string> {
        if (testCase.id === undefined) {
            return this.createRawTestCase(testCase);
        } else {
            return this.updateRawTestCase(testCase);
        }
    }

    createRawTestCase(testCase: TestCase): Observable<string> {
        const copy = ScenarioService.convert(testCase);
        return this.httpClient.post<string>(environment.backend + this.resourceUrl, copy);
    }

    updateRawTestCase(testCase: TestCase): Observable<string> {
        const copy = ScenarioService.convert(testCase);
        // an update should better use PUT :(
        return this.httpClient.post<string>(environment.backend + this.resourceUrl, copy);
    }

    findScenarios(): Observable<Array<ScenarioIndex>> {
        return this.httpClient.get<Array<ScenarioIndex>>(environment.backend + this.resourceUrlV2)
        .pipe(map((res: Array<any>) => {
            res = res.map(s => new ScenarioIndex(
                s.metadata.id,
                s.metadata.title,
                s.metadata.description,
                s.metadata.repositorySource,
                s.metadata.creationDate,
                s.metadata.updateDate,
                s.metadata.version,
                s.metadata.author,
                s.metadata.tags,
                s.metadata.executions
            ));

            return res;
        }));
    }

    findTestCase(id: string): Observable<GwtTestCase> {
        return this.httpClient.get<GwtTestCase>(environment.backend + `${this.resourceUrlV2}/${id}`).pipe(map((res: GwtTestCase) => {
            return GwtTestCase.deserialize(res);
        }));
    }

    createOrUpdateGwtTestCase(testCase: GwtTestCase): Observable<string> {
        if (testCase.id === undefined) {
            return this.createGwtTestCase(testCase);
        } else {
            return this.updateGwtTestCase(testCase);
        }
    }

    createGwtTestCase(testCase: GwtTestCase): Observable<string> {
        const gwtTestCaseJsonObject = ScenarioService.convertGwt(testCase);
        return this.httpClient.post<string>(environment.backend + this.resourceUrlV2, gwtTestCaseJsonObject);
    }

    updateGwtTestCase(testCase: GwtTestCase): Observable<string> {
        const gwtTestCaseJsonObject = ScenarioService.convertGwt(testCase);
        return this.httpClient.patch<string>(environment.backend + this.resourceUrlV2, gwtTestCaseJsonObject);
    }

    delete(id: string): Observable<Object> {
        return this.httpClient.delete(environment.backend + `${this.resourceUrlV2}/${id}`);
    }

}
