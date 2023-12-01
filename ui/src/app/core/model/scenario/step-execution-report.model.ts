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

export class StepExecutionReport {
    constructor(
        public duration: string,
        public status: string,
        public startDate: string,
        public information: string[],
        public errors: string[],
        public type: string,
        public strategy: string,
        public targetName: string,
        public targetUrl: string,
        public evaluatedInputs: Map<string, Object>,
        public steps: Array<StepExecutionReport>,
        public stepOutputs: Map<string, Object>,
        public name?: string,
        ) {}
}
