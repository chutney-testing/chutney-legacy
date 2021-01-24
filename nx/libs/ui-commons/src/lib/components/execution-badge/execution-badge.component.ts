import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewEncapsulation,
} from '@angular/core';

const statusClass = {
  SUCCESS: 'mat-chip-success',
  FAILURE: 'mat-chip-error',
  RUNNING: 'mat-chip-processing',
  NOT_EXECUTED: 'mat-chip-warning',
  STOPPED: 'mat-chip-warning',
  PAUSED: 'mat-chip-warning',
};

const statusLabel = {
  SUCCESS: 'Passed',
  FAILURE: 'Failed',
  RUNNING: 'In progress',
  NOT_EXECUTED: 'Not Run',
  STOPPED: 'Stopped',
  PAUSED: 'Paused',
};

//SUCCESS, WARN, FAILURE, NOT_EXECUTED, STOPPED, PAUSED, RUNNING, EXECUTED

@Component({
  selector: 'chutney-execution-badge',
  templateUrl: './execution-badge.component.html',
  styleUrls: ['./execution-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ExecutionBadgeComponent implements OnInit, OnChanges {
  @Input() status: string;

  class: string | null;
  label: string | null;

  ngOnInit(): void {
    this.updateClassMap();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updateClassMap();
  }

  private updateClassMap() {
    this.class = statusClass[this.status];
    this.label = statusLabel[this.status];
  }
}
