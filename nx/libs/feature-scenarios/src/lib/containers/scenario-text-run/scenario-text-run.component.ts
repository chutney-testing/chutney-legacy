import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  PauseScenarioGQL,
  ResumeScenarioGQL,
  RunScenarioGQL,
  RunScenarioHistoryGQL,
  Scenario,
  ScenarioGQL,
  StopScenarioGQL,
} from '@chutney/data-access';
import { combineLatest, Observable } from 'rxjs';
import { catchError, map, pluck, switchMap, tap } from 'rxjs/operators';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { MediaObserver } from '@angular/flex-layout';
import { chutneyAnimations, fromEventSource } from '@chutney/utils';
import * as hjson from 'hjson';
import * as jsyaml from 'js-yaml';
import * as dotProp from 'dot-prop-immutable';
import { layoutOprionsVar } from '@chutney/ui-layout';
import { SelectionModel } from '@angular/cdk/collections';

const formSerializer = () => {
    return {};
};

declare const monaco: any;

class TestNode {
    id: string;
    name: string;
    children: TestNode[]
}

@Component({
    selector: 'chutney-scenario-text-run',
    templateUrl: './scenario-text-run.component.html',
    styleUrls: ['./scenario-text-run.component.scss'],
    animations: [chutneyAnimations],
})
export class ScenarioTextRunComponent implements OnInit {
    private scenarioId: string;
    executionId: string;
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

  isHandset$: Observable<boolean> = this.mediaObserver.asObservable().pipe(
      map(
          () =>
              this.mediaObserver.isActive('xs') ||
              this.mediaObserver.isActive('sm') ||
              this.mediaObserver.isActive('lt-md')
      ),
      tap(() => this.changeDetectorRef.detectChanges())
  );
    running: boolean;
    environment: any;
    output: any;
    activeNode: any;
    activeNodeId: any;
    options: any = layoutOprionsVar();
    environments: any = ['GLOBAL', 'PERF'];
    expansionModel = new SelectionModel<string>(true);

    readonly trackBy = (_: number, node: any) => node.status + '-' + node.name + node.duration;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private mediaObserver: MediaObserver,
        private changeDetectorRef: ChangeDetectorRef,
        private scenarioGQL: ScenarioGQL,
        private runScenarioGQL: RunScenarioGQL,
        private stopScenarioGQL: StopScenarioGQL,
        private pauseScenarioGQL: PauseScenarioGQL,
    private resumeScenarioGQL: ResumeScenarioGQL,
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

      combineLatest([this.scenario$, this.report$])
      .pipe(
          map(([s, r]) => {
              const ns = this.normalizeScenario(s);
              return dotProp.set(r, 'report.steps', (list) =>
                  list.map((el, i) => dotProp.merge(el, 'keyword', ns[i].keyword))
              );
          }),
          map((data) => {
              const steps = this.buildFileTree(data.report.steps, 0);
              return dotProp.set(data, 'report.steps', steps)
          })
      )
      .subscribe(
        (data: any) => {
            this.running = data.report.status === 'RUNNING';
            this.environment = data.environment;
            this.report = data.report;
            this.dataSource.data = data.report.steps;
            this.treeControl.dataNodes = data.report.steps;
            this.expansionModel.selected.forEach((id) => {
                const node = this.treeControl.dataNodes.find((n) =>
                    n.id === id
                );
                this.treeControl.expand(node);
            });
        },
        (error) => {
          console.log(error);
          this.running = false;
        },
        () => {
          this.running = false;
        }
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
            Object.assign({}, x, {keyword: index == 0 ? keyword : 'And'})
        );
    }

    /**
     * Build the file structure tree. The `value` is the Json object, or a sub-tree of a Json object.
     * The return value is the list of `FileNode`.
     */
    buildFileTree(obj: any[], level: number, parentId: string = '0'): any[] {
        return obj.reduce<any[]>((accumulator, key, idx) => {
            const value = key;
            /**
             * Make sure your node has an id so we can properly rearrange the tree during drag'n'drop.
             * By passing parentId to buildFileTree, it constructs a path of indexes which make
             * it possible find the exact sub-array that the node was grabbed from when dropped.
             */
            value.id = `${parentId}/${idx}`;


            value.steps = this.buildFileTree(value.steps, level + 1, value.id);


            return accumulator.concat(value);
        }, []);
    }


    hasChild = (_: number, node: any) => !!node.steps && node.steps.length > 0;

    runScenario() {
        if (this.report.status === 'PAUSED') {
            this.resumeScenario();
        } else {
            this.runScenarioGQL
                .mutate({
                    scenarioId: this.scenarioId,
          environment: this.environment,
          dataset: [],
        })
        .subscribe((result) =>
          this.router.navigate([`../${result.data.runScenario}`], {
            relativeTo: this.route,
          })
        );
    }
  }

  toYaml(obj: any) {
    return jsyaml.dump(obj);
  }

  monacoEditorConfigChanged(theme: string) {
    monaco.editor.setTheme(theme);
  }

  stopScenario() {
    this.stopScenarioGQL
      .mutate({
        scenarioId: this.scenarioId,
        executionId: this.executionId,
        bodyBuilder: formSerializer,
      })
      .subscribe(
        (data) => {
          this.running = false;
        },
        (error) => {
          console.log(error);
        }
      );
  }

  resumeScenario() {
    this.resumeScenarioGQL
      .mutate({
        scenarioId: this.scenarioId,
        executionId: this.executionId,
        bodyBuilder: formSerializer,
      })
      .subscribe(
        (data) => {
          this.running = true;
        },
        (error) => {
          console.log(error);
        }
      );
  }

  pauseScenario() {
    this.pauseScenarioGQL
      .mutate({
        scenarioId: this.scenarioId,
        executionId: this.executionId,
        bodyBuilder: formSerializer,
      })
      // .valueChanges
      .subscribe(
        (data) => {
          this.running = false;
        },
          (error) => {
              console.log(error);
          }
      );
  }

    selectEnvironment(item: any) {
        this.environment = item;
    }

    toggleNode(node) {
        this.expansionModel.toggle(node.id)
    }

  selectNode(node) {
    this.activeNodeId = node.id
    this.activeNode = node;
  }
}
