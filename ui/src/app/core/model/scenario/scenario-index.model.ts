import { Execution } from '@core/model/scenario/execution.model';
import { TestCase } from '@core/model/scenario/test-case.model';

export class ScenarioIndex {

    constructor(
        public id?: string,
        public title?: string,
        public description?: string,
        public repositorySource?: string,
        public creationDate?: Date,
        public tags: Array<string> = [],
        public executions?: Array<Execution>
    ) {
    }

    public status() {
        if (this.executions && this.executions.length > 0) {
            return this.executions[0].status;
        } else {
            return 'NOT_EXECUTED';
        }
    }

    public lastTimeExec() {
        if (this.executions && this.executions.length > 0) {
            return this.executions[0].time;
        } else {
            return null;
        }
    }

    public numberOfExecution() {
        if (this.executions && this.executions.length > 0) {
            return this.executions.length;
        } else {
            return 0;
        }
    }

    public isComposed(): boolean {
        return TestCase.isComposed(this.id);
    }

    public getType(): ScenarioType {
        return this.isComposed() ? ScenarioType.COMPOSED : ScenarioType.FORM;
    }
}

export enum ScenarioType {
    FORM = 'FORM',
    COMPOSED = 'COMPOSED'
}
