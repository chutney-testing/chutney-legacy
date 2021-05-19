import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { StepExecutionReport } from '@model';
import { Subscription } from 'rxjs';
import { EventManagerService } from '@shared';

@Component({
    selector: 'chutney-scenario-step-report',
    templateUrl: './step-report.component.html',
    styleUrls: ['./step-report.component.scss']
})
export class StepReportComponent implements OnInit, OnDestroy {
    @Input() step: StepExecutionReport;
    @Input() id: number;

    stepsCollapsed = true;
    informationCollapsed = true;
    errorsCollapsed = true;
    inputCollapsed = true;

    private expandAllSubscription: Subscription;

    constructor(private eventManager: EventManagerService) { }

    ngOnInit() {
        this.expandAllSubscription = this.eventManager.subscribe('toggleScenarioDetails', (data) => {
            this.inputCollapsed = data.expand;
        });
        this.expandAllSubscription = this.eventManager.subscribe('toggleScenarioInfo', (data) => {
            this.informationCollapsed = data.expand;
        });
        this.stepsCollapsed = ('PAUSED' !== this.step.status && 'RUNNING' !== this.step.status && 'FAILURE' !== this.step.status);
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.expandAllSubscription);
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

    showInformation(event: MouseEvent) {
        this.informationCollapsed = !this.informationCollapsed;
        event.stopPropagation();
    }

    showDetails(event: MouseEvent) {
        this.inputCollapsed = !this.inputCollapsed;
        event.stopPropagation();
    }

    showErrors(event: MouseEvent) {
        this.errorsCollapsed = !this.errorsCollapsed;
        event.stopPropagation();
    }

    collapse(event: MouseEvent) {
        this.stepsCollapsed = !this.stepsCollapsed;
        this.informationCollapsed = this.stepsCollapsed;
        this.inputCollapsed = this.stepsCollapsed;
        this.errorsCollapsed = this.stepsCollapsed;
        event.stopPropagation();
    }
}
