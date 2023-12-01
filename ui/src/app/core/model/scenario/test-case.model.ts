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

import { areEquals, Equals } from '@shared/equals';
import { Clonable, cloneAsPossible } from '@shared/clonable';

import { KeyValue } from '@core/model/component-task.model';
import { Execution } from '@core/model/scenario/execution.model';

export class TestCase implements Equals<TestCase>, Clonable<TestCase> {

    constructor(
        public id?: string,
        public title?: string,
        public description?: string,
        public content?: string,
        public repositorySource?: string,
        public creationDate?: Date,
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public tags: Array<string> = [],
        public executions?: Array<Execution>,
        public computedParameters?: Array<KeyValue>,
        public defaultDataset?: string
    ) {
    }

    public equals(obj: TestCase): boolean {
        return obj
            && areEquals(this.title, obj.title)
            && areEquals(this.description, obj.description)
            && areEquals(this.content, obj.content)
            && areEquals(this.tags, obj.tags)
            && areEquals(this.computedParameters, obj.computedParameters);
    }

    public clone(): TestCase {
        return new TestCase(
            null,
            cloneAsPossible(this.title),
            cloneAsPossible(this.description),
            cloneAsPossible(this.content),
            null,
            null,
            null,
            null,
            null,
            cloneAsPossible(this.tags),
            null,
            cloneAsPossible(this.computedParameters),
            null
        );
    }

    hasParameters(): boolean {
        return (this.computedParameters && this.computedParameters.length > 0);
    }
    
    static fromRaw(raw: any): TestCase {
        return new TestCase(
            raw.id,
            raw.title,
            raw.description,
            raw.content,
            'local',
            raw.creationDate,
            raw.updateDate,
            raw.version,
            raw.author,
            raw.tags,
            raw.executions,
            raw.computedParameters,
            raw.defaultDataset
        );
    }
}
