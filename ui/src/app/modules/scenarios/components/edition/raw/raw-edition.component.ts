import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventManagerService } from '@shared/event-manager.service';
import { Subscription } from 'rxjs';
import { TestCase } from '@model';
import { HjsonParserService } from '@shared/hjson-parser/hjson-parser.service';
import { ScenarioService } from '@core/services';
import { CanDeactivatePage } from '@core/guards';
import { JiraPluginService } from '@core/services/jira-plugin.service';
import { FormBuilder, FormGroup } from '@angular/forms';

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
    modifiedContent = '';
    pluginsForm: FormGroup;
    saveErrorMessage: string;

    private routeParamsSubscription: Subscription;

    constructor(private eventManager: EventManagerService,
                private formBuilder: FormBuilder,
                private hjsonParser: HjsonParserService,
                private jiraLinkService: JiraPluginService,
                private route: ActivatedRoute,
                private router: Router,
                private scenarioService: ScenarioService,
    ) {
        super();
        this.testCase = new TestCase();
        this.previousTestCase = this.testCase.clone();
        this.pluginsForm = this.formBuilder.group({
            jiraId: ''
        });
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
                        this.testCase.updateDate = null;
                        this.testCase.author = null;
                        this.testCase.title = '--COPY-- ' + this.testCase.title;
                        this.previousTestCase.title = '--COPY-- ' + this.previousTestCase.title;
                    }
                },
                (error) => {
                    console.log(error);
                    this.errorMessage = error._body;
                }
            );
            this.loadJiraLink(id);
        }
    }

    private checkParseError() {
        try {
            this.hjsonParser.parse(this.modifiedContent);
            this.errorMessage = null;
        } catch (e) {
            this.errorMessage = e;
        }
    }

    private loadJiraLink(id: string) {
        this.jiraLinkService.findByScenarioId(id).subscribe(
            (jiraId) => {
                this.pluginsForm.controls['jiraId'].setValue(jiraId);
            },
            (error) => { console.log(error); }
        );
    }

    saveScenario() {
        this.testCase.content = this.modifiedContent;
        const jiraId = this.pluginsForm.value['jiraId'];
        this.scenarioService.createOrUpdateRawTestCase(this.testCase).subscribe(
            (response) => {
                this.modificationsSaved = true;
                this.jiraLinkService.saveForScenario(response, jiraId).subscribe(
                    () => {},
                    (error) => { console.log(error); }
                );
                this.router.navigateByUrl('/scenario/' + response + '/execution/last');
            },
            (error) => {
                console.log(error);
                if (error.error) {
                    this.saveErrorMessage = error.error;
                }
                this.errorMessage = error._body;
            }
        );
    }

    updateTags(event: string) {
        this.testCase.tags = event.split(',');
    }

    onScenarioContentChanged(data) {
        this.modifiedContent = data;
        this.checkParseError();
    }
}
