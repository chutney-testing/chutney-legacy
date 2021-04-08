import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { editor } from 'monaco-editor';
import { pluck } from 'rxjs/operators';

import { chutneyAnimations } from '@chutney/utils';
import { layoutOprionsVar } from '@chutney/ui-layout';
import {
  GlobalVariableGroupContentGQL,
  GlobalVariableGroupContent,
  SaveGlobalVariableGroupGQL,
} from '@chutney/data-access';

declare const monaco: any;

@Component({
  selector: 'chutney-variables-text-edit',
  templateUrl: './variables-text-edit.component.html',
  styleUrls: ['./variables-text-edit.component.scss'],
  animations: [chutneyAnimations],
})
export class VariablesTextEditComponent implements OnInit {
  breadcrumbs: any = [];

  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;
  options: any = layoutOprionsVar();

  private groupName: string;

  constructor(
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private globalVariableGroupContentGQL: GlobalVariableGroupContentGQL,
    private saveGlobalVariableGroupGQL: SaveGlobalVariableGroupGQL,
  ) { }

  ngOnInit(): void {
    this.groupName = decodeURIComponent(this.route.snapshot.params['groupName']);
    this.breadcrumbs = [
        { title: 'Home', link: ['/'] },
        { title: 'Variables', link: ['/variables'] },
        { title: `Edit group <${this.groupName}>`, link: [] },
      ]
    this.fetchVariableGroupContent(this.groupName);
  }

  async editorInitialized(editorInstance: any): Promise<void> {
    this._editor = editorInstance;
  }

  monacoEditorConfigChanged(theme: string) {
    monaco.editor.setTheme(theme);
  }

  saveGroupContent() {
    const groupNameToSave = this.groupName;
    this.saveGlobalVariableGroupGQL
      .mutate(
        {
          groupName: groupNameToSave,
          input: { message: this.monaco.value }
        }
      )
      .subscribe(
        () => {
          this.snackBar.open(`<${groupNameToSave}> variables group has been saved`);
        },
        (err) => {
          this.snackBar.open(err.message + ' : ' + err.networkError.result);
        }
      );
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
    }
  }
}
