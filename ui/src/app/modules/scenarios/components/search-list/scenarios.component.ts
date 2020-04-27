import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ScenarioService } from '@core/services';
import { SelectableTags, ScenarioIndex, ScenarioType } from '@model';
import { distinct, flatMap } from '@shared/tools/array-utils';
import { StateService } from 'src/app/shared/state/state.service';

@Component({
    selector: 'chutney-scenarios',
    templateUrl: './scenarios.component.html',
    styleUrls: ['./scenarios.component.scss']
})
export class ScenariosComponent implements OnInit {

    scenarios: Array<ScenarioIndex> = [];
    scenariosFilter: string;

    tagData = new SelectableTags<String>();
    allScenarioTypes = [ScenarioType.FORM, ScenarioType.COMPOSED];
    scenarioTypeData = new SelectableTags<ScenarioType>();

    statusCreationDateSort = false;
    statusLastExecutedSort = false;
    listView = true;

    asc = false;

    filteredScenarios: any;
    sortField: any;
    public sortReverse: any;

    constructor(
        private router: Router,
        private scenarioService: ScenarioService,
        private stateService: StateService
    ) {
    }

    ngOnInit() {
        this.initSelectedTypes();
        this.loadAll();
        this.listView = this.stateService.getScenarioListValue();
    }

    createNewScenario(compose: boolean) {
        if (compose) {
            this.router.navigateByUrl('/scenario/component-edition');
        } else {
            this.router.navigateByUrl('/scenario/edition');
        }
    }

    filterSearchChange(searchFilter: string) {
        this.scenariosFilter = searchFilter;
    }

    sortBy(property) {
        this.sortReverse = !this.sortReverse;
        this.sortScenarios(property, this.sortReverse)
    }

    sortScenarios(property, reverseOrder) {
        this.sortField = property;
        this.filteredScenarios.sort(this.dynamicSort(property, reverseOrder));
    }

    dynamicSort(property, reverseOrder) {
        let sortScenarios = 1;

        if (reverseOrder) {
            sortScenarios = -1;
        }

        return function (a, b) {
            let result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
            return result * sortScenarios;
        }
    }

    isSelectAll() {
        return this.tagData.isSelectAll();
    }

    selectAll() {
        this.tagData.selectAll();
        this.stateService.changeTags(this.tagData.selected());
        this.stateService.changeScenarioType(this.scenarioTypeData.selected());
    }

    unSelectAll() {
        this.tagData.unSelectAll();
    }

    toggleTagSelect(tag: String) {
        this.tagData.toggleSelect(tag);
        this.stateService.changeTags(this.tagData.selected());
    }

    toggleScenarioTypeSelect(scenarioType: ScenarioType) {
        this.scenarioTypeData.toggleSelect(scenarioType);
        this.stateService.changeScenarioType(this.scenarioTypeData.selected());
    }

    toggleNoTag() {
        this.tagData.toggleNoTag();
    }

    changeListView(list: boolean) {
        this.listView = list;
        this.stateService.changeScenarioList(list);
    }

    private loadAll() {
        this.scenarioService.findScenarios().subscribe(
            (res) => {
                this.scenarios = res;
                this.filteredScenarios = res;
                this.initSelectedTags();
                this.sortScenarios('title', false);
            },
            (error) => console.log(error)
        );
    }

    private initSelectedTags() {
        this.tagData.initialize(this.findAllTags());
        const savedTags = this.stateService.getTags();
        if (savedTags != null && savedTags.length > 0) {
            this.tagData.selectTags(savedTags);
        }
    }

    private findAllTags() {
        return distinct(flatMap(this.scenarios, (sc) => sc.tags)).sort();
    }

    private initSelectedTypes() {
        this.scenarioTypeData.initialize(this.allScenarioTypes);
        const savedScenarioType = this.stateService.getScenarioType();
        if (savedScenarioType != null && savedScenarioType.length > 0) {
            this.scenarioTypeData.selectTags(savedScenarioType);
        }
    }

}


