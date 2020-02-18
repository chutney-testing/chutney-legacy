import { Component, OnDestroy, OnInit, ViewChildren, QueryList } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, Observable, Subscription, timer } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import {
    Campaign,
    CampaignExecutionReport,
    ScenarioExecutionReportOutline,
    TestCase,
    EnvironmentMetadata,
    ScenarioIndex
} from '@core/model';
import { CampaignService, ScenarioService, EnvironmentAdminService } from '@core/services';
import { FileSaverService } from 'ngx-filesaver';
import * as JSZip from 'jszip';
import { NgbDropdown } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'chutney-execution-campaign',
    templateUrl: './execution-campaign.component.html',
    styleUrls: ['./execution-campaign.component.scss']
})
export class CampaignExecutionComponent implements OnInit, OnDestroy {

    deletionConfirmationTextPrefix: string;
    deletionConfirmationTextSuffix: string;
    executionError: String;

    campaign: Campaign;
    scenarios: Array<ScenarioIndex> = [];
    scenarioOK: number;
    scenarioKO: number;

    currentCampaignExecutionReport: CampaignExecutionReport;
    currentScenariosReportsOutlines: Array<ScenarioExecutionReportOutline> = [];
    campaignSub: Subscription;

    environments: EnvironmentMetadata[];

    @ViewChildren(NgbDropdown)
    private executeDropDown: QueryList<NgbDropdown>;

    running = false;
    // todo: ajouter errorMessage dans le template
    errorMessage: any;

    private subscriptionLoadCampaign: Subscription;

    constructor(private campaignService: CampaignService,
                private route: ActivatedRoute,
                private router: Router,
                private translate: TranslateService,
                private fileSaverService: FileSaverService,
                private scenarioService: ScenarioService,
                private environmentAdminService: EnvironmentAdminService
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
            this.loadCampaign(params['id'], false);
            this.loadScenarios(params['id']);
        });
        this.environmentAdminService.listEnvironments().subscribe(
            (res) => this.environments = res
        );
    }

    ngOnDestroy() {
        this.subscriptionLoadCampaign.unsubscribe();
        this.unsuscribeCampaign();
    }

    loadCampaign(campaignId: number, selectLast: boolean) {
        this.campaignService.find(campaignId).subscribe(
            (campaign) => {
                if (campaign) {
                    this.campaign = campaign;
                    this.loadReports(this.campaign, selectLast);
                }
            },
            (error) => {
                console.log(error);
                this.errorMessage = error;
            }
        );
    }

    loadReports(campaign: Campaign, selectLast: boolean) {
        if (this.campaign.campaignExecutionReports.length > 0) {
            this.sortCurrentCampaignReports();
            if (selectLast) {
                this.selectReport(campaign.campaignExecutionReports[0]);
            }
            this.running = CampaignService.existRunningCampaignReport(this.campaign.campaignExecutionReports);
            if (this.running) {
                this.unsuscribeCampaign();
                this.campaignSub = timer(10000).subscribe(() =>  {
                        this.loadCampaign(this.campaign.id, true);
                    }
                );
            }

            this.refreshScenarioCount(this.getLastNotPartialReport());
        }
    }

    getLastNotPartialReport() {
        for (const report of this.campaign.campaignExecutionReports) {
            if (!report.partialExecution) {
                return report;
            }
        }
    }

    refreshScenarioCount(report: CampaignExecutionReport) {
        this.scenarioOK = report.scenarioExecutionReports.filter(s => s.status === 'SUCCESS').length;
        this.scenarioKO = this.countFailedScenario(report);
    }

    countFailedScenario(report: CampaignExecutionReport) {
        return report.scenarioExecutionReports.filter(s => s.status === 'FAILURE').length;
    }

    loadScenarios(campaignId) {
        this.campaignService.findAllScenarios(campaignId).subscribe(
            (scenarios) => {
                this.scenarios = scenarios;
            },
            (error) => {
                console.log(error);
                this.errorMessage = error;
            }
        );
    }

    executeCampaign(env: string) {
        this.running = true;
        this.campaignService.executeCampaign(this.campaign.id, env).subscribe(
            () => {
                // Do nothing
            },
            (error) => {
                console.log(error);
                this.errorMessage = error;
            },
            () => this.running = false
        );
        this.campaignSub = timer(1500).subscribe(() =>  {
            this.loadCampaign(this.campaign.id, true);
            this.loadScenarios(this.campaign.id);
        });
    }

    executeCampaignOnToggle() {
        if (this.environments.length === 1) {
            this.executeDropDown.first.close();
            this.executeCampaign(this.environments[0].name);
        }
    }

    selectReport(campaignExecutionReport: CampaignExecutionReport) {
        this.currentCampaignExecutionReport = campaignExecutionReport;
        this.currentScenariosReportsOutlines = campaignExecutionReport.scenarioExecutionReports;
    }

    editCampaign(campaign: Campaign) {
        const url = '/campaign/' + campaign.id + '/edition';
        this.router.navigateByUrl(url);
    }

    deleteCampaign(idCampaign: number, title: string) {
        if (confirm(this.deletionConfirmationTextPrefix + title.toUpperCase() + this.deletionConfirmationTextSuffix)) {
            this.campaignService.delete(idCampaign).subscribe(
                (response) => {
                    this.router.navigateByUrl('/campaign');
                },
                (error) => {
                    console.log(error);
                    this.errorMessage = error;
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
        }, error => {
            this.executionError = 'Cannot stop campaign : ' + error.status + ' ' + error.statusText + ' ' + error.error;
        }, () => {
        });
    }

    closeExecutionPanel() {
        this.currentCampaignExecutionReport = null;
    }

    replayFailed() {
        this.running = true;
        this.campaignService.replayFailedScenario(this.currentCampaignExecutionReport.executionId).subscribe(
            () => {
                // Do nothing
            },
            (error) => {
                console.log(error);
                this.errorMessage = error;
            },
            () => this.running = false
        );
        this.campaignSub = timer(1500).subscribe(() =>  {
            this.loadCampaign(this.campaign.id, true);
            this.loadScenarios(this.campaign.id);
        });
    }

    private sortCurrentCampaignReports() {
        this.campaign.campaignExecutionReports.sort((a, b) => b.executionId - a.executionId);
    }

    private unsuscribeCampaign() {
        if (this.campaignSub) {
            this.campaignSub.unsubscribe();
        }
    }
}
