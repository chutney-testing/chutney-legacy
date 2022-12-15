import { Component, Input, OnInit, OnDestroy, Output, EventEmitter } from '@angular/core';
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
    @Input() executionId: number;

    stepsCollapsed = true;
    highlighted = false;

    private expandAllSubscription: Subscription;
    private highlightSubscription: Subscription;

    constructor(private eventManager: EventManagerService) { }

    ngOnInit() {
        this.expandAllSubscription = this.eventManager.subscribe('toggleScenarioStep_' + this.executionId, (data) => {
            this.stepsCollapsed = data.expand;
        });
        this.stepsCollapsed = ('PAUSED' !== this.step.status && 'RUNNING' !== this.step.status && 'FAILURE' !== this.step.status);
        this.highlightSubscription = this.eventManager.subscribe('highlightEvent_' + this.executionId, (data) => {
            this.highlighted = data.stepId === this.id;
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.expandAllSubscription);
        this.eventManager.destroy(this.highlightSubscription);
    }

    collapse(event: MouseEvent) {
        this.stepsCollapsed = !this.stepsCollapsed;
        this.eventManager.broadcast({name: 'selectStepEvent_' + this.executionId , step: this.step});
        this.eventManager.broadcast({name: 'highlightEvent_' + this.executionId, stepId: this.id});
        event.stopPropagation();
    }
}
