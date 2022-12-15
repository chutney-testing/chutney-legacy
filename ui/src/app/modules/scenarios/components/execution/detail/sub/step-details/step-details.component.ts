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

    collapseInput: boolean;
    collapseOutput: boolean;
    
    constructor() { }
    
    ngOnChanges(): void {
        this.collapseInput = true;
        this.collapseOutput = true;
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
        let size = 0;
        if (this.step.evaluatedInputs) {
            for (const key of Object.getOwnPropertyNames(this.step.evaluatedInputs)) {
                size++;
            }
        }
        return size > 0;
    }

    hasOutputs(): boolean {
        let size = 0;
        if (this.step.stepOutputs) {
            for (const key of Object.getOwnPropertyNames(this.step.stepOutputs)) {
                size++;
            }
        }
        return size > 0;
    }

    private switchCollapseInput() {
        this.collapseInput = !this.collapseInput;
    }

    private switchCollapseOutput() {
        this.collapseOutput = !this.collapseOutput;
    }
}
