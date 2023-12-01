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

export class JiraScenario {
    constructor(
        public id: string,
        public chutneyId: string,
        public executionStatus?: string) {
    }
}

export class JiraTestExecutionScenarios {
    constructor(
        public id: string,
        public jiraScenarios: JiraScenario[]) {
    }
}

export enum XrayStatus {
    PASS = 'PASS',
    FAIL = 'FAIL'
}
