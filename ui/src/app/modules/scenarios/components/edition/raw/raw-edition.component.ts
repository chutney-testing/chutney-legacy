/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventManagerService } from '@shared/event-manager.service';
import { Subscription } from 'rxjs';
import { TestCase } from '@model';
import { ScenarioService } from '@core/services';
import { CanDeactivatePage } from '@core/guards';
import { JiraPluginService } from '@core/services/jira-plugin.service';
import { FormBuilder, FormGroup } from '@angular/forms';
import { HjsonParserService } from '@shared/hjson-parser/hjson-parser.service';

@Component({
    selector: 'chutney-raw-edition',
    templateUrl: './raw-edition.component.html',
    styleUrls: ['./raw-edition.component.scss'],
})
export class RawEditionComponent
    extends CanDeactivatePage
    implements OnInit, OnDestroy
{
    previousTestCase: TestCase;
    testCase: TestCase;
    modificationsSaved = false;
    errorMessage: any;
    modifiedContent = '';
    pluginsForm: FormGroup;
    saveErrorMessage: string;
    defaultContent =
        '{\n' +
        '  givens:\n' +
        '  [\n' +
        '    {\n' +
        '      description: step description\n' +
        '      implementation:\n' +
        '      {\n' +
        '        type: success\n' +
        '        inputs:\n' +
        '        {\n' +
        '        }\n' +
        '        outputs:\n' +
        '        {\n' +
        '        }\n' +
        '        validations:\n' +
        '        {\n' +
        '        }\n' +
        '      }\n' +
        '    }\n' +
        '  ]\n' +
        '  when: {}\n' +
        '  thens: []\n' +
        '}';
    private routeParamsSubscription: Subscription;

    constructor(
        private eventManager: EventManagerService,
        private formBuilder: FormBuilder,
        private jiraLinkService: JiraPluginService,
        private route: ActivatedRoute,
        private router: Router,
        private scenarioService: ScenarioService,
        private hjsonParserService: HjsonParserService
    ) {
        super();
        this.testCase = new TestCase();
        this.previousTestCase = this.testCase.clone();
        this.pluginsForm = this.formBuilder.group({
            jiraId: '',
        });
    }

    ngOnInit() {
        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            const duplicate =
                this.route.snapshot.queryParamMap.get('duplicate');
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
        return (
            this.modificationsSaved ||
            this.testCase.equals(this.previousTestCase)
        );
    }

    cancel() {
        if (this.testCase.id != null) {
            this.router.navigateByUrl('/scenario/' + this.testCase.id + '/executions');
        } else {
            this.router.navigateByUrl('/scenario');
        }
    }

    load(id, duplicate: boolean) {
        if (id != null) {
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
                        this.testCase.defaultDataset = null;
                        this.previousTestCase.title =
                            '--COPY-- ' + this.previousTestCase.title;
                    }
                },
                (error) => {
                    console.log(error);
                    this.errorMessage = error._body;
                }
            );
            this.loadJiraLink(id);
        } else {
            this.testCase.title = 'scenario title';
            this.testCase.description = 'scenario description';
            this.testCase.content = this.defaultContent;
            this.modifiedContent = this.defaultContent;
            this.previousTestCase = this.testCase.clone();
        }
    }

    private checkParseError() {
        try {
            this.hjsonParserService.parse(this.modifiedContent);
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
            (error) => {
                console.log(error);
            }
        );
    }

    saveScenario() {
        this.testCase.content = this.modifiedContent;
        const jiraId = this.pluginsForm.value['jiraId'];
        this.scenarioService.createOrUpdateRawTestCase(this.testCase).subscribe(
            (response) => {
                this.modificationsSaved = true;
                this.jiraLinkService
                    .saveForScenario(response, jiraId)
                    .subscribe(
                        () => {},
                        (error) => {
                            console.log(error);
                        }
                    );
                this.router.navigateByUrl(
                    '/scenario/' + response + '/executions'
                );
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

    selectDataset(dataset: string) {
        this.testCase.defaultDataset = dataset;
    }

}
