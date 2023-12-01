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

import { StepExecutionReport } from '@core/model/scenario/step-execution-report.model';
import { ExecutionStatus } from './execution-status';

export class ScenarioExecutionReport {
    constructor(
        public executionId: string,
        public status: ExecutionStatus,
        public duration: number,
        public startDate: Date,
        public report: StepExecutionReport,
        public environment: string,
        public user: string,
        public scenarioName?: string,
        public error?: string,
        public contextVariables?: Map<string, Object>
    ) { }
}
