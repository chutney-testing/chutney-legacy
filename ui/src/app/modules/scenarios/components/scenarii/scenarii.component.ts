import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ScenarioService } from '@core/services';
import { SelectableTags, ScenarioIndex, ScenarioType } from '@model';
import { distinct, flatMap } from '@shared/tools/array-utils';
import { StateService } from 'src/app/shared/state/state.service';

@Component({
    selector: 'chutney-scenarii',
    templateUrl: './scenarii.component.html',
    styleUrls: ['./scenarii.component.scss']
})
export class ScenariiComponent implements OnInit {

    scenarii: Array<ScenarioIndex> = [];
    scenariiFilter: string;

    tagData = new SelectableTags<String>();
    allScenarioTypes = [ScenarioType.FORM, ScenarioType.COMPOSED];
    scenarioTypeData = new SelectableTags<ScenarioType>();

    sortField = 'title';
    statusCreationDateSort = false;
    statusLastExecutedSort = false;
    asc = false;
    listView = true;

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
        this.scenariiFilter = searchFilter;
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
                this.scenarii = res;
                this.initSelectedTags();
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
        return distinct(flatMap(this.scenarii, (sc) => sc.tags)).sort();
    }

    private initSelectedTypes() {
        this.scenarioTypeData.initialize(this.allScenarioTypes);
        const savedScenarioType = this.stateService.getScenarioType();
        if (savedScenarioType != null && savedScenarioType.length > 0) {
            this.scenarioTypeData.selectTags(savedScenarioType);
        }
    }

    public orderByCreationDate() {
        this.statusCreationDateSort = !this.statusCreationDateSort;
        this.sortField = this.statusCreationDateSort ? 'creationDate' : 'title';
        this.asc = this.statusCreationDateSort;
        if (this.statusCreationDateSort) {
            this.statusLastExecutedSort = false;
        }
    }

    public orderByLastExecuted() {
        this.statusLastExecutedSort = !this.statusLastExecutedSort;
        this.sortField = this.statusLastExecutedSort ? 'executions.0.time' : 'title';
        this.asc = this.statusLastExecutedSort;
        if (this.statusLastExecutedSort) {
            this.statusCreationDateSort = false;
        }
    }
}


