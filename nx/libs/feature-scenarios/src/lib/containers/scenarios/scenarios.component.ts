import { Component, OnInit } from '@angular/core';
import { DeleteScenarioGQL, Scenario, ScenariosDocument, ScenariosGQL, ScenariosQuery } from '@chutney/data-access';
import { pluck } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { TdDialogService } from '@covalent/core/dialogs';

@Component({
  selector: 'chutney-testing-scenarios',
  templateUrl: './scenarios.component.html',
  styleUrls: ['./scenarios.component.scss'],
})
export class ScenariosComponent implements OnInit {
  scenarios$: Observable<any[]>;

  constructor(private _dialogService: TdDialogService,
              private deleteScenarioGQL: DeleteScenarioGQL,
              private scenariosGQL: ScenariosGQL) {
  }

  ngOnInit(): void {
    this.scenarios$ = this.scenariosGQL
      .watch()
      .valueChanges.pipe(pluck('data', 'scenarios'));
  }

  onEdit(id: number) {
    console.log(`edit scenario with id${id}`);
  }

  onDelete(id: number) {
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
}
