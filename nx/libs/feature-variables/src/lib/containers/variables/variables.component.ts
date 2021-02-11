import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatInput } from '@angular/material/input';
import { Apollo } from 'apollo-angular';
import { Observable, BehaviorSubject, timer } from 'rxjs';
import { pluck } from 'rxjs/operators';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { editor } from 'monaco-editor';
import { MatSnackBar } from '@angular/material/snack-bar';

import {
  GlobalVariableGroupsNamesGQL,
  GlobalVariableGroupContentGQL,
  GlobalVariableGroupContent,
  DeleteGlobalVariableGroupGQL,
  SaveGlobalVariableGroupGQL,
} from '@chutney/data-access';
import { layoutOprionsVar } from '@chutney/ui-layout';
import {
  chutneyAnimations,
} from '@chutney/utils';

declare const monaco: any;

@Component({
  selector: 'chutney-variables',
  templateUrl: './variables.component.html',
  styleUrls: ['./variables.component.scss'],
  animations: [chutneyAnimations],
})
export class VariablesComponent implements OnInit, OnDestroy {
  breadcrumbs: any = [
      { title: 'Home', link: ['/'] },
      { title: 'Variables', link: ['/variables'] },
    ];

  private globalVariableGroupsStoreDataName: string = 'globalVariableGroupsNames';
  globalVariableGroupsNames$: Observable<String[]>;
  activeGroupName$: BehaviorSubject<string> = new BehaviorSubject(null);
  activeGroupNameIndex = 0;

  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;
  options: any = layoutOprionsVar();

  addMode: boolean = false;

  constructor(
    private snackBar: MatSnackBar,
    private apollo: Apollo,
    private globalVariableGroupsNamesGQL: GlobalVariableGroupsNamesGQL,
    private globalVariableGroupContentGQL: GlobalVariableGroupContentGQL,
    private deleteGlobalVariableGroupGQL: DeleteGlobalVariableGroupGQL,
    private saveGlobalVariableGroupGQL: SaveGlobalVariableGroupGQL
  ) { }

  ngOnInit(): void {
    this.globalVariableGroupsNames$ = this.globalVariableGroupsNamesGQL
      .watch({}, { fetchPolicy: 'network-only', nextFetchPolicy: 'cache-only' })
      .valueChanges.pipe(pluck('data', this.globalVariableGroupsStoreDataName));

    this.activeGroupName$.subscribe(
      v => this.fetchVariableGroupContent(v)
    );
  }

  ngOnDestroy() {
      this.activeGroupName$.complete();
      this.resetAddMode();
    }

  onTabChange(matTabChangeEvent) {
    let groupName: string = null;
    if (matTabChangeEvent.tab) {
      groupName = matTabChangeEvent.tab.textLabel;
    }
    this.activeGroupName$.next(groupName);
  }

  async editorInitialized(editorInstance: any): Promise<void> {
    this._editor = editorInstance;
  }

  monacoEditorConfigChanged(theme: string) {
    monaco.editor.setTheme(theme);
  }

  deleteGlobalVariableGroup() {
    if (!this.addMode) {
      const groupNameToDelete = this.activeGroupName$.value;
      const globalVariableGroupsNamesDocument = this.globalVariableGroupsNamesGQL.document;
      this.deleteGlobalVariableGroupGQL
        .mutate(
          { groupName: groupNameToDelete },
          {
            update: (store, result) => {
             let groupNamesStored: Array<string> = this.groupNamesStored(store);
             groupNamesStored.splice(this.activeGroupNameIndex, 1);
             this.storeGroupNames(store, groupNamesStored);
            }
          }
        )
        .subscribe(
          () => {
            this.snackBar.open(groupNameToDelete + ' variables group has been deleted');
            const groupNamesStored: Array<string> = this.groupNamesStored();
            if (groupNamesStored.length > 0) {
              this.activeGroupName$.next(groupNamesStored[this.activeGroupNameIndex]);
            } else {
              this.monaco.value = '';
            }
          },
          (err) => {
            this.snackBar.open(err.message + ' : ' + err.networkError.result);
          }
        );
    }
  }

  saveGlobalVariableGroup() {
    if (this.addMode && !this.validateNewGroupNameInput()) {
      return;
    }

    const groupNameToSave = this.addMode ? this.newGroupNameInput().value : this.activeGroupName$.value;
    const variableGroup = Object.assign(
      {},
      { groupName: groupNameToSave },
      { input: { message: this.monaco.value } }
    );

    this.saveGlobalVariableGroupGQL
      .mutate(
        variableGroup,
        {
          update: (store, result) => {
            if (this.addMode) {
              let groupNamesStored: Array<string> = this.groupNamesStored(store);
              groupNamesStored[0] = groupNameToSave;
              groupNamesStored.sort();
              const newGroupnameIndex = groupNamesStored.indexOf(groupNameToSave);
              this.storeGroupNames(store, groupNamesStored);
              this.addMode = false;
              this.setActiveGroupNameIndexAfterAndDo(1000, newGroupnameIndex);
            }
          }
        }
      )
      .subscribe(
        () => {
          this.snackBar.open(groupNameToSave + ' variables group has been saved');
        },
        (err) => {
          this.snackBar.open(err.message + ' : ' + err.networkError.result);
        }
      );
  }

  addGlobalVariableGroup() {
    if (!this.addMode) {
      this.addMode = true;
      const groupNames = this.groupNamesStored();
      groupNames.splice(0, 0, '');
      this.storeGroupNames(null, groupNames);
      if (this.activeGroupNameIndex == 0) {
        this.activeGroupNameIndex--;
      }
      this.setActiveGroupNameIndexAfterAndDo(1000, 0, () => this.newGroupNameInput().focus());
    } else {
      this.newGroupNameInput().focus();
    }
  }

  validateNewGroupNameInput(): boolean {
    const newGroupNameInput = this.newGroupNameInput();
    if (newGroupNameInput.value.length == 0) {
      this.snackBar.open('New group name cannot be empty');
      this.newGroupNameInput().focus();
      return false;
    }
    return true;
  }

  private fetchVariableGroupContent(groupName: string) {
    if (groupName && groupName.length > 0) {
      this.globalVariableGroupContentGQL
        .fetch({ groupName: groupName }, { fetchPolicy: 'no-cache' })
        .pipe(pluck('data', 'globalVariableGroupContent'))
        .subscribe(
          (content: GlobalVariableGroupContent) => {
            this.monaco.value = content.message;
          }
        );

      this.resetAddMode();
    }
  }

  private resetAddMode() {
    if (this.addMode) {
      this.addMode = false;
      const groupNames = this.groupNamesStored();
      groupNames.splice(0, 1);
      this.storeGroupNames(null, groupNames);
      this.setActiveGroupNameIndexAfterAndDo(1000, this.activeGroupNameIndex - 1);
    }
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

  private newGroupNameInput(): HTMLInputElement {
    return <HTMLInputElement>document.getElementById('newGroupNameInput');
  }

  private setActiveGroupNameIndexAfterAndDo(afterMs: number, newActiveIndex: number, action = null) {
    timer(afterMs).subscribe(x => {
        this.activeGroupNameIndex = newActiveIndex;
        action && action.apply();
    });
  }
}
