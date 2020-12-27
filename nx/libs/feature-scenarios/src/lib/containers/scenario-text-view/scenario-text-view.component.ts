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
  scenarioId: string;
  scenario$: Observable<Scenario>;
  treeControl = new NestedTreeControl<any>((node) => node.subSteps);
  dataSource = new MatTreeNestedDataSource<any>();
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Scenarios', link: ['/'] },
  ];
  environments: string[] = ['GLOBAL', 'PERF'];

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
    return steps.map((x, index) =>
      Object.assign({}, x, { keyword: index == 0 ? keyword : 'And' })
    );
  }

  runScenario(scenarioId: any, environment: string) {
    this.runScenarioGQL
      .mutate({ scenarioId: scenarioId, environment: environment, dataset: [] })
      .subscribe((result) =>
        this.router.navigate([`../run/${result.data.runScenario}`], {
          relativeTo: this.route,
        })
      );
  }

  hasChild = (_: number, node: any) =>
    !!node.subSteps && node.subSteps.length > 0;

  editScenario(scenarioId: string) {
    this.router.navigate([`../edit`], {
      relativeTo: this.route,
    });
  }
}
