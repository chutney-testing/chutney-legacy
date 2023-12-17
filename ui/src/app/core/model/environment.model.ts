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

import { Entry } from './entry.model';

export class Environment {
    constructor(
        public name: string,
        public description: string,
        public targets: Target [] = [],
        public variables: EnvironmentVariable[] = []) {
    }
}

export class Target {
    constructor(
        public name: string,
        public url: string,
        public properties: Entry [] = [],
        public environment: string = null,
    ) {
    }
}

export class EnvironmentVariable {
    constructor(
        public key: string,
        public value: string,
        public env: string = null
    ) {
    }
}

export class TargetFilter {
    constructor(
        public name: string = null,
        public environment: string = null,
    ) {
    }
}
