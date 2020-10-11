import { Component, OnInit } from '@angular/core';
import { Scenario, ScenariosGQL } from '@chutney/data-access';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Component({
  selector: 'chutney-testing-scenarios',
  templateUrl: './scenarios.component.html',
  styleUrls: ['./scenarios.component.scss'],
})
export class ScenariosComponent implements OnInit {
  scenarios$: Observable<any[]>;

  constructor(private scenariosGQL: ScenariosGQL) {}

  ngOnInit(): void {
    this.scenarios$ = this.scenariosGQL
      .watch()
      .valueChanges.pipe(pluck('data', 'scenarios'));
  }
}
