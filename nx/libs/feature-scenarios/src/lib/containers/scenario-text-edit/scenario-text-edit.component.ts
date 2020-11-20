import { Component, OnInit, ViewChild } from '@angular/core';
import * as hjson from 'hjson';
import { map, pluck, switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { ScenarioGQL } from '@chutney/data-access';
import { editor } from 'monaco-editor';
import { TdCodeEditorComponent } from '@covalent/code-editor';

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
  height: number = 200;

  constructor(
    private route: ActivatedRoute,
    private scenarioGQL: ScenarioGQL
  ) {}

  ngOnInit(): void {
    this.scenario$ = this.route.params.pipe(
      switchMap((p) => {
        return this.scenarioGQL
          .watch({ scenarioId: p.id })
          .valueChanges.pipe(pluck('data', 'scenario'));
      })
    );
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

  saveScenario() {}
}
