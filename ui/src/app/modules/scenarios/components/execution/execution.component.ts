///<reference path="../../../../../../node_modules/@angular/router/src/router.d.ts"/>
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, delay } from 'rxjs/internal/operators';

import { EventManagerService } from '@shared/event-manager.service';

import { Execution, GwtTestCase, ScenarioComponent, ScenarioExecutionReport, TestCase } from '@model';
import { ComponentService, ScenarioExecutionService, ScenarioService } from '@core/services';

@Component({
    selector: 'chutney-execution',
    providers: [Location, {provide: LocationStrategy, useClass: PathLocationStrategy}],
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.scss']
})
export class ScenarioExecutionComponent implements OnInit, OnDestroy {

    testCase: TestCase = null;
    scenarioComponent: ScenarioComponent = null;
    scenarioGwt: GwtTestCase = null;

    parseError: String;
    executionError: String;

    currentScenarioId: string;
    currentExecutionId: number;

    scenarioExecutionReport: ScenarioExecutionReport;

    lastExecutionRunning = false;
    toggleScenarioDetails = true;
    toggleScenarioInfo = true;

    isComposed = TestCase.isComposed;

    private routeParamsSubscription: Subscription;
    private scenarioExecutionAsyncSubscription: Subscription;

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
        const action: string = this.route.snapshot.queryParams['action'];
        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            this.currentScenarioId = params['id'];
            if (params['execId'] && params['execId'] !== 'last') {
                this.loadScenarioExecution(params['execId']);
            }

            this.loadScenario(action);
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.routeParamsSubscription);
        if (this.scenarioExecutionAsyncSubscription) {
            this.scenarioExecutionAsyncSubscription.unsubscribe();
        }
    }

    loadScenario(action: string = '') {
        if (this.isComposed(this.currentScenarioId)) {
            this.componentService.findComponentTestCase(this.currentScenarioId).subscribe((testCase: ScenarioComponent) => {
                this.testCase = TestCase.fromComponent(testCase);
                this.scenarioComponent = testCase;
            });
        } else {
            this.scenarioService.findRawTestCase(this.currentScenarioId).subscribe((testCase: TestCase) => {
                this.testCase = testCase;
            });
            this.scenarioService.findTestCase(this.currentScenarioId).subscribe((testCase: GwtTestCase) => {
                this.scenarioGwt = testCase;
            });
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
        if (execution !== null) {
            this.currentExecutionId = execution.executionId;
            this.executionError = '';

            if (this.scenarioExecutionAsyncSubscription) {
                this.scenarioExecutionAsyncSubscription.unsubscribe();
            }

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
        if (this.testCase.hasParameters()) {
            this.scenarioExecutionService.testCaseToExecute = this.testCase;
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
        this.scenarioExecutionAsyncSubscription =
            this.subscribeToScenarioExecutionReports(
                this.scenarioExecutionService.observeScenarioExecution(this.currentScenarioId, executionId));
    }

    private subscribeToScenarioExecutionReports(scenarioExecutionReportsObservable: Observable<ScenarioExecutionReport>): Subscription {
        return scenarioExecutionReportsObservable
            .pipe(debounceTime(500))
            .subscribe((scenarioExecutionReport: ScenarioExecutionReport) => {
                this.toggleScenarioDetails = true;
                this.scenarioExecutionReport = scenarioExecutionReport;
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
        this.location.replaceState('#/scenario/' + this.currentScenarioId + '/execution/' + executionId);
    }
}
