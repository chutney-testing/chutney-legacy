import { Component, Input, OnChanges } from '@angular/core';


@Component({
  selector: 'chutney-execution-badge',
  templateUrl: './execution-badge.component.html',
  styleUrls: ['./execution-badge.component.scss']
})
export class ExecutionBadgeComponent implements OnChanges {

  @Input() status: String;
  @Input() spin: boolean = false;

  status_h: String;
  constructor() { }

  ngOnChanges(): void {

    switch (this.status) {
      case 'SUCCESS':
        this.status_h = 'OK';
        break;
      case 'FAILURE':
        this.status_h = 'KO';
        break;
      case 'RUNNING':
        this.status_h = 'RUNNING';
        break;
      case 'PAUSED':
        this.status_h = 'PAUSE';
        break;
      case 'STOPPED':
        this.status_h = 'STOP';
        break;
      case 'NOT_EXECUTED':
        this.status_h = '';
        break;
    }
  }

}
