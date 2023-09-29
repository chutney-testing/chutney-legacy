import { Injectable } from '@angular/core';
import { ChutneyState } from '@model';

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

    public changeTags(tags: Array<String>) {
        this.state.tags = tags;
        this.save();
    }

    public getTags(): Array<String> {
        return this.state.tags;
    }

    public changeCampaignTags(tags: Array<String>) {
        this.state.campaignTags = tags;
        this.save();
    }

    public getCampaignTags(): Array<String> {
        return this.state.campaignTags;
    }

    public changeNoTag(noTag) {
        this.state.noTag = noTag;
        this.save();
    }

    public getNoTag() {
        return this.state.noTag;
    }

    public changeCampaignNoTag(noTag) {
        this.state.campaignNoTag = noTag;
        this.save();
    }

    public getCampaignNoTag() {
        return this.state.campaignNoTag;
    }
}
