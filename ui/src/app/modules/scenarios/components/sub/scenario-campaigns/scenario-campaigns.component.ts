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

import { Component, Input, OnChanges } from '@angular/core';

import { CampaignService } from '@core/services';
import { Campaign, Authorization } from '@model';

@Component({
    selector: 'chutney-scenario-campaigns',
    templateUrl: './scenario-campaigns.component.html',
    styleUrls: ['./scenario-campaigns.component.scss']
})
export class ScenarioCampaignsComponent implements OnChanges {

    @Input() idScenario: string;
    campaignsForScenario: Array<Campaign> = [];

    Authorization = Authorization;

    constructor(private campaignService: CampaignService) {
    }

    ngOnChanges() {
        if (this.idScenario) {
            this.load(this.idScenario);
        }
    }

    load(id) {
        this.campaignService.findAllCampaignsForScenario(id).subscribe(
            (response) => {
                this.campaignsForScenario = response;
            },
            (error) => {
                console.log(error);
            }
        );
    }
}
