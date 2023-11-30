import { Component, EventEmitter, Input, Renderer2, OnDestroy, OnInit, Output, AfterViewInit, TemplateRef, ElementRef, AfterViewChecked, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { Observable, Subscription, fromEvent, merge, timer } from 'rxjs';
import { debounceTime, delay, throttleTime } from 'rxjs/operators';
import { FileSaverService } from 'ngx-filesaver';
import { NgbOffcanvas } from '@ng-bootstrap/ng-bootstrap';
import { NGX_MONACO_EDITOR_CONFIG } from 'ngx-monaco-editor-v2';

import {
    Authorization,
    Execution,
    GwtTestCase,
    ScenarioExecutionReport,
    StepExecutionReport
} from '@model';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { StringifyPipe, PrettyPrintPipe } from '@shared/pipes';
import { findScrollContainer } from '@shared/tools';

@Component({
    selector: 'chutney-scenario-execution',
    providers: [
        Location,
        PrettyPrintPipe,
        StringifyPipe,
        {
            provide: NGX_MONACO_EDITOR_CONFIG,
            useValue: {
                defaultOptions: {
                    // readOnly: true
                }
            }
        }
    ],
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.scss']
})
export class ScenarioExecutionComponent implements OnInit, OnDestroy, AfterViewInit, AfterViewChecked {
    @Input() execution: Execution;
    @Input() scenario: GwtTestCase;
    @Input() stickyTop: string = '0';
    @Input() stickyTopElementSelector: string;
    @Output() onExecutionStatusUpdate = new EventEmitter<{ status: ExecutionStatus, error: string }>();

    Object = Object;
    ExecutionStatus = ExecutionStatus;
    Authorization = Authorization;

    executionError: String;

    scenarioExecutionReport: ScenarioExecutionReport;
    selectedStep: StepExecutionReport;

    hasContextVariables = false;
    collapseContextVariables = true;

    private scenarioExecutionAsyncSubscription: Subscription;
    private resizeLeftPanelSubscription: Subscription;

    @ViewChild('leftPanel') leftPanel;
    @ViewChild('grab') grabPanel;
    @ViewChild('rightPanel') rightPanel;
    @ViewChild('reportHeader') reportHeader;

    private stickyTopElement: HTMLElement;
    private stickyTopElementHeight: number = 0;
    private stickyTopElementResizeObserver: ResizeObserver;

    constructor(
        private scenarioExecutionService: ScenarioExecutionService,
        private fileSaverService: FileSaverService,
        private stringify: StringifyPipe,
        private prettyPrint: PrettyPrintPipe,
        private renderer: Renderer2,
        private offcanvasService: NgbOffcanvas,
        private elementRef: ElementRef) {
    }

    ngOnInit() {
        if (this.scenario) {
            this.loadScenarioExecution(this.execution.executionId);
        } else {
            this.scenarioExecutionReport = JSON.parse(this.execution.report);
            this.afterReportUpdate();
        }
    }

    ngAfterViewInit(): void {
        if(this.leftPanel) {
            this.resizeLeftPanelSubscription = merge(
                fromEvent(window, 'resize'),
                fromEvent(findScrollContainer(this.leftPanel.nativeElement),'scroll')
            ).pipe(
                throttleTime(150),
                debounceTime(150)
            ).subscribe(() => {
                this.setLeftPanelStyle();
            });
        }

        this.setReportHeaderTop();

        if (this.stickyTopElementSelector) {
            this.stickyTopElement = document.querySelector(this.stickyTopElementSelector) as HTMLElement;
            this.stickyTopElementHeight = this.stickyTopElement.offsetHeight;

            this.stickyTopElementResizeObserver = new ResizeObserver((entries) => {
                this.stickyTopElementHeight = this.stickyTopElement.offsetHeight;
                this.setReportHeaderTop();
                this.setLeftPanelStyle();
            });
            this.stickyTopElementResizeObserver.observe(this.stickyTopElement);
        }
    }

    private setReportHeaderTop() {
        var top = '0px';
        if (this.stickyTopElement) {
            const elemHeight = this.stickyTopElement.offsetHeight;
            top += ` + ${elemHeight}px`;
        }
        top += ` + (${this.stickyTop})`;
        this.renderer.setStyle(this.reportHeader.nativeElement, 'top', `calc(${top})`);
    }

    ngAfterViewChecked(): void {
        this.setLeftPanelStyle();
    }

    ngOnDestroy() {
        this.unsubscribeScenarioExecutionAsyncSubscription();
        if (this.resizeLeftPanelSubscription) this.resizeLeftPanelSubscription.unsubscribe();
        if (this.stickyTopElementResizeObserver) this.stickyTopElementResizeObserver.unobserve(this.stickyTopElement);
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
                        if(scenarioExecutionReport?.report) {
                            this.afterReportUpdate();
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

    private afterReportUpdate() {
        this.hasContextVariables = this.scenarioExecutionReport.contextVariables && Object.getOwnPropertyNames(this.scenarioExecutionReport.contextVariables).length > 0;
        this.computeAllStepRowId();
        this.selectFailedStep();
    }

    private selectFailedStep() {
        let failedStep = this.getFailureSteps(this.scenarioExecutionReport);
        if (failedStep?.length > 0) {
            timer(500).subscribe(() => {
                this.selectStep(failedStep[0], true);
            });
        }
    }

    stopScenario() {
        this.scenarioExecutionService.stopScenario(this.scenario.id, this.execution.executionId).subscribe({
            error: (error) => {
                const body = JSON.parse(error._body);
                this.executionError = 'Cannot stop scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
            }
        });
    }

    pauseScenario() {
        this.scenarioExecutionService.pauseScenario(this.scenario.id, this.execution.executionId).subscribe({
            error: (error) => {
                const body = JSON.parse(error._body);
                this.executionError = 'Cannot pause scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
            }
        });
    }

    resumeScenario() {
        this.scenarioExecutionService.resumeScenario(this.scenario.id, this.execution.executionId)
            .pipe(
                delay(1000)
            )
            .subscribe({
                next: () => this.loadScenarioExecution(Number(this.execution.executionId)),
                error: (error) => {
                    const body = JSON.parse(error._body);
                    this.executionError = 'Cannot resume scenario : ' + error.status + ' ' + error.statusText + ' ' + body.message;
                }
            });
    }

    isRunning() {
        return ExecutionStatus.RUNNING === this.scenarioExecutionReport?.report?.status;
    }

    isPaused() {
        return ExecutionStatus.PAUSED === this.scenarioExecutionReport?.report?.status;
    }

    toggleContextVariables() {
        this.collapseContextVariables = !this.collapseContextVariables;
        timer(250).subscribe(() => this.setLefPanelHeight());
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
                    this.afterReportUpdate();
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
                    this.ngOnInit();
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

    private unsubscribeScenarioExecutionAsyncSubscription() {
        if (this.scenarioExecutionAsyncSubscription) this.scenarioExecutionAsyncSubscription.unsubscribe();
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

    exportReport() {
        const fileName = `${this.scenario.title}.execution.${this.execution.executionId}.chutney.json`;
        this.execution.report = JSON.stringify(this.scenarioExecutionReport);
        this.fileSaverService.saveText(JSON.stringify(this.execution), fileName);
    }

////////////////////////////////////////////////////// REPORT new view

    private setLeftPanelStyle() {
        if(this.leftPanel) {
            this.setLefPanelHeight();
            this.setLefPanelTop();
        }
    }

    private leftPanelHeight = 0;
    private setLefPanelHeight() {
        const leftPanelCH = this.leftPanel.nativeElement.getBoundingClientRect().y;
        if (this.leftPanelHeight != leftPanelCH) {
            this.leftPanelHeight = leftPanelCH;
            this.renderer.setStyle(this.leftPanel.nativeElement, 'height', `calc(100vh - ${this.leftPanelHeight}px)`);
        }
    }

    private leftPanelTopStateHeight = 0;
    private setLefPanelTop() {
        const newHeight = this.reportHeader.nativeElement.offsetHeight + this.stickyTopElementHeight;
        if (this.leftPanelTopStateHeight != newHeight) {
            this.leftPanelTopStateHeight = newHeight;
            this.renderer.setStyle(this.leftPanel.nativeElement, 'top', `calc(${this.stickyTop}  + ${newHeight}px + 0.5rem)`);
        }
    }

    copy(e: any) {
        var text = e.value;
        navigator.clipboard.writeText(
            this.prettyPrint.transform(
                this.stringify.transform(text)
            )
        );
    }

    private panelState = 1;
    togglePanels() {
        this.panelState = (this.panelState + 1) % 3;
        switch (this.panelState) {
            case 0:
                this.leftPanel.nativeElement.className = this.leftPanel.nativeElement.className.replace(/ d-none/g, '');
                this.leftPanel.nativeElement.style.width = '100%';
                this.grabPanel.nativeElement.className += ' d-none';
                this.rightPanel.nativeElement.className += ' d-none';
                break;
            case 1:
                this.leftPanel.nativeElement.className = this.leftPanel.nativeElement.className.replace(/ d-none/g, '');
                this.leftPanel.nativeElement.style.width = '30%';
                this.grabPanel.nativeElement.className = this.grabPanel.nativeElement.className.replace(/ d-none/g, '');
                this.rightPanel.nativeElement.className = this.rightPanel.nativeElement.className.replace(/ d-none/g, '');
                this.rightPanel.nativeElement.style.width = '70%';
                break;
            case 2:
                this.leftPanel.nativeElement.className += ' d-none';
                this.grabPanel.nativeElement.className += ' d-none';
                this.rightPanel.nativeElement.className = this.rightPanel.nativeElement.className.replace(/ d-none/g, '');
                this.rightPanel.nativeElement.style.width = '100%';
                break;
        }
    }

    togglePayloads() {
        this.prettyPrintToggle = !this.prettyPrintToggle;

        timer(250).subscribe(() => this.setLefPanelHeight());
    }

    private inOutCtxToggle_onClass = 'm-0 text-wrap text-break';
    private inOutCtxToggle_offClass = 'm-0 d-block overflow-auto';
    prettyPrintToggle = true;
    inOutCtxToggleClass(toggleValue: boolean): string {
        return toggleValue ? this.inOutCtxToggle_onClass : this.inOutCtxToggle_offClass;
    }

    toggleInputsOutputs() {
        this.toggleInputs();
        this.toggleOutputs();
    }

    inputsDNoneToggle = true;
    private toggleInputs() {
        this.inputsDNoneToggle = !this.inputsDNoneToggle;
        this.querySelector('.report-raw .inputs').forEach(e => {
            this.toggleDisplayNone(e, this.inputsDNoneToggle);
        });
    }

    outputsDNoneToggle = true;
    private toggleOutputs() {
        this.outputsDNoneToggle = !this.outputsDNoneToggle;
        this.querySelector('.report-raw .outputs').forEach(e => {
            this.toggleDisplayNone(e, this.outputsDNoneToggle);
        });
    }

    toggleInfosErrors() {
        this.toggleInfos();
        this.toggleErrors();
    }

    infosToggle = true;
    private toggleInfos() {
        this.infosToggle = !this.infosToggle;
        this.querySelector('.report-raw .infos').forEach(e => {
            this.toggleDisplayNone(e, this.infosToggle);
        });
    }

    errorsToggle = true;
    private toggleErrors() {
        this.errorsToggle = !this.errorsToggle;
        this.querySelector('.report-raw .errors').forEach(e => {
            this.toggleDisplayNone(e, this.errorsToggle);
        });
    }

    private toggleDisplayNone(elem: any, on: boolean) {
        if (on) {
            elem.className = elem.className.replace(' d-none', '');
        } else {
            elem.className += ' d-none';
        }
    }

    toggleStepCollapsed(step: any, event: Event = null) {
        if (event != null) event.stopPropagation();
        step.collapsed = !step.collapsed;
    }

    isStepCollapsed(step: any): boolean {
        if ('collapsed' in step) {
            return step.collapsed as boolean;
        } else {
            step.collapsed = false;
            return false;
        }
    }

    setAllStepsCollapsed(collapsed: boolean, parentStep: StepExecutionReport = null) {
        if (parentStep == null) {
            this.scenarioExecutionReport.report.steps.forEach(step => {
                step['collapsed'] = collapsed;
                this.setAllStepsCollapsed(collapsed, step);
            });
            if (!collapsed) {
                timer(500).subscribe(() => {
                    this.selectStep(this.selectedStep, true);
                });
            }
        } else {
            parentStep['collapsed'] = collapsed;
            parentStep.steps.forEach(step => {
                step['collapsed'] = collapsed;
                this.setAllStepsCollapsed(collapsed, step);
            });
        }
    }

    selectStep(step: StepExecutionReport = null, scrollIntoView: boolean = false) {
        this.selectedStep = step;
        if (!this.collapseContextVariables) {
            this.toggleContextVariables();
        }
        if (scrollIntoView && step) {
            document.getElementById(step['rowId']).scrollIntoView({behavior: 'smooth', block: 'start'});
        }
        this.elementRef.nativeElement.scrollIntoView({behavior: 'instant', block: 'start'})
    }

    private computeAllStepRowId() {
        this.scenarioExecutionReport.report.steps.forEach((s, i) => {
            this.computeStepRowId(s, `${i}`);
        });
    }

    private computeStepRowId(step: StepExecutionReport, parentId: string) {
        step['rowId'] = `${parentId}`;
        step.steps.forEach((s, i) => {
            s['rowId'] = `${parentId}-${i}`;
            this.computeStepRowId(s, s['rowId']);
        });
    }

    private querySelector(selectors: any, all: boolean = true): any | [any] {
        if (all) {
            return this.elementRef.nativeElement.querySelectorAll(selectors);
        } else {
            return this.elementRef.nativeElement.querySelector(selectors);
        }
    }

////////////////////////////////////////////////////// MONACO canva view

    enableEditorView(value: any) {
        return this.stringify.transform(value).length > 200;
    }

    private _theme = 'hc-black';
    get editorTheme(): string {
        return this._theme;
    }
    set editorTheme(theme: string) {
        this._theme = (theme && theme.trim()) || 'hc-black';
        this.updateEditorOptions();
    }

    private _editorLanguage = 'json';
    get editorLanguage(): string {
        return this._editorLanguage;
    }
    set editorLanguage(lang: string) {
        this._editorLanguage = (lang && lang.trim()) || 'json';
        this.updateEditorOptions();
    }

    /*
    get monacoLanguages(): Array<string> {
        return ((window as any)?.monaco?.languages?.getLanguages().map(l => l.id)) || [];
    }
    */

    editorOptions = {theme: this.editorTheme, language: this.editorLanguage};
    code: string;

    openOffCanva(content: TemplateRef<any>, value: any) {
        this.code = this.prettyPrint.transform(
            this.stringify.transform(value.value, {space: 4})
        );
		const ref = this.offcanvasService.open(content, { position: 'bottom', panelClass: 'offcanvas-panel-report' });
	}

    exportEditorContent() {
        this.fileSaverService.saveText(this.code, 'content');
    }

    copyEditorContent() {
        navigator.clipboard.writeText(this.code);
    }

    private updateEditorOptions() {
        this.editorOptions = {theme: this.editorTheme, language: this.editorLanguage};
    }
}
