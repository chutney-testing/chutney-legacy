import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  RunScenarioGQL,
  RunScenarioHistoryGQL,
  Scenario,
  ScenarioGQL,
} from '@chutney/data-access';
import { combineLatest, Observable } from 'rxjs';
import { catchError, map, pluck, switchMap } from 'rxjs/operators';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { fromEventSource } from '@chutney/utils';
import * as hjson from 'hjson';
import * as dotProp from 'dot-prop-immutable';

@Component({
  selector: 'chutney-scenario-text-run',
  templateUrl: './scenario-text-run.component.html',
  styleUrls: ['./scenario-text-run.component.scss'],
})
export class ScenarioTextRunComponent implements OnInit {
  private scenarioId: string;
  private executionId: string;
  scenario$: Observable<any>;
  report$: Observable<any>;
  treeControl = new NestedTreeControl<any>((node) => node.steps);
  dataSource = new MatTreeNestedDataSource<any>();
  report: any;
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Scenarios', link: ['/'] },
    { title: 'View', link: ['../../view'] },
  ];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private scenarioGQL: ScenarioGQL,
    private runScenarioGQL: RunScenarioGQL,
    private runScenarioHistoryGQL: RunScenarioHistoryGQL
  ) {}

  ngOnInit(): void {
    this.scenario$ = this.route.params.pipe(
      switchMap((p) => {
        return this.scenarioGQL.watch({ scenarioId: p.id }).valueChanges.pipe(
          pluck('data', 'scenario'),
          map((value) => hjson.parse(value.content))
        );
      })
    );

    this.report$ = this.route.params.pipe(
      switchMap((p) => {
        this.scenarioId = p.id;
        this.executionId = p.executionId;
        return fromEventSource(
          `/api/ui/scenario/executionasync/v1/${p.id}/execution/${p.executionId}`
        ).pipe(
          map((evt: MessageEvent) => JSON.parse(evt.data)),
          catchError((e) => {
            return this.runScenarioHistoryGQL
              .fetch({
                scenarioId: this.scenarioId,
                executionId: this.executionId,
              })
              .pipe(
                pluck('data', 'runScenarioHistory'),
                map((data) => JSON.parse(data.report))
              );
          })
        );
      })
    );

    combineLatest(this.scenario$, this.report$)
      .pipe(
        map(([s, r]) => {
          const ns = this.normalizeScenario(s);
          return dotProp.set(r, 'report.steps', (list) =>
            list.map((el, i) => dotProp.merge(el, 'keyword', ns[i].keyword))
          );
        })
      )
      .subscribe(
        (data: any) => {
          this.report = data.report;
          this.dataSource.data = data.report.steps;
        },
        (error) => console.log(error)
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

  hasChild = (_: number, node: any) => !!node.steps && node.steps.length > 0;

  //readonly trackBy = (_: number, node: any) => node.status + '-' + node.name;
  model: any = 'select * from hello';

  runScenario() {
    this.runScenarioGQL
      .mutate({ scenarioId: this.scenarioId, dataset: [] })
      .subscribe((result) =>
        this.router.navigate([`../${result.data.runScenario}`], {
          relativeTo: this.route,
        })
      );
  }
}
