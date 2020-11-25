import { Component, OnInit, ViewChild } from '@angular/core';
import * as hjson from 'hjson';
import { map, pluck, switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { SaveScenarioGQL, ScenarioGQL } from '@chutney/data-access';
import { TdCodeEditorComponent } from '@covalent/code-editor';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'chutney-scenario-text-exit',
  templateUrl: './scenario-text-edit.component.html',
  styleUrls: ['./scenario-text-edit.component.scss'],
})
export class ScenarioTextEditComponent implements OnInit {
  @ViewChild(TdCodeEditorComponent, { static: false })
  public monaco: TdCodeEditorComponent;
  private _editor: any;

  scenario$: Observable<any>;
  scenario: any;
  height: number = 200;
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Scenarios', link: ['/'] },
    { title: 'View', link: ['../../view'] },
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private scenarioGQL: ScenarioGQL,
    private saveScenarioGQL: SaveScenarioGQL
  ) {}

  ngOnInit(): void {
    this.scenario = this.route.params
      .pipe(
        switchMap((p) => {
          return this.scenarioGQL
            .watch({ scenarioId: p.id })
            .valueChanges.pipe(pluck('data', 'scenario'));
        })
      )
      .subscribe((scenario) => (this.scenario = scenario));
  }

  callBackFunc() {
    console.log('on Init monaco');
  }

  editorInitialized(editorInstance: any): void {
    this._editor = editorInstance;
  }

  private registerCustomLanguage() {}

  hjson(content: string) {
    if (!content) {
      return;
    }
    return JSON.stringify(hjson.parse(content), undefined, 2);
  }

  saveScenario() {
    console.log('scenario saved' + this.monaco.value);
    const scenario = Object.assign({}, this.scenario, {
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
}
