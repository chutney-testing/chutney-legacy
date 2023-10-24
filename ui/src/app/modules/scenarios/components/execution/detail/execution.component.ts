import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Location } from '@angular/common';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, delay } from 'rxjs/operators';

import { EventManagerService } from '@shared/event-manager.service';

import {
    Authorization,
    Execution,
    GwtTestCase,
    ScenarioExecutionReport,
    StepExecutionReport
} from '@model';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { ObjectAsEntryListPipe } from '@shared/pipes';
import { FileSaverService } from 'ngx-filesaver';

@Component({
    selector: 'chutney-scenario-execution',
    providers: [Location],
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.scss']
})
export class ScenarioExecutionComponent implements OnInit, OnDestroy {
    @Input() execution: Execution;
    @Input() scenario: GwtTestCase;
    @Output() onExecutionStatusUpdate = new EventEmitter<{ status: ExecutionStatus, error: string }>();

    ExecutionStatus = ExecutionStatus;

    parseError: String;
    executionError: String;

    scenarioExecutionReport: ScenarioExecutionReport;
    selectedStep: StepExecutionReport;
    selectedStepId: number;

    isAllStepsCollapsed = true;
    hasContextVariables = false;
    collapseContextVariables = true;
    showDetails = true;

    private scenarioExecutionAsyncSubscription: Subscription;
    private stepDetailsSubscription: Subscription;

    Authorization = Authorization;

    active = 1;
    JSON = JSON;

    constructor(
        private eventManager: EventManagerService,
        private scenarioExecutionService: ScenarioExecutionService,
        private fileSaverService: FileSaverService) {
    }

    ngOnInit() {
        if (this.scenario) {
            this.loadScenarioExecution(this.execution.executionId);
        }
        else {
            this.scenarioExecutionReport = JSON.parse(this.execution.report);
        }
        this.stepDetailsSubscription = this.eventManager.subscribe('selectStepEvent_' + this.execution.executionId, (data) => {
            this.selectedStep = data.step;
            this.selectedStepId = data.stepId;
            this.showDetails = !this.isRootStepSelected();
        });
    }

    ngOnDestroy() {
        this.unsubscribeScenarioExecutionAsyncSubscription();
        this.eventManager.destroy(this.stepDetailsSubscription);
    }

    loadScenarioExecution(executionId: number) {
        this.executionError = '';
        this.execution.executionId = executionId;
        this.scenarioExecutionService.findExecutionReport(this.scenario.id, executionId)
            .subscribe({
                next: (scenarioExecutionReport: ScenarioExecutionReport) => {
                    if (scenarioExecutionReport?.report?.status === ExecutionStatus.RUNNING) {
                        this.observeScenarioExecution(executionId);
                    } else {
                        this.scenarioExecutionReport = scenarioExecutionReport;
                        this.hasContextVariables = this.scenarioExecutionReport.contextVariables && Object.getOwnPropertyNames(this.scenarioExecutionReport.contextVariables).length > 0;
                        let failedStep = this.getFailureSteps(this.scenarioExecutionReport);
                        if (failedStep?.length > 0) {
                            this.eventManager.broadcast({ name: 'selectStepEvent_' + this.execution.executionId, step: failedStep[0], stepId: this.scenarioExecutionReport.report.steps.indexOf(failedStep[0]) });
                        } else {
                            this.showRootStep();
                        }
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
        this.eventManager.broadcast({ name: 'toggleScenarioStep_' + this.execution.executionId, expand: this.isAllStepsCollapsed });
    }

    toggleDetails() {
        this.showDetails = !this.showDetails;
    }

    showRootStep() {
        this.eventManager.broadcast({ name: 'selectStepEvent_' + this.execution.executionId, step: this.scenarioExecutionReport?.report, stepId: -1 });
    }

    stopScenario() {
        this.scenarioExecutionService.stopScenario(this.scenario.id, this.execution.executionId).subscribe(() => {
        }, error => {
            const body = JSON.parse(error._body);
            this.executionError = 'Cannot stop scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
        }, () => {
        });
    }

    pauseScenario() {
        this.scenarioExecutionService.pauseScenario(this.scenario.id, this.execution.executionId).subscribe(() => {
        }, error => {
            const body = JSON.parse(error._body);
            this.executionError = 'Cannot pause scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
        });
    }

    resumeScenario() {
        this.scenarioExecutionService.resumeScenario(this.scenario.id, this.execution.executionId)
            .pipe(
                delay(1000)
            )
            .subscribe(
                () => this.loadScenarioExecution(Number(this.execution.executionId)),
                error => {
                    const body = JSON.parse(error._body);
                    this.executionError = 'Cannot resume scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
                }
            );
    }

    isRunning() {
        return ExecutionStatus.RUNNING === this.scenarioExecutionReport?.report?.status;
    }

    isPaused() {
        return ExecutionStatus.PAUSED === this.scenarioExecutionReport?.report?.status;
    }

    hasSubSteps() {
        return this.scenarioExecutionReport
            .report
            .steps
            .filter((step) => step.steps.length > 0)
            .length > 0;
    }

    hasInputOutputs(step: StepExecutionReport) {
        if (step?.steps.length) {
            return step.steps.filter((s) => this.hasInputOutputs(s)).length > 0;
        }
        return step && (Object.getOwnPropertyNames(step.evaluatedInputs).length > 0 || Object.getOwnPropertyNames(step.stepOutputs).length > 0);
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
                    if (this.isRootStepSelected()) {
                        this.showRootStep();
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
                    this.onExecutionStatusUpdate.emit({ status: executionStatus, error: executionError });
                }
            });
    }

    private getExecutionError(scenarioExecutionReport: ScenarioExecutionReport) {
        return this.getFailureSteps(scenarioExecutionReport)
            .map(step => step.errors)
            .flat()
            .toString();
    }

    private getFailureSteps(scenarioExecutionReport: ScenarioExecutionReport) {
        return scenarioExecutionReport
            .report
            .steps
            .filter((step) => step.status === ExecutionStatus.FAILURE);
    }

    private isRootStepSelected() {
        return this.selectedStepId === -1;
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

    private exportReport() {
        const fileName = `${this.scenario.title}.execution.${this.execution.executionId}.chutney.json`;
        this.execution.report = JSON.stringify(this.scenarioExecutionReport);
        this.fileSaverService.saveText(JSON.stringify(this.execution), fileName);
    }

    ////////////////////////////////////////////////////////////////////////////////

    copy(e: any) {
        var text = e.value;
        if (text instanceof Object) {
            text = JSON.stringify(text);
        }
        navigator.clipboard.writeText(text);
    }

    inputsToggle = true;
    inputsDNoneToggle = true;
    toggleInputs(dNone: boolean) {
        if (dNone) {
            this.inputsDNoneToggle = !this.inputsDNoneToggle;
            document.querySelectorAll('.report-raw .inputs').forEach(e => {
                if (this.inputsDNoneToggle) {
                    e.className = 'inputs';
                } else {
                    e.className = 'inputs d-none';
                }
            });
        } else {
            this.inputsToggle = !this.inputsToggle;
            document.querySelectorAll('.report-raw .inputs pre').forEach(e => {
                if (this.inputsToggle) {
                    e.className = 'm-0 text-wrap text-break';
                } else {
                    e.className = 'm-0 text-truncate';
                }
            });
        }
    }

    outputsToggle = true;
    outputsDNoneToggle = true;
    toggleOutputs(dNone: boolean) {
        if (dNone) {
            this.outputsDNoneToggle = !this.outputsDNoneToggle;
            document.querySelectorAll('.report-raw .outputs').forEach(e => {
                if (this.outputsDNoneToggle) {
                    e.className = 'outputs';
                } else {
                    e.className = 'outputs d-none';
                }
            });
        } else {
            this.outputsToggle = !this.outputsToggle;
            document.querySelectorAll('.report-raw .outputs pre').forEach(e => {
                if (this.outputsToggle) {
                    e.className = 'm-0 text-wrap text-break';
                } else {
                    e.className = 'm-0 text-truncate';
                }
            });
        }
    }

    functionalToggle = true;
    toggleFunctional() {
        this.functionalToggle = !this.functionalToggle;
            document.querySelectorAll('.report-raw :is(.success,.context-put,.fail,.debug,.sleep)').forEach(e => {
                if (this.functionalToggle) {
                    e.className = e.className.replace('d-none', '');
                } else {
                    e.className += ' d-none';
                }
            });
    }
}
