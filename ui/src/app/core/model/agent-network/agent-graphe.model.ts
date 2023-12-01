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

import { AgentInfo, TargetId } from '.';

export class AgentGraphe {
    constructor(
        public agents: Array<Agent>,
    ) { }
}

export class Agent {
    constructor(
        public info: AgentInfo,
        public reachableAgents: Array<string>,
        public reachableTargets: Array<TargetId>,
    ) { }

    public reachableSize() {
        return this.reachableAgents.length + this.reachableTargets.length;
    }
}
