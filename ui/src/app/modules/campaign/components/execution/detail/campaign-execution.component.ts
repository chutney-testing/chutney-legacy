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

import { Component, Input, OnInit } from "@angular/core";
import { forkJoin, Observable, of, switchMap, timer } from "rxjs";

import { Authorization, CampaignReport, JiraScenario, JiraTestExecutionScenarios, ScenarioExecutionReportOutline, XrayStatus } from "@core/model";
import { CampaignService, JiraPluginService } from "@core/services";
import { Params } from "@angular/router";
import { ExecutionStatus } from "@core/model/scenario/execution-status";
import { EventManagerService } from "@shared";
import { sortByAndOrder } from '@shared/tools';

@Component({
    selector: 'chutney-campaign-execution',
    templateUrl: './campaign-execution.component.html',
    styleUrls: ['./campaign-execution.component.scss']
})
export class CampaignExecutionComponent implements OnInit {

    @Input() campaignId: number;
    @Input() report: CampaignReport;
    @Input() jiraUrl: string;

    Authorization = Authorization;
    ExecutionStatus = ExecutionStatus;

    jiraTestExecutionId: string;
    private jiraScenarios: JiraScenario[] = [];
    UNSUPPORTED = 'UNSUPPORTED';
    selectedStatusByScenarioId: Map<string, string> = new Map();

    orderBy: string;
    reverseOrder: boolean;

    constructor(
        private jiraLinkService: JiraPluginService,
        private campaignService: CampaignService,
        private eventManagerService: EventManagerService
    ) { }

    ngOnInit(): void {
        this.cleanJiraUrl();
        forkJoin({
            jirjiraTestExecutionScenarios: this.jiraTestExecutionScenarios$()
        }).subscribe(result => {
            this.jiraScenarios = result.jirjiraTestExecutionScenarios.jiraScenarios;
            this.jiraTestExecutionId = result.jirjiraTestExecutionScenarios.id;
        });
    }

    private cleanJiraUrl() {
        if (this.jiraUrl && this.jiraUrl.length == 0) {
            this.jiraUrl = null;
        }
    }

    private jiraTestExecutionScenarios$(): Observable<JiraTestExecutionScenarios> {
        return this.jiraLinkService.findByCampaignId(this.campaignId).pipe(
            switchMap((jiraId) => { // TODO - Why this condition ? don't understand it !!
                if (jiraId) {
                    return this.jiraLinkService.findTestExecScenariosByCampaignExecution(this.report.report.executionId);
                } else {
                    return of(new JiraTestExecutionScenarios(null, []));
                }
            })
        );
    }

    xrayStatuses(): Array<string> {
        const keys = Object.keys(XrayStatus);
        return keys.slice();
    }

    selectedUpdateStatus(scenarioId: string, event: any) {
        this.selectedStatusByScenarioId.set(scenarioId, event.target.value);
    }

    updateStatus(scenarioId: string) {
        const newStatus = this.selectedStatusByScenarioId.get(scenarioId);
        if (newStatus === XrayStatus.PASS || newStatus === XrayStatus.FAIL) {
            this.jiraLinkService.updateScenarioStatus(this.jiraTestExecutionId, scenarioId, newStatus).subscribe(
                () => {},
                (error) => {
                    console.log(error);
                }
            );
        }
    }

    scenarioStatus(scenarioId: String): string {
        const jiraScenario = this.jiraScenarios.filter(s => s.chutneyId === scenarioId);
        if (jiraScenario.length > 0) {
            if (jiraScenario[0].executionStatus === XrayStatus.PASS || jiraScenario[0].executionStatus === XrayStatus.FAIL) {
                return jiraScenario[0].executionStatus;
            }
        }
        return this.UNSUPPORTED;
    }

    jiraLinkFrom(chutneyId: string) {
        const foundScenario = this.jiraScenarios.find(s => s.chutneyId === chutneyId);
        if (foundScenario) {
            return this.jiraUrl + '/browse/' + foundScenario.id;
        } else {
            return null;
        }
    }

    toQueryParams (scenarioExecutionReportOutline: ScenarioExecutionReportOutline): Params {
        let execId = scenarioExecutionReportOutline.executionId !== -1 ? scenarioExecutionReportOutline.executionId : 'last';
        return {
            active: execId,
            open: execId,
        }
    }

    replay() {
        this.campaignService.replayFailedScenario(this.report.report.executionId).subscribe({
            error: (error) => this.eventManagerService.broadcast({ name: 'error', msg: error.error })
        });

        timer(1000).pipe(
            switchMap(() => of(this.eventManagerService.broadcast({ name: 'replay', executionId: this.report.report.executionId })))
        ).subscribe();
    }

    stop() {
        this.campaignService.stopExecution(this.campaignId, this.report.report.executionId).subscribe({
            error: (error) => this.eventManagerService.broadcast({ name: 'error', msg: error.error })
        });
    }

    statusClass(scenarioReportOutline: ScenarioExecutionReportOutline): string {
        if (scenarioReportOutline.status === ExecutionStatus.SUCCESS) {
            return 'fa-solid fa-circle-check text-info';
        }
        if (scenarioReportOutline.status === ExecutionStatus.FAILURE) {
            return 'fa-solid fa-circle-xmark text-danger';
        }
        if (scenarioReportOutline.status === ExecutionStatus.RUNNING || scenarioReportOutline.status === ExecutionStatus.PAUSED) {
            return 'fa-solid fa-spinner fa-pulse text-warning';
        }
        if (scenarioReportOutline.status === ExecutionStatus.STOPPED) {
            return 'fa-solid fa-circle-stop text-warning';
        }
        if (scenarioReportOutline.status === ExecutionStatus.NOT_EXECUTED) {
            return 'fa-regular fa-circle text-warning';
        }
        return null;
    }

    sortBy(property) {
        if (this.orderBy === property) {
            this.reverseOrder = !this.reverseOrder;
        }
        this.orderBy = property;

        return sortByAndOrder(
            this.report.report.scenarioExecutionReports,
            (i) => i[property] == null ? '' : i[property],
            this.reverseOrder
        );
    }
}
