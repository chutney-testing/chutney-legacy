import { Component, Input, OnChanges } from '@angular/core';
import { StepExecutionReport } from '@model';

@Component({
    selector: 'chutney-scenario-step-details',
    templateUrl: './step-details.component.html',
    styleUrls: ['./step-details.component.scss']
})
export class StepDetailsComponent implements OnChanges {
    @Input() executionId: number;
    @Input() step: StepExecutionReport;
    @Input() showDetails: boolean;

    collapseInput: boolean;
    collapseOutput: boolean;
    
    constructor() { }
    
    ngOnChanges(): void {
        this.collapseInput = !this.showDetails;
        this.collapseOutput = !this.showDetails;
    }

    getInformation(): string[] {
        if (this.step != null && this.step.information != null) {
            return this.step.information;
        } else {
            return [];
        }
    }

    getErrors(): string[] {
        if (this.step != null && this.step.errors != null) {
            return this.step.errors;
        } else {
            return [];
        }
    }

    hasInputs(): boolean {
        return this.step.evaluatedInputs && !!Object.getOwnPropertyNames(this.step.evaluatedInputs).length;
    }

    hasOutputs(): boolean {
        return this.step.stepOutputs && !!Object.getOwnPropertyNames(this.step.stepOutputs).length;
    }

    private switchCollapseInput() {
        this.collapseInput = !this.collapseInput;
    }

    private switchCollapseOutput() {
        this.collapseOutput = !this.collapseOutput;
    }
}
