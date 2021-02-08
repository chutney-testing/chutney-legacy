import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { pluck } from 'rxjs/operators';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { editor } from 'monaco-editor';

import {
  GlobalVariableGroupsNamesGQL,
  GlobalVariableGroupContentGQL,
  GlobalVariableGroupContent,
  DeleteGlobalVariableGroupGQL,
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

  globalVariableGroupsNames$: Observable<String[]>;
  activeGroupName$: BehaviorSubject<string> = new BehaviorSubject(null);
  activeGroupNameIndex = 0;

  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;
  options: any = layoutOprionsVar();

  constructor(
    private globalVariableGroupsNamesGQL: GlobalVariableGroupsNamesGQL,
    private globalVariableGroupContentGQL: GlobalVariableGroupContentGQL,
    private deleteGlobalVariableGroupGQL: DeleteGlobalVariableGroupGQL
  ) { }

  ngOnInit(): void {
    this.globalVariableGroupsNames$ = this.globalVariableGroupsNamesGQL
      .watch()
      .valueChanges.pipe(pluck('data', 'globalVariableGroupsNames'));

    this.activeGroupName$.subscribe(
      v => this.fetchVariableGroupContent(v)
    );
  }

  ngOnDestroy() {
      this.activeGroupName$.complete();
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
    const groupNameToDelete = this.activeGroupName$.value;
    const globalVariableGroupsNamesDocument = this.globalVariableGroupsNamesGQL.document;
    this.deleteGlobalVariableGroupGQL
      .mutate(
        { groupName: groupNameToDelete },
        {
          update: (store, result) => {
           let groupNamesStored: Array<string> = Array.from(
            store.readQuery({
              query: globalVariableGroupsNamesDocument
            })['globalVariableGroupsNames']
           );
           groupNamesStored.splice(this.activeGroupNameIndex, 1);
           store.writeQuery({
             query: globalVariableGroupsNamesDocument,
             data: { globalVariableGroupsNames: groupNamesStored }
           });

           this.activeGroupName$.next(groupNamesStored[this.activeGroupNameIndex]);
          }
        }
      )
      .subscribe();
  }

  private fetchVariableGroupContent(groupName: string) {
    if (groupName) {
      this.globalVariableGroupContentGQL
        .fetch({ groupName: groupName })
        .pipe(pluck('data', 'globalVariableGroupContent'))
        .subscribe(
          (content: GlobalVariableGroupContent) => {
            this.monaco.value = content.message;
          }
        );
    }
  }

}
