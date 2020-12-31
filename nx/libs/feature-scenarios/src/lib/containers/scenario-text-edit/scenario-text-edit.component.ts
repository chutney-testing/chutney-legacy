import { Component, OnInit, ViewChild } from '@angular/core';
import * as hjson from 'hjson';
import { pluck, switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { SaveScenarioGQL, ScenarioGQL } from '@chutney/data-access';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { MatSnackBar } from '@angular/material/snack-bar';
import { editor } from 'monaco-editor';
import { layoutOprionsVar } from '../../../../../ui-layout/src/lib/cache';
import { chutneySchemaV2 } from '../../../../../data-models/schema.scenario.v2';
import * as dotProp from 'dot-prop-immutable';
import { chutneyAnimations } from '@chutney/utils';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';

declare const monaco: any;

@Component({
  selector: 'chutney-scenario-text-exit',
  templateUrl: './scenario-text-edit.component.html',
  styleUrls: ['./scenario-text-edit.component.scss'],
  animations: [chutneyAnimations],
})
export class ScenarioTextEditComponent implements OnInit {
  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;
  scenarioForm: FormGroup;
  id: string;
  isAddMode: boolean;
  loading = false;
  submitted = false;
  scenario$: Observable<any>;
  scenario: any;
  height: number = 200;
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Scenarios', link: ['/'] },
    { title: 'View', link: ['../../view'] },
  ];
  options: any = layoutOprionsVar();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private scenarioGQL: ScenarioGQL,
    private saveScenarioGQL: SaveScenarioGQL
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.params['id'];
    this.isAddMode = !this.id;

    this.scenarioForm = this.formBuilder.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      content: ['', Validators.required],
      tags: [[]],
    });

    if (!this.isAddMode) {
      this.scenarioGQL
        .watch({ scenarioId: this.id })
        .valueChanges.pipe(pluck('data', 'scenario'))
        .subscribe((x) => {
          this.scenarioForm.patchValue(x);
          this.scenario = x;
        });
    }
  }

  callBackFunc() {
    console.log('on Init monaco');
  }

  async editorInitialized(editorInstance: any): Promise<void> {
    this._editor = editorInstance;
    const model = await this.getModel();
    const schema = dotProp.set(
      chutneySchemaV2,
      'definitions.step.properties.implementation.properties.target.enum',
      //TODO get dynamic targets
      ['swapi.dev']
    );
    monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
      validate: true,
      schemas: [
        {
          uri: 'http://example.com/schema.json',
          fileMatch: [model.uri.toString()],
          schema: schema,
        },
      ],
    });
  }

  hjson(content: string) {
    if (!content) {
      return;
    }
    return JSON.stringify(hjson.parse(content), undefined, 2);
  }

  saveScenario() {
    console.log('scenario saved' + this.monaco.value);
    const scenario = Object.assign({}, this.scenario, this.scenarioForm.value, {
      content: this.monaco.value,
    });
    this.saveScenarioGQL.mutate({ input: scenario }).subscribe(
      (e) => {
        const matSnackBarRef = this.snackBar.open('Scenario saved!', 'View');
        matSnackBarRef.onAction().subscribe(() => {
          console.log('The snack-bar action was triggered!');
          this.router.navigate([`../view`], { relativeTo: this.route });
        });
      },
      (err) => this.snackBar.open(err.message)
    );
  }

  monacoEditorConfigChanged(theme: string) {
    monaco.editor.setTheme(theme);
  }

  async getModel(): Promise<any> {
    // tslint:disable-next-line:prefer-immediate-return
    const editorModel: editor.ITextModel = await this._editor.getModel();
    // do something with editorModel

    return editorModel;
  }

  // convenience getter for easy access to form fields
  get f() {
    return this.scenarioForm.controls;
  }
  get tags() {
    return this.f['tags'];
  }

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    // Add our tag
    if ((value || '').trim()) {
      this.tags.setValue([...this.tags.value, value.trim()]);
      this.tags.updateValueAndValidity();
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  removeTag(tag: string): void {
    const index = this.tags.value.indexOf(tag);

    if (index >= 0) {
      this.tags.value.splice(index, 1);
      this.tags.updateValueAndValidity();
    }
  }
}
