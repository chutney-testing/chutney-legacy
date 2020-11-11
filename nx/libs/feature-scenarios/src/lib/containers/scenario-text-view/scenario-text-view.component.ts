import { Component, OnInit } from '@angular/core';
import { map, pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { RunScenarioGQL, Scenario, ScenarioGQL } from '@chutney/data-access';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import Hjson from 'hjson';

@Component({
  selector: 'chutney-scenario-text-view',
  templateUrl: './scenario-text-view.component.html',
  styleUrls: ['./scenario-text-view.component.scss'],
})
export class ScenarioTextViewComponent implements OnInit {
  private scenarioId: string;
  scenario$: Observable<Scenario>;
  treeControl = new NestedTreeControl<any>((node) => node.subSteps);
  dataSource = new MatTreeNestedDataSource<any>();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private scenarioGQL: ScenarioGQL,
    private runScenarioGQL: RunScenarioGQL
  ) {}

  ngOnInit(): void {
    this.scenarioId = this.route.snapshot.paramMap.get('id');
    this.scenario$ = this.scenarioGQL
      .watch({ scenarioId: this.scenarioId })
      .valueChanges.pipe(pluck('data', 'scenario'));
    this.scenario$
      .pipe(
        map((value) => {
          return Hjson.parse(value.content);
        })
      )
      .subscribe(
        (scenario: any) =>
          (this.dataSource.data = this.normalizeScenario(scenario))
      );
  }

  normalizeScenario(scenario: any): any[] {
    return [
      ...this.normalize(scenario.givens, 'Given'),
      ...this.normalize([scenario.when], 'When'),
      ...this.normalize(scenario.thens, 'Then'),
    ];
  }

  normalize(steps: any[], keyword: string): any[] {
    return steps.map((x) => Object.assign({}, x, { keyword: keyword }));
  }

  runScenario(data: any) {
    this.runScenarioGQL
      .mutate({ scenarioId: data.id, dataset: [] })
      .subscribe((result) =>
        this.router.navigate([`../run/${result.data.runScenario}`], {
          relativeTo: this.route,
        })
      );
  }

  hasChild = (_: number, node: any) =>
    !!node.subSteps && node.subSteps.length > 0;
}
