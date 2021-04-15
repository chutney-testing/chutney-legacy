import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TdDialogService } from '@covalent/core/dialogs';
import { Apollo } from 'apollo-angular';
import { pluck } from 'rxjs/operators';

import { chutneyAnimations } from '@chutney/utils';
import {
  GlobalVariableGroupsNamesGQL,
  DeleteGlobalVariableGroupGQL
} from '@chutney/data-access';

@Component({
  selector: 'chutney-variables',
  templateUrl: './variables.component.html',
  styleUrls: ['./variables.component.scss'],
  animations: [chutneyAnimations],
})
export class VariablesComponent {
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Variables', link: ['/variables'] },
  ];

  globalVariableGroupsNames: string[] = [];

  private globalVariableGroupsStoreDataName: string = 'globalVariableGroupsNames';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dialogService: TdDialogService,
    private snackBar: MatSnackBar,
    private apollo: Apollo,
    private globalVariableGroupsNamesGQL: GlobalVariableGroupsNamesGQL,
    private deleteGlobalVariableGroupGQL: DeleteGlobalVariableGroupGQL
  ) {}

  ngOnInit(): void {
    this.globalVariableGroupsNamesGQL
      .watch({}, { fetchPolicy: 'network-only', nextFetchPolicy: 'cache-only' })
      .valueChanges.pipe(pluck('data', this.globalVariableGroupsStoreDataName))
      .subscribe(
        (r: string[]) => {
          if (r) {
            this.globalVariableGroupsNames = r;
          }
        }
      );
  }

  addGroup() {
    this.router.navigate(['text', 'add'], { relativeTo: this.route });
  }

  editGroup(groupNameToEdit: string) {
    this.router.navigate(['text', encodeURIComponent(groupNameToEdit), 'edit'], { relativeTo: this.route });
  }

  deleteGroup(groupNameToDelete: string) {
    this.dialogService
      .openConfirm({
        title: 'Confirm',
        message: 'After deletion, the variables group cannot be restored',
        cancelButton: 'Cancel',
        acceptButton: 'Ok',
      })
      .afterClosed()
      .subscribe((accept: boolean) => {
        if (accept) {
          this.deleteGlobalVariableGroupGQL
            .mutate(
              { groupName: groupNameToDelete },
              {
                update: (store, result) => {
                  let groupNamesStored: Array<string> = this.groupNamesStored(store);
                  const groupNameIndexToDelete = groupNamesStored.indexOf(groupNameToDelete);
                  groupNamesStored.splice(groupNameIndexToDelete, 1);
                  this.storeGroupNames(store, groupNamesStored);
                }
              }
            )
            .subscribe(
              () => {
                this.globalVariableGroupsNames = this.groupNamesStored();
                this.snackBar.open(`<${groupNameToDelete}> variables group has been deleted`);
              },
              (err) => {
                this.snackBar.open(err.message + ' : ' + err.networkError.result);
              }
            );
        }
      });
  }

  private groupNamesStored(_store = null): Array<string> {
    const store = _store || this.apollo.client.cache;

    return Array.from(
      store.readQuery({
        query: this.globalVariableGroupsNamesGQL.document
      })[this.globalVariableGroupsStoreDataName]
     );
  }

  private storeGroupNames(_store, groupNamesStored: Array<string>) {
    const store = _store || this.apollo.client.cache;

    let groupNamesToStore = Object.assign({});
    groupNamesToStore[this.globalVariableGroupsStoreDataName] = groupNamesStored;
    store.evict({ fieldName: this.globalVariableGroupsStoreDataName, broadcast: false });
    store.writeQuery({
      query: this.globalVariableGroupsNamesGQL.document,
      data: groupNamesToStore
    });
  }
}
