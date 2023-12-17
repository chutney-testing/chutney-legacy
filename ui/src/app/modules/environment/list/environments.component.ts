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

import { Component, DoCheck, OnInit } from '@angular/core';
import { Environment } from '@model';
import { ActivatedRoute } from '@angular/router';
import { EnvironmentService } from '@core/services';
import { ValidationService } from '../../../molecules/validation/validation.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'chutney-environments',
    templateUrl: './environments.component.html',
    styleUrls: ['./environments.component.scss']
})
export class EnvironmentsComponent implements OnInit, DoCheck {

    editableEnvironments: Environment[] = [];
    environments: Environment[] = [];
    environment: Environment;
    editionIndex: number;
    errorMessage: string;

    constructor(private route: ActivatedRoute,
                private environmentService: EnvironmentService,
                public validationService: ValidationService,
                private translateService: TranslateService) {
    }

    ngOnInit(): void {
        this.route.data.subscribe((data: { environments: Environment[] }) => {
            this.editableEnvironments = data.environments;
            this.environments = data.environments.map(env => ({...env}));
        });
    }

    ngDoCheck() {
        var isNewEnvironmentInvalid = this.environment && !this.validationService.isValidName(this.environment.name);
        var isEditableEnvironmentInvalid = this.editionIndex >= 0 && !this.validationService.isValidName(this.editableEnvironments[this.editionIndex]?.name);
        if ( isNewEnvironmentInvalid || isEditableEnvironmentInvalid) {
            this.errorMessage = this.translateService.instant('global.rules.name');
        } else {
            this.errorMessage = null;
        }
    }

    editing(index: number): boolean {
        return this.editionIndex === index;
    }

    save(index: number) {
        this.environmentService.update(this.environments[index].name, this.editableEnvironments[index]).subscribe({
            next: () => {
                this.environments[index] = {...this.editableEnvironments[index]};
                this.sort();
                this.editionIndex = null;
            },
            error: err => this.errorMessage = err.error
        });
    }

    enableAdd() {
        this.environment = new Environment('', '');
    }

    add() {
        this.environmentService.create(this.environment).subscribe({
            next: () => {
                this.editableEnvironments.push(this.environment);
                this.environments.push(this.environment);
                this.sort();
                this.environment = null;
            },
            error: err => {
                this.errorMessage = err.error
            }
        });
    }

    delete(name: string, index: number) {
        this.environmentService.delete(name).subscribe({
            next: () => {
                this.editableEnvironments.splice(index, 1);
                this.environments.splice(index, 1);
            },
            error: err => this.errorMessage = err.error
        })
    }

    export(env: Environment) {
        this.environmentService.export(env.name).subscribe({
            error: err => this.errorMessage = err.error
        });
    }

    import(file: File) {
        this.environmentService.import(file).subscribe({
            next: env => {
                this.editableEnvironments.push(env);
                this.environments.push(env);
                this.sort();
            },
            error: err => this.errorMessage = err.error
        })
    }

    private sort() {
        this.environments.sort((e1, e2) => e1.name.toUpperCase() > e2.name.toUpperCase() ? 1 : -1)
        this.editableEnvironments.sort((e1, e2) => e1.name.toUpperCase() >= e2.name.toUpperCase() ? 1 : -1)
    }
}
