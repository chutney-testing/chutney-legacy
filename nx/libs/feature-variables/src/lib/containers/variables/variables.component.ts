import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TdDialogService } from '@covalent/core/dialogs';
import { Apollo } from 'apollo-angular';
import { pluck } from 'rxjs/operators';

import { chutneyAnimations } from '@chutney/utils';
import {
  GlobalVariableGroupsNamesGQL,
  DeleteGlobalVariableGroupGQL,
  SaveGlobalVariableGroupGQL,
  RenameGlobalVariableGroupGQL,
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
    private deleteGlobalVariableGroupGQL: DeleteGlobalVariableGroupGQL,
    private saveGlobalVariableGroupGQL: SaveGlobalVariableGroupGQL,
    private renameGlobalVariableGroupGQL: RenameGlobalVariableGroupGQL,
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

  editGlobalVariableGroup(groupNameToEdit: string) {
    this.router.navigate(['text', encodeURIComponent(groupNameToEdit), 'edit'], { relativeTo: this.route });
  }

  deleteGlobalVariableGroup(groupNameIndexToDelete: number) {
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
          const groupNameToDelete = this.globalVariableGroupsNames[groupNameIndexToDelete];
          this.deleteGlobalVariableGroupGQL
            .mutate(
              { groupName: groupNameToDelete },
              {
                update: (store, result) => {
                  let groupNamesStored: Array<string> = this.groupNamesStored(store);
                  groupNamesStored.splice(groupNameIndexToDelete, 1);
                  this.storeGroupNames(store, groupNamesStored);
                }
              }
            )
            .subscribe(
              () => {
                this.updateGroupNamesList();
                this.snackBar.open(`<${groupNameToDelete}> variables group has been deleted`);
              },
              (err) => {
                this.snackBar.open(err.message + ' : ' + err.networkError.result);
              }
            );
        }
      });
  }

  addGlobalVariableGroup(groupNameToSave: string) {
    if (!this.validateGroupNameInput(groupNameToSave)) { return; }

    this.saveGlobalVariableGroupGQL
      .mutate(
        {
          groupName: groupNameToSave,
          input: { message: '' }
        },
        {
          update: (store, result) => {
            let groupNamesStored: Array<string> = this.groupNamesStored(store);
            groupNamesStored.push(groupNameToSave);
            groupNamesStored.sort();
            this.storeGroupNames(store, groupNamesStored);
          }
        }
      )
      .subscribe(
        () => {
          this.updateGroupNamesList();
          this.snackBar.open(`<${groupNameToSave}> variables group has been added`);
        },
        (err) => {
          this.snackBar.open(err.message + ' : ' + err.networkError.result);
        }
      );
  }

  renameGlobalVariableGroup(renameEvent) {
    const groupName = renameEvent.groupName;
    const groupNameToSave = renameEvent.newGroupName;
    if (!this.validateGroupNameInput(groupNameToSave)) { return; }

    this.renameGlobalVariableGroupGQL
      .mutate(
        {
          groupName: groupName,
          input: { message: groupNameToSave }
        },
        {
          update: (store, result) => {
            let groupNamesStored: Array<string> = this.groupNamesStored(store);
            groupNamesStored.splice(groupNamesStored.indexOf(groupName), 1, groupNameToSave);
            groupNamesStored.sort();
            this.storeGroupNames(store, groupNamesStored);
          }
        }
      )
      .subscribe(
        () => {
          this.snackBar.open(`<${groupName}> variables group has been renamed to <${groupNameToSave}>`);
        },
        (err) => {
          this.snackBar.open(err.message + ' : ' + err.networkError.result);
        }
      );
  }

  private validateGroupNameInput(groupNameToValidate: string): boolean {
    const groupNames = this.groupNamesStored();
    if (groupNameToValidate.length == 0) {
      this.snackBar.open('Group name cannot be empty');
      return false;
    }
    if (groupNames.findIndex(this.groupNameExistsPredicate(groupNameToValidate)) > -1) {
      this.snackBar.open('Group name already exists');
      return false;
    }
    return true;
  }

  private groupNameExistsPredicate(groupNameToTest: string) {
    return gn => gn.toUpperCase() === groupNameToTest.trim().toUpperCase();
  }

  private updateGroupNamesList() {
    this.globalVariableGroupsNames = this.groupNamesStored();
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
