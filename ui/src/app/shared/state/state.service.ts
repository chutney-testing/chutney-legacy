import { Injectable } from '@angular/core';
import { ChutneyState, ScenarioType } from '@model';

@Injectable()
export class StateService {

    private static localStorageKey = 'CHUTNEY_STATE';

    state: ChutneyState;

    constructor() {
        const stateString = localStorage.getItem(StateService.localStorageKey) || '{}';
        this.state = JSON.parse(stateString);
    }

    private save() {
        localStorage.setItem(StateService.localStorageKey, JSON.stringify(this.state));
    }

    public changeScenarioList(list: boolean) {
        this.state.scenarioList = list;
        this.save();
    }

    public getScenarioListValue(): boolean {
        return this.state.scenarioList;
    }

    public changeTags(tags: Array<String>) {
        this.state.tags = tags;
        this.save();
    }

    public getTags(): Array<String> {
        return this.state.tags;
    }

    public getScenarioType() {
        return this.state.scenarioTypes;
    }

    public changeScenarioType(scenarioTypes: Array<ScenarioType>)  {
        this.state.scenarioTypes = scenarioTypes;
        this.save();
    }

    public changeNoTag(noTag) {
        this.state.noTag = noTag;
        this.save();
    }

    public getNoTag() {
        return this.state.noTag;
    }
}
