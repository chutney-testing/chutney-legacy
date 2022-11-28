import { Component, OnDestroy, OnInit } from '@angular/core';
import { delay, switchMap, tap } from 'rxjs/operators';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Execution, GwtTestCase, ScenarioComponent, TestCase } from '@model';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { NgbNavChangeEvent } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of, Subscription, zip } from 'rxjs';
import { ComponentService, ScenarioService } from '@core/services';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { EventManagerService } from '@shared';

@Component({
    selector: 'chutney-scenario-executions-history',
    templateUrl: './scenario-executions-history.component.html',
    styleUrls: ['./scenario-executions-history.component.scss']
})
export class ScenarioExecutionsHistoryComponent implements OnInit, OnDestroy {

    tabs: Execution[] = [];
    activeTab = 0;
    executions: Execution [] = [];
    scenarioId: string;
    private _executionsFilters: Params = {};
    private tabFilters: Params = {};
    private hasParameters: boolean = null;
    scenario: ScenarioComponent | GwtTestCase;
    error: string;
    private scenarioExecution$: Subscription;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private scenarioExecutionService: ScenarioExecutionService,
                private scenarioService: ScenarioService,
                private componentService: ComponentService,
                private eventManagerService: EventManagerService) {
    }

    ngOnInit(): void {
        this.route.params
            .pipe(
                tap(params => this.scenarioId = params['id']),
                switchMap(() => this.loadScenario()),
                switchMap(() => this.getScenarioExecutions())
            ).subscribe({
            next: () => {
                this.onQueryParamsChange()
                this.onRightMenuAction();
            },
            error: (error) => this.error = error.error
        });
    }


    private onQueryParamsChange() {
        let executionsFilters: Params;
        this.route.queryParams.pipe(
            tap(queryParams => executionsFilters = queryParams),
            switchMap(queryParams => this.openTabs(queryParams)),
            tap(() => this.executionsFilters = executionsFilters)
        ).subscribe();
    }

    private getScenarioExecutions() {
        return this.scenarioExecutionService.findScenarioExecutions(this.scenarioId)
            .pipe(
                tap(executions => this.executions = executions)
            );
    }

    closeReport(event: MouseEvent, executionId: number) {
        event.preventDefault();
        event.stopImmediatePropagation();
        const openTabs = this.tabs.filter(exec => exec.executionId !== executionId);
        this.tabFilters['open'] = openTabs.map(exec => exec.executionId).toString();

        if (this.activeTab === executionId || !openTabs.length) {
            this.tabFilters['active'] = null;
        }
        this.updateQueryParams();
    }


    openReport(request: { execution: Execution, focus: boolean }) {

        let tabs = this.tabs;
        if (!this.isOpened(request.execution.executionId)) {
            tabs = tabs.concat(request.execution);
        }
        this.tabFilters['open'] = tabs.map(exec => exec.executionId).toString();
        this.tabFilters['active'] = request.focus ? request.execution.executionId : null;

        this.updateQueryParams();
    }

    onTabChange(changeEvent: NgbNavChangeEvent) {
        this.tabFilters['active'] = changeEvent.nextId;
        this.updateQueryParams();
    }

    get executionsFilters(): Params {
        return this._executionsFilters;
    }

    set executionsFilters(value: Params) {
        const {open, active, ...executionsParams} = value;
        this._executionsFilters = executionsParams;
        this.updateQueryParams();
    }

    updateExecutionStatus(executionId: number, update: { status: ExecutionStatus, error: string }) {
        const execution = this.executions.find(exec => exec.executionId === executionId);
        execution.status = update.status;
        execution.error = update.error;
    }

    private loadScenario(): Observable<ScenarioComponent | GwtTestCase> {
        let scenario$: Observable<ScenarioComponent | GwtTestCase>;
        let hasParameter: (scenario: ScenarioComponent | GwtTestCase) => boolean;
        if (TestCase.isComposed(this.scenarioId)) {
            scenario$ = this.componentService.findComponentTestCaseWithoutDeserializeImpl(this.scenarioId);
            hasParameter = (scenario: ScenarioComponent) => !!scenario.computedParameters?.length;
        } else {
            scenario$ = this.scenarioService.findTestCase(this.scenarioId);
            hasParameter = (scenario: GwtTestCase) => !!scenario.wrappedParams?.params?.length;
        }
        return scenario$.pipe(
            tap(scenario => {
                this.hasParameters = hasParameter(scenario);
                this.scenario = scenario;
            })
        );
    }


    private updateQueryParams() {
        let queryParams = this.cleanParams({...this.executionsFilters, ...this.tabFilters});
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: queryParams
        });
    }

    private isOpened(executionId: number) {
        const openTabs: string = this.tabFilters['open'];
        return openTabs.includes(executionId.toString());
    }

    private focusOnTab(executionId: string) {
        this.activeTab = executionId && this.isOpened(+executionId) ? +executionId : 0;
        this.tabFilters['active'] = this.activeTab;
    }

    private cleanParams(params: Params) {
        Object.keys(params).forEach(key => {
            if (params[key] === null || params[key] === '' || params[key] === 0) {
                delete params[key];
            }
        });
        return params;
    }

    private openTabs(queryParams: Params): Observable<Execution[]> {
        const executionsIds = queryParams['open'];
        let openedExecutions$ = of([]);
        if (executionsIds) {
            let executions$: Observable<Execution> [] = executionsIds
                .split(',')
                .map(id => {
                    let execution = this.executions.find(exec => exec.executionId.toString() === id);
                    if (!execution) {
                        return this.scenarioExecutionService.findScenarioExecutionSummary(this.scenarioId, +id);
                    }
                    return of(execution);
                });
            openedExecutions$ = zip(executions$);
        }
        return openedExecutions$.pipe(
            tap(executions => {
                this.tabs = executions;
                this.tabFilters['open'] = this.tabs.map(exec => exec.executionId).toString();
                this.focusOnTab(queryParams['active']);
            })
        )
    }

    private onRightMenuAction() {
        this.scenarioExecution$ = this.eventManagerService.subscribe('execute', (data) => this.executeScenario(data.env));
    }

    private executeScenario(env: string) {
        if (this.hasParameters) {
            this.router.navigateByUrl(`/scenario/${this.scenarioId}/execute/${env}`,);
        } else {
            this.scenarioExecutionService
                .executeScenarioAsync(this.scenarioId, [], env)
                .pipe(
                    delay(1000),
                    switchMap(executionId => this.scenarioExecutionService.findScenarioExecutionSummary(this.scenarioId, +executionId)))
                .subscribe({
                        next: (executionSummary) => {
                            this.openReport({
                                execution: executionSummary,
                                focus: true
                            });
                            this.executions.unshift(executionSummary);
                        },
                        error: error => {
                            this.error = error.error;
                        }
                    }
                );
        }
    }

    ngOnDestroy(): void {
        this.eventManagerService.destroy(this.scenarioExecution$);
    }
}
