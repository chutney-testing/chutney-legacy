import { Execution } from '@core/model/scenario/execution.model';
import { TestCase } from '@core/model/scenario/test-case.model';

export class ScenarioIndex {

    public status;
    public lastExecution;
    public executionCount;
    public isComposed;
    public type;

    constructor(
        public id?: string,
        public title?: string,
        public description?: string,
        public repositorySource?: string,
        public creationDate?: Date,
        public tags: Array<string> = [],
        public executions?: Array<Execution>
    ) {
        this.status = this.getStatus();
        this.lastExecution = this.lastTimeExec();
        this.executionCount = this.numberOfExecution();
        this.isComposed = this.getIfComposed();
        this.type = this.getType();
    }

    private getStatus() {
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

    private numberOfExecution() {
        if (this.executions && this.executions.length > 0) {
            return this.executions.length;
        } else {
            return 0;
        }
    }

    private getIfComposed(): boolean {
        return TestCase.isComposed(this.id);
    }

    private getType(): ScenarioType {
        return this.isComposed ? ScenarioType.COMPOSED : ScenarioType.FORM;
    }
}

export enum ScenarioType {
    FORM = 'FORM',
    COMPOSED = 'COMPOSED'
}
