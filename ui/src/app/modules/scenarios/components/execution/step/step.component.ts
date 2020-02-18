import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Step } from '@model';
import { Subscription } from 'rxjs';
import { EventManagerService } from '@shared';

@Component({
    selector: 'chutney-scenario-step',
    templateUrl: './step.component.html',
    styleUrls: ['./step.component.scss']
})
export class StepComponent implements OnInit, OnDestroy {
    @Input() step: Step;
    @Input() id: number;

    inputCollapsed = true;

    private expandAllSubscription: Subscription;

    constructor(private eventManager: EventManagerService) { }

    ngOnInit() {
        this.expandAllSubscription = this.eventManager.subscribe('toggleScenarioDetails', (data) => {
            this.inputCollapsed = data.expand;
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.expandAllSubscription);
    }

}
