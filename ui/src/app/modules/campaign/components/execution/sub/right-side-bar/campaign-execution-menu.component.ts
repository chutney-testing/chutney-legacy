import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { combineLatest, Observable, of, tap, identity, timer } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { FileSaverService } from 'ngx-filesaver';

import * as JSZip from 'jszip';

import { CampaignService, EnvironmentAdminService, JiraPluginService, LoginService, ScenarioService } from '@core/services';
import { Authorization, ScenarioIndex, TestCase } from '@model';
import { EventManagerService } from '@shared';
import { MenuItem } from '@shared/components/layout/menuItem';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'chutney-campaign-execution-menu',
    templateUrl: './campaign-execution-menu.component.html',
    styleUrls: ['./campaign-execution-menu.component.scss']
})
export class CampaignExecutionMenuComponent implements OnInit {

    campaignId: number;
    rightMenuItems: MenuItem[] = [];

    private environments: Array<string> = [];
    private modalRef: BsModalRef;

    private campaignWithComponentError: string;

    @ViewChild('delete_modal') deleteModal: TemplateRef<any>;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private campaignService: CampaignService,
        private scenarioService: ScenarioService,
        private environmentAdminService: EnvironmentAdminService,
        private fileSaverService: FileSaverService,
        private jiraLinkService: JiraPluginService,
        private loginService: LoginService,
        private modalService: BsModalService,
        private translateService: TranslateService,
        private eventManagerService: EventManagerService) {
    }

    ngOnInit(): void {
        this.initTranslation();

        this.route.params.pipe(
            tap(params => this.campaignId = params['id']),
            switchMap(() => this.environments$())
        ).subscribe(environments => {
            this.environments = environments;
            this.initRightMenu();
        });
    }

    private initTranslation() {
      this.translateService.get('campaigns.export.component.error').subscribe((res: string) => {
          this.campaignWithComponentError = res;
        });
    }

    private environments$(): Observable<string[]> {
        if (this.loginService.hasAuthorization(Authorization.CAMPAIGN_EXECUTE)) {
            return this.environmentAdminService.listEnvironmentsNames();
        }
        return of([]);
    }

    private executeCampaign(envName: string) {
        this.broadcastCatchError(this.campaignService.executeCampaign(this.campaignId, envName)).subscribe();
        timer(1000).pipe(
            switchMap(() => of(this.eventManagerService.broadcast({ name: 'execute', env: envName })))
        ).subscribe();
    }

    private deleteCampaign() {
        combineLatest([
            this.broadcastCatchError(this.campaignService.delete(this.campaignId)),
            this.broadcastCatchError(this.jiraLinkService.removeForCampaign(this.campaignId))
        ]).subscribe(() => this.router.navigateByUrl('/campaign'));
    }

    private exportCampaign() {
        combineLatest([
            this.broadcastCatchError(this.campaignService.find(this.campaignId)),
            this.broadcastCatchError(this.campaignService.findAllScenarios(this.campaignId))
        ]).subscribe(([campaign, scenarios]) => this.createZip(campaign.title, scenarios));
    }

    private createZip(campaignTitle: string, scenarios: ScenarioIndex[]) {
        const $rawTestCases: Array<Observable<TestCase>> = [];

        var existComponentScenarios: number = 0;
        for (const testCase of scenarios) {
            if (!TestCase.isComposed(testCase.id)) {
                $rawTestCases.push(this.scenarioService.findRawTestCase(testCase.id));
            } else {
                existComponentScenarios++;
            }
        }
        if (!!existComponentScenarios) {
            this.broadcastError(this.campaignWithComponentError + ` (${existComponentScenarios})`);
        }

        combineLatest($rawTestCases).subscribe(rawTestCases => {
            const zip = new JSZip();
            rawTestCases.forEach(testCase => {
                const fileName = `${testCase.id}-${testCase.title}.chutney.hjson`;
                zip.file(fileName, testCase.content);
            });

            zip.generateAsync({ type: 'blob' })
                .then(blob => this.fileSaverService.save(blob, campaignTitle));
        });
    }

    private openDeleteModal() {
        this.modalRef = this.modalService.show(this.deleteModal, { class: 'modal-sm' });
    }

    confirmDelete(): void {
        this.modalRef.hide();
        this.deleteCampaign();
    }

    declineDelete(): void {
        this.modalRef.hide();
    }

    private initRightMenu() {
        this.rightMenuItems = [
            {
                label: 'global.actions.execute',
                click: this.executeCampaign.bind(this),
                iconClass: 'fa fa-play',
                authorizations: [Authorization.CAMPAIGN_EXECUTE],
                options: this.environments.map(env => {
                    return { id: env, label: env };
                })
            },
            {
                label: 'global.actions.edit',
                link: `/campaign/${this.campaignId}/edition`,
                iconClass: 'fa fa-pencil-alt',
                authorizations: [Authorization.CAMPAIGN_WRITE]
            },
            {
                label: 'global.actions.delete',
                click: this.openDeleteModal.bind(this),
                iconClass: 'fa fa-trash',
                authorizations: [Authorization.CAMPAIGN_WRITE]
            },
            {
                label: 'global.actions.export',
                click: this.exportCampaign.bind(this),
                iconClass: 'fa fa-file-code',
                authorizations: [Authorization.CAMPAIGN_WRITE]
            }
        ];
    }

    private broadcastError(errorMessage: string) {
        this.eventManagerService.broadcast({ name: 'error', msg: errorMessage });
    }

    private broadcastCatchError(obs: Observable<any>, errorHandler: (error: any) => string = identity): Observable<any> {
        return obs.pipe(
            catchError((err: HttpErrorResponse) => {
                this.broadcastError(errorHandler(err.error));
                throw err;
            })
        );
    }
}
