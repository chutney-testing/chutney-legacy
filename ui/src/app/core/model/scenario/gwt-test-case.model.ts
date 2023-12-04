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

import { addOptionalParam } from '@shared/tools/object-utils';
import { areEquals, Equals } from '@shared/equals';
import { Clonable, cloneAsPossible } from '@shared/clonable';
import { Execution } from '@core/model/scenario/execution.model';
import { Scenario } from '@core/model/scenario/scenario.model';

export class GwtTestCase implements Equals<GwtTestCase>, Clonable<GwtTestCase> {

    constructor(
        public id?: string,
        public title: string = 'Title',
        public description: string = 'Description',
        public creationDate?: Date,
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public repositorySource?: string,
        public executions: Array<Execution> = [],
        public tags?: Array<string>,
        public scenario: Scenario = new Scenario(),
        public defaultDataset?: string,
    ) {
    }

    static deserialize(jsonObject: any): GwtTestCase {
        return new GwtTestCase(
            jsonObject.id,
            jsonObject.title || 'Title',
            jsonObject.description || 'Description',
            jsonObject.creationDate,
            jsonObject.updateDate,
            jsonObject.version,
            jsonObject.author,
            jsonObject.repositorySource,
            Execution.deserializeExecutions(jsonObject.executions),
            jsonObject.tags,
            Scenario.deserialize(jsonObject.scenario),
            jsonObject.defaultDataset
        );
    }

    serialize(): any {
        const jsonObject = {};

        addOptionalParam(jsonObject, 'id', this.id);
        jsonObject['title'] = this.title;
        jsonObject['description'] = this.description;
        addOptionalParam(jsonObject, 'creationDate', this.creationDate);
        addOptionalParam(jsonObject, 'updateDate', this.updateDate);
        addOptionalParam(jsonObject, 'version', this.version);
        addOptionalParam(jsonObject, 'author', this.author);
        addOptionalParam(jsonObject, 'repositorySource', this.repositorySource);
        jsonObject['executions'] = this.executions;
        addOptionalParam(jsonObject, 'tags', this.tags);
        jsonObject['scenario'] = this.scenario.serialize();
        addOptionalParam(jsonObject, 'defaultDataset', this.defaultDataset);
        return jsonObject;
    }

    public equals(obj: GwtTestCase): boolean {
        return obj
            && areEquals(this.title, obj.title)
            && areEquals(this.description, obj.description)
            && areEquals(this.tags, obj.tags)
            && areEquals(this.scenario, obj.scenario);
    }

    public clone(): GwtTestCase {
        return new GwtTestCase(
            null,
            cloneAsPossible(this.title),
            cloneAsPossible(this.description),
            null,
            null,
            null,
            null,
            null,
            null,
            cloneAsPossible(this.tags),
            cloneAsPossible(this.scenario),
            null
        );
    }
}
