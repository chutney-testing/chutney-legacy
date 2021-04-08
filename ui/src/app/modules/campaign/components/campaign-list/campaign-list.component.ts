import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { Campaign, CampaignExecutionReport } from '@core/model';
import { CampaignService } from '@core/services';
import { Subscription, timer } from 'rxjs';
import { JiraPluginService } from '@core/services/jira-plugin.service';
import { CampaignSchedulingService } from '@core/services/campaign-scheduling.service';
import { CampaignScheduling } from '@core/model/campaign/campaign-scheduling.model';
import { JiraPluginConfigurationService } from '@core/services/jira-plugin-configuration.service';
import { FREQUENCY } from '@core/model/campaign/FREQUENCY';

@Component({
    selector: 'chutney-campaigns',
    templateUrl: './campaign-list.component.html',
    styleUrls: ['./campaign-list.component.scss']
})
export class CampaignListComponent implements OnInit, OnDestroy {

    deletionConfirmationTextPrefix: string;
    deletionConfirmationTextSuffix: string;

    campaigns: Array<Campaign> = [];
    lastCampaignReports: Array<CampaignExecutionReport> = [];
    lastCampaignReportsSub: Subscription;
    campaignFilter: string;
    jiraMap: Map<string, string> = new Map();
    jiraUrl: string = '';
    isScheduled: Boolean;

    scheduledCampaigns: Array<CampaignScheduling> = [];

    constructor(private campaignService: CampaignService,
                private jiraLinkService: JiraPluginService,
                private jiraPluginConfigurationService: JiraPluginConfigurationService,
                private router: Router,
                private translate: TranslateService,
                private campaignSchedulingService: CampaignSchedulingService,
    ) {
        translate.get('campaigns.confirm.deletion.prefix').subscribe((res: string) => {
            this.deletionConfirmationTextPrefix = res;
        });
        translate.get('campaigns.confirm.deletion.suffix').subscribe((res: string) => {
            this.deletionConfirmationTextSuffix = res;
        });
    }

    ngOnInit() {
        this.loadAll();
    }

    ngOnDestroy(): void {
        this.unsuscribeLastCampaignReport();
    }

    loadAll() {
        this.initJiraPlugin();
        this.campaignService.findAllCampaigns().subscribe(
            (res) => this.campaigns = res,
            (error) => console.log(error)
        );

        this.findLastCampaignReports();

        this.loadSchedulingCampaign();
    }

    createCampaign() {
        const url = '/campaign/edition';
        this.router.navigateByUrl(url);
    }

    editCampaign(campaign: Campaign) {
        const url = '/campaign/' + campaign.id + '/edition';
        this.router.navigateByUrl(url);
    }

    deleteCampaign(id: number, title: string) {
        if (confirm(this.deletionConfirmationTextPrefix + title + this.deletionConfirmationTextSuffix)) {
            this.campaignService.delete(id).subscribe(
                () => {
                    this.removeJiraLink(id);
                    this.campaigns.splice(this.getIndexFromId(id), 1);
                    this.campaigns = this.campaigns.slice();
                });
        }
    }

    // Jira link //

    initJiraPlugin() {
        this.jiraPluginConfigurationService.get()
            .subscribe((r) => {
                if (r && r.url !== '') {
                    this.jiraUrl = r.url;
                    this.jiraLinkService.findCampaigns()
                        .subscribe(
                            (result) => {
                                this.jiraMap = result;
                            }
                        );
                }
            });
    }

    getJiraLink(id: string) {
        return this.jiraUrl + '/browse/' + this.jiraMap.get(id);
    }

    private getIndexFromId(id: number): number {
        return this.campaigns.findIndex((campaign: Campaign) => campaign.id === id);
    }

    private findLastCampaignReports() {
        this.campaignService.findLastCampaignReports().subscribe(
            (lastCampaignReports) => {
                this.lastCampaignReports = lastCampaignReports;
                if (CampaignService.existRunningCampaignReport(lastCampaignReports)) {
                    this.unsuscribeLastCampaignReport();
                    this.lastCampaignReportsSub = timer(5000).subscribe(() => this.findLastCampaignReports());
                }
            },
            (error) => console.log(error)
        );
    }

    private unsuscribeLastCampaignReport() {
        if (this.lastCampaignReportsSub) this.lastCampaignReportsSub.unsubscribe();
    }

    private removeJiraLink(campaignId: number) {
        this.jiraLinkService.removeForCampaign(campaignId).subscribe(
            () => {
            },
            (error) => console.log(error)
        );
    }

    private loadSchedulingCampaign() {
        this.campaignSchedulingService.findAll().subscribe(
            (res) => {
                this.scheduledCampaigns = res;
            },
            (error) => {
                console.log(error)
            });
    }

    isFrequencyCampaign(scheduledCampaign: CampaignScheduling) {
        return scheduledCampaign.frequency !== undefined;
    }
}
