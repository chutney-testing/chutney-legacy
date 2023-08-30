import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject, Subscription } from 'rxjs';
import { debounceTime, map } from 'rxjs/operators';

import {
    distinct,
    filterOnTextContent,
    flatMap,
    intersection,
    newInstance,
    sortByAndOrder
} from '@shared/tools/array-utils';
import { StateService } from '@shared/state/state.service';
import { JiraPluginConfigurationService, JiraPluginService, ScenarioService } from '@core/services';
import { Authorization, ScenarioIndex, ScenarioType, SelectableTags, TestCase } from '@model';
import { FeatureService } from '@core/feature/feature.service';
import { FeatureName } from '@core/feature/feature.model';
import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { TranslateService } from '@ngx-translate/core';
import { AngularMultiSelect } from 'angular2-multiselect-dropdown';

@Component({
    selector: 'chutney-scenarios',
    templateUrl: './scenarios.component.html',
    styleUrls: ['./scenarios.component.scss']
})
export class ScenariosComponent implements OnInit, OnDestroy {

    SCENARIO_TYPES = [ScenarioType.FORM, ScenarioType.COMPOSED];
    componentsActive = false;
    urlParams: Subscription;

    scenarios: Array<ScenarioIndex> = [];

    // Filter
    viewedScenarios: Array<ScenarioIndex> = [];
    textFilter: string;
    fullTextFilter: string;
    scenarioTypeFilter = new SelectableTags<ScenarioType>();
    settings = {};
    tags = [];
    selectedTags = [];
    fullTextSearch = false;
    status: { id: string, itemName: string }[] = [];
    selectedStatus= [];
    // Jira
    jiraMap: Map<string, string> = new Map();
    jiraUrl = '';
    // Order
    orderBy = 'lastExecution';
    reverseOrder = false;

    private isComposed = TestCase.isComposed;
    private searchSub$ = new Subject<string>();

    Authorization = Authorization;

    constructor(
        private router: Router,
        private scenarioService: ScenarioService,
        private jiraLinkService: JiraPluginService,
        private jiraPluginConfigurationService: JiraPluginConfigurationService,
        private stateService: StateService,
        private readonly route: ActivatedRoute,
        private featureService: FeatureService,
        private translateService: TranslateService
    ) {
    }

    ngOnInit() {
        this.componentsActive = this.featureService.active(FeatureName.COMPONENT);
        this.initJiraPlugin();
        this.searchSub$.pipe(
            debounceTime(400)
            // distinctUntilChanged()
        ).subscribe((inputValue) => {
            this.scenarioService.search(this.fullTextFilter).subscribe((res) => {
                    if (this.fullTextFilter) {
                        this.localFilter(res);
                    } else {
                        this.localFilter(this.scenarios);
                    }
                }
            );
        });

        this.getScenarios()
            .then(r => {
                this.scenarios = r || [];
                this.applyDefaultState();
                this.applySavedState();
                this.applyUriState();
                this.applyFilters();
            })
            .catch(err => console.log(err));


        this.settings = {
            text: '',
            enableCheckAll: false,
            enableSearchFilter: true,
            autoPosition: false
        };
    }

    ngOnDestroy(): void {
        if (this.urlParams) {
            this.urlParams.unsubscribe();
        }
    }

    private initFilters() {
        const allTagsInScenario: string[] = this.findAllTags();
        this.tags = this.getTagsForComboModel(allTagsInScenario);
        this.status = [...new Set(this.scenarios.map(scenario => scenario.status))].map(status => this.toSelectOption(status,  this.translateService.instant(ExecutionStatus.toString(status))));
    }

    private toSelectOption(id: string, label: string = id) {
        return {id: id, itemName: label };
    }

    toggleDropDown(dropDown: AngularMultiSelect, event) {
        event.stopPropagation();
        dropDown.toggleDropdown(event);
    }

    private async getScenarios() {
        return this.scenarioService.findScenarios().toPromise();
    }

    private applyDefaultState() {
        this.viewedScenarios = this.scenarios;
        this.scenarioTypeFilter.initialize(this.SCENARIO_TYPES);
        this.initFilters();
    }

    private findAllTags() {
        return distinct(flatMap(this.scenarios, (sc) => sc.tags)).sort();
    }

    private applySavedState() {
        this.setSelectedTypes();
        this.setSelectedTags();
    }

    private setSelectedTags() {
        const savedTags = this.stateService.getTags();
        if (savedTags != null) {
            this.selectedTags = this.getTagsForComboModel(savedTags);
        }
    }
    private setSelectedTypes() {
        const savedScenarioType = this.stateService.getScenarioType();
        if (savedScenarioType != null && savedScenarioType.length > 0) {
            this.scenarioTypeFilter.selectTags(savedScenarioType);
        }
    }

    private applyUriState() {
        this.urlParams = this.route.queryParams
            .pipe(map((params: Array<any>) => {
                    if (params['text']) {
                        this.textFilter = params['text'];
                    } else {
                        this.textFilter = '';
                    }
                    if (params['orderBy']) {
                        this.orderBy = params['orderBy'];
                    }
                    if (params['status']) {
                        this.selectedStatus = this.status.filter((status) => params['status'].split(',').includes(status.itemName));
                    }
                    if (params['reverseOrder']) {
                        this.reverseOrder = params['reverseOrder'] === 'true';
                    }
                    if (params['type']) {
                        this.scenarioTypeFilter.selectTags(params['type'].split(','));
                    }
                    if (params['tags']) {
                        const uriTag = params['tags'].split(',');
                        if (uriTag != null) {
                            this.selectedTags = this.getTagsForComboModel(uriTag);
                        }
                    }
                    this.applyFilters();
                    this.ngOnDestroy();
                },
                (error) => console.log(error)))
            .subscribe();

    }

    createNewScenario(compose: boolean) {
        if (compose) {
            this.router.navigateByUrl('/scenario/component-edition');
        } else {
            this.router.navigateByUrl('/scenario/raw-edition');
        }
    }

    // Ordering //
    sortBy(property) {
        if (this.orderBy === property) {
            this.reverseOrder = !this.reverseOrder;
        }

        this.orderBy = property;
        this.applyFilters();
    }

    sortScenarios(property, reverseOrder) {
        this.viewedScenarios = sortByAndOrder(
            this.viewedScenarios,
            this.getKeyExtractorBy(property),
            reverseOrder
        );
    }

    private getKeyExtractorBy(property: string) {
        if (property === 'title') {
            return i => i[property] == null ? '' : i[property].toLowerCase();
        }
        if (property === 'lastExecution' || property === 'creationDate' || property === 'updateDate') {
            const now = Date.now();
            return i => i[property] == null ? now - 1491841324 /*2017-04-10T16:22:04*/ : now - Date.parse(i[property]);
        } else {
            return i => i[property] == null ? '' : i[property];
        }
    }

    // Filtering //

    updateTextFilter(text: string) {
        this.textFilter = text;
        this.applyFilters();
    }

    updateFullTextFilter(text: string) {
        this.fullTextFilter = text;
        this.applyFilters();
    }

    toggleScenarioTypeFilter(scenarioType: ScenarioType) {
        this.scenarioTypeFilter.toggleSelect(scenarioType);
        this.stateService.changeScenarioType(this.scenarioTypeFilter.selected());
        this.applyFilters();
    }

    applyFilters() {
        if (this.fullTextFilter) {
            this.searchSub$.next(this.fullTextFilter);
        } else {
            this.localFilter(this.scenarios);
        }
    }

    private localFilter(scenarios: Array<ScenarioIndex>) {
        const scenariosWithJiraId = scenarios.map(sce => {
            const jiraId = this.jiraMap.get(sce.id);
            if (jiraId) {
                sce.jiraId = jiraId;
            }
            return sce;
        });
        this.viewedScenarios = filterOnTextContent(scenariosWithJiraId, this.textFilter, ['title', 'id', 'jiraId', 'tags']);
        this.viewedScenarios = this.filterOnAttributes();
        this.sortScenarios(this.orderBy, this.reverseOrder);
        this.applyFiltersToRoute();
    }

    // Jira link //
    initJiraPlugin() {
        this.jiraPluginConfigurationService.getUrl()
            .subscribe((url) => {
                if (url !== '') {
                    this.jiraUrl = url;
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

    private filterOnAttributes() {
        const input = this.viewedScenarios;

        const tags = this.getSelectedTags();
        const scenarioTypes = this.scenarioTypeFilter.selected();

        return input.filter((scenario: ScenarioIndex) => {
            return this.tagPresent(tags, scenario) && this.scenarioTypePresent(scenarioTypes, scenario) && this.scenarioStatusPresent(this.selectedStatus, scenario);
        });
    }

    private scenarioTypePresent(scenarioTypes: ScenarioType[], scenario: ScenarioIndex): boolean {
        return intersection(scenarioTypes, [scenario.type]).length > 0;
    }

    private scenarioStatusPresent(statusFilter: any[], scenario: ScenarioIndex): boolean {
        if (statusFilter.length > 0) {
            return intersection(statusFilter.map((status)=>status.id), [scenario.status]).length > 0;
        } else {
            return true;
        }
    }

    private applyFiltersToRoute(): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
                text: this.textFilter,
                orderBy: this.orderBy,
                status:this.selectedStatus.map((status) => status.itemName).join(','),
                reverseOrder: this.reverseOrder,
                type: this.scenarioTypeFilter.selected().toString(),
                tags: this.getSelectedTags().toString()
            }
        });
    }

    private tagPresent(tags: String[], scenario: ScenarioIndex): boolean {
        if(tags.length > 0) {
            return intersection(tags, scenario.tags).length > 0;
        } else {
            return true;
        }
    }

    onItemSelect() {
        this.stateService.changeTags(this.getSelectedTags());
        this.applyFilters();
    }

    OnItemDeSelect() {
        this.stateService.changeTags(this.getSelectedTags());
        this.applyFilters();
    }

    OnItemDeSelectAll() {
        this.selectedTags = newInstance([]);
        this.stateService.changeTags(this.getSelectedTags());
        this.applyFilters();
    }

    private getSelectedTags() {
        return this.selectedTags.map((i) => i.itemName);
    }

    private getTagsForComboModel(tags: String[]) {
        let index = 0;
        return tags.map((t) => {
            index++;
            return {'id': index, 'itemName': t};
        });
    }
}
