import { Component, OnDestroy, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, delay, tap } from 'rxjs/internal/operators';

import { EventManagerService } from '@shared/event-manager.service';

import { Execution, GwtTestCase, ScenarioComponent, ScenarioExecutionReport, StepExecutionReport, TestCase, Authorization } from '@model';
import { ComponentService, ScenarioExecutionService, ScenarioService } from '@core/services';

@Component({
    selector: 'chutney-execution',
    providers: [Location],
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.scss']
})
export class ScenarioExecutionComponent implements OnInit, OnDestroy {

    scenarioComponent$: Observable<ScenarioComponent> = null;
    scenarioGwt$: Observable<GwtTestCase> = null;

    parseError: String;
    executionError: String;

    currentScenarioId: string;
    currentExecutionId: number;

    scenarioExecutionReport: ScenarioExecutionReport;

    lastExecutionRunning = false;
    toggleScenarioDetails = true;
    toggleScenarioInfo = true;

    private isComposed = TestCase.isComposed;
    private hasParameters: boolean = null;

    private routeParamsSubscription: Subscription;
    private scenarioExecutionAsyncSubscription: Subscription;

    Authorization = Authorization;

    constructor(
        private eventManager: EventManagerService,
        private scenarioService: ScenarioService,
        private scenarioExecutionService: ScenarioExecutionService,
        private componentService: ComponentService,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location,
    ) {
    }

    ngOnInit() {
        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            this.currentScenarioId = params['id'];
            if (params['execId'] && params['execId'] !== 'last') {
                this.loadScenarioExecution(params['execId']);
            }

            this.loadScenario();
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.routeParamsSubscription);
        this.unsubscribeScenarioExecutionAsyncSubscription();
    }

    loadScenario() {
        if (this.isComposed(this.currentScenarioId)) {
            this.scenarioComponent$ = this.componentService
                .findComponentTestCaseWithoutDeserializeImpl(this.currentScenarioId).pipe(
                    tap(sc => {
                        this.hasParameters = (sc.computedParameters && sc.computedParameters.length > 0);
                    })
                );
        } else {
            this.scenarioGwt$ = this.scenarioService
                .findTestCase(this.currentScenarioId).pipe(
                     tap(gwt => {
                        this.hasParameters = (gwt.wrappedParams.params && gwt.wrappedParams.params.length > 0);
                    })
                 );
        }
    }

    onlastStatusExecution(status: String) {
        this.lastExecutionRunning = (status === 'RUNNING' || status === 'PAUSED');
    }

    onLastIdExecution(execution: Execution) {
        if (Execution.NO_EXECUTION === execution) {
            this.currentExecutionId = null;
        } else if (this.currentExecutionId !== execution.executionId) {
            this.currentExecutionId = execution.executionId;
            if (!this.scenarioExecutionAsyncSubscription || this.scenarioExecutionAsyncSubscription.closed) {
                if ('RUNNING' === execution.status) {
                    this.observeScenarioExecution(execution.executionId);
                } else {
                    this.loadScenarioExecution(execution.executionId);
                }
            }
        }
    }

    onSelectExecution(execution: Execution) {
        if (execution != null) {
            this.currentExecutionId = execution.executionId;
            this.executionError = '';

            this.unsubscribeScenarioExecutionAsyncSubscription();

            if ('RUNNING' === execution.status) {
                this.observeScenarioExecution(execution.executionId);
            } else {
                this.loadScenarioExecution(execution.executionId);
            }
        } else {
            this.currentExecutionId = null;
            this.executionError = '';
            this.scenarioExecutionReport = null;
        }
    }

    loadScenarioExecution(executionId: number) {
        this.executionError = '';
        this.currentExecutionId = executionId;
        this.scenarioExecutionService.findExecutionReport(this.currentScenarioId, executionId)
            .subscribe((scenarioExecutionReport: ScenarioExecutionReport) => {

                this.updateLocation(executionId);

                if (scenarioExecutionReport && scenarioExecutionReport.report.status === 'RUNNING') {
                    this.observeScenarioExecution(executionId);
                } else {
                    this.toggleScenarioDetails = true;
                    this.scenarioExecutionReport = scenarioExecutionReport;
                }

            }, error => {
                console.error(error.message);
                this.executionError = 'Cannot find execution n°' + executionId;
                this.scenarioExecutionReport = null;
            });
    }

    executeScenario(env: string) {
        if (this.hasParameters == null) {
            let scenario$ : Observable<Object> = this.isComposed(this.currentScenarioId) ? this.scenarioComponent$ : this.scenarioGwt$;
            scenario$.subscribe(() => this.executeScenario(env));
        } else if (this.hasParameters) {
            this.router.navigateByUrl(`/scenario/${this.currentScenarioId}/execute/${env}`)
                .then(null);
        } else {
            this.scenarioExecutionService.executeScenarioAsync(this.currentScenarioId, [], env)
                .pipe(
                    delay(1000)
                )
                .subscribe(
                    executionId => {
                        this.loadScenarioExecution(parseInt(executionId, 10));
                    },
                    error => {
                        this.executionError = error.error;
                    }
                );
        }
    }

    expandAllDetails() {
        this.toggleScenarioDetails = !this.toggleScenarioDetails;
        this.eventManager.broadcast({name: 'toggleScenarioDetails', expand: this.toggleScenarioDetails});
    }

    expandAllInfo() {
        this.toggleScenarioInfo = !this.toggleScenarioInfo;
        this.eventManager.broadcast({name: 'toggleScenarioInfo', expand: this.toggleScenarioInfo});
    }

    stopScenario() {
        this.scenarioExecutionService.stopScenario(this.currentScenarioId, this.currentExecutionId).subscribe(() => {
        }, error => {
            const body = JSON.parse(error._body);
            this.executionError = 'Cannot stop scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
        }, () => {
        });
    }

    pauseScenario() {
        this.scenarioExecutionService.pauseScenario(this.currentScenarioId, this.currentExecutionId).subscribe(() => {
        }, error => {
            const body = JSON.parse(error._body);
            this.executionError = 'Cannot pause scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
        });
    }

    resumeScenario() {
        this.scenarioExecutionService.resumeScenario(this.currentScenarioId, this.currentExecutionId)
            .pipe(
                delay(1000)
            )
            .subscribe(
                () => this.loadScenarioExecution(Number(this.currentExecutionId)),
                error => {
                    const body = JSON.parse(error._body);
                    this.executionError = 'Cannot resume scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
                }
            );
    }

    private observeScenarioExecution(executionId: number) {
        this.unsubscribeScenarioExecutionAsyncSubscription();
        this.scenarioExecutionAsyncSubscription =
            this.subscribeToScenarioExecutionReports(
                this.scenarioExecutionService.observeScenarioExecution(this.currentScenarioId, executionId));
    }

    private subscribeToScenarioExecutionReports(scenarioExecutionReportsObservable: Observable<ScenarioExecutionReport>): Subscription {
        return scenarioExecutionReportsObservable
            .pipe(debounceTime(500))
            .subscribe((scenarioExecutionReport: ScenarioExecutionReport) => {
                this.toggleScenarioDetails = true;
                if (this.scenarioExecutionReport) {
                    this.scenarioExecutionReport.report.duration = scenarioExecutionReport.report.duration;
                    this.updateStepExecutionReport(this.scenarioExecutionReport.report, scenarioExecutionReport.report, []);
                } else {
                    this.scenarioExecutionReport = scenarioExecutionReport;
                }
            }, (error) => {
                if (error.status) {
                    this.executionError = error.status + ' ' + error.statusText + ' ' + error._body;
                } else {
                    this.executionError = error.error;
                }
                this.scenarioExecutionReport = null;
            });
    }

    private updateLocation(executionId: number) {
        this.location.replaceState('/scenario/' + this.currentScenarioId + '/execution/' + executionId);
    }

    private unsubscribeScenarioExecutionAsyncSubscription() {
        if (this.scenarioExecutionAsyncSubscription) {
            this.scenarioExecutionAsyncSubscription.unsubscribe();
        }
    }

    private updateStepExecutionReport(oldStepExecutionReport: StepExecutionReport, newStepExecutionReport: StepExecutionReport, depths: Array<number>) {
        if (oldStepExecutionReport.status !== newStepExecutionReport.status || (newStepExecutionReport.status === 'FAILURE' && oldStepExecutionReport.strategy === 'retry-with-timeout')) {
            if (depths.length === 0) {
                this.scenarioExecutionReport.report = newStepExecutionReport;
            } else if (depths.length === 1) {
                this.updateReport(this.scenarioExecutionReport.report.steps[depths[0]], newStepExecutionReport);
            } else {
                let stepReport = this.scenarioExecutionReport.report.steps[depths[0]];
                for (let i = 1; i < depths.length-1; i++) {
                    stepReport = stepReport.steps[depths[i]];
                }
                this.updateReport(stepReport.steps[depths[depths.length-1]], newStepExecutionReport);
            }
        } else {
            for (let i = 0; i < oldStepExecutionReport.steps.length; i++) {
                this.updateStepExecutionReport(oldStepExecutionReport.steps[i], newStepExecutionReport.steps[i], depths.concat(i));
            }

            if (newStepExecutionReport.steps.length > oldStepExecutionReport.steps.length) {
                for (let i=oldStepExecutionReport.steps.length; i < newStepExecutionReport.steps.length; i++) {
                    oldStepExecutionReport.steps.push(newStepExecutionReport.steps[i]);
                }
            }
        }
    }

    private updateReport(oldReport: StepExecutionReport, report: StepExecutionReport) {
        oldReport.name = report.name;
        oldReport.duration = report.duration;
        oldReport.status = report.status;
        oldReport.startDate = report.startDate;
        oldReport.information = report.information;
        oldReport.errors = report.errors;
        oldReport.type = report.type;
        oldReport.strategy = report.strategy;
        oldReport.targetName = report.targetName;
        oldReport.targetUrl = report.targetUrl;
        oldReport.evaluatedInputs = report.evaluatedInputs;
        oldReport.stepOutputs = report.stepOutputs;

        for (let i=0; i < oldReport.steps.length; i++) {
            this.updateReport(oldReport.steps[i], report.steps[i]);
        }
    }
}
