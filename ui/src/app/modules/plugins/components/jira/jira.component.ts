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

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { JiraPluginConfiguration } from '@core/model/jira-plugin-configuration.model';
import { JiraPluginConfigurationService } from '@core/services/jira-plugin-configuration.service';
import { TranslateService } from '@ngx-translate/core';
import { delay } from '@shared/tools';
import { ValidationService } from '../../../../molecules/validation/validation.service';


@Component({
    selector: 'chutney-config-jira',
    templateUrl: './jira.component.html',
    styleUrls: ['./jira.component.scss']
})
export class JiraComponent implements OnInit {

    configuration: JiraPluginConfiguration = new JiraPluginConfiguration('', '', '', '', '', '');
    configurationForm: FormGroup;

    message;
    private savedMessage: string;

    isErrorNotification: boolean = false;

    constructor(private configurationService: JiraPluginConfigurationService,
                private translate: TranslateService,
                private validationService: ValidationService,
                private formBuilder: FormBuilder) {
    }

    ngOnInit() {
        this.configurationForm = this.formBuilder.group({
            url: ['', Validators.required],
            username: '',
            password: '',
            urlProxy: '',
            userProxy: '',
            passwordProxy: ''
        });

        this.loadConfiguration();
        this.initTranslation();
    }

    private initTranslation() {
        this.translate.get('global.actions.done.saved').subscribe((res: string) => {
            this.savedMessage = res;
        });
    }

    loadConfiguration() {
        this.configurationService.get().subscribe(
            (config: JiraPluginConfiguration) => {
                this.configuration = config;
                this.configurationForm.controls['url'].patchValue(config.url);
                this.configurationForm.controls['username'].patchValue(config.username);
                this.configurationForm.controls['password'].patchValue(config.password);
                this.configurationForm.controls['urlProxy'].patchValue(config.urlProxy);
                this.configurationForm.controls['userProxy'].patchValue(config.userProxy);
                this.configurationForm.controls['passwordProxy'].patchValue(config.passwordProxy);
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    save() {
        const url = this.configurationForm.value['url'] ? this.configurationForm.value['url'] : '';
        const username = this.configurationForm.value['username'] ? this.configurationForm.value['username'] : '';
        const password = this.configurationForm.value['password'] ? this.configurationForm.value['password'] : '';
        const urlProxy = this.configurationForm.value['urlProxy'] ? this.configurationForm.value['urlProxy'] : '';
        const userProxy = this.configurationForm.value['userProxy'] ? this.configurationForm.value['userProxy'] : '';
        const passwordProxy = this.configurationForm.value['passwordProxy'] ? this.configurationForm.value['passwordProxy'] : '';
        this.configuration = new JiraPluginConfiguration(url, username, password, urlProxy, userProxy, passwordProxy);

        this.configurationService.save(this.configuration).subscribe(
            (res) => {
                this.notify(this.savedMessage, false);
            },
            (error) => {
                this.notify(error.error, true);
            }
        );
    }

    notify(message: string, isErrorNotification: boolean) {
        (async () => {
            this.isErrorNotification = isErrorNotification;
            this.message = message;
            await delay(3000);
            this.message = null;
        })();
    }

    isValid(): boolean {
        return this.validationService.isValidUrl(this.configurationForm.value['url'])
            && this.validationService.isNotEmpty(this.configurationForm.value['url']);
    }

}
