/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnDestroy, OnInit } from '@angular/core';
import { catchError, delay, switchMap, tap } from 'rxjs/operators';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Execution, GwtTestCase, TestCase } from '@model';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { NgbNavChangeEvent } from '@ng-bootstrap/ng-bootstrap';
import { EMPTY, Observable, of, Subscription, zip } from 'rxjs';
import { ScenarioService } from '@core/services';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { AlertService, EventManagerService } from '@shared';

@Component({
    selector: 'chutney-scenario-executions-history',
    templateUrl: './scenario-executions-history.component.html',
    styleUrls: ['./scenario-executions-history.component.scss']
})
export class ScenarioExecutionsHistoryComponent implements OnInit, OnDestroy {

    tabs: Execution[] = [];
    activeTab = '0';
    executions: Execution [] = [];
    scenarioId: string;
    private _executionsFilters: Params = {};
    private tabFilters: Params = {};
    private hasParameters: boolean = null;
    scenario: GwtTestCase;
    error: string;
    private scenarioExecution$: Subscription;
    private readonly LAST_ID = 'last';

    constructor(private route: ActivatedRoute,
                private router: Router,
                private scenarioExecutionService: ScenarioExecutionService,
                private scenarioService: ScenarioService,
                private eventManagerService: EventManagerService,
                private alertService: AlertService) {
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

        if (this.activeTab === executionId.toString() || !openTabs.length) {
            this.tabFilters['active'] = null;
        }
        this.updateQueryParams();
    }


    openReport(request: { execution: Execution, focus: boolean }) {

        let tabs = this.tabs;
        if (!this.isOpened(request.execution.executionId.toString())) {
            tabs = tabs.concat(request.execution);
        }
        this.tabFilters['open'] = tabs.map(exec => exec.executionId).toString();
        this.tabFilters['active'] = request.focus ? request.execution.executionId : null;

        this.updateQueryParams();
    }

    onTabChange(changeEvent: NgbNavChangeEvent) {
        this.activeTab = changeEvent.nextId;
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
        const execution = this.getExecution(executionId.toString());
        execution.status = update.status;
        execution.error = update.error;
    }

    private loadScenario(): Observable<GwtTestCase> {
        let scenario$ = this.scenarioService.findTestCase(this.scenarioId);
        let hasParameter = (scenario: GwtTestCase) => !!scenario.wrappedParams?.params?.length;
        return scenario$.pipe(
            tap(scenario => {
                this.hasParameters = hasParameter(scenario);
                this.scenario = scenario;
            }),
            catchError(err => {
                this.alertService.error(err.error);
                this.router.navigate(['scenario']);
                return EMPTY;
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

    private isOpened(executionId: string) {
        const lastExecId = this.executions[0]?.executionId?.toString();
        const openTabs: string = this.tabFilters['open'];
        return !!openTabs && !!openTabs.split(',')
            .find(openId => {
                let id = openId;
                if (openId === this.LAST_ID && executionId === lastExecId) {
                    id = lastExecId;
                }
                return executionId === id;
            });
    }

    private focusOnTab(executionId: string) {
        this.activeTab = executionId && this.isOpened(executionId) ? executionId : '0';
        this.tabFilters['active'] = this.activeTab;
    }

    private cleanParams(params: Params) {
        Object.keys(params).forEach(key => {
            if (params[key] === null || params[key] === '' || params[key] === '0') {
                delete params[key];
            }
        });
        return params;
    }

    private openTabs(queryParams: Params): Observable<Execution[]> {
        if (!this.executions.length) {
            return EMPTY;
        }

        let openedExecutions$ = of([]);
        this.cleanQueryParams(queryParams);
        if (queryParams['open']) {
            let executions$: Observable<Execution> [] = queryParams['open']
                .split(',')
                .map(id => {
                    let execution = this.getExecution(id);
                    if (!execution) {
                        return this.findScenarioExecutionSummary(id);
                    }
                    return of(execution);
                });
            openedExecutions$ = zip(executions$);
        }
        return openedExecutions$.pipe(
            tap(executions => {
                this.tabs = executions;
                this.tabFilters['open'] = this.getOpenTabs(queryParams['open']);
                this.focusOnTab(queryParams['active']);
            })
        )
    }

    private findScenarioExecutionSummary(id): Observable<Execution> {
        return this.scenarioExecutionService.findScenarioExecutionSummary(+id)
            .pipe(
            catchError(error => {
                this.error = error.error;
                return EMPTY
            })
        );
    }

    private getOpenTabs(opens: string) {
        return this.tabs. map((exec, i) => {
            if (opens.includes(this.LAST_ID) && i === 0) {
                return this.LAST_ID;
            }
            return exec.executionId;
        }).toString();
    }

    private getExecution(id: string): Execution {
        if (id === this.LAST_ID){
            return this.executions[0];
        }
        return this.executions.find(exec => exec.executionId.toString() === id);
    }

    private cleanQueryParams(queryParams: Params) {
        let redirect = false;
        const lastExecutionId = this.executions.length && this.executions[0].executionId.toString();
        if (queryParams['open']) {
            let openExecutionsIds: string[] = queryParams['open'].split(',');
            if (openExecutionsIds.includes(this.LAST_ID) && openExecutionsIds.includes(lastExecutionId)) {
                openExecutionsIds = openExecutionsIds.filter(id => id !== lastExecutionId);
                this.tabFilters['open'] = openExecutionsIds;
                redirect = true;
            }
        }

        if (queryParams['active']) {
            let activeExecutionsIds: string[] = queryParams['active'].split(',');
            if (activeExecutionsIds.includes(this.LAST_ID) && activeExecutionsIds.includes(lastExecutionId)) {
                activeExecutionsIds = activeExecutionsIds.filter(id => id !== lastExecutionId);
                this.tabFilters['active'] = activeExecutionsIds;
                redirect = true;
            }
        }

        redirect && this.updateQueryParams();
    }

    private onRightMenuAction() {
        this.scenarioExecution$ = this.eventManagerService.subscribe('execute', (data) => this.executeScenario(data.env));
    }

    private executeScenario(env: string) {
        if (this.hasParameters) {
            this.router.navigateByUrl(`/scenario/${this.scenarioId}/execute/${env}`,);
        } else {
            this.scenarioExecutionService
                .executeScenarioAsync(this.scenarioId, env)
                .pipe(
                    delay(1000),
                    switchMap(executionId => this.findScenarioExecutionSummary(+executionId)))
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

    getActiveTab() {
        return this.activeTab === this.LAST_ID ? this.executions[0]?.executionId?.toString() : this.activeTab;
    }
}
