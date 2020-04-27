import { Component, OnInit, Input } from '@angular/core';
import { ScenarioIndex } from '@model';

@Component({
  selector: 'chutney-scenario-card',
  templateUrl: './scenario-card.component.html',
  styleUrls: ['./scenario-card.component.scss']
})
export class ScenarioCardComponent implements OnInit {

  @Input() scenario: ScenarioIndex;

  lastExecutionStatus: string;
  lastExecutionDate: Date;
  numberOfExecution: number;

  constructor() { }

  ngOnInit() {
    if (this.scenario) {
      if (this.scenario.executions && this.scenario.executions.length > 0) {
        this.lastExecutionStatus = this.scenario.executions[0].status;
        this.lastExecutionDate = this.scenario.executions[0].time;
        this.numberOfExecution = this.scenario.executions.length;
      } else {
        this.numberOfExecution = 0;
        this.lastExecutionStatus = 'NOT_EXECUTED';
      }
    }
  }
}
