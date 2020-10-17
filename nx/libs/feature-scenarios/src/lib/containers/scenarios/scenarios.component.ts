import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import {
  DeleteScenarioGQL,
  Scenario,
  ScenariosDocument,
  ScenariosFilterGQL,
  ScenariosGQL,
  ScenariosQuery,
} from '@chutney/data-access';
import { map, pluck, startWith, take, takeUntil } from 'rxjs/operators';
import { combineLatest, Observable, ReplaySubject, Subject } from 'rxjs';
import { TdDialogService } from '@covalent/core/dialogs';
import { tdCollapseAnimation } from '@covalent/core/common';
import { filter, sortByKeys } from '@chutney/utils';

@Component({
  selector: 'chutney-testing-scenarios',
  templateUrl: './scenarios.component.html',
  styleUrls: ['./scenarios.component.scss'],
  animations: [tdCollapseAnimation],
})
export class ScenariosComponent implements OnInit, OnDestroy {
  scenarios$: Observable<any[]>;
  scenariosFilter$: Observable<any>;
  filteredScenarios$: Observable<any[]>;

  /** Subject that emits when the component has been destroyed. */
  protected _onDestroy = new Subject<void>();

  constructor(
    private _dialogService: TdDialogService,
    private deleteScenarioGQL: DeleteScenarioGQL,
    private scenariosGQL: ScenariosGQL,
    private scenariosFilterGQL: ScenariosFilterGQL
  ) {}

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  ngOnInit(): void {
    this.scenarios$ = this.scenariosGQL
      .watch()
      .valueChanges.pipe(pluck('data', 'scenarios'));

    this.scenariosFilter$ = this.scenariosFilterGQL
      .watch()
      .valueChanges.pipe(pluck('data', 'scenariosFilter'));

    this.filteredScenarios$ = combineLatest([
      this.scenarios$,
      this.scenariosFilter$.pipe(
        startWith({ text: null, tags: null, date: null, advanced: false })
      ),
    ]).pipe(
      map(([states, filterString]) =>
        this.filterScenarios(states, filterString)
      )
    );
  }

  onEdit(id: string) {
    console.log(`edit scenario with id ${id}`);
  }

  onDelete(id: string) {
    this._dialogService
      .openConfirm({
        title: 'Confirm',
        message: 'After deletion, the scenario cannot be restored',
        cancelButton: 'Cancel',
        acceptButton: 'Ok',
      })
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          console.log(`delete scenario with id ${id}`);
          this.deleteScenarioGQL
            .mutate(
              { input: id },
              {
                update: (store, result) => {
                  const data: ScenariosQuery = store.readQuery({
                    query: ScenariosDocument,
                  });
                  const index = data.scenarios.findIndex(
                    (scenario) => scenario.id === id
                  );
                  const scenarios = [
                    ...data.scenarios.slice(0, index),
                    ...data.scenarios.slice(index + 1),
                  ];
                  store.writeQuery({
                    query: ScenariosDocument,
                    data: { scenarios },
                  });
                },
              }
            )
            .subscribe();
        }
      });
  }

  private filterScenarios(scenarios: any, scenariosFilter: any): Scenario[] {
    if (!scenarios) {
      return [];
    }

    if (!scenariosFilter) {
      return scenarios;
    }

    const filteredScenarios = filter(scenarios, [
      (scenario) =>
        !scenariosFilter.text || scenario.title.includes(scenariosFilter.text),
      (scenario) =>
        !scenariosFilter.tags ||
        scenariosFilter.tags.length === 0 ||
        scenario.tags.filter((x) => scenariosFilter.tags.includes(x)).length >
          0,
    ]);
    if (scenariosFilter.date) {
      return sortByKeys(filteredScenarios, `-${scenariosFilter.date}`);
    }
    return filteredScenarios;
  }
}
