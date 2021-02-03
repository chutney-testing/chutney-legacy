import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { pluck } from 'rxjs/operators';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { editor } from 'monaco-editor';

import {
  GlobalVariableGroupsNamesGQL,
  GlobalVariableGroupContentGQL,
  GlobalVariableGroupContent,
} from '@chutney/data-access';
import { layoutOprionsVar } from '@chutney/ui-layout';

declare const monaco: any;

@Component({
  selector: 'chutney-variables',
  templateUrl: './variables.component.html',
  styleUrls: ['./variables.component.scss']
})
export class VariablesComponent implements OnInit {
  breadcrumbs: any = [
      { title: 'Home', link: ['/'] },
      { title: 'Variables', link: ['/variables'] },
    ];

  globalVariableGroupsNames$: Observable<String[]>;
  private selectedGroupName: string;

  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;
  options: any = layoutOprionsVar();

  constructor(
    private globalVariableGroupsNamesGQL: GlobalVariableGroupsNamesGQL,
    private globalVariableGroupContentGQL: GlobalVariableGroupContentGQL
  ) { }

  ngOnInit(): void {
    this.globalVariableGroupsNames$ = this.globalVariableGroupsNamesGQL
          .watch()
          .valueChanges.pipe(pluck('data', 'globalVariableGroupsNames'));
  }

  onTabChange(matTabChangeEvent) {
    this.globalVariableGroupContentGQL
      .fetch({ groupName: matTabChangeEvent.tab.textLabel })
      .pipe(pluck('data', 'globalVariableGroupContent'))
      .subscribe(
        (content: GlobalVariableGroupContent) => {
          this.monaco.value = content.message;
        }
      );
  }

  async editorInitialized(editorInstance: any): Promise<void> {
    this._editor = editorInstance;
  }

  monacoEditorConfigChanged(theme: string) {
    monaco.editor.setTheme(theme);
  }

}
