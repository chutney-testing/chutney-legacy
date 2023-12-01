/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
