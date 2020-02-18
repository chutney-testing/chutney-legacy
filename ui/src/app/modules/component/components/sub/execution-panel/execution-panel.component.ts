import { Component, EventEmitter, Input, Output, OnChanges} from '@angular/core';

@Component({
    selector: 'chutney-execution-panel-component',
    templateUrl: './execution-panel.component.html',
    styleUrls: ['./execution-panel.component.scss']
})
export class ExecutionPanelComponent implements OnChanges {


    @Input() executionResult: any;
    @Output() closeEvent = new EventEmitter();

    info: Object[];
    errors: Object[];

    constructor(
    ) {
    }

    ngOnChanges()  {

        this.info = this.getInfos();
        this.errors = this.getErrors();
    }

    close() {
        this.info = [];
        this.errors = [];
        this.closeEvent.emit();
    }

    getErrors() {
        const result = [];
        if (this.executionResult.report) {
            this.searchErrors(this.executionResult.report, result);
        }
        return result;
    }

    getInfos() {
        const result = [];
        if (this.executionResult.report) {
            this.searchInfo(this.executionResult.report, result);
        }
        return result;
    }

    private searchInfo(report: Object, result: Array<Object>) {
        if ((report['steps'] as Array<Object>).length > 0) {
            (report['steps'] as Array<Object>).forEach(s => this.searchInfo(s, result));
            (report['information'] as Array<Object>).forEach(s => result.push(s));
        } else {
            (report['information'] as Array<Object>).forEach(s => result.push(s));
        }
    }

    private searchErrors(report: Object, result: Array<Object>) {
        if ((report['steps'] as Array<Object>).length > 0) {
            (report['steps'] as Array<Object>).forEach(s => this.searchErrors(s, result));
            (report['errors'] as Array<Object>).forEach(s => result.push(s));
        } else {
            (report['errors'] as Array<Object>).forEach(s => result.push(s));
        }
    }
}
