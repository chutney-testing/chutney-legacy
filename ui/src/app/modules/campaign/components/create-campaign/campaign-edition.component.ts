import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { DragulaService } from 'ng2-dragula';

import {
    Campaign, JiraScenario,
    KeyValue,
    ScenarioIndex,
    TestCase
} from '@model';
import {
    CampaignService,
    ComponentService,
    EnvironmentAdminService,
    ScenarioService,
    JiraPluginService,
    JiraPluginConfigurationService
} from '@core/services';
import { distinct, flatMap, newInstance } from '@shared/tools/array-utils';
import { isNotEmpty } from '@shared';
import { TranslateService } from '@ngx-translate/core';

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

    environments: Array<string>;
    selectedEnvironment: string;

    itemList = [];
    jiraItemList = [];
    settings = {};
    jirasettings = {};
    selectedTags: string[] = [];
    jiraSelectedTags: string[] = [];
    datasetId: string;
    jiraId: string;
    jiraMap: Map<string, string> = new Map();
    jiraUrl = '';
    jiraScenarios: JiraScenario[] = [];
    jiraScenariosToExclude: Array<ScenarioIndex> = [];

    constructor(
        private campaignService: CampaignService,
        private scenarioService: ScenarioService,
        private componentService: ComponentService,
        private jiraLinkService: JiraPluginService,
        private jiraPluginConfigurationService: JiraPluginConfigurationService,
        private formBuilder: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        private dragulaService: DragulaService,
        private environmentAdminService: EnvironmentAdminService,
        private translate: TranslateService,
    ) {
        this.campaignForm = this.formBuilder.group({
            title: ['', Validators.required],
            description: '',
            tags: [],
            jiratags: [],
            campaignTags: '',
            scenarioIds: [],
            parameters: this.formBuilder.array([]),
            parallelRun: false,
            retryAuto: false,
            jiraId: '',
            onlyLinkedScenarios: false
        });
    }

    ngOnInit() {
        this.initMultiSelectSettings();
        this.submitted = false;
        this.loadEnvironment();
        this.loadAllScenarios();
    }

    private initMultiSelectSettings() {
        this.translate.get('campaigns.edition.selectTag').subscribe((res: string) => {
            this.settings = {
                text: res,
                enableCheckAll: false,
                autoPosition: false
            };
        });
        this.translate.get('campaigns.edition.selectJiraTag').subscribe((res: string) => {
            this.jirasettings = {
                text: res,
                enableCheckAll: false,
                autoPosition: false
            };
        });
    }

    onItemSelect(item: any) {
        this.selectedTags.push(item.itemName);
        this.selectedTags = newInstance(this.selectedTags);
    }

    OnItemDeSelect(item: any) {
        this.selectedTags.splice(this.selectedTags.indexOf(item.itemName), 1);
        this.selectedTags = newInstance(this.selectedTags);
    }

    OnItemDeSelectAll() {
        this.selectedTags = newInstance([]);
    }

    onJiraItemSelect(item: any) {
        this.jiraSelectedTags.push(item.itemName);
        this.jiraSelectedTags = newInstance(this.jiraSelectedTags);
        this.jiraFilter();
    }

    OnJiraItemDeSelect(item: any) {
        this.jiraSelectedTags.splice(this.jiraSelectedTags.indexOf(item.itemName), 1);
        this.jiraSelectedTags = newInstance(this.jiraSelectedTags);
        this.jiraFilter();
    }

    OnJiraItemDeSelectAll() {
        this.jiraSelectedTags = newInstance([]);
        this.jiraFilter();
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
        if (id != null) {
            this.campaignService.find(id).subscribe(
                (campaignFound) => {
                    this.campaign = campaignFound;
                    this.campaignForm.controls['title'].setValue(this.campaign.title);
                    this.campaignForm.controls['description'].setValue(this.campaign.description);
                    this.campaignForm.controls['parallelRun'].setValue(this.campaign.parallelRun);
                    this.campaignForm.controls['retryAuto'].setValue(this.campaign.retryAuto);
                    this.campaignForm.controls['campaignTags'].setValue(this.campaign.tags);
                    this.selectedEnvironment = this.campaign.environment;
                    this.setCampaignScenarios();
                    this.updateCampaignParameters();
                    this.datasetId = this.campaign.datasetId;
                    this.initJiraPlugin();
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

       allTagsInScenario.forEach((currentValue, index) => {
            this.itemList.push( {'id': index, 'itemName': currentValue});
        });
    }

    loadEnvironment() {
        this.environmentAdminService.listEnvironmentsNames().subscribe(
            (res) => {
                this.environments = res.sort((t1, t2) => t1.toUpperCase() > t2.toUpperCase() ? 1 : 0);
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
                this.refreshJiraScenarios();
            },
            (error) => {
                this.errorMessage = error.error;
            }
        );

    }

    initJiraPlugin() {
        this.jiraPluginConfigurationService.getUrl()
            .subscribe((r) => {
                if (r !== '') {
                    this.jiraUrl = r;
                    this.loadJiraLink();
                    this.jiraLinkService.findScenarios()
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

    getJiraLastExecutionStatus(id: string) {
        const jiraScenario = this.jiraScenarios.filter(s => s.chutneyId === id);
        if  (jiraScenario.length > 0) {
            return jiraScenario[0].executionStatus;
        } else {
            return '';
        }
    }

    getJiraLastExecutionStatusClass(id: string) {
        const status = this.getJiraLastExecutionStatus(id);
        switch (status) {
            case 'PASS' : return 'badge-success';
            case 'FAIL' : return 'badge-danger';
            default : return 'badge-secondary';
        }
    }

    hasJiraId() {
        return this.campaignForm.value['jiraId'] != null && this.campaignForm.value['jiraId'] !== '';
    }

    refreshJiraScenarios() {
        if (this.campaignForm.value['jiraId'] !== '') {
            this.jiraLinkService.findTestExecScenarios(this.campaignForm.value['jiraId'])
                .subscribe(
                    (result) => {
                        this.jiraScenarios = result;
                        let index = 0;
                        this.jiraScenarios.forEach((currentValue) => {
                            if (isNotEmpty(currentValue.executionStatus)) {
                                this.jiraItemList.push( {'id': index, 'itemName': currentValue.executionStatus});
                                index++;
                            }
                        });
                        this.jiraFilter();
                    }
                );
        } else {
            this.jiraScenarios = [];
            this.jiraScenariosToExclude = [];
            this.campaignForm.controls['onlyLinkedScenarios'].setValue(false);
        }
    }

    jiraFilter() {
        if (this.campaignForm.controls['onlyLinkedScenarios'].value === true) {
            this.jiraScenariosToExclude = this.scenarios.filter((item) => {
                let jiraTagFilter = false;
                if (this.jiraSelectedTags.length > 0) {

                    jiraTagFilter = (this.jiraScenarios.find(s => item.id === s.chutneyId &&
                                    this.jiraSelectedTags.includes(s.executionStatus))) === undefined;
                }
                return (!this.jiraScenarios.map(j => j.chutneyId).includes(item.id)) || jiraTagFilter;
            });
        } else {
            this.jiraScenariosToExclude = [];
        }
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
        this.campaign.environment = this.selectedEnvironment;
        this.campaign.parallelRun = formValue['parallelRun'];
        this.campaign.retryAuto = formValue['retryAuto'];
        this.campaign.datasetId = this.datasetId;
        const tags = formValue['campaignTags'] + '';
        this.campaign.tags = tags.length !== 0 ? tags.split(',') : [];

        this.setCampaignScenariosIdsToAdd(this.scenariosToAdd);
        if (this.campaign.id != null) {
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
                this.componentService.findComponentTestCaseExecutableParameters(scenario.id)
                    .subscribe((computedParameters: Array<KeyValue>) => {
                        computedParameters.forEach((keyValue: KeyValue) => {
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

    setSelectedEnvironment(event: string) {
        this.selectedEnvironment = event;
    }

    selectDataset(datasetId: string) {
        this.datasetId = datasetId;
    }

    private updateJiraLink(campaignId: number) {
        this.jiraId = this.campaignForm.value['jiraId'];
        this.jiraLinkService.saveForCampaign(campaignId, this.jiraId).subscribe(
            () => {},
            (error) => {
                this.errorMessage = error.error;
            });
    }
}
