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

import { CampaignExecutionReport } from '.';

export class Campaign {

    constructor(public id?: number,
        public title: string = '',
        public description: string = '',
        public scenarioIds: Array<string> = [],
        public campaignExecutionReports: Array<CampaignExecutionReport> = [],
        public computedParameters: Map<string, string> = new Map(),
        public environment: string = '',
        public parallelRun?: false,
        public retryAuto?: false,
        public datasetId?: string,
        public tags: Array<string> = []) {
    }
}
