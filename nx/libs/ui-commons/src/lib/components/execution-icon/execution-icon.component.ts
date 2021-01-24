import { Component, Input, OnChanges, OnInit } from '@angular/core';

@Component({
  selector: 'chutney-execution-icon',
  templateUrl: './execution-icon.component.html',
  styleUrls: ['./execution-icon.component.scss'],
})
export class ExecutionIconComponent implements OnChanges {
  @Input() testStatus: string;

  color: string;
  icon: string;

  ngOnChanges(): void {
    switch (this.testStatus) {
      case 'SUCCESS':
        this.color = '#62B543';
        this.icon = 'done';
        break;
      case 'FAILURE':
        this.color = '#E05555';
        this.icon = 'error';
        break;
      case 'RUNNING':
        this.icon = 'spinner';
        break;
      case 'PAUSED':
        this.color = '#9AA7B0';
        this.icon = 'pause_circle_filled';
        break;
      case 'STOPPED':
        this.color = '#9AA7B0';
        this.icon = 'pause_circle_filled';
        break;
      case 'NOT_EXECUTED':
        this.color = '#9AA7B0';
        this.icon = 'adjust';
        break;
    }
  }
}
