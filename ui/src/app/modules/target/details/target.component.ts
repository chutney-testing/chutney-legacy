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
import { ActivatedRoute, Router } from '@angular/router';
import { Target } from '@model';
import { ValidationService } from '../../../molecules/validation/validation.service';
import { EnvironmentService } from '@core/services';
import { Observable, zip } from 'rxjs';

@Component({
    selector: 'chutney-target',
    templateUrl: './target.component.html',
    styleUrls: ['./target.component.scss']
})
export class TargetComponent implements OnInit {
    targets: Target[] = [];
    existingEnvs: string[] = [];
    environmentsNames: string[];
    name: string = '';
    oldName: string = '';
    errorMessage: string;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private environmentService: EnvironmentService,
                public validationService: ValidationService) {
    }

    ngOnInit(): void {
        this.route.data.subscribe((data: { targets: Target[], environmentsNames: string [] }) => {
            if (data.targets.length) {
                this.name = data.targets[0].name;
                this.oldName = this.name;
            }
            this.existingEnvs = data.targets.map(target => target.environment);
            this.environmentsNames = data.environmentsNames;
            const newTargets = this.environmentsNames.filter(name => !data.targets.map(target => target.environment).includes(name))
                .map(name => new Target(
                    this.name,
                    '',
                    null,
                    name
                ));
            this.targets = [...data.targets, ...newTargets]
        });
    }

    save() {
        const actions$: Observable<any> [] = this.targets
            .filter(target => this.validationService.isValidUrl(target.url))
            .map(target => ({...target, name: this.name}))
            .map(target => this.existOn(target.environment) ? this.environmentService.updateTarget(this.oldName, target) : this.environmentService.addTarget(target));

        zip([...actions$]).subscribe({
            next: () => {
                this.router.navigate(['targets']);
            },
            error: err => this.errorMessage = err.error
        });
    }

    deleteAll() {
        this.environmentService.deleteTarget(this.oldName).subscribe(() => {
            this.router.navigate(['targets']);
        });
    }

    delete(index: number) {
        let environmentName = this.targets[index].environment;
        this.environmentService.deleteEnvironmentTarget(environmentName, this.oldName)
            .subscribe(() => {
                this.targets[index] = new Target(this.oldName, '', null, environmentName);
                this.existingEnvs = this.existingEnvs.filter(env => env !== environmentName);
            });
    }

    existOn(environment: string): boolean {
        return this.existingEnvs.includes(environment);
    }

    canSave() {
        let definedTargets = this.targets.filter(target => target.url);
        return this.name && definedTargets.length &&
            definedTargets.every(target => this.validationService.isValidUrlOrSpel(target.url));
    }

    export(item: Target) {
        this.environmentService.exportTargetOn(item.environment, item.name).subscribe({
                error: (error) => {
                    this.errorMessage = error.error;
                }
            }
        );
    }

    import(file: File, index: number) {
        const environment = this.targets[index].environment;
        this.environmentService.importTarget(file, environment).subscribe({
            next: target => {
                this.router.navigate(['targets', target.name]);
            },
            error: err => this.errorMessage = err.error
        })
    }
}
