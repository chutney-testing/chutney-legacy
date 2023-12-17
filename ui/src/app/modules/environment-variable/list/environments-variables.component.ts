/*
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
import { Environment, EnvironmentVariable } from '@model';
import { distinct, match } from '@shared/tools';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { EnvironmentService } from '@core/services';
import { Observable, tap } from 'rxjs';
import { ValidationService } from '../../../molecules/validation/validation.service';

@Component({
    selector: 'chutney-environments-variables',
    templateUrl: './environments-variables.component.html',
    styleUrls: ['./environments-variables.component.scss']
})
export class EnvironmentsVariablesComponent implements OnInit {

    errorMessage: string = null;

    environments: Environment[] = [];
    variables: EnvironmentVariable[] = [];
    variablesKeys: string[] = [];

    selectedEnvironment: Environment;
    keyword = '';
    variableEditionForm: FormGroup = null;


    constructor(private environmentService: EnvironmentService,
                private validationService: ValidationService) {
    }

    ngOnInit() {
        this.loadVariables();
    }


    filter(env: Environment = null) {
        if (env) {
            if (env.name === this.selectedEnvironment?.name) {
                this.selectedEnvironment = null;
                this.variables = this.environments.flatMap(e => e.variables);
            } else {
                this.selectedEnvironment = env;
                this.variables = env.variables;
            }
            this.variables.sort(this.variablesSortFunction());
        }

        this.variablesKeys = distinct(this.filterByKeyword(this.variables).map(variable => variable.key));
    }

    activeEnvironmentTab(): string {
        if (this.selectedEnvironment) {
            return this.selectedEnvironment.name;
        } else {
            return this.environments[0].name;
        }
    }

    findVariable(VariableKey: string, environment: Environment): EnvironmentVariable {
        return environment?.variables.find(item => item.key === VariableKey);
    }

    initVariableEdition(variableKey: string = '') {
        this.variableEditionForm = new FormGroup({
            key: new FormControl(variableKey, [Validators.required, this.validationService.asValidatorFn(this.validationService.isValidName.bind(this.validationService), 'name')]),
            oldKey: new FormControl(variableKey),
            values: new FormArray(
                this.environments.map(env => {
                    return new FormGroup({
                        value: new FormControl(variableKey ? this.findVariable(variableKey, env)?.value : ''),
                        env: new FormControl(env.name),
                    });
                })
            )
        })
    }

    submitEdition() {
        let action$: Observable<void>;
        if (!!this.variableEditionForm.value.oldKey) {
            action$ = this.environmentService.updateVariable(this.variableEditionForm.value.oldKey, this.values())
        } else {
            action$ = this.environmentService.addVariable(this.values().filter(variable => !!variable.value))
        }
        action$.subscribe({
            next: () => {
                this.variableEditionForm = null;
                this.loadVariables();
            },
            error: (error) => this.errorMessage = error.error
        })
    }

    delete(variableKey: string) {
        this.environmentService.deleteVariable(variableKey)
            .pipe(tap(() => this.loadVariables()))
            .subscribe();
    }

    valuesArrayForm(): FormArray {
        return this.variableEditionForm.controls['values'] as FormArray;
    }

    values(): EnvironmentVariable[] {
        const key = this.variableEditionForm.value.key;
        return this.valuesArrayForm().controls.map(control => new EnvironmentVariable(key, control.value.value, control.value.env));
    }


    editing(variableKey: string = '') {
        return this.variableEditionForm?.value.oldKey === variableKey;
    }

    private filterByKeyword(variables: EnvironmentVariable[]): EnvironmentVariable[] {
        if (!!this.keyword) {
            return variables.filter(target => this.match(target));
        }
        return variables;

    }

    // DIFF
    private match(variable: EnvironmentVariable): boolean {
        return match(variable.key, this.keyword) || match(variable.value, this.keyword)
    }

    private matchEnv(env: Environment, variableKey: string) {
        return !!env.variables.find(item => item.key === variableKey && this.match(item));
    }

    private variablesSortFunction(): (a: EnvironmentVariable, b: EnvironmentVariable) => number {
        return (t1, t2) => t1.key.localeCompare(t2.key);
    }

    private loadVariables() {
        this.environmentService.list().subscribe({
            next: envs => {
                this.environments = envs;
                this.variables = envs.flatMap(env => env.variables).sort(this.variablesSortFunction());
                this.variablesKeys = distinct(this.variables.map(item => item.key));
            },
            error: error => this.errorMessage = error.error
        });
    }
}
