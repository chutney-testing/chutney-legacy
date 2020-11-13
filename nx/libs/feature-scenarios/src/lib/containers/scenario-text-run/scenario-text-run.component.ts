import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import {
  RunScenarioGQL,
  RunScenarioHistoryGQL,
  Scenario,
  ScenarioGQL,
} from '@chutney/data-access';
import { Observable } from 'rxjs';
import { map, pluck } from 'rxjs/operators';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import Hjson from 'hjson';

@Component({
  selector: 'chutney-scenario-text-run',
  templateUrl: './scenario-text-run.component.html',
  styleUrls: ['./scenario-text-run.component.scss'],
})
export class ScenarioTextRunComponent implements OnInit {
  private scenarioId: string;
  private executionId: string;
  private params$: Observable<Params>;
  scenario$: Observable<Scenario>;
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
    this.scenarioId = this.route.snapshot.paramMap.get('id');

    this.scenario$ = this.scenarioGQL
      .watch({ scenarioId: this.scenarioId })
      .valueChanges.pipe(pluck('data', 'scenario'));

    this.scenario$.pipe(
      map((value) => {
        return Hjson.parse(value.content);
      })
    );

    this.params$ = this.route.params;
    this.params$.subscribe((p) => {
      this.scenarioId = p.id;
      this.executionId = p.executionId;
      new Observable<any>((obs) => {
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
      }).subscribe(
        (data) => {
          //console.log(JSON.stringify(data));
          this.report = data.report;
          this.dataSource.data = data.report.steps; //this.normalizeScenario(scenario)
        },
        (msg) => {
          this.runScenarioHistoryGQL
            .fetch({
              scenarioId: this.scenarioId,
              executionId: this.executionId,
            })
            .pipe(pluck('data', 'runScenarioHistory'))
            .subscribe((data) => {
              const runScenarioHistory = JSON.parse(data.report);
              console.log(JSON.stringify(runScenarioHistory));
              this.report = runScenarioHistory.report;
              this.dataSource.data = runScenarioHistory.report.steps;
            });
        },
        () => {
          console.log('completed...');
        }
      );
    });
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
