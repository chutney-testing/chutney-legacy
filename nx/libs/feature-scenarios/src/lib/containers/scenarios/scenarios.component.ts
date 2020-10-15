import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DeleteScenarioGQL, Scenario, ScenariosDocument, ScenariosGQL, ScenariosQuery } from '@chutney/data-access';
import { map, pluck, startWith, take, takeUntil } from 'rxjs/operators';
import { combineLatest, Observable, ReplaySubject, Subject } from 'rxjs';
import { TdDialogService } from '@covalent/core/dialogs';
import { tdCollapseAnimation } from '@covalent/core/common';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MatSelect } from '@angular/material/select';
import { Apollo } from 'apollo-angular';
import { filter, sortByKeys } from '@chutney/utils';

@Component({
  selector: 'chutney-testing-scenarios',
  templateUrl: './scenarios.component.html',
  styleUrls: ['./scenarios.component.scss'],
  animations: [tdCollapseAnimation]
})
export class ScenariosComponent implements OnInit, OnDestroy {


  protected tags: String[] = [];

  /** control for the MatSelect filter keyword multi-selection */
  public tagFilterCtrl: FormControl = new FormControl();

  /** list of tags filtered by search keyword */
  public filteredTags: ReplaySubject<String[]> = new ReplaySubject<String[]>(1);

  @ViewChild('multiSelect', {static: true}) multiSelect: MatSelect;

  scenarios$: Observable<any[]>;
  filteredScenarios$: Observable<any[]>;
  triggerState: boolean = true;
  searchForm!: FormGroup;

  /** Subject that emits when the component has been destroyed. */
  protected _onDestroy = new Subject<void>();


  constructor(private _dialogService: TdDialogService,
              private deleteScenarioGQL: DeleteScenarioGQL,
              private scenariosGQL: ScenariosGQL,
              private apollo: Apollo,
              private fb: FormBuilder) {
    this.searchForm = this.fb.group({
      text: null,
      tags: null,
      date: null
    });
  }

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  ngOnInit(): void {
    this.scenarios$ = this.scenariosGQL
      .watch()
      .valueChanges
      .pipe(
        pluck('data', 'scenarios')
      );

    // load the initial tags list

    this.scenarios$.pipe(
      map((scenarios: Scenario[]) => {
        return [...new Set<string>(scenarios.map(s => s.tags).flat())];
      })
    ).subscribe(
      (data) => {
        this.tags = [...data]
        this.filteredTags.next(data.slice());
      }
    )
    //this.filteredTags.next(this.tags.slice());

    // listen for search field value changes
    this.tagFilterCtrl.valueChanges
      .pipe(takeUntil(this._onDestroy))
      .subscribe(() => {
        this.filterTags();
      });

    this.filteredScenarios$ =
      combineLatest([this.scenarios$, this.searchForm.valueChanges
        .pipe(startWith({text: null, tags: null, date: null}))])
        .pipe(map(([states, filterString]) => this.filterScenarios(states, filterString)))


  }

  onEdit(id: string) {
    console.log(`edit scenario with id ${id}`);
  }

  onDelete(id: string) {
    this._dialogService.openConfirm({
      title: 'Confirm',
      message: 'After deletion, the scenario cannot be restored',
      cancelButton: 'Cancel',
      acceptButton: 'Ok',
    }).afterClosed().subscribe((accept: boolean) => {
      if (accept) {
        console.log(`delete scenario with id ${id}`);
        this.deleteScenarioGQL.mutate({input: id}, {
          update: (store, result) => {
            const data: ScenariosQuery = store.readQuery({query: ScenariosDocument});
            const index = data.scenarios.findIndex(scenario => scenario.id === id);
            const scenarios = [...data.scenarios.slice(0, index), ...data.scenarios.slice(index + 1)];
            store.writeQuery({query: ScenariosDocument, data: {scenarios}});
          }
        }).subscribe()
      }
    });
  }

  showFilter() {
    this.triggerState = !this.triggerState
  }

  toggleSelectAll(selectAllValue: boolean) {
    this.filteredTags.pipe(take(1), takeUntil(this._onDestroy))
      .subscribe(val => {
        if (selectAllValue) {
          this.searchForm.controls['tags'].patchValue(val);
        } else {
          this.searchForm.controls['tags'].patchValue([]);
        }
      });
  }

  private filterScenarios(scenarios: any, scenariosFilter: any): Scenario[] {
    if (!scenarios) {
      return [];
    }

    const filteredScenarios = filter(scenarios, [
        scenario => !scenariosFilter.text || scenario.title.includes(scenariosFilter.text),
        scenario => !scenariosFilter.tags || scenariosFilter.tags.length === 0
          || scenario.tags.filter(x => scenariosFilter.tags.includes(x)).length > 0
      ]
    );
    if (scenariosFilter.date) {
      return sortByKeys(filteredScenarios, `-${scenariosFilter.date}`);
    }
    return filteredScenarios;
  }


  protected filterTags() {
    if (!this.tags) {
      return;
    }
    // get the search keyword
    let search = this.tagFilterCtrl.value;
    if (!search) {
      this.filteredTags.next(this.tags.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    // filter the tags
    this.filteredTags.next(
      this.tags.filter(bank => bank.toLowerCase().indexOf(search) > -1)
    );
  }

}
