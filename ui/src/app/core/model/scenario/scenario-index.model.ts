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

import { Execution } from '@core/model/scenario/execution.model';
import { TestCase } from '@core/model/scenario/test-case.model';

export class ScenarioIndex {

    public status;
    public lastExecution;

    constructor(
        public id?: string,
        public title?: string,
        public description?: string,
        public repositorySource?: string,
        public creationDate?: Date,
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public tags: Array<string> = [],
        public executions?: Array<Execution>,
        public jiraId?: string
    ) {
        this.status = this.findStatus();
        this.lastExecution = this.lastTimeExec();
    }

    private findStatus() {
        if (this.executions && this.executions.length > 0) {
            return this.executions[0].status;
        } else {
            return 'NOT_EXECUTED';
        }
    }

    private lastTimeExec() {
        if (this.executions && this.executions.length > 0) {
            return this.executions[0].time;
        } else {
            return null;
        }
    }
}
