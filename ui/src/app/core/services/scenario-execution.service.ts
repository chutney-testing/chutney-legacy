import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '@env/environment';
import { Execution, ScenarioExecutionReport, TestCase, KeyValue } from '@model';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class ScenarioExecutionService {

    public testCaseToExecute: TestCase;

    private resourceUrl = '/api/ui/scenario';

    constructor(private http: HttpClient) {
    }

    findScenarioExecutions(scenarioId: string): Observable<Array<Execution>> {
        return this.http.get<Array<Execution>>(environment.backend + `${this.resourceUrl}/${scenarioId}/execution/v1`)
            .pipe(map((res: Array<Execution>) => {
                return res.map((execution) => Execution.deserialize(execution));
            }));
    }

    executeScenario(scenarioId: string): Observable<ScenarioExecutionReport> {
        return this.http.post<ScenarioExecutionReport>(environment.backend + `${this.resourceUrl}/execution/v1/${scenarioId}`, {})
            .pipe(map((res: ScenarioExecutionReport) => {
                return this.buildExecutionReport(res);
            }));
    }

    executeScenarioAsync(scenarioId: string, computedParameters: Array<KeyValue> = [], env: string): Observable<string> {
        return this.http.post<string>(environment.backend + `${this.resourceUrl}/executionasync/v1/${scenarioId}/${env}`, computedParameters);
    }

    observeScenarioExecution(scenarioId: string, executionId: number): Observable<ScenarioExecutionReport> {
        return this.createScenarioExecutionObservable(environment.backend +
            `${this.resourceUrl}/executionasync/v1/${scenarioId}/execution/${executionId}`);
    }

    findExecutionReport(scenarioId: string, executionId: number): Observable<ScenarioExecutionReport> {
        return this.http.get<ScenarioExecutionReport>(environment.backend + `${this.resourceUrl}/${scenarioId}/execution/${executionId}/v1`)
            .pipe(map((res: Object) => {
                if (res != null && res !== '') {
                    return this.buildExecutionReport(res);
                }
            }));
    }

    stopScenario(scenarioId: string, executionId: number): Observable<void> {
        return this.http.post(environment.backend +
            `${this.resourceUrl}/executionasync/v1/${scenarioId}/execution/${executionId}/stop`, {}).pipe(map((res: Response) => {
            }));
    }

    pauseScenario(scenarioId: string, executionId: number): Observable<void> {
        return this.http.post(environment.backend +
            `${this.resourceUrl}/executionasync/v1/${scenarioId}/execution/${executionId}/pause`, {}).pipe(map((res: Response) => {
            }));
    }

    resumeScenario(scenarioId: string, executionId: number): Observable<void> {
        return this.http.post(environment.backend +
            `${this.resourceUrl}/executionasync/v1/${scenarioId}/execution/${executionId}/resume`, {}).pipe(map((res: Response) => {
            }));
    }

    private createScenarioExecutionObservable(url: string) {
        return new Observable<ScenarioExecutionReport>(obs => {
            let es;
            try {
                es = new EventSource(url);
                es.onerror = () => obs.error('Error loading execution');
                es.addEventListener('partial', (evt: any) => {
                    obs.next(this.buildExecutionReportFromEvent(JSON.parse(evt.data)));
                });
                es.addEventListener('last', (evt: any) => {
                    obs.next(this.buildExecutionReportFromEvent(JSON.parse(evt.data)));
                    obs.complete();
                });
            } catch (error) {
                obs.error('Error creating source event');
            }

            return () => {
                if (es) {
                    es.close();
                }
            };
        });
    }

    private buildExecutionReport(jsonResponse: any): ScenarioExecutionReport {
        return new ScenarioExecutionReport(
            jsonResponse.executionId,
            JSON.parse(jsonResponse.report).report,
            jsonResponse.environment,
            jsonResponse.user,
            jsonResponse.testCaseTitle
        );
    }

    private buildExecutionReportFromEvent(jsonResponse: any): ScenarioExecutionReport {
        return new ScenarioExecutionReport(
            jsonResponse.executionId,
            jsonResponse.report,
            jsonResponse.environment,
            jsonResponse.user,
            jsonResponse.scenarioName
        );
    }
}
