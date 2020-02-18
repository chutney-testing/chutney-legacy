import { Component, OnInit } from '@angular/core';

import { BehaviorSubject } from 'rxjs/index';
import { debounceTime, distinctUntilChanged } from 'rxjs/internal/operators';

import { StepService } from '@core/services';
import { ReferentialStep, stepsFromObjects } from '@model';

@Component({
  selector: 'chutney-step',
  templateUrl: './step.component.html',
  styleUrls: ['./step.component.scss']
})
export class StepComponent implements OnInit {

  steps: Array<ReferentialStep> = [];
  private limit = 25;
  totalCount = 0;

  private stepsFilterValue: BehaviorSubject<string>;
  searchValue: string;
  usage = 'all';
  sort = 'asc';

  constructor(private stepService: StepService) { }

  ngOnInit() {
    if (this.stepsFilterValue) {
      this.stepsFilterValue.complete();
    }

    this.stepsFilterValue = new BehaviorSubject<string>(this.searchValue ? this.searchValue : '');

    this.stepsFilterValue.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(
      () => {
        this.loadSteps();
      }
    );
  }

  loadSteps(next: boolean = false) {
    const start = (next ? this.steps.length + 1 : 1);

    if (next && start > this.totalCount) {
      return;
    }
    this.stepService.findAllSteps(start, this.limit, this.searchValue, this.usage, this.sort).subscribe(
      (res: Array<Object>) => {
        if (!next) {
          this.steps.length = 0;
        }

        this.steps = this.steps.concat(stepsFromObjects(res['data']));
        this.totalCount = res['totalCount'];
      },
      (err) => console.log(err)
    );
  }

  filterSearchChange(searchValue: string) {
    this.searchValue = searchValue;
    this.stepsFilterValue.next(searchValue);
  }
}
