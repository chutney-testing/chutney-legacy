import { areEquals, Equals } from '@shared/equals';
import { Clonable, cloneAsPossible } from '@shared/clonable';

import { KeyValue } from '@core/model/component-task.model';
import { Execution } from '@core/model/scenario/execution.model';
import { ScenarioComponent } from '@core/model/scenario/scenario-component.model';

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

    static isComposed(testCaseOrId: TestCase | string): boolean {
        let id: string;
        if (testCaseOrId instanceof TestCase) {
            id = testCaseOrId.id;
        } else {
            id = testCaseOrId;
        }
        return (id && id.indexOf('-') > 0);
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

    static fromComponent(testCase: ScenarioComponent) {
        return new TestCase(
            testCase.id,
            testCase.title,
            testCase.description,
            '',
            'local',
            testCase.creationDate,
            testCase.updateDate,
            testCase.version,
            testCase.author,
            [],
            null,
            testCase.computedParameters,
            ''
        );
    }
}
