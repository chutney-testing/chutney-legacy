import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChange,
    SimpleChanges,
    OnInit,
    OnDestroy
} from '@angular/core';

import { Execution } from '@model';
import { interval, Subscription } from 'rxjs/index';
import { ScenarioExecutionService } from '@core/services';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'chutney-execution-history',
    templateUrl: './history.component.html',
    styleUrls: ['./history.component.scss']
})
export class HistoryComponent implements OnInit, OnDestroy, OnChanges {

    @Input() scenarioId: string;
    @Input() selectedExecutionId: number;
    @Input() checkForEndExecutions = 5000;

    @Output() onselectExecution: EventEmitter<Execution> = new EventEmitter<Execution>();
    @Output() onlastIdExecution: EventEmitter<Execution> = new EventEmitter<Execution>();
    @Output() onlastStatusExecution: EventEmitter<String> = new EventEmitter<String>();

    executions: Execution[] = [];
    selectedLast = true;

    private _checkRunningExecutionsSubscription: Subscription;

    constructor(
        private scenarioExecutionService: ScenarioExecutionService,
        private route: ActivatedRoute,
    ) { }

    ngOnChanges(simpleChanges: SimpleChanges) {
        this.findScenarioExecutions();
    }

    ngOnInit() {
        this.route.params.subscribe((params) => {
            if (params['execId'] && params['execId'] !== 'last') {
                this.selectedExecutionId = params['execId'];
                this.selectedLast = false;
            } else {
                this.selectedLast = true;
            }
            this.findScenarioExecutions();
        });
    }

    ngOnDestroy() {
        this.checkRunningExecutionsSubscription();
    }

    selectExecution(execution: Execution) {
        this.selectedExecutionId = execution.executionId;
        this.onselectExecution.emit(execution);
    }

    findScenarioExecutions() {
        this.scenarioExecutionService.findScenarioExecutions(this.scenarioId)
            .subscribe((executions) => {
                if (executions.length > 0) {
                    // Output last execution id if necessary
                    if (this.selectedLast) {
                        this.selectedExecutionId = executions[0].executionId;
                        this.onlastIdExecution.emit(executions[0]);
                    }
                    // Update executions
                    if (this.executions.length === 0) {
                        this.executions = executions;
                    } else {
                        this.updateExecutions(executions);
                    }
                    // Check if executions are running in order to auto reload
                    this.checkRunningExecutions();

                    this.onlastStatusExecution.emit(executions[0].status);
                } else {
                    this.onlastIdExecution.emit(Execution.NO_EXECUTION);
                }
            });
    }

    isOlderThan(date: Date, durationInHours: number) {
        return new Date().getTime() - date.getTime() > durationInHours * 60 * 60 * 1000;
    }

    private checkRunningExecutions() {
        if (this.executions.filter(execution => 'RUNNING' === execution.status || 'PAUSED' === execution.status).length > 0) {
            this.checkRunningExecutionsSubscription();
            this._checkRunningExecutionsSubscription = interval(this.checkForEndExecutions).subscribe(
                (n) => this.findScenarioExecutions(),
                (err) => console.log('Error checking running executions : ' + err),
                () => this.checkRunningExecutionsSubscription()
            );
        } else {
            this.checkRunningExecutionsSubscription();
        }
    }

    private updateExecutions(newExecutions: Execution[]) {
        for (let i = 0; i < this.executions.length; i++) {
            if (this.executions[i].executionId === newExecutions[i].executionId) {
                if (this.executions[i].status !== newExecutions[i].status) {
                    this.executions[i] = newExecutions[i];
                } else {
                    break;
                }
            } else {
                this.executions.splice(i, 0, newExecutions[i]);
            }
        }
    }

    private checkRunningExecutionsSubscription() {
        if (this._checkRunningExecutionsSubscription) {
            this._checkRunningExecutionsSubscription.unsubscribe();
        }
    }
}
