import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Location } from '@angular/common';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, delay } from 'rxjs/operators';

import { EventManagerService } from '@shared/event-manager.service';

import {
    Authorization,
    Execution,
    GwtTestCase,
    ScenarioComponent,
    ScenarioExecutionReport,
    StepExecutionReport, stepFromObject
} from '@model';
import { ScenarioService } from '@core/services';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { ExecutionStatus } from '@core/model/scenario/execution-status';

@Component({
    selector: 'chutney-scenario-execution',
    providers: [Location],
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.scss']
})
export class ScenarioExecutionComponent implements OnInit, OnDestroy {
    @Input() executionId: number;
    @Input() campaignExecution: object;
    @Input() scenario: ScenarioComponent | GwtTestCase;
    @Output() onExecutionStatusUpdate = new EventEmitter<{status: ExecutionStatus, error: string}>() ;

    ExecutionStatus = ExecutionStatus;

    parseError: String;
    executionError: String;

    scenarioExecutionReport: ScenarioExecutionReport;
    selectedStep: StepExecutionReport;

    isAllStepsCollapsed = true;
    isRootStepDetailsVisible = false;
    collapseContextVariables = true;

    private scenarioExecutionAsyncSubscription: Subscription;
    private stepDetailsSubscription: Subscription;

    Authorization = Authorization;

    constructor(
        private eventManager: EventManagerService,
        private scenarioService: ScenarioService,
        private scenarioExecutionService: ScenarioExecutionService) {
    }

    ngOnInit() {
        this.loadScenarioExecution(this.executionId);
        this.stepDetailsSubscription = this.eventManager.subscribe('selectStepEvent_' + this.executionId, (data) => {
            this.selectedStep = data.step;
            this.isRootStepDetailsVisible = this.selectedStep === this.scenarioExecutionReport?.report;
        });
    }

    ngOnDestroy() {
        this.unsubscribeScenarioExecutionAsyncSubscription();
        this.eventManager.destroy(this.stepDetailsSubscription);
    }

    onLastIdExecution(execution: Execution) {
        if (Execution.NO_EXECUTION === execution) {
            this.executionId = null;
        } else if (this.executionId !== execution.executionId) {
            this.executionId = execution.executionId;
            if (!this.scenarioExecutionAsyncSubscription || this.scenarioExecutionAsyncSubscription.closed) {
                if (ExecutionStatus.RUNNING === execution.status) {
                    this.observeScenarioExecution(execution.executionId);
                } else {
                    this.loadScenarioExecution(execution.executionId);
                }
            }
        }
    }

    onSelectExecution(execution: Execution) {
        if (execution != null) {
            this.executionId = execution.executionId;
            this.executionError = '';

            this.unsubscribeScenarioExecutionAsyncSubscription();

            if (ExecutionStatus.RUNNING === execution.status) {
                this.observeScenarioExecution(execution.executionId);
            } else {
                this.loadScenarioExecution(execution.executionId);
            }
        } else {
            this.executionId = null;
            this.executionError = '';
            this.scenarioExecutionReport = null;
        }
    }

    loadScenarioExecution(executionId: number) {
        this.executionError = '';
        this.executionId = executionId;
        this.scenarioExecutionService.findExecutionReport(this.scenario.id, executionId)
            .subscribe({
                next: (scenarioExecutionReport: ScenarioExecutionReport) => {
                    if (scenarioExecutionReport?.report?.status === ExecutionStatus.RUNNING) {
                        this.observeScenarioExecution(executionId);
                    } else {
                        this.scenarioExecutionReport = scenarioExecutionReport;
                    }
                },
                error: error => {
                    console.error(error.message);
                    this.executionError = 'Cannot find execution n°' + executionId;
                    this.scenarioExecutionReport = null;
                }
            });
    }

    expandAll() {
        this.isAllStepsCollapsed = !this.isAllStepsCollapsed;
        this.eventManager.broadcast({name: 'toggleScenarioStep_' + this.executionId, expand: this.isAllStepsCollapsed});
    }

    showRootStep(){
        this.isRootStepDetailsVisible = ! this.isRootStepDetailsVisible;
        if (this.isRootStepDetailsVisible) {
            this.eventManager.broadcast({name: 'selectStepEvent_' + this.executionId , step: this.scenarioExecutionReport?.report});
            this.eventManager.broadcast({name: 'highlightEvent_' + this.executionId, stepId: null});
        } else {
            this.eventManager.broadcast({name: 'selectStepEvent_' + this.executionId , step: null});
        }
    }

    stopScenario() {
        this.scenarioExecutionService.stopScenario(this.scenario.id, this.executionId).subscribe(() => {
        }, error => {
            const body = JSON.parse(error._body);
            this.executionError = 'Cannot stop scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
        }, () => {
        });
    }

    pauseScenario() {
        this.scenarioExecutionService.pauseScenario(this.scenario.id, this.executionId).subscribe(() => {
        }, error => {
            const body = JSON.parse(error._body);
            this.executionError = 'Cannot pause scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
        });
    }

    resumeScenario() {
        this.scenarioExecutionService.resumeScenario(this.scenario.id, this.executionId)
            .pipe(
                delay(1000)
            )
            .subscribe(
                () => this.loadScenarioExecution(Number(this.executionId)),
                error => {
                    const body = JSON.parse(error._body);
                    this.executionError = 'Cannot resume scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
                }
            );
    }

    isComponentScenario() {
        return this.scenario instanceof ScenarioComponent;
    }

    isRunning() {
        return ExecutionStatus.RUNNING === this.scenarioExecutionReport?.report?.status;
    }

    isPaused() {
        return ExecutionStatus.PAUSED === this.scenarioExecutionReport?.report?.status;
    }

    private switchCollapseContextVariables() {
        this.collapseContextVariables = !this.collapseContextVariables;
    }

    private observeScenarioExecution(executionId: number) {
        this.unsubscribeScenarioExecutionAsyncSubscription();
        this.scenarioExecutionAsyncSubscription =
            this.subscribeToScenarioExecutionReports(
                this.scenarioExecutionService.observeScenarioExecution(this.scenario.id, executionId));
    }

    private subscribeToScenarioExecutionReports(scenarioExecutionReportsObservable: Observable<ScenarioExecutionReport>): Subscription {
        let executionStatus: ExecutionStatus;
        let executionError: string;
        return scenarioExecutionReportsObservable
            .pipe(debounceTime(500))
            .subscribe({
                next: (scenarioExecutionReport: ScenarioExecutionReport) => {
                    executionStatus = ExecutionStatus[scenarioExecutionReport.report.status];
                    executionError = this.getExecutionError(scenarioExecutionReport);
                    if (this.scenarioExecutionReport) {
                        this.scenarioExecutionReport.report.duration = scenarioExecutionReport.report.duration;
                        this.updateStepExecutionReport(this.scenarioExecutionReport.report, scenarioExecutionReport.report, []);
                    } else {
                        this.scenarioExecutionReport = scenarioExecutionReport;
                    }
                },
                error: (error) => {
                    if (error.status) {
                        this.executionError = error.status + ' ' + error.statusText + ' ' + error._body;
                    } else {
                        this.executionError = error.error;
                        executionError = error.error;
                    }
                    this.scenarioExecutionReport = null;
                },
                complete: () => {
                    this.onExecutionStatusUpdate.emit({status: executionStatus, error: executionError});
                }
            });
    }

    private getExecutionError(scenarioExecutionReport: ScenarioExecutionReport) {
        return scenarioExecutionReport
            .report
            .steps
            .filter(step => step.status === ExecutionStatus.FAILURE)
            .map(step => step.errors)
            .flat()
            .toString();
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
                for (let i = 1; i < depths.length - 1; i++) {
                    stepReport = stepReport.steps[depths[i]];
                }
                this.updateReport(stepReport.steps[depths[depths.length - 1]], newStepExecutionReport);
            }
        } else {
            for (let i = 0; i < oldStepExecutionReport.steps.length; i++) {
                this.updateStepExecutionReport(oldStepExecutionReport.steps[i], newStepExecutionReport.steps[i], depths.concat(i));
            }

            if (newStepExecutionReport.steps.length > oldStepExecutionReport.steps.length) {
                for (let i = oldStepExecutionReport.steps.length; i < newStepExecutionReport.steps.length; i++) {
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

        for (let i = 0; i < oldReport.steps.length; i++) {
            this.updateReport(oldReport.steps[i], report.steps[i]);
        }
    }


}
