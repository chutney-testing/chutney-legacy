import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { Campaign, CampaignExecutionReport, SelectableTags } from '@core/model';
import { CampaignService } from '@core/services';
import { Subscription, timer } from 'rxjs';
import { JiraPluginService } from '@core/services/jira-plugin.service';
import { CampaignSchedulingService } from '@core/services/campaign-scheduling.service';
import { CampaignScheduling } from '@core/model/campaign/campaign-scheduling.model';
import { JiraPluginConfigurationService } from '@core/services/jira-plugin-configuration.service';
import { StateService } from '@shared/state/state.service';
import { distinct, filterOnTextContent, flatMap, intersection } from '@shared/tools/array-utils';

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
    jiraMap: Map<string, string> = new Map();
    jiraUrl = '';
    isScheduled: Boolean;
    // Filter
    campaignFilter: string;
    viewedCampaigns: Array<Campaign> = [];
    tagFilter = new SelectableTags<String>();

    scheduledCampaigns: Array<CampaignScheduling> = [];

    constructor(private campaignService: CampaignService,
                private jiraLinkService: JiraPluginService,
                private jiraPluginConfigurationService: JiraPluginConfigurationService,
                private router: Router,
                private translate: TranslateService,
                private stateService: StateService,
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
        this.unsubscribeLastCampaignReport();
    }

    loadAll() {
        this.initJiraPlugin();
        this.campaignService.findAllCampaigns().subscribe(
            (res) => {
                this.campaigns = res;
                this.applyDefaultState();
                this.applySavedState();
                this.applyFilters();
            },
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

    // Filtering //
    updateTextFilter(text: string) {
        this.campaignFilter = text;
        this.applyFilters();
    }

    selectAll() {
        this.tagFilter.selectAll();
        this.stateService.changeCampaignTags(this.tagFilter.selected());
        this.stateService.changeCampaignNoTag(this.tagFilter.setNoTag(true));
        this.applyFilters();
    }

    isSelectAll() {
        return this.tagFilter.isSelectAll();
    }

    deselectAll() {
        this.tagFilter.deselectAll();
        this.stateService.changeCampaignNoTag(false);
        this.applyFilters();
    }

    toggleNoTagFilter() {
        this.tagFilter.toggleNoTag();
        this.stateService.changeCampaignNoTag(this.tagFilter.isNoTagSelected());
        this.applyFilters();
    }

    toggleTagFilter(tag: String) {
        this.tagFilter.toggleSelect(tag);
        this.stateService.changeCampaignTags(this.tagFilter.selected());
        this.applyFilters();
    }

    applyFilters() {
        this.viewedCampaigns = filterOnTextContent(this.campaigns, this.campaignFilter, ['title', 'id']);
        this.viewedCampaigns = this.filterOnAttributes();
    }

    private applyDefaultState() {
        this.viewedCampaigns = this.campaigns;
        this.tagFilter.initialize(this.findAllTags());
    }

    private findAllTags() {
        return distinct(flatMap(this.campaigns, (campaign) => campaign.tags)).sort();
    }

    private applySavedState() {
        this.setSelectedTags();
    }


    private setSelectedTags() {
        const savedTags = this.stateService.getCampaignTags();
        if (savedTags != null) {
            this.tagFilter.selectTags(savedTags);
        }

        const noTag = this.stateService.getCampaignNoTag();
        if (noTag != null) {
            this.tagFilter.setNoTag(noTag);
        }
    }

    private filterOnAttributes() {
        const input = this.viewedCampaigns;
        if (this.tagFilter.isSelectAll()) {
            return input;
        }

        const tags = this.tagFilter.selected();
        const noTag = this.tagFilter.isNoTagSelected();

        return input.filter((campaign: Campaign) => {
            return (this.tagPresent(tags, campaign)
                    || this.noTagPresent(noTag, campaign));
            });
    }

    private tagPresent(tags: String[], campaign: Campaign): boolean {
        return intersection(tags, campaign.tags).length > 0;
    }

    private noTagPresent(noTag: boolean, campaign: Campaign): boolean {
        return noTag && campaign.tags.length === 0;
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
                    this.unsubscribeLastCampaignReport();
                    this.lastCampaignReportsSub = timer(5000).subscribe(() => this.findLastCampaignReports());
                }
            },
            (error) => console.log(error)
        );
    }

    private unsubscribeLastCampaignReport() {
        if (this.lastCampaignReportsSub) {
            this.lastCampaignReportsSub.unsubscribe();
        }
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
                console.log(error);
            });
    }

    isFrequencyCampaign(scheduledCampaign: CampaignScheduling) {
        return scheduledCampaign.frequency != null;
    }
}
