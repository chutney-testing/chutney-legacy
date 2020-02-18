import { Component } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { TestCase, FunctionalStep, Execution, TechnicalStep, Entry, Table, Row } from '@model';

@Component({
    selector: 'chutney-showroom',
    templateUrl: './showroom.component.html',
    styleUrls: ['./showroom.component.scss']
})
export class ShowroomComponent {

    scenarioSuccess: TestCase;
    scenarioFailed: TestCase;
    scenarioNotExecuted: TestCase;
    scenarioInProgress: TestCase;
    step: FunctionalStep;
    exampleParams$: BehaviorSubject<any> = new BehaviorSubject<any>({});
    technicalStep: TechnicalStep;
    properties: Array<Entry> = [];
    table: Table;
    validate: (value: string) => boolean = (_) => true;
    invalidate: (value: string) => boolean = (_) => false;


    constructor() {
        this.properties.push(new Entry('key1', 'value1'));
        this.properties.push(new Entry('key2', 'value2'));

        const rows: Array<Row> = [new Row(['value1', 'value2']), new Row(['value3', 'value4'])];
        this.table = new Table(['name1', 'name2'], rows);

        this.scenarioSuccess = new TestCase('-1', 'Sucess scenario', 'description', 'content', 'repo', new Date(), ['tag1', 'tag2']);
        this.scenarioSuccess.executions = [new Execution(1000, 'SUCCESS', 'report', -1, new Date())];
        this.scenarioFailed = new TestCase('-1', 'Failed scenario', 'description', 'content', 'repo', new Date(), ['tag1', 'tag2']);
        this.scenarioFailed.executions = [new Execution(1000, 'FAILURE', 'report', -1, new Date())];
        this.scenarioNotExecuted = new TestCase('-1', 'Never executed', 'description', 'content', 'repo', new Date(), ['tag1', 'tag2']);
        this.scenarioInProgress = new TestCase('-1', 'In progress scenario', 'description', 'content', 'repo', new Date(), ['tag1', 'tag2']);
        this.scenarioInProgress.executions = [new Execution(1000, 'in progress', 'report', -1, new Date())];

        this.step = new FunctionalStep('id', 'description', null, null, []);
        this.technicalStep = new TechnicalStep('task implemetation');
    }


}
