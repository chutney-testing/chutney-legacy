import { Component } from '@angular/core';
import { Execution } from '@core/model';

@Component({
    selector: 'chutney-report-preview',
    templateUrl: './report-preview.component.html',
    styleUrls: ['./report-preview.component.scss']
})
export class ReportPreviewComponent {

    scenarioName: string;
    execution: Execution;
    errorMessage: string;

    preview(file: File) {
        this.execution = null;
        this.scenarioName = '';
        this.errorMessage = null;
        file.text()
            .then(data => {
                this.execution = Execution.deserialize(JSON.parse(data));
                this.scenarioName = JSON.parse(this.execution.report).scenarioName;
            })
            .catch(error => {
                this.errorMessage = error;
            });
    }
}
