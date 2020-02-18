import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventManagerService } from '@shared/event-manager.service';
import { Subscription } from 'rxjs';
import { TestCase } from '@model';
import { AceEditorDirective } from '@shared/ace-editor/ace-editor.directive';
import { HjsonParserService } from '@shared/hjson-parser/hjson-parser.service';
import { ScenarioService } from '@core/services';
import { CanDeactivatePage } from '@core/guards';

@Component({
    selector: 'chutney-raw-edition',
    templateUrl: './raw-edition.component.html',
    styleUrls: ['./raw-edition.component.scss']
})
export class RawEditionComponent extends CanDeactivatePage implements OnInit, OnDestroy {

    previousTestCase: TestCase;
    testCase: TestCase;
    modificationsSaved = false;

    errorMessage: any;

    // TODO - there is an odd behavior on changing mode or theme, cannot change back to 1st option
    editorModes: Array<EditorMode> = [
        new EditorMode('JSON', 'json'), new EditorMode('Hjson', 'hjson')
    ];
    editorMode: EditorMode = this.editorModes[1];

    editorThemes: Array<EditorTheme> = [
        new EditorTheme('Monokai', 'monokai'), new EditorTheme('Eclipse', 'eclipse'), new EditorTheme('Terminal', 'terminal')
    ];
    editorTheme: EditorTheme = this.editorThemes[0];

    editorOptionCollapsed = true;

    aceOptions: any = {
        fontSize: '13pt',
        enableBasicAutocompletion: true,
        showPrintMargin: false
    };

    private resizeInit = 0;
    private routeParamsSubscription: Subscription;

    @ViewChild(AceEditorDirective) aceEditorDirective: AceEditorDirective;

    constructor(private scenarioService: ScenarioService,
                private router: Router,
                private route: ActivatedRoute,
                private eventManager: EventManagerService,
                private hjsonParser: HjsonParserService
    ) {
        super();
        this.testCase = new TestCase();
        this.previousTestCase = this.testCase.clone();
    }

    ngOnInit() {
        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            const duplicate = this.route.snapshot.queryParamMap.get('duplicate');
            if (duplicate) {
                this.load(params['id'], true);
            } else {
                this.load(params['id'], false);
            }
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.routeParamsSubscription);
    }

    canDeactivatePage(): boolean {
        return this.modificationsSaved || this.testCase.equals(this.previousTestCase);
    }

    load(id, duplicate: boolean) {
        if (id !== undefined) {
            this.scenarioService.findRawTestCase(id).subscribe(
                (rawScenario) => {
                    this.testCase = rawScenario;
                    this.previousTestCase = this.testCase.clone();
                    this.checkParseError();

                    if (duplicate) {
                        this.previousTestCase.id = null;
                        this.testCase.id = null;
                        this.testCase.creationDate = null;
                        this.testCase.title = '--COPY-- ' + this.testCase.title;
                        this.previousTestCase.title = '--COPY-- ' + this.previousTestCase.title;
                    }
                },
                (error) => {
                    console.log(error);
                    this.errorMessage = error._body;
                }
            );
        }
    }

    private checkParseError() {
        const previousErrorMessage = this.errorMessage;
        try {
            this.hjsonParser.parse(this.testCase.content);
            this.errorMessage = null;
        } catch (e) {
            this.errorMessage = e;
        }
        if (previousErrorMessage !== this.errorMessage) {
            this.resizeEditor();
        }
    }

    saveScenario() {
        this.scenarioService.createOrUpdateRawTestCase(this.testCase).subscribe(
            (response) => {
                this.modificationsSaved = true;
                this.router.navigateByUrl('/scenario/' + response + '/execution/last');
            },
            (error) => {
                console.log(error);
                this.errorMessage = error._body;
            }
        );
    }

    updateTags(event: string) {
        this.testCase.tags = event.split(',');
    }

    onScenarioContentChanged(data) {
        this.testCase.content = data;
        this.checkParseError();
    }

    editorOptionShowHide() {
        this.editorOptionCollapsed = !this.editorOptionCollapsed;
        this.resizeEditor();
    }

    resizeEditor() {
        if (this.resizeInit === 0) {
            const mainContentClientHeight = document.getElementsByClassName('main-content')[0].clientHeight;
            const mainContentHeaderClientHeight = document.getElementsByClassName('edition-header')[0].clientHeight;
            const editorHeaderClientHeight = document.getElementById('ace-editor-header').clientHeight + 30;

            this.resizeInit = mainContentClientHeight - mainContentHeaderClientHeight - editorHeaderClientHeight;
        }

        let editorHeight = this.resizeInit;

        if (this.errorMessage) {
            editorHeight -= 70;
        }

        // TODO remove horizontal scroll
        if (!this.editorOptionCollapsed) {
            editorHeight -= 50;
        }

        const editor = document.getElementById('editor');
        if (editor) {
            editor.style.height = editorHeight + 'px';
        }
        if (this.aceEditorDirective) {
            this.aceEditorDirective.editor.resize();
        }
    }
}

class EditorMode {
    constructor(public label: string, public name: string) {
    }
}

class EditorTheme {
    constructor(public label: string, public name: string) {
    }
}
