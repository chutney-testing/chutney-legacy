import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';

import { ActivatedRoute, Router } from '@angular/router';
import { Campaign, EnvironmentMetadata, KeyValue, ScenarioComponent, ScenarioIndex, TestCase } from '@model';
import { CampaignService, ComponentService, EnvironmentAdminService, ScenarioService } from '@core/services';
import { DragulaService } from 'ng2-dragula';
import { distinct, flatMap, newInstance } from '@shared/tools/array-utils';
import { JiraLinkService } from '@core/services/jira-link.service';

@Component({
    selector: 'chutney-campaign-edition',
    templateUrl: './campaign-edition.component.html',
    styleUrls: ['./campaign-edition.component.scss']
})
export class CampaignEditionComponent implements OnInit, OnDestroy {

    campaignForm: FormGroup;

    campaign = new Campaign();
    submitted: boolean;
    scenarios: Array<ScenarioIndex> = [];
    scenariosToAdd: Array<ScenarioIndex> = [];
    errorMessage: any;
    scenariosFilter: string;
    subscription = new Subscription();

    hasParam = true;
    collapseParam = true;

    private routeParamsSubscription: Subscription;

    DRAGGABLE = 'DRAGGABLE';

    environments: EnvironmentMetadata[];
    selectedEnvironment: EnvironmentMetadata;

    itemList = [];
    settings = {};
    selectedTags: string[] = [];
    datasetId: string;
    jiraId: string;

    constructor(
        private campaignService: CampaignService,
        private scenarioService: ScenarioService,
        private componentService: ComponentService,
        private jiraLinkService: JiraLinkService,
        private formBuilder: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        private dragulaService: DragulaService,
        private environmentAdminService: EnvironmentAdminService
    ) {
        this.campaignForm = this.formBuilder.group({
            title: ['', Validators.required],
            description: '',
            tags: [],
            scenarioIds: [],
            scheduleTime: ['', Validators.pattern('^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$')],
            parameters: this.formBuilder.array([]),
            parallelRun: false,
            retryAuto: false,
            jiraId: ''
        });
    }

    ngOnInit() {

        this.submitted = false;
        this.loadEnvironment();
        this.loadAllScenarios();

        this.settings = {
            text: 'Sélectionner tag',
            enableCheckAll: false,
            autoPosition: false
        };
    }

    onItemSelect(item: any) {
        this.selectedTags.push(item.itemName);
        this.selectedTags = newInstance(this.selectedTags);
    }

    OnItemDeSelect(item: any) {
        this.selectedTags.splice(this.selectedTags.indexOf(item.itemName), 1);
        this.selectedTags = newInstance(this.selectedTags);
    }

    // convenience getter for easy access to form fields
    get f() {
        return this.campaignForm.controls;
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
        this.dragulaService.destroy(this.DRAGGABLE);
    }

    load(id) {
        if (id !== undefined) {
            this.campaignService.find(id).subscribe(
                (campaignFound) => {
                    this.campaign = campaignFound;
                    this.campaignForm.controls['title'].setValue(this.campaign.title);
                    this.campaignForm.controls['description'].setValue(this.campaign.description);
                    this.campaignForm.controls['scheduleTime'].setValue(this.campaign.scheduleTime);
                    this.campaignForm.controls['parallelRun'].setValue(this.campaign.parallelRun);
                    this.campaignForm.controls['retryAuto'].setValue(this.campaign.retryAuto);
                    this.selectedEnvironment = new EnvironmentMetadata(this.campaign.environment, '');
                    this.setCampaignScenarios();
                    this.updateCampaignParameters();
                    this.datasetId = this.campaign.datasetId;
                    this.loadJiraLink();
                },
                (error) => {
                    this.errorMessage = error._body;
                }
            );
        }
    }

    loadAllScenarios() {
        this.subscription = this.scenarioService.findScenarios().subscribe(
            (res) => {
                this.scenarios = res;
                this.routeParamsSubscription = this.route.params.subscribe((params) => {
                    this.load(params['id']);
                });
                this.initTags();
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }

    private initTags() {
        const allTagsInScenario: string[] = distinct(flatMap(this.scenarios, (sc) => sc.tags)).sort();
        let index = 0;
        this.itemList = allTagsInScenario.map(t => {
            index++;
            return {'id': index, 'itemName': t};
        });
    }

    loadEnvironment() {
        this.environmentAdminService.listEnvironments().subscribe(
            (res) => {
                this.environments = res.sort((t1, t2) => t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0);
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );
    }

    loadJiraLink() {
        this.jiraLinkService.findByCampaignId(this.campaign.id).subscribe(
            (jiraId) => {
                this.campaignForm.controls['jiraId'].setValue(jiraId);
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );

    }

    clear() {
        this.campaignForm.reset();
        let url: string;
        if (this.campaign.id) {
            url = '/campaign/' + this.campaign.id + '/execution';
        } else {
            url = '/campaign';
        }
        this.router.navigateByUrl(url);
    }

    saveCampaign() {
        this.submitted = true;
        const formValue = this.campaignForm.value;

        if (this.campaignForm.invalid) {
            return;
        }

        const computedParameters = new Map();
        formValue['parameters'].forEach((keyValue: KeyValue) => {
            computedParameters[keyValue.key] = keyValue.value;
        });

        this.campaign.title = formValue['title'];
        this.campaign.description = formValue['description'];
        this.campaign.scenarioIds = formValue['scenarioIds'];
        this.campaign.computedParameters = computedParameters;
        this.campaign.scheduleTime = formValue['scheduleTime'];
        this.campaign.environment = this.selectedEnvironment.name;
        this.campaign.parallelRun = formValue['parallelRun'];
        this.campaign.retryAuto = formValue['retryAuto'];
        this.campaign.datasetId = this.datasetId;

        this.setCampaignScenariosIdsToAdd(this.scenariosToAdd);
        if (this.campaign.id !== undefined) {
            this.subscribeToSaveResponse(
                this.campaignService.update(this.campaign));
        } else {
            this.subscribeToSaveResponse(
                this.campaignService.create(this.campaign));
        }
    }

    setCampaignScenarios() {
        this.scenariosToAdd = [];
        if (this.campaign.scenarioIds) {
            for (const idScenario of this.campaign.scenarioIds) {
                const scenarioFound = this.scenarios.find((x) => x.id === idScenario);
                if (!this.scenariosToAdd.some((s) => s.id === scenarioFound.id)) {
                    this.scenariosToAdd.push(scenarioFound);
                }
            }
        }
    }

    updateCampaignParameters() {
        const params = this.campaignForm.controls.parameters as FormArray;
        const addedParams = new Set();

        while (params.length !== 0) {
            params.removeAt(0);
        }

        for (const scenario of this.scenariosToAdd) {
            if (TestCase.isComposed(scenario.id)) {
                this.componentService.findComponentTestCase(scenario.id).subscribe((testCase: ScenarioComponent) => {
                    testCase.computedParameters.forEach((keyValue: KeyValue) => {
                        if (!addedParams.has(keyValue.key)) {
                            params.push(this.formBuilder.group({
                                key: keyValue.key,
                                value: this.campaign.computedParameters[keyValue.key] ?
                                    this.campaign.computedParameters[keyValue.key] : ''
                            }));
                            addedParams.add(keyValue.key);
                        }
                    });
                });
            }
        }
    }

    setCampaignScenariosIdsToAdd(scenariosToAdd: Array<ScenarioIndex>) {
        this.campaign.scenarioIds = [];
        for (const scenario of scenariosToAdd) {
            if (!this.campaign.scenarioIds.some((s) => s === scenario.id)) {
                this.campaign.scenarioIds.push(scenario.id);
            }
        }
    }

    addScenario(scenario: ScenarioIndex) {
        if (!this.scenariosToAdd.some((s) => s.id === scenario.id)) {
            this.scenariosToAdd.push(scenario);
            this.updateCampaignParameters();
            this.refreshForPipe();
        }
    }

    removeScenario(scenario: ScenarioIndex) {
        const index = this.scenariosToAdd.indexOf(scenario);
        this.scenariosToAdd.splice(index, 1);
        this.updateCampaignParameters();
        this.refreshForPipe();
    }

    switchCollapseParam() {
        this.collapseParam = !this.collapseParam;
    }

    private subscribeToSaveResponse(result: Observable<Campaign>) {
        result.subscribe(
            (res: Campaign) => this.onSaveSuccess(res),
            (error) => this.onSaveError(error));
    }

    private onSaveSuccess(result: Campaign) {
        this.submitted = false;
        const url = '/campaign/' + result.id + '/execution';
        this.updateJiraLink(result.id);
        this.router.navigateByUrl(url);
    }

    private onSaveError(error) {
        console.log(error);
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.submitted = false;
        this.errorMessage = error.message;
    }

    private refreshForPipe() {
        // force instance to change for pipe refresh
        this.scenariosToAdd = Object.assign([], this.scenariosToAdd);
    }

    setSelectedEnvironment(event: EnvironmentMetadata) {
        this.selectedEnvironment = event;
    }

    selectDataset(datasetId: string) {
        this.datasetId = datasetId;
    }

    private updateJiraLink(campaignId: Number) {
        this.jiraId = this.campaignForm.value['jiraId'];
        this.jiraLinkService.saveForCampaign(campaignId, this.jiraId).subscribe(
            () => {
            },
            (error) => {
                this.errorMessage = error.error;
            });
    }
}
