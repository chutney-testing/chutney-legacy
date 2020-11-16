import { Component, Input, OnChanges, OnInit } from '@angular/core';

@Component({
  selector: 'chutney-execution-icon',
  templateUrl: './execution-icon.component.html',
  styleUrls: ['./execution-icon.component.scss'],
})
export class ExecutionIconComponent implements OnChanges {
  @Input() testStatus: String;

  color: String;
  icon: String;
  constructor() {}

  /*  <!--testPassed-->
  <mat-icon class="icon-18" [ngStyle]="{'color':'#62B543'}">done</mat-icon>
      <!--testError-->
      <mat-icon class="icon-18" [ngStyle]="{'color':'#E05555'}">error</mat-icon>
      <!--testFailed-->
      <mat-icon class="icon-18" [ngStyle]="{'color':'#F4AF3D'}">highlight_off</mat-icon>
      <!--testIgnored-->
      <mat-icon class="icon-18" [ngStyle]="{'color':'#9AA7B0'}">not_interested</mat-icon>
      <!--testNotRan-->
      <mat-icon class="icon-18" [ngStyle]="{'color':'#9AA7B0'}">adjust</mat-icon>
      <!--testPaused-->
      <mat-icon class="icon-18" [ngStyle]="{'color':'#9AA7B0'}">pause_circle_filled</mat-icon>*/

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
