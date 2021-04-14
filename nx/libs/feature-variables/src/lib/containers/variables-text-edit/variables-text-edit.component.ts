import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { Apollo } from 'apollo-angular';
import { editor } from 'monaco-editor';
import { pluck } from 'rxjs/operators';

import { chutneyAnimations } from '@chutney/utils';
import { layoutOprionsVar } from '@chutney/ui-layout';
import {
  GlobalVariableGroupsNamesGQL,
  GlobalVariableGroupContentGQL,
  GlobalVariableGroupContent,
  SaveGlobalVariableGroupGQL,
  DeleteGlobalVariableGroupGQL
} from '@chutney/data-access';

declare const monaco: any;

@Component({
  selector: 'chutney-variables-text-edit',
  templateUrl: './variables-text-edit.component.html',
  styleUrls: ['./variables-text-edit.component.scss'],
  animations: [chutneyAnimations],
})
export class VariablesTextEditComponent implements OnInit {
  breadcrumbs: any = [
   { title: 'Home', link: ['/'] },
   { title: 'Variables', link: ['/variables'] },
   { title: 'Edit', link: [] },
  ];

  private globalVariableGroupsNames: string[] = [];
  private globalVariableGroupsStoreDataName: string = 'globalVariableGroupsNames';

  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;
  options: any = layoutOprionsVar();

  private groupName: string;
  groupForm: FormGroup;
  isAddMode: boolean;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private formBuilder: FormBuilder,
    private apollo: Apollo,
    private globalVariableGroupsNamesGQL: GlobalVariableGroupsNamesGQL,
    private globalVariableGroupContentGQL: GlobalVariableGroupContentGQL,
    private saveGlobalVariableGroupGQL: SaveGlobalVariableGroupGQL,
    private deleteGlobalVariableGroupGQL: DeleteGlobalVariableGroupGQL
  ) { }

  ngOnInit(): void {
    this.groupName = this.route.snapshot.params['groupName'] || '';
    this.isAddMode = this.groupName === '';

    this.groupForm = this.formBuilder.group({
      name: [this.groupName, Validators.required],
      variables: [this.monaco, Validators.required]
    });

    this.fetchGroupNamesList();

    if (!this.isAddMode) {
      this.groupName = decodeURIComponent(this.groupName)
      this.fName.setValue(this.groupName);
      this.fetchVariableGroupContent(this.fName.value);
    } else {
      this.fVariables.setValue('{}');
    }
  }

  async editorInitialized(editorInstance: any): Promise<void> {
    this._editor = editorInstance;
  }

  monacoEditorConfigChanged(theme: string) {
    monaco.editor.setTheme(theme);
  }

  saveGroupVariables() {
    const groupNameToSave = this.fName.value;
    const groupVariables = this.fVariables.value;

    if (!this.validateGroupNameInput(groupNameToSave)) { return; }
    this.saveGroup(groupNameToSave, groupVariables);
  }

  private validateGroupNameInput(groupNameToValidate: string): boolean {
    if (this.isAddMode || (this.groupName != this.fName.value)) {
      if (groupNameToValidate.length == 0) {
        this.snackBar.open('Group name cannot be empty');
        return false;
      }
      if (this.globalVariableGroupsNames.findIndex(this.groupNameExistsPredicate(groupNameToValidate)) > -1) {
        this.snackBar.open('Group name already exists');
        return false;
      }
    }
    return true;
  }

  private groupNameExistsPredicate(groupNameToTest: string) {
    return gn => gn.toUpperCase() === groupNameToTest.trim().toUpperCase();
  }

  private saveGroup(groupNameToSave: string, groupVariables: string) {
    this.saveGlobalVariableGroupGQL
      .mutate(
        {
          groupName: groupNameToSave,
          input: { message: groupVariables }
        }
      )
      .subscribe(
        () => {
          this.snackBar.open(`<${groupNameToSave}> variables group has been saved`);
          if (!this.isAddMode && (this.groupName != this.fName.value)) {
            this.quietlyDeleteGroup(this.groupName);
          }
          this.router.navigate(['variables']);
        },
        (err) => {
          this.snackBar.open(err.message + ' : ' + err.networkError.result);
        }
      );
  }

  private quietlyDeleteGroup(groupName: string) {
    this.deleteGlobalVariableGroupGQL
      .mutate(
        { groupName: groupName }
      )
      .subscribe(
        () => {},
        (err) => {
          this.snackBar.open(err.message + ' : ' + err.networkError.result);
        }
      );
  }

  private fetchGroupNamesList() {
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

  private fetchVariableGroupContent(groupName: string) {
    if (groupName && groupName.length > 0) {
      this.globalVariableGroupContentGQL
        .fetch({ groupName: groupName }, { fetchPolicy: 'no-cache' })
        .pipe(pluck('data', 'globalVariableGroupContent'))
        .subscribe(
          (content: GlobalVariableGroupContent) => {
            this.fVariables.setValue(content.message);
          }
        );
    }
  }

  // convenience getter for easy access to form fields
  get f() {
    return this.groupForm.controls;
  }
  get fName() {
    return this.f['name'];
  }
  get fVariables() {
    return this.f['variables'];
  }
}
