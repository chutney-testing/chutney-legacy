import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import {
  RunScenarioGQL,
  RunScenarioHistoryGQL,
  Scenario,
  ScenarioGQL,
} from '@chutney/data-access';
import { Observable, of } from 'rxjs';
import { catchError, map, pluck, switchMap } from 'rxjs/operators';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';

@Component({
  selector: 'chutney-scenario-text-run',
  templateUrl: './scenario-text-run.component.html',
  styleUrls: ['./scenario-text-run.component.scss'],
})
export class ScenarioTextRunComponent implements OnInit {
  private scenarioId: string;
  private executionId: string;
  private params$: Observable<Params>;
  scenario$: Observable<any>;
  treeControl = new NestedTreeControl<any>((node) => node.steps);
  dataSource = new MatTreeNestedDataSource<any>();
  report: any;

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
        return this.scenarioGQL
          .watch({ scenarioId: p.id })
          .valueChanges.pipe(pluck('data', 'scenario'));
      })
    );

    this.route.params
      .pipe(
        switchMap((p) => {
          this.scenarioId = p.id;
          this.executionId = p.executionId;
          let observable = new Observable<any>((obs) => {
            let es;
            try {
              const url =
                `/api/ui/scenario/executionasync/v1/${p.id}` +
                `/execution/${p.executionId}`;
              es = new EventSource(url);
              es.addEventListener('partial', (evt: any) => {
                obs.next(JSON.parse(evt.data));
              });
              es.addEventListener('last', (evt: any) => {
                obs.next(JSON.parse(evt.data));
                obs.complete();
              });
              es.addEventListener('error', (e) => {
                console.log('An error occurred while attempting to connect.');
                obs.error(e);
                // throwError('An error occurred while attempting to connect.');
              });
            } catch (error) {
              obs.error(error);
              // throw Error('An error occurred while attempting to connect.');
            }

            return () => {
              if (es) {
                es.close();
              }
            };
          });
          return observable.pipe(
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
      )
      .subscribe(
        (data: any) => {
          //console.log(JSON.stringify(data));
          this.report = data.report;
          this.dataSource.data = data.report.steps; //this.normalizeScenario(scenario)
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
    return steps.map((x) => Object.assign({}, x, { keyword: keyword }));
  }

  hasChild = (_: number, node: any) => !!node.steps && node.steps.length > 0;

  runScenario(scenario: Scenario) {
    this.runScenarioGQL
      .mutate({ scenarioId: this.scenarioId, dataset: [] })
      .subscribe((result) =>
        this.router.navigate([`../${result.data.runScenario}`], {
          relativeTo: this.route,
        })
      );
  }
}
