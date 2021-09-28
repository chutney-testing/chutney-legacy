import { Component, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { FileSaverService } from 'ngx-filesaver';
import { NgbDropdown } from '@ng-bootstrap/ng-bootstrap';

import { combineLatest, Observable, Subscription, timer } from 'rxjs';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Color, Label } from 'ng2-charts';
import * as JSZip from 'jszip';

import {
    Campaign,
    CampaignExecutionReport,
    ScenarioExecutionReportOutline,
    ScenarioIndex,
    TestCase,
    Authorization
} from '@model';
import {
    CampaignService,
    EnvironmentAdminService,
    ScenarioService,
    JiraPluginService,
    LoginService
} from '@core/services';
import { newInstance, sortByAndOrder } from '@shared/tools';

@Component({
    selector: 'chutney-execution-campaign',
    providers: [Location],
    templateUrl: './execution-campaign.component.html',
    styleUrls: ['./execution-campaign.component.scss']
})
export class CampaignExecutionComponent implements OnInit, OnDestroy {

    TIMER = 2000;

    deletionConfirmationTextPrefix: string;
    deletionConfirmationTextSuffix: string;
    executionError: String;

    campaign: Campaign;
    scenarios: Array<ScenarioIndex> = [];
    orderedScenarios: Array<ScenarioIndex> = [];

    last: CampaignReport;
    current: CampaignReport;
    stopRequested = false;

    currentCampaignExecutionReport: CampaignExecutionReport;
    currentScenariosReportsOutlines: Array<ScenarioExecutionReportOutline> = [];
    campaignSub: Subscription;

    environments: Array<string>;

    orderBy: any;
    reverseOrder: any;

    @ViewChildren(NgbDropdown)
    private executeDropDown: QueryList<NgbDropdown>;

    running = false;
    // todo: add errorMessage in template
    errorMessage: any;

    private subscriptionLoadCampaign: Subscription;


    public lineChartData: ChartDataSets[] = [];
    public lineChartLabels: Label[] = [];
    public lineChartOptions: (ChartOptions) = {
        responsive: true,
    };
    public lineChartColors: Color[] = [
        {
            borderColor: 'green',
            backgroundColor: 'rgba(0,255,0,0.1)'
        },
        {
            borderColor: 'red',
            backgroundColor: 'rgba(255,0,0,0.3)'
        },
    ];
    public lineChartLegend = false;
    public lineChartType = 'line';
    public lineChartPlugins = [];

    Authorization = Authorization;

    constructor(private campaignService: CampaignService,
                private environmentAdminService: EnvironmentAdminService,
                private fileSaverService: FileSaverService,
                private jiraLinkService: JiraPluginService,
                private route: ActivatedRoute,
                private router: Router,
                private scenarioService: ScenarioService,
                private translate: TranslateService,
                private loginService: LoginService,
                private location: Location
    ) {
        translate.get('campaigns.confirm.deletion.prefix').subscribe((res: string) => {
            this.deletionConfirmationTextPrefix = res;
        });
        translate.get('campaigns.confirm.deletion.suffix').subscribe((res: string) => {
            this.deletionConfirmationTextSuffix = res;
        });
    }

    ngOnInit() {
        this.subscriptionLoadCampaign = this.route.params.subscribe((params) => {
            this.loadCampaign(params['id'], false, params['execId']);
            this.loadScenarios(params['id']);
        });
        if (this.loginService.hasAuthorization(Authorization.CAMPAIGN_EXECUTE)) {
            this.environmentAdminService.listEnvironmentsNames().subscribe(
                (res) => this.environments = res
            );
        }
    }

    ngOnDestroy() {
        if (this.subscriptionLoadCampaign) {
            this.subscriptionLoadCampaign.unsubscribe();
        }
        this.unsubscribeCampaign();
    }

    private loadCampaign(campaignId: number, selectLast: boolean, executionId: number = null) {
        this.campaignService.find(campaignId).subscribe(
            (campaign) => {
                if (campaign) {
                    this.campaign = campaign;
                    this.loadReports(this.campaign, selectLast, executionId);
                }
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }

    private loadReports(campaign: Campaign, selectLast: boolean, executionId: number = null) {
        if (this.campaign.campaignExecutionReports.length > 0) {
            this.sortCurrentCampaignReports();
            if (selectLast) {
                this.selectReport(campaign.campaignExecutionReports[0]);
            }
            if (executionId) {
                const execution = campaign.campaignExecutionReports.filter((r) => r.executionId === executionId);
                if (execution.length === 1) {
                    this.selectReport(execution[0]);
                }
            }
            this.running = CampaignService.existRunningCampaignReport(this.campaign.campaignExecutionReports);
            if (this.running) {
                this.unsubscribeCampaign();
                this.campaignSub = timer(this.TIMER).subscribe(() => {
                        this.updateRunningReport();
                    }
                );
            }
            this.setChartData(campaign.campaignExecutionReports);
            this.last = this.getLastCompleteReport();
        }
    }

    selectLastCompleteExecution() {
        this.currentCampaignExecutionReport = null;
        this.updateLocation('last');
    }

    private updateRunningReport() {
        this.campaignService.find(this.campaign.id).subscribe(
            (campaign) => {
                const sortedReports = campaign.campaignExecutionReports.sort((a, b) => b.executionId - a.executionId);
                if (this.campaign.campaignExecutionReports[0] && this.campaign.campaignExecutionReports[0].executionId !== sortedReports[0].executionId) {
                    // Add new running report
                    this.campaign.campaignExecutionReports.unshift(sortedReports[0]);
                    this.selectReport(sortedReports[0]);
                } else {
                    // Update running report
                    this.campaign.campaignExecutionReports[0] = sortedReports[0];
                    if (this.currentCampaignExecutionReport && this.currentCampaignExecutionReport.executionId === sortedReports[0].executionId) {
                        this.currentCampaignExecutionReport = sortedReports[0];
                        this.currentScenariosReportsOutlines = newInstance(sortedReports[0].scenarioExecutionReports);
                    }
                }
                this.unsubscribeCampaign();
                if (sortedReports[0].status === 'RUNNING') {
                    this.campaignSub = timer(this.TIMER).subscribe(() => {
                            this.updateRunningReport();
                        }
                    );
                }
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }

    private setChartData(reports: Array<CampaignExecutionReport>) {
        const scenarioOK = reports.filter(r => !r.partialExecution).map(r => r.scenarioExecutionReports
            .filter(s => s.status === 'SUCCESS').length).reverse();
        const scenarioKO = reports.filter(r => !r.partialExecution).map(r => r.scenarioExecutionReports
            .filter(s => s.status === 'FAILURE').length).reverse();
        this.lineChartData = [{data: scenarioOK}, {data: scenarioKO}];
        this.lineChartLabels = reports.filter(r => !r.partialExecution).map(r => '' + r.executionId).reverse();
    }

    private getLastCompleteReport() {
        for (const report of this.campaign.campaignExecutionReports) {
            const campaignReport = new CampaignReport(report);
            if (!campaignReport.isRunning() && !report.partialExecution && !campaignReport.hasNotExecuted() && !campaignReport.hasStopped()) {
                return campaignReport;
            }
        }
        return null;
    }

    private loadScenarios(campaignId) {
        this.campaignService.findAllScenarios(campaignId).subscribe(
            (scenarios) => {
                this.scenarios = scenarios;
                this.orderedScenarios = newInstance(scenarios);
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }

    sortCurrentBy(property) {
        this.sortBy(this.currentScenariosReportsOutlines, property);
    }

    sortLastBy(property) {
        this.sortBy(this.orderedScenarios, property);
    }

    sortBy(collection: any, property) {
        if (this.orderBy === property) {
            this.reverseOrder = !this.reverseOrder;
        }
        this.orderBy = property;

        return sortByAndOrder(
            collection,
            this.getKeyExtractorBy(property),
            this.reverseOrder
        );
    }

    private getKeyExtractorBy(property: string) {
        if (property == 'title') {
            return i => i[property] == null ? '' : i[property].toLowerCase();
        }
        if (property == 'creationDate') {
            const now = Date.now();
            return i => i[property] == null ? now - 1491841324 /*2017-04-10T16:22:04*/ : now - Date.parse(i[property]);
        } else {
            return i => i[property] == null ? '' : i[property];
        }
    }

    executeCampaign(env: string) {
        this.running = true;
        this.stopRequested = false;
        this.campaignService.executeCampaign(this.campaign.id, env).subscribe(
            () => {
                // Do nothing
            },
            (error) => {
                this.errorMessage = error.error;
            },
            () => this.running = false
        );
        this.campaignSub = timer(this.TIMER).subscribe(() => {
            this.loadCampaign(this.campaign.id, true);
        });
    }

    executeCampaignOnToggle() {
        if (this.environments.length === 1) {
            this.executeDropDown.first.close();
            this.executeCampaign(this.environments[0]);
        }
    }

    selectReport(campaignExecutionReport: CampaignExecutionReport) {
        this.resetOrdering();
        this.current = new CampaignReport(campaignExecutionReport);
        this.currentCampaignExecutionReport = campaignExecutionReport;
        this.currentScenariosReportsOutlines = newInstance(campaignExecutionReport.scenarioExecutionReports);
        this.updateLocation(this.currentCampaignExecutionReport.executionId);
    }

    private resetOrdering() {
        this.orderBy = '';
        this.reverseOrder = false;
        this.orderedScenarios = newInstance(this.scenarios);
    }

    editCampaign(campaign: Campaign) {
        const url = '/campaign/' + campaign.id + '/edition';
        this.router.navigateByUrl(url);
    }

    deleteCampaign(campaignId: number, title: string) {
        if (confirm(this.deletionConfirmationTextPrefix + title + this.deletionConfirmationTextSuffix)) {
            this.campaignService.delete(campaignId).subscribe(
                (response) => {
                    this.removeJiraLink(campaignId);
                    this.router.navigateByUrl('/campaign');
                },
                (error) => {
                    this.errorMessage = error.error;
                }
            );
        }
    }

    isOlderThan(date: string, durationInHours: number) {
        if (date) {
            return new Date().getTime() - new Date(date).getTime() > durationInHours * 60 * 60 * 1000;
        }
        return false;
    }

    exportScenarioByCampaign() {

        const rawTestCaseContent: Array<Observable<TestCase>> = [];

        for (const testCase of this.scenarios) {
            if (!TestCase.isComposed(testCase.id)) {
                rawTestCaseContent.push(this.scenarioService.findRawTestCase(testCase.id));
            } else {
                this.errorMessage = 'La campagne possede des scénarios component qui ne sont pas exportables';
            }
        }

        const zip = new JSZip();
        const campaignTitle = this.campaign.title;

        combineLatest(rawTestCaseContent).subscribe(results => {
                results.forEach(testCase => {
                    const fileName = `${testCase.id}-${testCase.title}.chutney.hjson`;
                    zip.file(fileName, testCase.content);
                });

                zip
                    .generateAsync({type: 'blob'})
                    .then(blob => this.fileSaverService.save(blob, campaignTitle));
            }
        );
    }

    stopScenario() {
        this.campaignService.stopExecution(this.campaign.id, this.currentCampaignExecutionReport.executionId).subscribe(() => {
            this.stopRequested = true;
        }, error => {
            this.executionError = 'Cannot stop campaign : ' + error.status + ' ' + error.statusText + ' ' + error.error;
        }, () => {
        });
    }

    replayFailed() {
        this.running = true;
        this.campaignService.replayFailedScenario(this.currentCampaignExecutionReport.executionId).subscribe(
            () => {
                // Do nothing
            },
            (error) => {
                this.errorMessage = error.error;
            },
            () => this.running = false
        );
        this.campaignSub = timer(this.TIMER).subscribe(() => {
            this.updateRunningReport();
        });
    }

    private sortCurrentCampaignReports() {
        this.campaign.campaignExecutionReports.sort((a, b) => b.executionId - a.executionId);
    }

    private unsubscribeCampaign() {
        if (this.campaignSub) {
            this.campaignSub.unsubscribe();
        }
    }

    private removeJiraLink(campaignId: number) {
        this.jiraLinkService.removeForCampaign(campaignId).subscribe(
            () => {},
            (error) => { console.log(error); }
        );
    }

    hasAuthorization(authorizations: Array<Authorization>): boolean {
        return this.loginService.hasAuthorization(authorizations);
    }

    private updateLocation(executionId) {
        this.location.replaceState('/campaign/' + this.campaign.id + '/execution/' + executionId);
    }
}

class CampaignReport {
    private report: CampaignExecutionReport;

    passed: number;
    failed: number;
    stopped: number;
    notexecuted: number;
    total: number;

    constructor(report: CampaignExecutionReport) {
        this.report = report;
        this.passed = this.countScenarioByStatus('SUCCESS', report);
        this.failed = this.countScenarioByStatus('FAILURE', report);
        this.stopped = this.countScenarioByStatus('STOPPED', report);
        this.notexecuted = this.countScenarioByStatus('NOT_EXECUTED', report);
        this.total = this.passed + this.failed + this.stopped + this.notexecuted;
    }

    private countScenarioByStatus(status: String, report: CampaignExecutionReport) {
        return report.scenarioExecutionReports.filter(s => s.status === status).length;
    }

    allPassed() {
        return !this.hasStopped() && !this.hasStopped();
    }

    hasPassed() {
        return this.passed > 0;
    }

    hasFailure() {
        return this.failed > 0;
    }

    hasStopped() {
        return this.stopped > 0;
    }

    hasNotExecuted() {
        return this.notexecuted > 0;
    }

    isRunning() {
        return 'RUNNING' === this.report.status;
    }
}
