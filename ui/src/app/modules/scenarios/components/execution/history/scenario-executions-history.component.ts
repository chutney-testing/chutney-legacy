import { Component, OnInit } from '@angular/core';
import { combineLatestWith, delay, switchMap, tap } from 'rxjs/operators';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Authorization, Execution, TestCase } from '@model';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';
import { NgbNavChangeEvent } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { EventManagerService } from '@shared';
import { ScenarioService } from '@core/services';
import { FileSaverService } from 'ngx-filesaver';

@Component({
    selector: 'chutney-scenario-executions-history',
    templateUrl: './scenario-executions-history.component.html',
    styleUrls: ['./scenario-executions-history.component.scss']
})
export class ScenarioExecutionsHistoryComponent implements OnInit {

    tabs: Execution[] = [];
    activeTab = 0;
    executions: Execution [] = [];
    scenarioId: string;
    private _executionsFilters: Params = {};
    private tabFilters: Params = {};
    rightMenuItems = [];

    constructor(private route: ActivatedRoute,
                private router: Router,
                private scenarioExecutionService: ScenarioExecutionService,
                private scenarioService: ScenarioService,
                private fileSaverService: FileSaverService) {
    }

    ngOnInit(): void {
        this.route.params
            .pipe(
                tap(params => this.scenarioId = params['id']),
                switchMap(() => this.scenarioExecutionService.findScenarioExecutions(this.scenarioId)),
                combineLatestWith(this.route.queryParams))
            .subscribe(([executions, queryParams]) => {
                this.executions = executions;
                this.openTabs(queryParams['open']);
                this.focusOnTab(queryParams['active'])
                this.executionsFilters = queryParams;
                this.initRightMenu();
            });
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
        if (!this.opened(request.execution.executionId)) {
            tabs = tabs.concat(request.execution);
        }
        this.tabFilters['open'] = tabs.map(exec => exec.executionId).toString();
        this.tabFilters['active'] = request.focus ? request.execution.executionId : null;

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


    private updateQueryParams() {
        let queryParams = this.cleanParams({...this.executionsFilters, ...this.tabFilters});
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: queryParams
        });
    }

    private opened(executionId: number) {
        return this.tabs.find(exec => exec.executionId === executionId);
    }

    private openTabs(executionsIds: string) {
        this.tabs = [];
        if (executionsIds) {
            this.tabs = executionsIds.split(',')
                .map(id => this.executions.find(exec => exec.executionId.toString() === id))
                .filter(exec => !!exec);
        }
        this.tabFilters['open'] = this.tabs.map(exec => exec.executionId).toString();
    }

    private focusOnTab(executionId: string) {
        this.activeTab = executionId ? +executionId : 0;
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

    onTabChange(changeEvent: NgbNavChangeEvent) {
        this.tabFilters['active'] = changeEvent.nextId;
        this.updateQueryParams();
    }

    private initRightMenu() {
        this.rightMenuItems = [
            {
                label: 'global.actions.execute',
                //click: this.onTabChange,
                class: 'fa fa-play',
                authorizations: [Authorization.SCENARIO_EXECUTE]
            },
            {
                label: 'global.actions.edit',
                link: TestCase.isComposed(this.scenarioId) ? '/scenario/' + this.scenarioId + '/component-edition' : '/scenario/' + this.scenarioId + '/raw-edition',
                class: 'fa fa-pencil-alt',
                authorizations: [Authorization.SCENARIO_WRITE]
            },
            {
                label: 'scenarios.execution.actions.remove',
                //click:
                class: 'fa fa-trash',
                authorizations: [Authorization.SCENARIO_WRITE]
            },
            {
                label: 'global.actions.clone',
                //click:
                class: 'fa fa-clone',
                authorizations: [Authorization.SCENARIO_WRITE]
            },
        ];

        if(!TestCase.isComposed(this.scenarioId)) {
            this.rightMenuItems.push({
                label: 'global.actions.export',
                click: this.exportScenario.bind(this),
                class: 'fa fa-file-code'
            })
        }
    }
    private exportScenario() {
        this.scenarioService.findRawTestCase(this.scenarioId)
            .subscribe((testCase: TestCase) => {
                const fileName = `${this.scenarioId}-${testCase.title}.chutney.hjson`;
                this.fileSaverService.saveText(testCase.content, fileName);
        });
    }
}
