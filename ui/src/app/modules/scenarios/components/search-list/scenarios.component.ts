import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ScenarioService } from '@core/services';
import { ScenarioIndex, ScenarioType, SelectableTags } from '@model';
import { distinct, filterOnTextContent, flatMap, intersection, sortByAndOrder } from '@shared/tools/array-utils';
import { StateService } from '@shared/state/state.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'chutney-scenarios',
    templateUrl: './scenarios.component.html',
    styleUrls: ['./scenarios.component.scss']
})
export class ScenariosComponent implements OnInit, OnDestroy {

    SCENARIO_TYPES = [ScenarioType.FORM, ScenarioType.COMPOSED];
    urlParams: Subscription;

    scenarios: Array<ScenarioIndex> = [];

    listView = false;

    // Filter
    viewedScenarios: Array<ScenarioIndex> = [];
    textFilter: any = '';
    tagFilter = new SelectableTags<String>();
    scenarioTypeFilter = new SelectableTags<ScenarioType>();

    // Order
    orderBy = 'title';
    reverseOrder = false;

    constructor(
        private router: Router,
        private scenarioService: ScenarioService,
        private stateService: StateService,
        private readonly route: ActivatedRoute,
    ) {
    }

    async ngOnInit() {
        try {
            this.scenarios = await this.getScenarios();
            this.applyDefaultState();
            this.applySavedState();
            this.applyUriState();
            this.applyFilters();
        } catch (e) {
            console.log(e);
        }
    }

    ngOnDestroy(): void {
        this.urlParams.unsubscribe();
    }

    private async getScenarios() {
        return this.scenarioService.findScenarios().toPromise();
    }

    private applyDefaultState() {
        this.viewedScenarios = this.scenarios;
        this.scenarioTypeFilter.initialize(this.SCENARIO_TYPES);
        this.tagFilter.initialize(this.findAllTags());
    }

    private findAllTags() {
        return distinct(flatMap(this.scenarios, (sc) => sc.tags)).sort();
    }

    private applySavedState() {
        this.setView();
        this.setSelectedTypes();
        this.setSelectedTags();
    }

    private setView() {
        const listView = this.stateService.getScenarioListValue();
        if (listView) {
            this.listView = listView;
        }
    }

    private setSelectedTypes() {
        const savedScenarioType = this.stateService.getScenarioType();
        if (savedScenarioType != null && savedScenarioType.length > 0) {
            this.scenarioTypeFilter.selectTags(savedScenarioType);
        }
    }

    private setSelectedTags() {
        const savedTags = this.stateService.getTags();
        if (savedTags != null) {
            this.tagFilter.selectTags(savedTags);
        }

        const noTag = this.stateService.getNoTag();
        if (noTag != null) {
            this.tagFilter.setNoTag(noTag);
        }
    }

    private applyUriState() {
        this.urlParams = this.route.queryParams.subscribe(
            (params) => {
                if (params['text']) { this.textFilter = params['text']; } else { this.textFilter = ''; }
                if (params['orderBy']) { this.orderBy = params['orderBy']; }
                if (params['reverseOrder']) { this.reverseOrder = params['reverseOrder'] === 'true'; }
                if (params['type']) { this.scenarioTypeFilter.selectTags(params['type'].split(',')); }
                if (params['noTag']) { this.tagFilter.setNoTag(params['noTag'] === 'true'); }
                if (params['tags']) { this.tagFilter.selectTags(params['tags'].split(',')); }
                this.applyFilters();
            },
            (error) => console.log(error)
        );
    }

    createNewScenario(compose: boolean) {
        if (compose) {
            this.router.navigateByUrl('/scenario/component-edition');
        } else {
            this.router.navigateByUrl('/scenario/edition');
        }
    }

    toggleListView() {
        this.listView = !this.listView;
        this.stateService.changeScenarioList(this.listView);
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
        if (property == 'title') {
            return i => i[property] == null ? '' : i[property].toLowerCase();
        }
        if (property == 'lastExecution' || property == 'creationDate') {
            const now = Date.now();
            return i => i[property] == null ? now - 1491841324 /*2017-04-10T16:22:04*/ : now - Date.parse(i[property]);
        }
        else {
            return i => i[property] == null ? '' : i[property];
        }
    }

    // Filtering //

    updateTextFilter(text: string) {
        this.textFilter = text;
        this.applyFilters();
    }

    selectAll() {
        this.tagFilter.selectAll();
        this.stateService.changeTags(this.tagFilter.selected());
        this.stateService.changeNoTag(this.tagFilter.setNoTag(true));
        this.stateService.changeScenarioType(this.scenarioTypeFilter.selected());
        this.applyFilters();
    }

    isSelectAll() {
        return this.tagFilter.isSelectAll();
    }

    deselectAll() {
        this.tagFilter.deselectAll();
        this.stateService.changeNoTag(false);
        this.applyFilters();
    }

    toggleScenarioTypeFilter(scenarioType: ScenarioType) {
        this.scenarioTypeFilter.toggleSelect(scenarioType);
        this.stateService.changeScenarioType(this.scenarioTypeFilter.selected());
        this.applyFilters();
    }

    toggleNoTagFilter() {
        this.tagFilter.toggleNoTag();
        this.stateService.changeNoTag(this.tagFilter.isNoTagSelected());
        this.applyFilters();
    }

    toggleTagFilter(tag: String) {
        this.tagFilter.toggleSelect(tag);
        this.stateService.changeTags(this.tagFilter.selected());
        this.applyFilters();
    }

    applyFilters() {
        this.viewedScenarios = filterOnTextContent(this.scenarios, this.textFilter, ['title', 'id']);
        this.viewedScenarios = this.filterOnAttributes();
        this.sortScenarios(this.orderBy, this.reverseOrder);
        this.applyFiltersToRoute();
    }

    private filterOnAttributes() {
        const input = this.viewedScenarios;
        if (this.tagFilter.isSelectAll() && this.scenarioTypeFilter.isSelectAll()) {
            return input;
        }

        const tags = this.tagFilter.selected();
        const noTag = this.tagFilter.isNoTagSelected();
        const scenarioTypes = this.scenarioTypeFilter.selected();

        return input.filter((scenario: ScenarioIndex) => {
            return (this.tagPresent(tags, scenario)
                    || this.noTagPresent(noTag, scenario))
                    && this.scenarioTypePresent(scenarioTypes, scenario);
            });
    }

    private tagPresent(tags: String[], scenario: ScenarioIndex): boolean {
        return intersection(tags, scenario.tags).length > 0;
    }

    private noTagPresent(noTag: boolean, scenario: ScenarioIndex): boolean {
        return noTag && scenario.tags.length === 0;
    }

    private scenarioTypePresent(scenarioTypes: ScenarioType[], scenario: ScenarioIndex): boolean {
        return intersection(scenarioTypes, [scenario.type]).length > 0;
    }

    private applyFiltersToRoute(): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
                text: this.textFilter,
                orderBy: this.orderBy,
                reverseOrder: this.reverseOrder,
                type: this.scenarioTypeFilter.selected().toString(),
                noTag: this.tagFilter.isNoTagSelected(),
                tags: this.tagFilter.selected().toString()
            }
        });
    }
}
