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
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public tags: Array<string> = [],
        public executions?: Array<Execution>,
        public jiraId?: string
    ) {
        this.status = this.findStatus();
        this.lastExecution = this.lastTimeExec();
        this.executionCount = this.countExecutions();
        this.isComposed = this.findIfComposed();
        this.type = this.setType();
    }

    private findStatus() {
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

    private countExecutions() {
        if (this.executions && this.executions.length > 0) {
            return this.executions.length;
        } else {
            return 0;
        }
    }

    private findIfComposed(): boolean {
        return TestCase.isComposed(this.id);
    }

    private setType(): ScenarioType {
        return this.isComposed ? ScenarioType.COMPOSED : ScenarioType.FORM;
    }
}

export enum ScenarioType {
    FORM = 'FORM',
    COMPOSED = 'COMPOSED'
}
